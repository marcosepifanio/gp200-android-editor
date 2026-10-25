package com.valeton.editor.ui.chain

import kotlin.math.abs
import kotlin.math.roundToInt

object MasterControlsFormatter {

    fun formatPanValue(pan: Int): String {
        return when {
            pan == 0 -> "CENTER"
            pan < 0 -> "L${abs(pan)}"
            else -> "R$pan"
        }
    }

    fun formatVolumeValue(volume: Int): String {
        return volume.coerceIn(0, 100).toString()
    }

    fun panToFraction(pan: Int): Float {
        val clamped = pan.coerceIn(-50, 50)
        return (clamped + 50) / 100f
    }

    fun fractionToPan(fraction: Float): Int {
        val clampedFraction = fraction.coerceIn(0f, 1f)
        return (clampedFraction * 100f - 50f).roundToInt().coerceIn(-50, 50)
    }

    fun volumeToFraction(volume: Int): Float {
        val clamped = volume.coerceIn(0, 100)
        return clamped / 100f
    }

    fun fractionToVolume(fraction: Float): Int {
        val clampedFraction = fraction.coerceIn(0f, 1f)
        return (clampedFraction * 100f).roundToInt().coerceIn(0, 100)
    }
}
