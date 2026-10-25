package com.valeton.editor.ui.chain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MasterControlsFormatterTest {

    @Test
    fun `formatPanValue formats center, left and right accurately`() {
        assertEquals("CENTER", MasterControlsFormatter.formatPanValue(0))
        assertEquals("L50", MasterControlsFormatter.formatPanValue(-50))
        assertEquals("L25", MasterControlsFormatter.formatPanValue(-25))
        assertEquals("R50", MasterControlsFormatter.formatPanValue(50))
        assertEquals("R12", MasterControlsFormatter.formatPanValue(12))
    }

    @Test
    fun `formatVolumeValue formats volume with percentage`() {
        assertEquals("0", MasterControlsFormatter.formatVolumeValue(0))
        assertEquals("50", MasterControlsFormatter.formatVolumeValue(50))
        assertEquals("100", MasterControlsFormatter.formatVolumeValue(100))
    }

    @Test
    fun `pan to and from fraction mapping`() {
        // -50 -> 0.0f, 0 -> 0.5f, 50 -> 1.0f
        assertEquals(0.0f, MasterControlsFormatter.panToFraction(-50))
        assertEquals(0.5f, MasterControlsFormatter.panToFraction(0))
        assertEquals(1.0f, MasterControlsFormatter.panToFraction(50))

        assertEquals(-50, MasterControlsFormatter.fractionToPan(0.0f))
        assertEquals(0, MasterControlsFormatter.fractionToPan(0.5f))
        assertEquals(50, MasterControlsFormatter.fractionToPan(1.0f))
        assertEquals(-25, MasterControlsFormatter.fractionToPan(0.25f))
        assertEquals(25, MasterControlsFormatter.fractionToPan(0.75f))
    }

    @Test
    fun `volume to and from fraction mapping`() {
        // 0 -> 0.0f, 50 -> 0.5f, 100 -> 1.0f
        assertEquals(0.0f, MasterControlsFormatter.volumeToFraction(0))
        assertEquals(0.5f, MasterControlsFormatter.volumeToFraction(50))
        assertEquals(1.0f, MasterControlsFormatter.volumeToFraction(100))

        assertEquals(0, MasterControlsFormatter.fractionToVolume(0.0f))
        assertEquals(50, MasterControlsFormatter.fractionToVolume(0.5f))
        assertEquals(100, MasterControlsFormatter.fractionToVolume(1.0f))
    }
}
