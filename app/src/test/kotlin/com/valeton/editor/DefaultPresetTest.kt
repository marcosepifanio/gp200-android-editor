package com.valeton.editor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DefaultPresetTest {
    @Test
    fun `offline preset starts bypassed with the first models shown in the picker`() {
        val preset = createDefaultPreset()

        assertEquals((0..10).toList(), preset.effects.map { it.slotIndex })
        assertTrue(preset.effects.all { !it.enabled })
        assertEquals(
            listOf(16777216, 83886087, 50331712, 134217850, 27, 167772220,
                16777273, 67108864, 184549394, 201326609, 100663299),
            preset.effects.map { it.effectId }
        )
        assertEquals("Preset Inicial", preset.patchName)
        assertEquals("", preset.author)
        assertNull(preset.rawSource)
    }

    @Test
    fun `midpoints use protocol indices and catalog steps instead of a universal fifty`() {
        val preset = createDefaultPreset()
        val cabinet = preset.effects.single { it.slotIndex == 5 }
        assertEquals(15, cabinet.params.size)
        assertEquals(0f, cabinet.params[0]) // Unmodeled byte positions stay neutral.
        assertEquals(50f, cabinet.params[1])
        assertEquals(1010f, cabinet.params[5]) // 19..2000, integer step.
        assertEquals(11001f, cabinet.params[6]) // 2000..20001, integer step.

        val eq = preset.effects.single { it.slotIndex == 6 }
        assertEquals(listOf(0f, 0f, 0f, 0f, 0f, 50f), eq.params.take(6))
        val chorus = preset.effects.single { it.slotIndex == 7 }
        // 5.05 is halfway between two valid steps; either nearest step is central.
        assertTrue(listOf(5f, 5.1f).any { kotlin.math.abs(it - chorus.params[1]) < 0.0001f })
        assertEquals(0f, chorus.params[3]) // Central lower option keeps Sync OFF.

        val delay = preset.effects.single { it.slotIndex == 8 }
        assertEquals(2010f, delay.params[1]) // 20..4000 milliseconds.
        assertEquals(0f, delay.params[3])
        assertEquals(50f, preset.effects.single { it.slotIndex == 10 }.params[0])
    }
}
