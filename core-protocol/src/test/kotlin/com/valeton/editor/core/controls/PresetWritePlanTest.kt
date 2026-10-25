package com.valeton.editor.core.controls

import com.valeton.editor.core.codec.PRSTDecoder
import com.valeton.editor.core.codec.PresetWritePlan
import com.valeton.editor.core.codec.SysExCodec
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PresetWritePlanTest {
    private fun fixture() = PRSTDecoder(javaClass.getResourceAsStream("/fixtures/clean.prst")!!.readBytes()).decode()

    @Test
    fun `unchanged state and volatile CTRL padding require no replay`() {
        val expected = fixture()
        val controls = expected.controlMappings!!
        val actual = expected.copy(controlMappings = controls.copy(ctrls = controls.ctrls.map { it.copy(reserved = it.reserved xor 0x1234) }))
        assertTrue(PresetWritePlan.commands(expected, actual).isEmpty())
    }

    @Test
    fun `single parameter edit leaves other effects untouched`() {
        val actual = fixture()
        val effect = actual.effects.first()
        val changed = effect.copy(params = effect.params.toMutableList().apply { set(1, get(1) + 1f) })
        val expected = actual.copy(effects = actual.effects.map { if (it.slotIndex == effect.slotIndex) changed else it })
        val commands = PresetWritePlan.commands(expected, actual)
        assertEquals(1, commands.size)
        assertArrayEquals(SysExCodec.buildParamChange(effect.slotIndex, 1, effect.effectId, changed.params[1]), commands.single())
    }

    @Test
    fun `model change precedes all parameters and final enabled state`() {
        val actual = fixture()
        val effect = actual.effects.first()
        val changed = effect.copy(effectId = 1, enabled = !effect.enabled)
        val expected = actual.copy(effects = actual.effects.map { if (it.slotIndex == effect.slotIndex) changed else it })
        val commands = PresetWritePlan.commands(expected, actual)
        assertEquals(6, commands.size)
        assertArrayEquals(SysExCodec.buildEffectChange(changed.slotIndex, changed.effectId), commands.first())
        for (index in 0 until 4) assertArrayEquals(SysExCodec.buildParamChange(changed.slotIndex, index, changed.effectId, changed.params[index]), commands[index + 1])
        assertArrayEquals(SysExCodec.buildToggle(changed.slotIndex, changed.enabled), commands.last())
    }
    @Test
    fun `COMP replay sends Sustain 89 and Volume 60 without unused parameter writes`() {
        val expected = fixture().copy(effects = listOf(com.valeton.editor.core.model.EffectSlot(
            slotIndex = 0, effectId = 0, enabled = true,
            params = listOf(89f, 60f) + List(13) { 0f }
        )))
        val commands = PresetWritePlan.commands(expected, null)
        val parameterCommands = commands.filter { it.size == 62 }
        assertEquals(2, parameterCommands.size)
        assertArrayEquals(SysExCodec.buildParamChange(0, 0, 0, 89f), parameterCommands[0])
        assertArrayEquals(SysExCodec.buildParamChange(0, 1, 0, 60f), parameterCommands[1])
    }
    @Test
    fun `all models send each supported parameter once and bound switches precede their knobs`() {
        val rows = javaClass.getResourceAsStream("/fixtures/effect-parameter-schema.tsv")!!.bufferedReader()
            .readLines().filterNot { it.startsWith("#") || it.isBlank() }
        for (row in rows) {
            val fields = row.split('\t')
            val model = com.valeton.editor.core.catalog.EffectCatalog.getEffectById(fields[0].toInt())!!
            val effect = com.valeton.editor.core.model.EffectSlot(model.module.slotIndex, model.effectId, true)
            val expected = com.valeton.editor.core.model.GP200Preset(patchName = "Audit", effects = listOf(effect))
            val commands = PresetWritePlan.commands(expected, null).filter { it.size == 62 }
            val indices = commands.map { SysExCodec.nibbleDecode(it.copyOfRange(13, 61))[13].toInt() and 255 }
            val supported = fields[2].split(',').map(String::toInt)
            assertEquals(supported.sorted(), indices.sorted(), model.name)
            if (fields[3] != "-") for (binding in fields[3].split(',')) {
                val (selector, knob) = binding.split('>').map(String::toInt)
                assertTrue(indices.indexOf(selector) < indices.indexOf(knob), "${model.name}: selector $selector must precede knob $knob")
            }
        }
    }

    @Test
    fun `changing Analog Sync restores the expected Time after the switch`() {
        val effect = com.valeton.editor.core.model.EffectSlot(8, 184549377, true,
            listOf(20f, 400f, 30f, 0f, 0f) + List(10) { 0f })
        val expected = com.valeton.editor.core.model.GP200Preset(patchName = "Analog", effects = listOf(effect))
        val before = expected.copy(effects = listOf(effect.copy(params = effect.params.toMutableList().apply { set(3, 1f) })))
        val commands = PresetWritePlan.commands(expected, before)
        assertEquals(2, commands.size)
        assertArrayEquals(SysExCodec.buildParamChange(8, 3, effect.effectId, 0f), commands[0])
        assertArrayEquals(SysExCodec.buildParamChange(8, 1, effect.effectId, 400f), commands[1])
    }
    @Test
    fun `BPM edits restore Dual Echo and Ring Echo time fields with a confirmed baseline`() {
        for ((id, times) in listOf(184549379 to listOf(2, 5), 184549385 to listOf(2))) {
            val effect = com.valeton.editor.core.model.EffectSlot(8, id, true)
            val before = com.valeton.editor.core.model.GP200Preset(patchName = "Tempo", effects = listOf(effect))
            val expected = before.copy(patchTempo = 98)
            val commands = PresetWritePlan.commands(expected, before)
            val indices = commands.filter { it.size == 62 }.map { SysExCodec.nibbleDecode(it.copyOfRange(13, 61))[13].toInt() and 255 }
            assertEquals(times, indices, "model $id must restore Time, not Feedback")
        }
    }
}
