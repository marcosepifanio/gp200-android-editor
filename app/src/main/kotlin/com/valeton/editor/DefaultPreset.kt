package com.valeton.editor

import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.ComboBoxParam
import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.core.model.KnobParam
import com.valeton.editor.core.model.ModuleType
import com.valeton.editor.core.model.SwitchParam
import com.valeton.editor.ui.editor.EffectPickerLogic
import kotlin.math.roundToInt

/** Initial local patch, used only when no cached preset was restored. */
internal fun createDefaultPreset(): GP200Preset {
    val effects = (0..10).map { slotIndex ->
        val module = ModuleType.fromSlotIndex(slotIndex)
        val effect = EffectPickerLogic.effectsInDisplayOrder(
            EffectCatalog.getEffectsByModule(module),
            EffectCatalog.getCategoriesForModule(module)
        ).first()
        val params = MutableList(15) { 0f }
        effect.params.forEach { param ->
            params[param.index] = when (param) {
                is KnobParam -> {
                    val midpoint = param.min + (param.max - param.min) / 2f
                    if (param.step > 0f) {
                        (param.min + ((midpoint - param.min) / param.step).roundToInt() * param.step)
                            .coerceIn(param.min, param.max)
                    } else midpoint
                }
                // For an even option count, choose the lower central entry.
                is SwitchParam -> param.options.getOrNull((param.options.size - 1) / 2)
                    ?.id?.toFloat() ?: param.defaultValue
                is ComboBoxParam -> param.options.getOrNull((param.options.size - 1) / 2)
                    ?.id?.toFloat() ?: param.defaultValue
            }
        }
        EffectSlot(slotIndex, effect.effectId, enabled = false, params = params)
    }

    return GP200Preset(
        patchName = "Preset Inicial",
        effects = effects,
        routingOrder = listOf(4, 0, 1, 2, 3, 5, 6, 7, 8, 9, 10)
    )
}
