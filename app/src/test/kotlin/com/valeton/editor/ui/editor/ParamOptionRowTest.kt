package com.valeton.editor.ui.editor

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParamOptionRowTest {

    @Test
    fun `fits when label, gap and all options fit`() {
        // 100 + 12 + 60 + 6 + 60 = 238
        assertTrue(optionsFitInline(labelWidth = 100, optionWidths = listOf(60, 60), optionGap = 6, labelGap = 12, maxWidth = 300))
        assertTrue(optionsFitInline(labelWidth = 100, optionWidths = listOf(60, 60), optionGap = 6, labelGap = 12, maxWidth = 238))
    }

    @Test
    fun `does not fit when the sum passes the width by one pixel`() {
        assertFalse(optionsFitInline(labelWidth = 100, optionWidths = listOf(60, 60), optionGap = 6, labelGap = 12, maxWidth = 237))
    }

    @Test
    fun `name wider than the room left wraps`() {
        assertFalse(optionsFitInline(labelWidth = 250, optionWidths = listOf(60), optionGap = 6, labelGap = 12, maxWidth = 300))
    }

    @Test
    fun `classic bass midrange at 360dp does not fit`() {
        // Approximate pixels at density 3: "MIDRANGE" label and the five 12sp mono option chips.
        assertFalse(
            optionsFitInline(
                labelWidth = 230,
                optionWidths = listOf(160, 160, 160, 185, 140),
                optionGap = 18,
                labelGap = 36,
                maxWidth = 924
            )
        )
    }
}
