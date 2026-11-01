package com.valeton.editor.core.codec

import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.ComboBoxParam
import com.valeton.editor.core.model.ControlMappings
import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.core.model.KnobParam
import com.valeton.editor.core.model.QuickKnobAssignment
import com.valeton.editor.core.model.SwitchParam

/**
 * Checks a preset from a shared .prst before it can be written to the pedal. A broken chain is refused.
 * Values the catalog defines are made valid: knobs are held to their range (a knob a Sync switch has turned
 * into a note division to 0..max), switch and combo box values outside their options go back to the default,
 * patch volume and pan are held to the editor ranges, and quick knobs pointing at a missing block or
 * parameter are turned off. Models the catalog does not know are only reported: they may come from a newer
 * firmware, so they are kept as they are.
 */
object PresetImportCheck {

    data class Result(
        val preset: GP200Preset,
        /** Values moved to a valid value (knob range, switch/combo default, volume/pan, quick knob OFF). */
        val clampedParams: Int,
        /** Block indices (0 = PRE … 10 = VOL) whose model is not in the catalog. */
        val unknownEffectSlots: List<Int>
    )

    private val BLOCKS = (0..10).toList()
    private val PATCH_VOLUME_RANGE = 0..100
    private val PATCH_PAN_RANGE = -50..50
    private const val SYNC_ON = 1f

    /** @throws IllegalArgumentException when the chain does not hold each of the 11 blocks exactly once or a parameter is NaN. */
    fun check(preset: GP200Preset): Result {
        require(preset.effects.map { it.slotIndex }.sorted() == BLOCKS) {
            "Invalid .prst file: the chain must hold each of the 11 blocks once"
        }
        require(preset.routingOrder.sorted() == BLOCKS) {
            "Invalid .prst file: the routing order must list each of the 11 blocks once"
        }
        // PRSTDecoder already turns non-finite floats into 0; this guards presets built any other way.
        require(preset.effects.none { slot -> slot.params.any { it.isNaN() } }) {
            "Invalid .prst file: a parameter value is not a number"
        }

        var adjusted = 0
        val unknown = mutableListOf<Int>()
        val effects = preset.effects.map { slot ->
            if (EffectCatalog.getEffectById(slot.effectId) == null) {
                unknown += slot.slotIndex
                slot
            } else {
                val (checked, count) = checkParams(slot)
                adjusted += count
                checked
            }
        }

        val volume = preset.patchVolume.coerceIn(PATCH_VOLUME_RANGE)
        val pan = preset.patchPan.coerceIn(PATCH_PAN_RANGE)
        if (volume != preset.patchVolume) adjusted++
        if (pan != preset.patchPan) adjusted++

        val mappings = preset.controlMappings?.let { controls ->
            val knobs = controls.knobs.map { if (quickKnobValid(it, effects)) it else QuickKnobAssignment.OFF }
            adjusted += knobs.zip(controls.knobs).count { (new, old) -> new != old }
            ControlMappings(knobs, controls.ctrls)
        }

        return Result(
            preset.copy(effects = effects, patchVolume = volume, patchPan = pan, controlMappings = mappings),
            adjusted,
            unknown.sorted()
        )
    }

    private fun checkParams(slot: EffectSlot): Pair<EffectSlot, Int> {
        val definition = EffectCatalog.getEffectById(slot.effectId)!!
        val params = slot.params.toMutableList()
        var adjusted = 0
        fun set(index: Int, value: Float) {
            if (params[index] != value) {
                params[index] = value
                adjusted++
            }
        }

        // Options first, so a garbage Sync value cannot exempt its knob from the range below.
        definition.params.forEach { param ->
            val value = params.getOrNull(param.index) ?: return@forEach
            val options = when (param) {
                is SwitchParam -> param.options
                is ComboBoxParam -> param.options
                else -> return@forEach
            }
            if (options.none { it.id.toFloat() == value }) set(param.index, param.defaultValue)
        }

        // With Sync on, a bound knob (delay Time, modulation Rate) holds a note division, not its range.
        // The division range is not documented, so it is only held to 0..max.
        val synced = definition.params.filterIsInstance<SwitchParam>()
            .filter { it.boundParamIndex != null && params.getOrNull(it.index) == SYNC_ON }
            .mapNotNull { it.boundParamIndex }
            .toSet()
        definition.params.filterIsInstance<KnobParam>().forEach { knob ->
            val value = params.getOrNull(knob.index) ?: return@forEach
            set(knob.index, if (knob.index in synced) value.coerceIn(0f, knob.max) else value.coerceIn(knob.min, knob.max))
        }
        return slot.copy(params = params) to adjusted
    }

    /** Same rule as the Controls screen; files store OFF with any parameter, so block 255 alone means OFF. */
    private fun quickKnobValid(assignment: QuickKnobAssignment, effects: List<EffectSlot>): Boolean {
        if (assignment.blockIndex in listOf(11, 12, 255)) return true
        if (assignment.blockIndex !in 0..9) return false
        val slot = effects.firstOrNull { it.slotIndex == assignment.blockIndex } ?: return false
        val definition = EffectCatalog.getEffectById(slot.effectId) ?: return true
        return definition.params.any { it.index == assignment.paramIndex }
    }
}
