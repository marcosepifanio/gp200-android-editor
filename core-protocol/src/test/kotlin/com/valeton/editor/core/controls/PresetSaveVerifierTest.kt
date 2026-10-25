package com.valeton.editor.core.controls

import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.codec.PresetSaveVerifier
import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.GP200Preset
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PresetSaveVerifierTest {
    @Test
    fun `every catalog model verifies only its supported parameters and still detects real changes`() {
        for (model in EffectCatalog.getAllEffects()) {
            val supported = model.params.map { it.index }.toSet()
            assertTrue(supported.all { it in 0..14 }, model.name)
            val effect = EffectSlot(model.module.slotIndex, model.effectId, true,
                List(15) { index -> model.params.find { it.index == index }?.defaultValue ?: 77f })
            val expected = GP200Preset(patchName = "Audit", effects = listOf(effect))
            val normalized = effect.copy(params = effect.params.mapIndexed { index, value -> if (index in supported) value else 0f })
            assertTrue(PresetSaveVerifier.differences(expected, expected.copy(effects = listOf(normalized))).isEmpty(),
                "${model.module.code}/${model.name}: unused file positions must not cause save failure")
            for (index in supported) {
                val changed = normalized.copy(params = normalized.params.toMutableList().apply { set(index, get(index) + 1f) })
                assertTrue(PresetSaveVerifier.differences(expected, expected.copy(effects = listOf(changed)))
                    .contains("${model.module.code} parâmetro ${index + 1}"), "${model.name}: real parameter $index must be checked")
            }
        }
    }

    @Test
    fun `P-Wah fifth parameter remains verified because it is a real EQ switch`() {
        val effect = EffectSlot(1, 83886090, true)
        val expected = GP200Preset(patchName = "P-Wah", effects = listOf(effect))
        val changed = effect.copy(params = effect.params.toMutableList().apply { set(4, 1f) })
        assertEquals(listOf("WAH parâmetro 5"), PresetSaveVerifier.differences(expected, expected.copy(effects = listOf(changed))))
    }

    @Test
    fun `unknown models retain strict comparison of all stored parameters`() {
        val effect = EffectSlot(1, Int.MAX_VALUE, true)
        val expected = GP200Preset(patchName = "Unknown", effects = listOf(effect))
        val changed = effect.copy(params = effect.params.toMutableList().apply { set(14, 1f) })
        assertEquals(listOf("WAH parâmetro 15"), PresetSaveVerifier.differences(expected, expected.copy(effects = listOf(changed))))
    }
}
