package com.valeton.editor.core.codec

import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.SwitchParam

/** Replay pending state against a complete device read, rather than assuming live writes arrived. */
object PresetWritePlan {
    fun commands(expected: GP200Preset, confirmed: GP200Preset?, forceSettings: Boolean = false): List<ByteArray> = buildList {
        val tempoChanged = forceSettings || expected.patchTempo != confirmed?.patchTempo
        if (tempoChanged) {
            addAll(SysExCodec.buildTempoControlChange(expected.patchTempo))
            add(SysExCodec.buildPatchSetting(1, expected.patchTempo.coerceIn(40, 250)))
        }
        if (forceSettings || expected.drumSync != confirmed?.drumSync) {
            val enabled = if (expected.drumSync) 1 else 0
            add(SysExCodec.buildPatchSettingParam(0x19, enabled))
            add(SysExCodec.buildDrumSync(expected.drumSync))
            add(SysExCodec.buildDrumParam(3, enabled))
        }
        for (effect in expected.effects.sortedBy { it.slotIndex }) {
            val previous = confirmed?.effects?.find { it.slotIndex == effect.slotIndex }
            val modelChanged = previous?.effectId != effect.effectId
            if (modelChanged) add(SysExCodec.buildEffectChange(effect.slotIndex, effect.effectId))
            val indices = EffectCatalog.liveParameterIndices(effect.effectId, effect.params.size)
            val changed = indices.filter { index ->
                modelChanged || previous == null || kotlin.math.abs(
                    effect.params[index] - previous.params.getOrElse(index) { 0f }
                ) >= 0.001f
            }.toSet()
            // A Sync switch can reset its Time/Rate; restore the intended bound value afterward.
            val bound = EffectCatalog.getEffectById(effect.effectId)?.params
                ?.filterIsInstance<SwitchParam>()
                ?.filter { it.index in changed || tempoChanged }
                ?.mapNotNull { it.boundParamIndex }?.toSet().orEmpty()
            for (index in indices) {
                if (index in changed || index in bound) {
                    add(SysExCodec.buildParamChange(effect.slotIndex, index, effect.effectId, effect.params[index]))
                }
            }
            if (modelChanged || previous?.enabled != effect.enabled) add(SysExCodec.buildToggle(effect.slotIndex, effect.enabled))
        }
        if (forceSettings || confirmed == null || expected.routingOrder != confirmed.routingOrder ||
            expected.fxLoopSend != confirmed.fxLoopSend || expected.fxLoopReturn != confirmed.fxLoopReturn) {
            add(SysExCodec.buildReorder(expected.routingOrder, expected.fxLoopSend, expected.fxLoopReturn))
        }
        if (forceSettings || expected.patchName != confirmed?.patchName) add(SysExCodec.buildPatchName(expected.patchName))
        if (forceSettings || expected.author != confirmed?.author) add(SysExCodec.buildAuthorName(expected.author.take(16)))
        if (forceSettings || expected.patchVolume != confirmed?.patchVolume) {
            add(SysExCodec.buildControlChange(7, expected.patchVolume.coerceIn(0, 100)))
            add(SysExCodec.buildPatchSetting(0, expected.patchVolume.coerceIn(0, 100)))
        }
        if (forceSettings || expected.patchPan != confirmed?.patchPan) {
            val pan = expected.patchPan.coerceIn(-50, 50)
            add(SysExCodec.buildPatchSetting(6, if (pan < 0) 256 + pan else pan))
        }
        expected.controlMappings?.let { mappings ->
            // Preserve every record and opaque field. Reserved CTRL padding is not a pending edit.
            mappings.knobs.forEachIndexed { index, assignment ->
                val previous = confirmed?.controlMappings?.knobs?.get(index)
                if (forceSettings || previous == null || previous.blockIndex != assignment.blockIndex ||
                    (assignment.blockIndex !in listOf(11, 12, 255) && previous.paramIndex != assignment.paramIndex)) {
                    add(SysExCodec.buildQuickKnobAssignment(index, assignment))
                }
            }
            mappings.ctrls.forEachIndexed { index, assignment ->
                if (forceSettings || confirmed?.controlMappings?.ctrls?.get(index)?.matchesDevice(assignment) != true) {
                    add(SysExCodec.buildCtrlAssignment(index, assignment))
                }
            }
        }
    }
}
