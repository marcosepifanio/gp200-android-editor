package com.valeton.editor.ui.controls

import com.valeton.editor.core.model.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ControlMappingOptionsTest {
    private val preset = GP200Preset(patchName = "Controls", effects = listOf(
        EffectSlot(0, 25, true), EffectSlot(10, 100663299, true), EffectSlot(3, 117440565, false)
    ))

    @Test
    fun `knob destinations follow hardware limits and include special targets`() {
        val ids = ControlMappingOptions.knobTargets(preset).map { it.id }
        assertTrue(ids.containsAll(listOf(0, 3, 11, 12, 255)))
        assertFalse(10 in ids)
        assertEquals(ids, ControlMappingOptions.knobTargets(preset.copy(effects = preset.effects.reversed())).map { it.id })
    }

    @Test
    fun `parameter options include supported discrete parameters and reject unknown effects`() {
        val parameters = ControlMappingOptions.parameters(preset, 0)
        assertEquals(listOf(0, 1, 2, 3), parameters.map { it.id })
        assertEquals("Low Cut", parameters.last().label)
        assertTrue(ControlMappingOptions.parameters(preset, 9).isEmpty())
    }

    @Test
    fun `existing multiple CTRL targets are displayed without choosing a single default`() {
        val ctrl = CtrlAssignment(7, 123, (1L shl 0) or (1L shl 3))
        assertTrue(ControlMappingOptions.ctrlLabel(preset, ctrl).startsWith("Vários efeitos"))
        assertEquals(2, ctrl.effectBlocks.size)
    }

    @Test
    fun `effect label uses the pedal name for a user IR`() {
        val preset = GP200Preset(patchName = "P", effects = listOf(EffectSlot(slotIndex = 5, effectId = 168820736, enabled = true)))
        assertEquals("CAB · ir1", ControlMappingOptions.effectLabel(preset, 5, mapOf(168820736 to "ir1")))
        assertEquals("CAB · ir1", ControlMappingOptions.knobTargetLabel(preset, 5, mapOf(168820736 to "ir1")))
    }
}
