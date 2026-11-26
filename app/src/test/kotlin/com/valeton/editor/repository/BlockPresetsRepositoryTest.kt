package com.valeton.editor.repository

import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.ModuleType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BlockPresetsRepositoryTest {
    @TempDir
    lateinit var dir: File

    private val uk45 = 117440554
    private val tweedy = 117440513
    private fun params(vararg v: Float) = List(15) { i -> v.getOrElse(i) { 0f } }
    private fun repo() = BlockPresetsRepository(dir)
    private fun names(r: BlockPresetResult) = (r as BlockPresetResult.Ok).presets.map { it.name }

    @Test fun saveAndListSorted() {
        val r = repo()
        r.save(ModuleType.AMP, "lead", uk45, params(80f))
        r.save(ModuleType.AMP, "Clean", tweedy, params(20f))
        assertEquals(listOf("Clean", "lead"), r.list(ModuleType.AMP).map { it.name })
        assertEquals(BlockPreset("Clean", tweedy, params(20f)), r.list(ModuleType.AMP).first())
    }

    @Test fun saveRejectsEmptyAndDuplicateIgnoringCase() {
        val r = repo()
        assertEquals(BlockPresetResult.EmptyName, r.save(ModuleType.AMP, "   ", uk45, params()))
        r.save(ModuleType.AMP, "Lead", uk45, params())
        assertEquals(BlockPresetResult.Duplicate, r.save(ModuleType.AMP, " lead ", uk45, params()))
    }

    @Test fun saveLimit100() {
        val r = repo()
        repeat(100) { assertTrue(r.save(ModuleType.DLY, "P$it", 1, params()) is BlockPresetResult.Ok) }
        assertEquals(BlockPresetResult.LimitReached, r.save(ModuleType.DLY, "extra", 1, params()))
        assertEquals(100, r.list(ModuleType.DLY).size)
    }

    @Test fun renameRejectsDuplicateIgnoringCase() {
        val r = repo()
        r.save(ModuleType.AMP, "Lead", uk45, params())
        r.save(ModuleType.AMP, "Clean", uk45, params())
        assertEquals(BlockPresetResult.Duplicate, r.rename(ModuleType.AMP, "Clean", "LEAD"))
        // Mudar só a caixa do próprio nome é permitido.
        assertEquals(listOf("clean", "Lead"), names(r.rename(ModuleType.AMP, "Clean", "clean")))
    }

    @Test fun renameAndDelete() {
        val r = repo()
        r.save(ModuleType.AMP, "Lead", uk45, params())
        assertEquals(listOf("Solo"), names(r.rename(ModuleType.AMP, "Lead", "Solo")))
        assertEquals(BlockPresetResult.NotFound, r.rename(ModuleType.AMP, "Lead", "X"))
        assertEquals(emptyList<String>(), names(r.delete(ModuleType.AMP, "Solo")))
        assertEquals(BlockPresetResult.NotFound, r.delete(ModuleType.AMP, "Solo"))
    }

    @Test fun replaceKeepsName() {
        val r = repo()
        r.save(ModuleType.AMP, "Lead", uk45, params(10f))
        r.replace(ModuleType.AMP, "Lead", tweedy, params(90f))
        assertEquals(listOf(BlockPreset("Lead", tweedy, params(90f))), r.list(ModuleType.AMP))
    }

    @Test fun blocksAreSeparate() {
        val r = repo()
        r.save(ModuleType.AMP, "Lead", uk45, params())
        assertEquals(emptyList<BlockPreset>(), r.list(ModuleType.DLY))
        assertTrue(r.save(ModuleType.DLY, "Lead", 1, params()) is BlockPresetResult.Ok)
    }

    @Test fun survivesReload() {
        repo().save(ModuleType.RVB, "Hall longo", 5, params(1.5f, 2f))
        assertEquals(listOf(BlockPreset("Hall longo", 5, params(1.5f, 2f))), repo().list(ModuleType.RVB))
    }

    @Test fun corruptFileIsPreserved() {
        File(dir, "block_presets.json").writeText("{isto não é json")
        val r = repo()
        assertEquals(emptyList<BlockPreset>(), r.list(ModuleType.AMP))
        assertTrue(r.save(ModuleType.AMP, "Lead", uk45, params()) is BlockPresetResult.Ok)
        assertEquals("{isto não é json", File(dir, "block_presets.json.corrompido").readText())
        assertEquals(listOf("Lead"), repo().list(ModuleType.AMP).map { it.name })
    }

    @Test fun writeFailureKeepsPrevious() {
        val r = repo()
        r.save(ModuleType.AMP, "Lead", uk45, params())
        dir.setWritable(false)
        try {
            assertEquals(BlockPresetResult.WriteFailed, r.save(ModuleType.AMP, "Clean", uk45, params()))
        } finally {
            dir.setWritable(true)
        }
        assertEquals(listOf("Lead"), repo().list(ModuleType.AMP).map { it.name })
    }

    @Test fun nameTrimmedAndCapped() {
        val r = repo()
        r.save(ModuleType.AMP, "  " + "x".repeat(40) + "  ", uk45, params())
        assertEquals("x".repeat(32), r.list(ModuleType.AMP).single().name)
    }

    @Test fun matchesComparesCatalogParams() {
        // UK 45 usa os índices 0..5; o índice 9 não é do catálogo e não conta.
        val preset = BlockPreset("Lead", uk45, params(25f, 65f, 50f, 45f, 50f, 65f))
        val slot = EffectSlot(3, uk45, true, params(25f, 65f, 50f, 45f, 50f, 65f).toMutableList().also { it[9] = 7f })
        assertTrue(BlockPresetRules.matches(preset, slot))
        assertFalse(BlockPresetRules.matches(preset, slot.copy(params = slot.params.toMutableList().also { it[0] = 26f })))
        assertFalse(BlockPresetRules.matches(preset, slot.copy(effectId = tweedy)))
    }

    private fun writeRaw(json: String) = File(dir, "block_presets.json").writeText(json)

    @Test fun nonFiniteValueDoesNotBlockWrites() {
        writeRaw("""{"version":1,"blocks":{"AMP":[{"name":"Lead","effectId":$uk45,"params":[NaN,65,50]}]}}""")
        val r = repo()
        val lead = r.list(ModuleType.AMP).single()
        assertTrue(lead.params.all { it.isFinite() })
        assertTrue(r.save(ModuleType.DLY, "Eco", 1, params()) is BlockPresetResult.Ok)
    }

    @Test fun duplicateNamesInFileKeepFirst() {
        writeRaw("""{"version":1,"blocks":{"AMP":[
            {"name":"Lead","effectId":$uk45,"params":[1]},
            {"name":"lead","effectId":$uk45,"params":[2]},
            {"name":"Lead","effectId":$uk45,"params":[3]}]}}""")
        val list = repo().list(ModuleType.AMP)
        assertEquals(listOf("Lead"), list.map { it.name })
        assertEquals(1f, list.single().params[0])
    }

    @Test fun badEntrySkippedOthersKept() {
        writeRaw("""{"version":1,"blocks":{
            "AMP":[{"name":"Lead","effectId":$uk45,"params":[1]}],
            "DLY":[{"name":"Ruim","effectId":1,"params":[true]},{"name":"Eco","effectId":1,"params":[2]}]}}""")
        val r = repo()
        assertEquals(listOf("Lead"), r.list(ModuleType.AMP).map { it.name })
        assertEquals(listOf("Eco"), r.list(ModuleType.DLY).map { it.name })
        assertTrue(r.hasDamagedEntries())
        r.save(ModuleType.AMP, "Clean", uk45, params())
        assertTrue(File(dir, "block_presets.json.corrompido").readText().contains("Ruim"))
        // A sheet assume que uma gravação bem-sucedida deixa o arquivo limpo.
        assertFalse(r.hasDamagedEntries())
    }

    @Test fun snapshotReadsListAndDamageTogether() {
        writeRaw("""{"version":1,"blocks":{
            "AMP":[{"name":"Lead","effectId":$uk45,"params":[1]},{"name":"Ruim","effectId":$uk45,"params":[true]}]}}""")
        val snap = repo().snapshot(ModuleType.AMP)
        assertEquals(listOf("Lead"), snap.presets.map { it.name })
        assertTrue(snap.damaged)
        assertFalse(repo().snapshot(ModuleType.DLY).presets.any())
    }

    @Test fun cleanFileHasNoDamage() {
        repo().save(ModuleType.AMP, "Lead", uk45, params())
        assertFalse(repo().hasDamagedEntries())
    }

    @Test fun secondCorruptionKeepsFirstBackup() {
        writeRaw("primeiro lixo")
        repo().save(ModuleType.AMP, "Lead", uk45, params())
        writeRaw("segundo lixo")
        repo().save(ModuleType.AMP, "Clean", uk45, params())
        assertEquals("primeiro lixo", File(dir, "block_presets.json.corrompido").readText())
        assertEquals("segundo lixo", File(dir, "block_presets.json.corrompido.2").readText())
    }

    @Test fun repeatedFailedWritesDoNotDuplicateBackup() {
        writeRaw("lixo")
        // Um diretório no lugar do arquivo temporário faz a gravação falhar depois da cópia.
        File(dir, "block_presets.json.tmp").mkdir()
        val r = repo()
        assertEquals(BlockPresetResult.WriteFailed, r.save(ModuleType.AMP, "A", uk45, params()))
        assertEquals(BlockPresetResult.WriteFailed, r.save(ModuleType.AMP, "B", uk45, params()))
        assertEquals("lixo", File(dir, "block_presets.json.corrompido").readText())
        assertFalse(File(dir, "block_presets.json.corrompido.2").exists())
    }
}
