package com.valeton.editor.ui.manage

import com.valeton.editor.core.codec.SysExCodec
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PatchMetadataDialogTextTest {

    @Test
    fun `overwrite confirmation names the target slot and its preset`() {
        val text = overwriteConfirmationText(10, "Clean")
        assertTrue(SysExCodec.slotToLabel(10) in text, text)
        assertTrue("Clean" in text, text)
    }

    @Test
    fun `overwrite confirmation without a known name shows only the slot`() {
        listOf(null, "", "  ").forEach { name ->
            val text = overwriteConfirmationText(10, name)
            assertTrue(SysExCodec.slotToLabel(10) in text, text)
            assertFalse("\"\"" in text || "“”" in text, text)
        }
    }
}
