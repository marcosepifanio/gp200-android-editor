package com.valeton.editor.ui.editor

import com.valeton.editor.core.catalog.EffectCatalog
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EffectInfoDialogTest {

    private val memoryMan = EffectCatalog.getAllEffects().first { it.basedOn.contains("Deluxe Memory Man") }

    @Test
    fun `info is available for an effect with a description`() {
        assertTrue(infoAvailable(memoryMan))
    }

    @Test
    fun `no info without a description`() {
        assertFalse(infoAvailable(null))
        assertFalse(infoAvailable(memoryMan.copy(basedOn = "")))
        assertFalse(infoAvailable(memoryMan.copy(basedOn = "  ")))
    }
}
