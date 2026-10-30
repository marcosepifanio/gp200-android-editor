package com.valeton.editor.repository

import org.json.JSONObject
import java.io.File

/** Caches user IR/SnapTone names read from the pedal (effectId → name), apart from the preset library. */
class UserModelNamesRepository(
    private val storageDir: File
) {
    private val cacheFile: File
        get() = File(storageDir, "user_model_names.json")

    private val tempFile: File
        get() = File(storageDir, "user_model_names.json.tmp")

    @Synchronized
    fun load(): Map<Int, String> {
        if (!cacheFile.exists()) return emptyMap()
        return try {
            val root = JSONObject(cacheFile.readText(Charsets.UTF_8))
            root.keys().asSequence()
                .mapNotNull { key -> key.toIntOrNull()?.let { it to root.getString(key) } }
                .toMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    @Synchronized
    fun save(names: Map<Int, String>) {
        try {
            val root = JSONObject()
            names.forEach { (effectId, name) -> root.put(effectId.toString(), name) }
            tempFile.writeText(root.toString(), Charsets.UTF_8)
            if (!tempFile.renameTo(cacheFile)) {
                cacheFile.writeText(root.toString(), Charsets.UTF_8)
                tempFile.delete()
            }
        } catch (_: Exception) {
        }
    }
}
