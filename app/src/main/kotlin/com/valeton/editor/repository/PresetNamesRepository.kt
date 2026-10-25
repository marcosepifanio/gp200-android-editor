package com.valeton.editor.repository

import com.valeton.editor.core.codec.SysExCodec
import java.io.File

class PresetNamesRepository(
    private val storageDir: File
) {
    private val cacheFile: File
        get() = File(storageDir, "preset_names_cache.txt")

    private val defaultSeedNames: List<String> by lazy {
        (0..255).map { index ->
            val label = SysExCodec.slotToLabel(index)
            "Patch $label"
        }
    }

    @Synchronized
    fun loadCachedNames(): List<String> {
        if (!cacheFile.exists()) {
            return defaultSeedNames
        }
        return try {
            val lines = cacheFile.readLines(Charsets.UTF_8)
            if (lines.isEmpty()) return defaultSeedNames
            val list = lines.toMutableList()
            while (list.size < 256) {
                list.add(defaultSeedNames.getOrElse(list.size) { "Patch %02d".format(list.size) })
            }
            list.take(256)
        } catch (_: Exception) {
            defaultSeedNames
        }
    }

    @Synchronized
    fun saveCachedNames(names: List<String>) {
        try {
            val text = names.take(256).joinToString("\n")
            val tempFile = File(storageDir, "preset_names_cache.txt.tmp")
            tempFile.writeText(text, Charsets.UTF_8)
            if (!tempFile.renameTo(cacheFile)) {
                tempFile.copyTo(cacheFile, overwrite = true)
                tempFile.delete()
            }
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun updateSlotName(slot: Int, name: String) {
        if (slot !in 0..255) return
        val current = loadCachedNames().toMutableList()
        current[slot] = name
        saveCachedNames(current)
    }
}
