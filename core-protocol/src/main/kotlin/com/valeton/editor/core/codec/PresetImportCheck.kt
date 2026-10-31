package com.valeton.editor.core.codec

import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.core.model.KnobParam
import com.valeton.editor.core.model.SwitchParam

/**
 * Checks a preset from a shared .prst before it can be written to the pedal. A broken chain is refused,
 * knob values are held to the catalog range (except a knob a Sync switch has turned into a note division), and models the catalog does not know are only reported:
 * they may come from a newer firmware, so they are kept as they are.
 */
object PresetImportCheck {

    data class Result(
        val preset: GP200Preset,
        /** Knob values moved to the catalog min/max. */
        val clampedParams: Int,
        /** Block indices (0 = PRE … 10 = VOL) whose model is not in the catalog. */
        val unknownEffectSlots: List<Int>
    )

    private val BLOCKS = (0..10).toList()

    /** @throws IllegalArgumentException when the chain does not hold each of the 11 blocks exactly once. */
    fun check(preset: GP200Preset): Result {
        require(preset.effects.map { it.slotIndex }.sorted() == BLOCKS) {
            "Invalid .prst file: the chain must hold each of the 11 blocks once"
        }
        require(preset.routingOrder.sorted() == BLOCKS) {
            "Invalid .prst file: the routing order must list each of the 11 blocks once"
        }

        var clamped = 0
        val unknown = mutableListOf<Int>()
        val effects = preset.effects.map { slot ->
            val definition = EffectCatalog.getEffectById(slot.effectId)
            if (definition == null) {
                unknown += slot.slotIndex
                return@map slot
            }
            val params = slot.params.toMutableList()
            // With Sync on, a bound knob (delay Time, modulation Rate) holds a note division, not its range.
            val synced = definition.params.filterIsInstance<SwitchParam>()
                .filter { it.boundParamIndex != null && (params.getOrNull(it.index) ?: 0f) != 0f }
                .mapNotNull { it.boundParamIndex }
                .toSet()
            definition.params.filterIsInstance<KnobParam>().filter { it.index !in synced }.forEach { knob ->
                val value = params.getOrNull(knob.index) ?: return@forEach
                val held = value.coerceIn(knob.min, knob.max)
                if (held != value) {
                    params[knob.index] = held
                    clamped++
                }
            }
            slot.copy(params = params)
        }

        return Result(preset.copy(effects = effects), clamped, unknown.sorted())
    }
}
