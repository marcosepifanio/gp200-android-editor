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

    @Test
    fun `keeps the base size when it fits`() {
        assertEquals(10f, fittingLabelSize({ it * 7.6f }, available = 80f))
    }

    @Test
    fun `shrinks in half steps until it fits`() {
        // 9.5 * 7.6 = 72.2 does not fit; 9 * 7.6 = 68.4 does.
        assertEquals(9f, fittingLabelSize({ it * 7.6f }, available = 70f))
    }

    @Test
    fun `stops at the minimum`() {
        assertEquals(8f, fittingLabelSize({ it * 7.6f }, available = 40f))
    }
}
