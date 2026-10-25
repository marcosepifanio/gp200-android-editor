package com.valeton.editor.ui.chain

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.valeton.editor.ui.components.StudioTempoControl
import com.valeton.editor.ui.theme.StudioPalette
import kotlin.math.abs

@Composable
fun StudioMasterBar(
    volume: Int,
    pan: Int,
    onVolumeChange: (Int) -> Unit,
    onPanChange: (Int) -> Unit,
    tempo: Int = 120,
    onTempoChange: (Int) -> Unit = {},
    onTapTempo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StudioPalette.Surface)
            .border(1.dp, StudioPalette.Border, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAÍDA PRINCIPAL",
                    color = StudioPalette.TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "NÍVEL DO PATCH",
                    color = StudioPalette.TextMuted.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Volume Row & Bar
            MasterVolumeControl(
                volume = volume,
                onVolumeChange = onVolumeChange
            )

            // Pan Row & Bar
            MasterPanControl(
                pan = pan,
                onPanChange = onPanChange
            )

            // Tempo Row & Bar
            StudioTempoControl(
                tempo = tempo,
                onTempoChange = onTempoChange,
                onTapTempo = onTapTempo,
                compact = false
            )
        }
    }
}

@Composable
private fun MasterVolumeControl(
    volume: Int,
    onVolumeChange: (Int) -> Unit
) {
    val fraction = MasterControlsFormatter.volumeToFraction(volume)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VOL",
                color = StudioPalette.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = MasterControlsFormatter.formatVolumeValue(volume),
                color = StudioPalette.Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val f = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onVolumeChange(MasterControlsFormatter.fractionToVolume(f))
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        val f = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onVolumeChange(MasterControlsFormatter.fractionToVolume(f))
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
    }
}

@Composable
private fun MasterPanControl(
    pan: Int,
    onPanChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PAN",
                color = StudioPalette.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = MasterControlsFormatter.formatPanValue(pan),
                color = if (pan == 0) StudioPalette.TextPrimary else StudioPalette.Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { onPanChange(0) },
                        onTap = { offset ->
                            val f = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            // If tapped within 5% of center, snap to 0
                            if (abs(f - 0.5f) < 0.05f) {
                                onPanChange(0)
                            } else {
                                onPanChange(MasterControlsFormatter.fractionToPan(f))
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        val f = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        if (abs(f - 0.5f) < 0.03f) {
                            onPanChange(0)
                        } else {
                            onPanChange(MasterControlsFormatter.fractionToPan(f))
                        }
                    }
                }
        ) {
            val centerWidth = maxWidth / 2f
            val fraction = MasterControlsFormatter.panToFraction(pan)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StudioPalette.SurfaceRaised)
                    .border(1.dp, StudioPalette.Border, RoundedCornerShape(4.dp))
            ) {

            // Fill from center to left or center to right
            if (pan < 0) {
                val leftWidth = centerWidth * (abs(pan) / 50f)
                Box(
                    modifier = Modifier
                        .offset(x = centerWidth - leftWidth)
                        .width(leftWidth)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(StudioPalette.Accent, StudioPalette.AccentDim)
                            )
                        )
                )
            } else if (pan > 0) {
                val rightWidth = centerWidth * (pan / 50f)
                Box(
                    modifier = Modifier
                        .offset(x = centerWidth)
                        .width(rightWidth)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(StudioPalette.AccentDim, StudioPalette.Accent)
                            )
                        )
                )
            }

            // Center Notch Line
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.5f))
            )
            }
        }
    }
}
