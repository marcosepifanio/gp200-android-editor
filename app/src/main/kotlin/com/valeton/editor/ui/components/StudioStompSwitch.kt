package com.valeton.editor.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.ui.theme.StudioPalette

@Composable
fun StudioStompSwitch(
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    ledColor: Color = StudioPalette.Accent
) {
    val animatedLedColor by animateColorAsState(
        targetValue = if (enabled) ledColor else StudioPalette.LedOff,
        animationSpec = tween(150),
        label = "ledColor"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (enabled) ledColor.copy(alpha = 0.5f) else StudioPalette.Border,
        animationSpec = tween(150),
        label = "borderColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        StudioPalette.ChromeFace,
                        StudioPalette.ChromeBg,
                        StudioPalette.Case
                    )
                )
            )
            .border(1.dp, animatedBorderColor, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ledColor),
                onClick = onToggle
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Luminous LED Dot with Halo
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(animatedLedColor.copy(alpha = if (enabled) 0.35f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(animatedLedColor)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Stomp Tread Texture Graphic
            Canvas(modifier = Modifier.size(28.dp, 16.dp)) {
                val step = size.width / 5f
                for (i in 0..4) {
                    val x = i * step + step / 2f
                    drawLine(
                        color = StudioPalette.BorderActive,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // State Label
            Text(
                text = if (enabled) "ON" else "BYPASSED",
                color = if (enabled) StudioPalette.TextPrimary else StudioPalette.TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}
