package com.valeton.editor.core.codec

import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.ModuleType

/** Compare saved settings, excluding only wire/file metadata and proven volatile CTRL padding. */
object PresetSaveVerifier {
    fun differences(expected: GP200Preset, actual: GP200Preset): List<String> = buildList {
        fun deviceText(value: String) = String(value.take(16).toByteArray(Charsets.US_ASCII), Charsets.US_ASCII).trim()
        if (deviceText(expected.patchName) != actual.patchName.trim()) add("nome do patch")
        if (deviceText(expected.author) != actual.author.trim()) add("autor")
        if (expected.patchVolume != actual.patchVolume) add("volume do patch")
        if (expected.patchPan != actual.patchPan) add("pan do patch")
        if (expected.patchTempo != actual.patchTempo) add("tempo do patch")
        if (expected.drumSync != actual.drumSync) add("sincronismo da bateria")
        if (expected.routingOrder != actual.routingOrder) add("ordem dos efeitos")
        if (expected.fxLoopSend != actual.fxLoopSend || expected.fxLoopReturn != actual.fxLoopReturn) add("FX loop")
        if (expected.effects.size != actual.effects.size) add("quantidade de efeitos")
        for (effect in expected.effects) {
            val module = ModuleType.fromSlotIndex(effect.slotIndex).code
            val received = actual.effects.find { it.slotIndex == effect.slotIndex }
            if (received == null) { add("bloco $module"); continue }
            if (effect.effectId != received.effectId) add("modelo de $module")
            if (effect.enabled != received.enabled) add("estado de $module")
            for (index in EffectCatalog.liveParameterIndices(effect.effectId, 15)) {
                val value = effect.params.getOrElse(index) { 0f }
                val read = received.params.getOrElse(index) { 0f }
                if (!value.isFinite() || !read.isFinite() || kotlin.math.abs(value - read) >= 0.001f) add("$module parâmetro ${index + 1}")
            }
        }
        val controls = expected.controlMappings
        if (controls != null && actual.controlMappings?.let { controls.matchesDevice(it) } != true) add("mapeamentos dos controles")
    }
}
