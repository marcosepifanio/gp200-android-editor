package com.valeton.editor.ui.controls

import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.CtrlAssignment
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.core.model.ModuleType

/** IDs are stable hardware block/parameter IDs, independent of the visual chain order. */
data class MappingOption(val id: Int, val label: String)

object ControlMappingOptions {
    fun effectLabel(preset: GP200Preset, block: Int): String {
        val slot = preset.effects.find { it.slotIndex == block }
        val name = slot?.let { EffectCatalog.getEffectById(it.effectId)?.name } ?: "Efeito não identificado"
        return "${ModuleType.fromSlotIndex(block).code} · $name"
    }

    fun knobTargetLabel(preset: GP200Preset, block: Int): String = when (block) {
        255 -> "Desligado"
        11 -> "Volume do patch"
        12 -> "Tempo do patch"
        in 0..9 -> effectLabel(preset, block)
        else -> "Destino $block · preservado"
    }

    fun knobTargets(preset: GP200Preset): List<MappingOption> =
        listOf(MappingOption(255, "Desligado")) + preset.effects.sortedBy { it.slotIndex }
            .filter { it.slotIndex in 0..9 && EffectCatalog.getEffectById(it.effectId)?.params?.isNotEmpty() == true }
            .map { MappingOption(it.slotIndex, effectLabel(preset, it.slotIndex)) } +
            listOf(MappingOption(11, "Volume do patch"), MappingOption(12, "Tempo do patch"))

    fun parameters(preset: GP200Preset, block: Int): List<MappingOption> =
        preset.effects.find { it.slotIndex == block }?.let { EffectCatalog.getEffectById(it.effectId) }
            ?.params?.map { MappingOption(it.index, it.name) }.orEmpty()

    fun ctrlLabel(preset: GP200Preset, assignment: CtrlAssignment): String = when (assignment.effectBlocks.size) {
        0 -> "Nenhum efeito"
        1 -> effectLabel(preset, assignment.effectBlocks.single())
        else -> "Vários efeitos · " + assignment.effectBlocks.joinToString(" / ") { ModuleType.fromSlotIndex(it).code }
    }
}
