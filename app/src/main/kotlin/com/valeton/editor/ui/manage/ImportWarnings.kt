package com.valeton.editor.ui.manage

import com.valeton.editor.core.codec.PresetImportCheck
import com.valeton.editor.core.model.ModuleType

/** Lines shown in the import dialog for what [PresetImportCheck] adjusted or could not recognize. */
internal object ImportWarnings {
    fun lines(result: PresetImportCheck.Result): List<String> = buildList {
        if (result.unknownEffectSlots.isNotEmpty()) {
            val modules = result.unknownEffectSlots.joinToString(", ") { slot ->
                ModuleType.entries.firstOrNull { it.slotIndex == slot }?.code ?: "bloco ${slot + 1}"
            }
            add("Modelo desconhecido pelo app em $modules. Ele será enviado como está no arquivo.")
        }
        when (result.clampedParams) {
            0 -> Unit
            1 -> add("1 parâmetro estava fora da faixa e foi ajustado ao limite.")
            else -> add("${result.clampedParams} parâmetros estavam fora da faixa e foram ajustados ao limite.")
        }
    }
}
