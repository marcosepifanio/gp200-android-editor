package com.valeton.editor.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class UserModelNamesRepositoryTest {

    @Test
    fun `load returns empty map when file is missing`(@TempDir tempDir: File) {
        assertEquals(emptyMap<Int, String>(), UserModelNamesRepository(tempDir).load())
    }

    @Test
    fun `save then load round-trips names`(@TempDir tempDir: File) {
        val names = mapOf(168820736 to "ir1", 251658245 to "nam1")
        UserModelNamesRepository(tempDir).save(names)
        assertEquals(names, UserModelNamesRepository(tempDir).load())
    }

    @Test
    fun `corrupt file loads as empty map`(@TempDir tempDir: File) {
        File(tempDir, "user_model_names.json").writeText("{not json")
        assertEquals(emptyMap<Int, String>(), UserModelNamesRepository(tempDir).load())
    }
}
