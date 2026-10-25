package com.valeton.editor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.ui.theme.StudioPalette
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StudioKnob(
    name: String,
    value: Float,
    min: Float = 0f,
    max: Float = 100f,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    displayValue: String? = null,
    accentColor: Color = StudioPalette.Accent
) {
    val fraction = remember(value, min, max) {
        if (max > min) ((value - min) / (max - min)).coerceIn(0f, 1f) else 0f
    }

    Column(
        modifier = modifier
            .width(72.dp)
            .pointerInput(min, max, value) {
                var accumulatedDelta = 0f
                detectVerticalDragGestures(
                    onDragStart = { accumulatedDelta = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedDelta -= dragAmount // Dragging UP increases value
                        val range = max - min
                        val deltaValue = (accumulatedDelta / 150f) * range
                        if (kotlin.math.abs(deltaValue) >= 0.5f) {
                            val newValue = (value + deltaValue).coerceIn(min, max)
                            onValueChange(newValue)
                            accumulatedDelta = 0f
                        }
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Rotary Dial Canvas
        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val diameter = size.minDimension
                val strokeWidth = 3.5.dp.toPx()
                val radius = (diameter - strokeWidth) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                val startAngle = 135f
                val sweepTotal = 270f
                val activeSweep = sweepTotal * fraction

                // 1. Inactive background track
                drawArc(
                    color = StudioPalette.KnobTrack,
                    startAngle = startAngle,
                    sweepAngle = sweepTotal,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // 2. Active illuminated sweep arc
                if (activeSweep > 0f) {
                    drawArc(
                        color = accentColor,
                        startAngle = startAngle,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // 3. Inner Knob Cap Face
                val innerRadius = radius - strokeWidth * 1.2f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(StudioPalette.ChromeFace, StudioPalette.ChromeBg, Color(0xFF16171A)),
                        center = center,
                        radius = innerRadius
                    ),
                    radius = innerRadius,
                    center = center
                )

                // 4. Subtle Bezel Outline
                drawCircle(
                    color = StudioPalette.BorderActive,
                    radius = innerRadius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // 5. Directional Indicator Pointer Notch
                val currentAngleRad = Math.toRadians((startAngle + activeSweep).toDouble())
                val pointerLength = innerRadius * 0.75f
                val pointerEnd = Offset(
                    (center.x + pointerLength * cos(currentAngleRad)).toFloat(),
                    (center.y + pointerLength * sin(currentAngleRad)).toFloat()
                )
                val pointerStart = Offset(
                    (center.x + innerRadius * 0.25f * cos(currentAngleRad)).toFloat(),
                    (center.y + innerRadius * 0.25f * sin(currentAngleRad)).toFloat()
                )

                drawLine(
                    color = Color.White,
                    start = pointerStart,
                    end = pointerEnd,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Formatted Value Display
        Text(
            text = displayValue ?: if (max <= 10f) String.format("%.1f", value) else value.toInt().toString(),
            color = StudioPalette.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )

        // Parameter Name Label
        Text(
            text = name.uppercase(),
            color = StudioPalette.TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
