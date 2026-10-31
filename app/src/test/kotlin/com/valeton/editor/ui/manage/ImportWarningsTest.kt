package com.valeton.editor.ui.manage

import com.valeton.editor.core.codec.PresetImportCheck
import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.GP200Preset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ImportWarningsTest {
    private val preset = GP200Preset(patchName = "x", effects = (0..10).map { EffectSlot(it, 0, false) })

    @Test
    fun `a clean import has no warning`() {
        assertEquals(emptyList<String>(), ImportWarnings.lines(PresetImportCheck.Result(preset, 0, emptyList())))
    }

    @Test
    fun `unknown models name their blocks and clamped values are counted`() {
        assertEquals(
            listOf(
                "Modelo desconhecido pelo app em DST, CAB. Ele será enviado como está no arquivo.",
                "3 parâmetros estavam fora da faixa e foram ajustados ao limite."
            ),
            ImportWarnings.lines(PresetImportCheck.Result(preset, 3, listOf(2, 5)))
        )
        assertEquals(
            listOf("1 parâmetro estava fora da faixa e foi ajustado ao limite."),
            ImportWarnings.lines(PresetImportCheck.Result(preset, 1, emptyList()))
        )
    }
}
