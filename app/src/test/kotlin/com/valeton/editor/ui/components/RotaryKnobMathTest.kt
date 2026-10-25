package com.valeton.editor.ui.components

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RotaryKnobMathTest {

    @Test
    fun `valueToAngle maps min, mid, and max accurately`() {
        // min = 0, max = 100
        assertEquals(-135f, RotaryKnobMath.valueToAngle(0f, 0f, 100f))
        assertEquals(0f, RotaryKnobMath.valueToAngle(50f, 0f, 100f))
        assertEquals(135f, RotaryKnobMath.valueToAngle(100f, 0f, 100f))
    }

    @Test
    fun `dragDeltaToNewValue increases on upward drag and respects bounds`() {
        // Drag up 150px (half sensitivity 300px) from 50 should reach 100
        val newValue = RotaryKnobMath.dragDeltaToNewValue(
            currentValue = 50f,
            dragAmountY = -150f,
            min = 0f,
            max = 100f,
            step = 1f,
            sensitivityPx = 300f
        )
        assertEquals(100f, newValue)

        // Drag beyond max clamps at 100
        val clamped = RotaryKnobMath.dragDeltaToNewValue(
            currentValue = 90f,
            dragAmountY = -100f,
            min = 0f,
            max = 100f,
            step = 1f,
            sensitivityPx = 300f
        )
        assertEquals(100f, clamped)
    }
}
