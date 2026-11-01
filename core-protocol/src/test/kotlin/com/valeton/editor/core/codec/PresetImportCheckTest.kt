package com.valeton.editor.core.codec

import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.core.model.ComboBoxParam
import com.valeton.editor.core.model.ControlMappings
import com.valeton.editor.core.model.KnobParam
import com.valeton.editor.core.model.QuickKnobAssignment
import com.valeton.editor.core.model.SwitchParam
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PresetImportCheckTest {

    private fun fixture(name: String): GP200Preset {
        val bytes = javaClass.classLoader.getResourceAsStream("fixtures/$name")!!.use { it.readBytes() }
        return PRSTDecoder(bytes).decode()
    }

    @Test
    fun `known presets pass unchanged`() {
        for (name in listOf("clean.prst", "01-A_CTRL_TEST.prst")) {
            val preset = fixture(name)
            val result = PresetImportCheck.check(preset)
            assertEquals(0, result.clampedParams, name)
            assertEquals(emptyList<Int>(), result.unknownEffectSlots, name)
            assertEquals(preset, result.preset, name)
        }
    }

    @Test
    fun `knob values outside the catalog range are held to it`() {
        val preset = fixture("clean.prst")
        val slot = preset.effects.first { slot ->
            EffectCatalog.getEffectById(slot.effectId)?.params?.none { it is SwitchParam && it.boundParamIndex != null } == true &&
                EffectCatalog.getEffectById(slot.effectId)?.params?.any { it is KnobParam } == true
        }
        val knob = EffectCatalog.getEffectById(slot.effectId)!!.params.filterIsInstance<KnobParam>().first()
        val params = slot.params.toMutableList().apply { this[knob.index] = knob.max + 1000f }
        val broken = preset.copy(effects = preset.effects.map { if (it === slot) it.copy(params = params) else it })

        val result = PresetImportCheck.check(broken)

        assertEquals(1, result.clampedParams)
        assertEquals(knob.max, result.preset.effects.first { it.slotIndex == slot.slotIndex }.params[knob.index])
    }

    @Test
    fun `a knob turned into a note division by Sync is left alone`() {
        val preset = fixture("clean.prst")
        // clean.prst has a delay with Sync on and Time holding a division index below the millisecond range.
        val delay = preset.effects.first { it.slotIndex == 8 }
        val sync = EffectCatalog.getEffectById(delay.effectId)!!.params
            .filterIsInstance<SwitchParam>().first { it.boundParamIndex != null }
        assertNotEquals(0f, delay.params[sync.index])

        val result = PresetImportCheck.check(preset)

        assertEquals(delay.params, result.preset.effects.first { it.slotIndex == 8 }.params)
    }

    @Test
    fun `unknown models are reported and kept`() {
        val preset = fixture("clean.prst")
        val broken = preset.copy(effects = preset.effects.map { if (it.slotIndex == 2) it.copy(effectId = 0x7ABCDEF) else it })

        val result = PresetImportCheck.check(broken)

        assertEquals(listOf(2), result.unknownEffectSlots)
        assertEquals(0x7ABCDEF, result.preset.effects.first { it.slotIndex == 2 }.effectId)
    }

    @Test
    fun `a chain with a missing or repeated block is refused`() {
        val preset = fixture("clean.prst")
        assertThrows<IllegalArgumentException> { PresetImportCheck.check(preset.copy(effects = preset.effects.drop(1))) }
        val repeated = preset.effects.map { if (it.slotIndex == 3) it.copy(slotIndex = 4) else it }
        assertThrows<IllegalArgumentException> { PresetImportCheck.check(preset.copy(effects = repeated)) }
    }

    @Test
    fun `a decoded file whose blocks carry an invalid index is refused`() {
        val bytes = javaClass.classLoader.getResourceAsStream("fixtures/clean.prst")!!.use { it.readBytes() }
        // Block 3 claims to be block 40: the decoder drops it, leaving ten blocks in the chain.
        bytes[PRSTDecoder.EFFECT_BLOCK_START + 3 * PRSTDecoder.EFFECT_BLOCK_SIZE + PRSTDecoder.SLOT_OFFSET] = 40
        assertThrows<IllegalArgumentException> { PresetImportCheck.check(PRSTDecoder(bytes).decode()) }
    }

    private fun withParam(preset: GP200Preset, value: Float, pick: (com.valeton.editor.core.model.EffectDefinition) -> Int?): Pair<GP200Preset, Int> {
        val slot = preset.effects.first { s -> EffectCatalog.getEffectById(s.effectId)?.let(pick) != null }
        val index = pick(EffectCatalog.getEffectById(slot.effectId)!!)!!
        val params = slot.params.toMutableList().apply { this[index] = value }
        return preset.copy(effects = preset.effects.map { if (it === slot) it.copy(params = params) else it }) to slot.slotIndex
    }

    @Test
    fun `a NaN knob value refuses the file`() {
        val (broken, _) = withParam(fixture("clean.prst"), Float.NaN) { def -> def.params.filterIsInstance<KnobParam>().firstOrNull()?.index }
        assertThrows<IllegalArgumentException> { PresetImportCheck.check(broken) }
    }

    @Test
    fun `a NaN switch value refuses the file`() {
        val (broken, _) = withParam(fixture("01-A_CTRL_TEST.prst"), Float.NaN) { def -> def.params.filterIsInstance<SwitchParam>().firstOrNull()?.index }
        assertThrows<IllegalArgumentException> { PresetImportCheck.check(broken) }
    }

    @Test
    fun `an infinite knob value is held to the catalog max`() {
        val preset = fixture("clean.prst")
        val (broken, slotIndex) = withParam(preset, Float.POSITIVE_INFINITY) { def ->
            def.takeIf { d -> d.params.none { it is SwitchParam && it.boundParamIndex != null } }
                ?.params?.filterIsInstance<KnobParam>()?.firstOrNull()?.index
        }
        val result = PresetImportCheck.check(broken)
        val slot = result.preset.effects.first { it.slotIndex == slotIndex }
        val knob = EffectCatalog.getEffectById(slot.effectId)!!.params.filterIsInstance<KnobParam>().first()
        assertEquals(knob.max, slot.params[knob.index])
        assertEquals(1, result.clampedParams)
    }

    private fun firstWith(preset: GP200Preset, pick: (com.valeton.editor.core.model.EffectDefinition) -> Boolean) =
        preset.effects.firstOrNull { EffectCatalog.getEffectById(it.effectId)?.let(pick) == true }
            ?: error("fixture has no matching effect")

    @Test
    fun `a switch value outside its options goes back to the catalog default`() {
        val (broken, slotIndex) = withParam(fixture("01-A_CTRL_TEST.prst"), 7f) { def -> def.params.filterIsInstance<SwitchParam>().firstOrNull()?.index }
        val result = PresetImportCheck.check(broken)
        val slot = result.preset.effects.first { it.slotIndex == slotIndex }
        val switch = EffectCatalog.getEffectById(slot.effectId)!!.params.filterIsInstance<SwitchParam>().first()
        assertEquals(switch.defaultValue, slot.params[switch.index])
        assertEquals(1, result.clampedParams)
    }

    @Test
    fun `a combo box value outside its options goes back to the catalog default`() {
        // The fixtures hold no model with a combo box, so put one into its module's block.
        val def = EffectCatalog.getAllEffects().first { d -> d.params.any { it is ComboBoxParam } }
        val params = MutableList(15) { 0f }.apply { def.params.forEach { this[it.index] = it.defaultValue } }
        val preset = fixture("clean.prst").let { p ->
            p.copy(effects = p.effects.map { if (it.slotIndex == def.module.slotIndex) it.copy(effectId = def.effectId, params = params) else it })
        }
        val (broken, slotIndex) = withParam(preset, 99f) { d -> d.params.filterIsInstance<ComboBoxParam>().firstOrNull()?.index }
        val result = PresetImportCheck.check(broken)
        val slot = result.preset.effects.first { it.slotIndex == slotIndex }
        val combo = EffectCatalog.getEffectById(slot.effectId)!!.params.filterIsInstance<ComboBoxParam>().first()
        assertEquals(combo.defaultValue, slot.params[combo.index])
        assertEquals(1, result.clampedParams)
    }

    @Test
    fun `an invalid Sync value does not exempt its knob from the range`() {
        val preset = fixture("clean.prst")
        val delay = preset.effects.first { it.slotIndex == 8 }
        val def = EffectCatalog.getEffectById(delay.effectId)!!
        val sync = def.params.filterIsInstance<SwitchParam>().first { it.boundParamIndex != null }
        val time = def.params.filterIsInstance<KnobParam>().first { it.index == sync.boundParamIndex }
        val params = delay.params.toMutableList().apply { this[sync.index] = 1e6f; this[time.index] = 1e9f }
        val result = PresetImportCheck.check(preset.copy(effects = preset.effects.map { if (it === delay) it.copy(params = params) else it }))
        val checked = result.preset.effects.first { it.slotIndex == 8 }.params
        assertEquals(sync.defaultValue, checked[sync.index])
        assertEquals(time.max, checked[time.index])
    }

    @Test
    fun `a synced knob is held between zero and its max`() {
        val preset = fixture("clean.prst")
        val delay = preset.effects.first { it.slotIndex == 8 }
        val def = EffectCatalog.getEffectById(delay.effectId)!!
        val sync = def.params.filterIsInstance<SwitchParam>().first { it.boundParamIndex != null }
        val time = def.params.filterIsInstance<KnobParam>().first { it.index == sync.boundParamIndex }
        for ((value, expected) in listOf(1e9f to time.max, -5f to 0f)) {
            val params = delay.params.toMutableList().apply { this[time.index] = value }
            val result = PresetImportCheck.check(preset.copy(effects = preset.effects.map { if (it === delay) it.copy(params = params) else it }))
            assertEquals(expected, result.preset.effects.first { it.slotIndex == 8 }.params[time.index])
        }
    }

    @Test
    fun `patch volume and pan are held to the editor ranges`() {
        val preset = fixture("clean.prst")
        val result = PresetImportCheck.check(preset.copy(patchVolume = 255, patchPan = -3000))
        assertEquals(100, result.preset.patchVolume)
        assertEquals(-50, result.preset.patchPan)
        assertEquals(2, result.clampedParams)
    }

    @Test
    fun `quick knobs pointing at a missing block or parameter are turned off`() {
        val preset = fixture("01-A_CTRL_TEST.prst")
        val mappings = preset.controlMappings!!
        val validBlock = firstWith(preset) { def -> def.params.isNotEmpty() }.slotIndex.takeIf { it in 0..9 }
            ?: preset.effects.first { it.slotIndex in 0..9 && EffectCatalog.getEffectById(it.effectId)?.params?.isNotEmpty() == true }.slotIndex
        val broken = preset.copy(controlMappings = ControlMappings(
            knobs = listOf(QuickKnobAssignment(10, 0), QuickKnobAssignment(validBlock, 999), QuickKnobAssignment(255, 0)),
            ctrls = mappings.ctrls
        ))
        val result = PresetImportCheck.check(broken)
        val knobs = result.preset.controlMappings!!.knobs
        assertEquals(QuickKnobAssignment.OFF, knobs[0])
        assertEquals(QuickKnobAssignment.OFF, knobs[1])
        assertEquals(QuickKnobAssignment(255, 0), knobs[2], "Files store OFF with parameter 0; keep it")
        assertEquals(mappings.ctrls, result.preset.controlMappings!!.ctrls)
        assertEquals(2, result.clampedParams)
    }
}
