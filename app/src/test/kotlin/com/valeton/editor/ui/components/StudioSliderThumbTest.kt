package com.valeton.editor.ui.components

import androidx.compose.ui.unit.dp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StudioSliderThumbTest {
    @Test
    fun `thumb centre matches the fill end and the tap position`() {
        listOf(0f, 0.25f, 0.5f, 1f).forEach { f ->
            val centre = sliderThumbOffset(200.dp, f) + SliderThumbSize / 2
            assertEquals((200 * f), centre.value, 0.001f)
        }
    }

    @Test
    fun `fraction outside the track is clamped`() {
        assertEquals(sliderThumbOffset(200.dp, 1f), sliderThumbOffset(200.dp, 1.4f))
        assertEquals(sliderThumbOffset(200.dp, 0f), sliderThumbOffset(200.dp, -0.2f))
    }
}
