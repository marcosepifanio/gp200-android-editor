package com.valeton.editor.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.ui.theme.StudioPalette
import kotlin.math.roundToInt

@Composable
fun StudioTempoControl(
    tempo: Int,
    onTempoChange: (Int) -> Unit,
    onTapTempo: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    showTap: Boolean = true
) {
    val clampedTempo = tempo.coerceIn(40, 250)
    val beatPeriodMs = (60_000L / clampedTempo).toInt()

    // Pulse animation mimicking a visual metronome synchronized to the BPM
    val infiniteTransition = rememberInfiniteTransition(label = "tempoPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (beatPeriodMs / 2).coerceAtLeast(100), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    if (compact) {
        // Compact single-row layout for Pedal Editor (MOD / DLY)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StudioPalette.Surface)
                .border(1.dp, StudioPalette.Border, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // BPM Label & Steppers
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "TEMPO",
                        color = StudioPalette.TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )

                    // Step Down
                    Box(
                        modifier = Modifier
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(StudioPalette.SurfaceRaised)
                            .border(1.dp, StudioPalette.Border, RoundedCornerShape(4.dp))
                            .clickable { onTempoChange((clampedTempo - 1).coerceAtLeast(40)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("–", color = StudioPalette.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    // BPM Display
                    Text(
                        text = "$clampedTempo BPM",
                        color = StudioPalette.Accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    // Step Up
                    Box(
                        modifier = Modifier
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(StudioPalette.SurfaceRaised)
                            .border(1.dp, StudioPalette.Border, RoundedCornerShape(4.dp))
                            .clickable { onTempoChange((clampedTempo + 1).coerceAtMost(250)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = StudioPalette.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Tap Button
                if (showTap && onTapTempo != null) {
                    TapTempoButton(
                        pulseAlpha = pulseAlpha,
                        onClick = onTapTempo
                    )
                }
            }
        }
    } else {
        // Full layout for Master Output card
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "TEMPO",
                        color = StudioPalette.TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "$clampedTempo BPM",
                        color = StudioPalette.Accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (showTap && onTapTempo != null) {
                    TapTempoButton(
                        pulseAlpha = pulseAlpha,
                        onClick = onTapTempo
                    )
                }
            }

            // Slider & Stepper Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Step Down
                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(StudioPalette.SurfaceRaised)
                        .border(1.dp, StudioPalette.Border, RoundedCornerShape(6.dp))
                        .clickable { onTempoChange((clampedTempo - 1).coerceAtLeast(40)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("–", color = StudioPalette.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                // Slider Track (40..250 BPM)
                val fraction = (clampedTempo - 40f) / (250f - 40f)
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val f = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                val newBpm = (40 + f * (250 - 40)).roundToInt().coerceIn(40, 250)
                                onTempoChange(newBpm)
                            }
                        }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, _ ->
                                val f = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                val newBpm = (40 + f * (250 - 40)).roundToInt().coerceIn(40, 250)
                                onTempoChange(newBpm)
                            }
                        }
                ) {
                    val fillWidth = maxWidth * fraction
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .align(Alignment.CenterStart)
                            .clip(RoundedCornerShape(4.dp))
                            .background(StudioPalette.SurfaceRaised)
                            .border(1.dp, StudioPalette.Border, RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .width(fillWidth)
                                .fillMaxHeight()
                                .background(Brush.horizontalGradient(listOf(StudioPalette.AccentDim, StudioPalette.Accent)))
                        )
                    }
                }

                // Step Up
                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(StudioPalette.SurfaceRaised)
                        .border(1.dp, StudioPalette.Border, RoundedCornerShape(6.dp))
                        .clickable { onTempoChange((clampedTempo + 1).coerceAtMost(250)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = StudioPalette.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TapTempoButton(
    pulseAlpha: Float,
    onClick: () -> Unit
) {
    var isTapped by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isTapped) 0.94f else 1f,
        animationSpec = tween(60),
        label = "tapScale"
    )

    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(StudioPalette.SurfaceRaised)
            .border(1.dp, StudioPalette.Accent.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = StudioPalette.Accent),
                onClick = {
                    isTapped = true
                    onClick()
                }
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        LaunchedEffect(isTapped) {
            if (isTapped) {
                kotlinx.coroutines.delay(80)
                isTapped = false
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Pulsing LED dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(StudioPalette.Accent.copy(alpha = pulseAlpha))
            )
            Text(
                text = "TOQUE",
                color = StudioPalette.Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
    }
}
