package com.valeton.editor.ui.drums

import com.valeton.editor.core.model.DrumGenre
import com.valeton.editor.core.model.DrumPattern
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StudioDrumMiniPlayerTest {

    @Test
    fun `calculateBeatDurationMs clamps BPM between 40 and 250`() {
        assertEquals(1500, calculateBeatDurationMs(40))
        assertEquals(500, calculateBeatDurationMs(120))
        assertEquals(240, calculateBeatDurationMs(250))
        // Clamping below 40
        assertEquals(1500, calculateBeatDurationMs(20))
        // Clamping above 250
        assertEquals(240, calculateBeatDurationMs(300))
    }

    @Test
    fun `formatMiniPlayerPrimaryText formats name and bpm accurately`() {
        val pattern = DrumPattern(id = 0, name = "Classic Rock 1", genre = DrumGenre.ROCK, timeSignature = "4/4")
        val formatted = formatMiniPlayerPrimaryText(pattern, 120)
        assertEquals("CLASSIC ROCK 1 · 120 BPM", formatted)
    }

    @Test
    fun `formatMiniPlayerSecondaryText formats genre display name and time signature`() {
        val pattern = DrumPattern(id = 45, name = "Pop 1", genre = DrumGenre.POP, timeSignature = "4/4")
        val formatted = formatMiniPlayerSecondaryText(pattern)
        assertEquals("POP · 4/4", formatted)
    }
}
