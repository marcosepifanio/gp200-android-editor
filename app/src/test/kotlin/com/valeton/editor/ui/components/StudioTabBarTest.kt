package com.valeton.editor.ui.components

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StudioTabBarTest {

    @Test
    fun `StudioTab includes DRUMS tab with Portuguese label RITMOS`() {
        val tabNames = StudioTab.entries.map { it.name }
        assertTrue(tabNames.contains("DRUMS"))

        val drumsTab = StudioTab.valueOf("DRUMS")
        assertEquals("RITMOS", drumsTab.label)
    }
}
