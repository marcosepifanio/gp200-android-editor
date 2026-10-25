package com.valeton.editor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.ui.theme.AmberAccent
import com.valeton.editor.ui.theme.SurfaceVariantDark
import com.valeton.editor.ui.theme.TextPrimary
import com.valeton.editor.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RotaryKnob(
    value: Float,
    min: Float = 0f,
    max: Float = 100f,
    step: Float = 1f,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = AmberAccent,
    onValueChange: (Float) -> Unit
) {
    var currentValue by remember(value) { mutableFloatStateOf(value) }

    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .pointerInput(min, max, step) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        val newValue = RotaryKnobMath.dragDeltaToNewValue(
                            currentValue = currentValue,
                            dragAmountY = dragAmount,
                            min = min,
                            max = max,
                            step = step
                        )
                        if (newValue != currentValue) {
                            currentValue = newValue
                            onValueChange(newValue)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f - 6.dp.toPx()
                val center = Offset(size.width / 2f, size.height / 2f)

                // Background track
                drawArc(
                    color = SurfaceVariantDark,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                // Active arc track
                val fraction = ((currentValue - min) / (max - min)).coerceIn(0f, 1f)
                val activeSweep = fraction * 270f
                if (activeSweep > 0f) {
                    drawArc(
                        color = accentColor,
                        startAngle = 135f,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner knob circle
                val innerRadius = radius - 8.dp.toPx()
                drawCircle(
                    color = Color(0xFF222428),
                    radius = innerRadius,
                    center = center
                )

                // Indicator line
                val currentAngleDeg = RotaryKnobMath.valueToAngle(currentValue, min, max)
                val angleRad = Math.toRadians((currentAngleDeg - 90.0)).toFloat()
                val pointerStart = Offset(
                    center.x + (innerRadius * 0.3f) * cos(angleRad),
                    center.y + (innerRadius * 0.3f) * sin(angleRad)
                )
                val pointerEnd = Offset(
                    center.x + (innerRadius * 0.85f) * cos(angleRad),
                    center.y + (innerRadius * 0.85f) * sin(angleRad)
                )

                drawLine(
                    color = Color.White,
                    start = pointerStart,
                    end = pointerEnd,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Value text
        Text(
            text = if (step >= 1f) "${currentValue.toInt()}" else String.format("%.1f", currentValue),
            color = TextPrimary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        // Label text
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}
