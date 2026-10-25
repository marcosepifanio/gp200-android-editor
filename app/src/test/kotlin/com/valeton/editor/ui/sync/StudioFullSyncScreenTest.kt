package com.valeton.editor.ui.sync

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StudioFullSyncScreenTest {

    @Test
    fun `formatSyncStatusText with slot 0 formats correctly`() {
        val result = formatSyncStatusText(slot = 0, name = "Acoustic Sim")
        assertEquals("[01-A · #01/256] ACOUSTIC SIM", result)
    }

    @Test
    fun `formatSyncStatusText with slot 53 produces 14-B and 54 of 256`() {
        val result = formatSyncStatusText(slot = 53, name = "modern highgain")
        assertEquals("[14-B · #54/256] MODERN HIGHGAIN", result)
    }

    @Test
    fun `formatSyncStatusText with slot 255 produces 64-D and 256 of 256`() {
        val result = formatSyncStatusText(slot = 255, name = "Ultimate Lead")
        assertEquals("[64-D · #256/256] ULTIMATE LEAD", result)
    }

    @Test
    fun `formatSyncStatusText with blank or whitespace name defaults to CARREGANDO`() {
        assertEquals("[01-A · #01/256] CARREGANDO...", formatSyncStatusText(0, ""))
        assertEquals("[01-A · #01/256] CARREGANDO...", formatSyncStatusText(0, "   "))
    }

    @Test
    fun `formatSyncStatusText clamps negative slot to 0`() {
        val result = formatSyncStatusText(slot = -5, name = "Preset Minus")
        assertEquals("[01-A · #01/256] PRESET MINUS", result)
    }

    @Test
    fun `formatSyncStatusText clamps out of bounds slot to 255`() {
        val result = formatSyncStatusText(slot = 999, name = "Preset Over")
        assertEquals("[64-D · #256/256] PRESET OVER", result)
    }
}
