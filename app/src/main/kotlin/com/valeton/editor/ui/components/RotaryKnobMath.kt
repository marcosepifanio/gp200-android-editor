package com.valeton.editor.ui.components

object RotaryKnobMath {
    const val MIN_ANGLE = -135f
    const val MAX_ANGLE = 135f
    const val TOTAL_SWEEP = 270f

    fun valueToAngle(value: Float, min: Float, max: Float): Float {
        if (max <= min) return MIN_ANGLE
        val fraction = ((value - min) / (max - min)).coerceIn(0f, 1f)
        return MIN_ANGLE + fraction * TOTAL_SWEEP
    }

    fun dragDeltaToNewValue(
        currentValue: Float,
        dragAmountY: Float,
        min: Float,
        max: Float,
        step: Float,
        sensitivityPx: Float = 300f
    ): Float {
        val deltaFraction = -dragAmountY / sensitivityPx
        val deltaValue = deltaFraction * (max - min)
        val rawNewValue = (currentValue + deltaValue).coerceIn(min, max)
        return if (step > 0f) {
            val steps = Math.round((rawNewValue - min) / step)
            (min + steps * step).coerceIn(min, max)
        } else {
            rawNewValue
        }
    }
}
