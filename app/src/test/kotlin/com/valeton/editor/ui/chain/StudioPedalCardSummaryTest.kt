package com.valeton.editor.ui.chain

import com.valeton.editor.core.catalog.EffectCatalog
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StudioPedalCardSummaryTest {
    @Test
    fun `cabinet summary displays values at the catalog protocol indices`() {
        val params = MutableList(15) { 0f }.apply {
            this[1] = 50f
            this[5] = 1010f
            this[6] = 11001f
        }
        assertEquals("Volume 50 · Low Cut 1010", summarizeParams(EffectCatalog.getEffectById(167772220), params))
    }
}
