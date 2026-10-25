package com.valeton.editor.repository

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PresetNamesRepositoryTest {

    @Test
    fun `loadCachedNames returns 256 default seeded names when file does not exist`(@TempDir tempDir: File) {
        val repo = PresetNamesRepository(tempDir)
        val names = repo.loadCachedNames()

        assertEquals(256, names.size)
        assertEquals("Patch 1A", names[0])
        assertEquals("Patch 1B", names[1])
        assertEquals("Patch 64D", names[255])
    }

    @Test
    fun `saveCachedNames persists names to json file and reloads accurately`(@TempDir tempDir: File) {
        val repo = PresetNamesRepository(tempDir)
        val customNames = (0..255).map { "Custom %02d".format(it) }

        repo.saveCachedNames(customNames)

        val reloaded = repo.loadCachedNames()
        assertEquals(256, reloaded.size)
        assertEquals("Custom 00", reloaded[0])
        assertEquals("Custom 42", reloaded[42])
    }

    @Test
    fun `updateSlotName updates specific slot and persists`(@TempDir tempDir: File) {
        val repo = PresetNamesRepository(tempDir)
        repo.updateSlotName(10, "Acoustic Stage")

        val reloaded = repo.loadCachedNames()
        assertEquals("Acoustic Stage", reloaded[10])
    }
}
