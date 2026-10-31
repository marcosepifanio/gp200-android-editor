package com.valeton.editor.core.codec

import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.core.model.KnobParam
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
}
