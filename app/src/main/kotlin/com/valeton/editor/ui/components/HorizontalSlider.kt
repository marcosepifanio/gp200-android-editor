package com.valeton.editor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.ui.theme.SurfaceVariantDark
import com.valeton.editor.ui.theme.TextPrimary
import com.valeton.editor.ui.theme.ValetonBlue
import kotlin.math.roundToInt

@Composable
fun HorizontalSlider(
    value: Float,
    min: Float = 0f,
    max: Float = 100f,
    step: Float = 1f,
    label: String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val range = (max - min).coerceAtLeast(0.001f)
    val fraction = ((value - min) / range).coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceVariantDark)
            .pointerInput(min, max, step) {
                detectTapGestures { offset ->
                    val newFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    val rawValue = min + newFraction * range
                    val stepped = if (step > 0f) {
                        ((rawValue - min) / step).roundToInt() * step + min
                    } else rawValue
                    onValueChange(stepped.coerceIn(min, max))
                }
            }
            .pointerInput(min, max, step) {
                detectHorizontalDragGestures { change, _ ->
                    val newFraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    val rawValue = min + newFraction * range
                    val stepped = if (step > 0f) {
                        ((rawValue - min) / step).roundToInt() * step + min
                    } else rawValue
                    onValueChange(stepped.coerceIn(min, max))
                }
            }
    ) {
        val fillWidth = maxWidth * fraction

        // Blue Fill bar
        Box(
            modifier = Modifier
                .width(fillWidth)
                .fillMaxHeight()
                .background(ValetonBlue)
        )

        // Text Content Row (Label on left, Value on right)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            val displayValue = if (step >= 1f) {
                value.toInt().toString()
            } else {
                "%.1f".format(value)
            }

            Text(
                text = displayValue,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
