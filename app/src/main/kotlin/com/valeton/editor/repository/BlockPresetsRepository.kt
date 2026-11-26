package com.valeton.editor.repository

import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.ModuleType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Ajustes salvos de um tipo de bloco: modelo e os 15 parâmetros. Não guarda ligado/desligado nem K1–K3/CTRL. */
data class BlockPreset(val name: String, val effectId: Int, val params: List<Float>)

data class BlockPresetsSnapshot(val presets: List<BlockPreset>, val damaged: Boolean)

sealed interface BlockPresetResult {
    data class Ok(val presets: List<BlockPreset>) : BlockPresetResult
    data object EmptyName : BlockPresetResult
    data object Duplicate : BlockPresetResult
    data object LimitReached : BlockPresetResult
    data object NotFound : BlockPresetResult
    data object WriteFailed : BlockPresetResult
}

object BlockPresetRules {
    const val MAX_PER_BLOCK = 100
    const val MAX_NAME = 32

    fun normalize(name: String): String = name.trim().take(MAX_NAME).trim()

    /** O bloco está "nesse preset": mesmo modelo e mesmos valores nos parâmetros do catálogo. */
    fun matches(preset: BlockPreset, slot: EffectSlot): Boolean {
        if (preset.effectId != slot.effectId) return false
        val indices = EffectCatalog.getEffectById(slot.effectId)?.params?.map { it.index }.orEmpty()
        return indices.all { preset.params.getOrNull(it) == slot.params.getOrNull(it) }
    }
}

/**
 * Presets por tipo de bloco, guardados em `block_presets.json` (até [BlockPresetRules.MAX_PER_BLOCK] por tipo).
 * Gravação atômica; uma falha mantém o arquivo anterior. Um arquivo ilegível é lido como vazio e guardado como
 * `block_presets.json.corrompido` antes da próxima gravação, para não ser apagado sem aviso.
 */
class BlockPresetsRepository(private val storageDir: File) {
    private val file: File get() = File(storageDir, FILE_NAME)

    @Synchronized
    fun list(module: ModuleType): List<BlockPreset> = sorted(readAll().first[module.code].orEmpty())

    /** Lista do bloco e o estado de dano do arquivo numa única leitura (usado ao abrir a tela). */
    @Synchronized
    fun snapshot(module: ModuleType): BlockPresetsSnapshot {
        val (all, damaged) = readAll()
        return BlockPresetsSnapshot(sorted(all[module.code].orEmpty()), damaged)
    }

    /** O arquivo tinha itens ilegíveis, valores inválidos ou nomes repetidos (ignorados ou corrigidos na leitura). */
    @Synchronized
    fun hasDamagedEntries(): Boolean = readAll().second

    @Synchronized
    fun save(module: ModuleType, name: String, effectId: Int, params: List<Float>): BlockPresetResult {
        val clean = BlockPresetRules.normalize(name)
        if (clean.isEmpty()) return BlockPresetResult.EmptyName
        return update(module) { list ->
            when {
                list.any { it.name.equals(clean, ignoreCase = true) } -> BlockPresetResult.Duplicate
                list.size >= BlockPresetRules.MAX_PER_BLOCK -> BlockPresetResult.LimitReached
                else -> Updated(list + BlockPreset(clean, effectId, params.toList()))
            }
        }
    }

    @Synchronized
    fun rename(module: ModuleType, oldName: String, newName: String): BlockPresetResult {
        val clean = BlockPresetRules.normalize(newName)
        if (clean.isEmpty()) return BlockPresetResult.EmptyName
        return update(module) { list ->
            val target = list.firstOrNull { it.name == oldName } ?: return@update BlockPresetResult.NotFound
            if (list.any { it !== target && it.name.equals(clean, ignoreCase = true) }) BlockPresetResult.Duplicate
            else Updated(list.map { if (it === target) it.copy(name = clean) else it })
        }
    }

    @Synchronized
    fun replace(module: ModuleType, name: String, effectId: Int, params: List<Float>): BlockPresetResult =
        update(module) { list ->
            if (list.none { it.name == name }) BlockPresetResult.NotFound
            else Updated(list.map { if (it.name == name) it.copy(effectId = effectId, params = params.toList()) else it })
        }

    @Synchronized
    fun delete(module: ModuleType, name: String): BlockPresetResult =
        update(module) { list ->
            if (list.none { it.name == name }) BlockPresetResult.NotFound else Updated(list.filterNot { it.name == name })
        }

    /** Resultado intermediário de [update]: a nova lista a gravar. */
    private class Updated(val list: List<BlockPreset>) : BlockPresetResult

    private fun update(module: ModuleType, change: (List<BlockPreset>) -> BlockPresetResult): BlockPresetResult {
        val (all, corrupt) = readAll()
        val result = change(all[module.code].orEmpty())
        if (result !is Updated) return result
        val next = all + (module.code to result.list)
        val written = runCatching {
            storageDir.mkdirs()
            if (corrupt && !alreadyBackedUp()) file.copyTo(backupFile())
            val tmp = File(storageDir, "$FILE_NAME.tmp")
            tmp.writeText(encode(next))
            if (!tmp.renameTo(file)) {
                file.delete()
                check(tmp.renameTo(file)) { "Não foi possível gravar os presets de bloco" }
            }
        }.isSuccess
        return if (written) BlockPresetResult.Ok(sorted(result.list)) else BlockPresetResult.WriteFailed
    }

    /** Já existe uma cópia com o mesmo conteúdo (gravações que falham seguidas não acumulam cópias iguais). */
    private fun alreadyBackedUp(): Boolean {
        val current = file.readBytes()
        return storageDir.listFiles { f -> f.name.startsWith("$FILE_NAME.corrompido") }.orEmpty()
            .any { it.readBytes().contentEquals(current) }
    }

    /** Primeiro nome livre para a cópia do arquivo danificado: `.corrompido`, depois `.corrompido.2`, `.3`… */
    private fun backupFile(): File {
        val first = File(storageDir, "$FILE_NAME.corrompido")
        if (!first.exists()) return first
        return generateSequence(2) { it + 1 }.map { File(storageDir, "$FILE_NAME.corrompido.$it") }.first { !it.exists() }
    }

    /**
     * Presets por código de módulo e se o arquivo existente estava danificado (ilegível por inteiro ou com itens
     * ignorados/corrigidos). Danificado → a próxima gravação guarda o original em `.corrompido`.
     */
    private fun readAll(): Pair<Map<String, List<BlockPreset>>, Boolean> {
        if (!file.exists()) return emptyMap<String, List<BlockPreset>>() to false
        return runCatching { decode(file.readText()) }.getOrElse { emptyMap<String, List<BlockPreset>>() to true }
    }

    private fun sorted(list: List<BlockPreset>) = list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

    private fun encode(all: Map<String, List<BlockPreset>>): String {
        val blocks = JSONObject()
        all.forEach { (code, list) ->
            blocks.put(code, JSONArray().apply {
                list.forEach { p ->
                    put(JSONObject().apply {
                        put("name", p.name)
                        put("effectId", p.effectId)
                        put("params", JSONArray().apply { p.params.forEach { put(it.toDouble()) } })
                    })
                }
            })
        }
        return JSONObject().put("version", 1).put("blocks", blocks).toString(2)
    }

    /**
     * Lê item por item: um item ilegível é ignorado sem apagar os outros; valor não finito vira o padrão do catálogo
     * (ou 0); nome repetido (sem distinção de caixa) no mesmo bloco fica só o primeiro.
     */
    private fun decode(text: String): Pair<Map<String, List<BlockPreset>>, Boolean> {
        var damaged = false
        val blocks = JSONObject(text).getJSONObject("blocks")
        val all = blocks.keys().asSequence().toList().associateWith { code ->
            val arr = blocks.optJSONArray(code) ?: run { damaged = true; return@associateWith emptyList() }
            val seen = mutableSetOf<String>()
            (0 until arr.length()).mapNotNull { i ->
                val entry = runCatching {
                    val o = arr.getJSONObject(i)
                    val effectId = o.getInt("effectId")
                    val name = BlockPresetRules.normalize(o.getString("name"))
                    require(name.isNotEmpty())
                    val ps = o.getJSONArray("params")
                    val defaults = EffectCatalog.getEffectById(effectId)?.params?.associate { it.index to it.defaultValue }.orEmpty()
                    val params = List(15) { j ->
                        if (j >= ps.length()) return@List defaults[j] ?: 0f
                        val v = ps.getDouble(j).toFloat()
                        if (v.isFinite()) v else { damaged = true; defaults[j] ?: 0f }
                    }
                    BlockPreset(name, effectId, params)
                }.getOrNull()
                when {
                    entry == null -> { damaged = true; null }
                    !seen.add(entry.name.lowercase()) -> { damaged = true; null }
                    else -> entry
                }
            }
        }
        return all to damaged
    }

    private companion object {
        const val FILE_NAME = "block_presets.json"
    }
}
