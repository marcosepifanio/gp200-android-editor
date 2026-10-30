package com.valeton.editor.repository

import com.valeton.editor.core.codec.ControlMappingCodec
import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.GP200Preset
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.util.Base64

class PresetLibraryRepository(
    private val storageDir: File
) {
    private val cacheFile: File
        get() = File(storageDir, "library_cache.json")

    private val tempFile: File
        get() = File(storageDir, "library_cache.json.tmp")

    /** Day of the last complete library sync, kept apart so library_cache.json keeps its format. */
    private val lastSyncFile: File
        get() = File(storageDir, "library_last_sync.txt")

    @Synchronized
    fun lastFullSyncDate(): LocalDate? = try {
        if (lastSyncFile.exists()) LocalDate.parse(lastSyncFile.readText(Charsets.UTF_8).trim()) else null
    } catch (_: Exception) {
        null
    }

    @Synchronized
    fun markFullSync(date: LocalDate) {
        try {
            if (!storageDir.exists()) storageDir.mkdirs()
            lastSyncFile.writeText(date.toString(), Charsets.UTF_8)
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun isLibraryCached(): Boolean {
        if (!cacheFile.exists() || cacheFile.length() == 0L) return false
        val lib = loadFullLibrary() ?: return false
        return lib.isNotEmpty() && lib.values.any { it.effects.isNotEmpty() }
    }

    @Synchronized
    fun loadFullLibrary(): Map<Int, GP200Preset>? {
        if (!cacheFile.exists() || cacheFile.length() == 0L) return null
        return try {
            val text = cacheFile.readText(Charsets.UTF_8)
            if (text.isBlank()) return null
            val root = JSONObject(text)
            val result = mutableMapOf<Int, GP200Preset>()
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val slot = key.toIntOrNull() ?: continue
                if (slot !in 0..255) continue
                val obj = root.getJSONObject(key)

                val effectsList = mutableListOf<EffectSlot>()
                val effectsArr = obj.optJSONArray("effects")
                if (effectsArr != null) {
                    for (i in 0 until effectsArr.length()) {
                        val effObj = effectsArr.getJSONObject(i)
                        val slotIndex = effObj.optInt("slotIndex", i)
                        val effectId = effObj.optInt("effectId", 0)
                        val enabled = effObj.optBoolean("enabled", false)
                        val paramsArr = effObj.optJSONArray("params")
                        val paramsList = mutableListOf<Float>()
                        if (paramsArr != null) {
                            for (p in 0 until paramsArr.length()) {
                                paramsList.add(paramsArr.getDouble(p).toFloat())
                            }
                        }
                        effectsList.add(
                            EffectSlot(
                                slotIndex = slotIndex,
                                effectId = effectId,
                                enabled = enabled,
                                params = paramsList
                            )
                        )
                    }
                }

                val routingList = mutableListOf<Int>()
                val routingArr = obj.optJSONArray("routingOrder")
                if (routingArr != null) {
                    for (r in 0 until routingArr.length()) {
                        routingList.add(routingArr.getInt(r))
                    }
                } else {
                    routingList.addAll(0..10)
                }

                val rawSource = if (obj.has("rawSource") && !obj.isNull("rawSource")) {
                    Base64.getDecoder().decode(obj.getString("rawSource"))
                } else {
                    null
                }

                val preset = GP200Preset(
                    version = obj.optString("version", "1.0"),
                    patchName = obj.optString("patchName", ""),
                    author = obj.optString("author", ""),
                    effects = effectsList,
                    routingOrder = routingList,
                    fxLoopSend = obj.optInt("fxLoopSend", 4),
                    fxLoopReturn = obj.optInt("fxLoopReturn", 4),
                    patchVolume = obj.optInt("patchVolume", 50),
                    patchPan = obj.optInt("patchPan", 0),
                    patchTempo = obj.optInt("patchTempo", 120),
                    checksum = obj.optInt("checksum", 0),
                    rawSource = rawSource,
                    drumSync = obj.optBoolean("drumSync", false),
                    controlMappings = obj.optString("controlMappings", "").takeIf { it.isNotBlank() }?.let {
                        runCatching { ControlMappingCodec.decode(Base64.getDecoder().decode(it)) }.getOrNull()
                    }
                )
                result[slot] = preset
            }
            result
        } catch (_: Exception) {
            null
        }
    }

    @Synchronized
    fun saveFullLibrary(presets: Map<Int, GP200Preset>) {
        try {
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            val root = JSONObject()
            for ((slot, preset) in presets) {
                val slotObj = JSONObject()
                slotObj.put("version", preset.version)
                slotObj.put("patchName", preset.patchName)
                slotObj.put("author", preset.author)
                slotObj.put("patchTempo", preset.patchTempo)
                slotObj.put("drumSync", preset.drumSync)
                slotObj.put("patchVolume", preset.patchVolume)
                slotObj.put("patchPan", preset.patchPan)
                slotObj.put("fxLoopSend", preset.fxLoopSend)
                slotObj.put("fxLoopReturn", preset.fxLoopReturn)
                slotObj.put("checksum", preset.checksum)

                val routingArr = JSONArray()
                preset.routingOrder.forEach { routingArr.put(it) }
                slotObj.put("routingOrder", routingArr)

                val effectsArr = JSONArray()
                for (effect in preset.effects) {
                    val effObj = JSONObject()
                    effObj.put("slotIndex", effect.slotIndex)
                    effObj.put("effectId", effect.effectId)
                    effObj.put("enabled", effect.enabled)
                    val paramsArr = JSONArray()
                    effect.params.forEach { paramsArr.put(it.toDouble()) }
                    effObj.put("params", paramsArr)
                    effectsArr.put(effObj)
                }
                slotObj.put("effects", effectsArr)

                if (preset.rawSource != null) {
                    slotObj.put("rawSource", Base64.getEncoder().encodeToString(preset.rawSource))
                }

                preset.controlMappings?.let {
                    slotObj.put("controlMappings", Base64.getEncoder().encodeToString(ControlMappingCodec.encode(it)))
                }
                root.put(slot.toString(), slotObj)
            }

            tempFile.writeText(root.toString(), Charsets.UTF_8)
            if (!tempFile.renameTo(cacheFile)) {
                tempFile.copyTo(cacheFile, overwrite = true)
                tempFile.delete()
            }
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun updatePreset(slot: Int, preset: GP200Preset) {
        if (slot !in 0..255) return
        val current = loadFullLibrary()?.toMutableMap() ?: mutableMapOf()
        current[slot] = preset
        saveFullLibrary(current)
    }

    @Synchronized
    fun clearCache() {
        try {
            if (cacheFile.exists()) {
                cacheFile.delete()
            }
            if (tempFile.exists()) {
                tempFile.delete()
            }
            if (lastSyncFile.exists()) {
                lastSyncFile.delete()
            }
        } catch (_: Exception) {
        }
    }
}
