package com.valeton.editor.ui.drums

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.core.model.DrumGenre
import com.valeton.editor.core.model.DrumPattern
import com.valeton.editor.ui.theme.StudioPalette

fun calculateBeatDurationMs(bpm: Int): Int = 60000 / bpm.coerceIn(40, 250)

fun formatMiniPlayerPrimaryText(pattern: DrumPattern, bpm: Int): String =
    "${pattern.name.uppercase()} · $bpm BPM"

fun formatMiniPlayerSecondaryText(pattern: DrumPattern): String =
    "${pattern.genre.displayName} · ${pattern.timeSignature}"

@Composable
fun StudioDrumMiniPlayer(
    pattern: DrumPattern,
    bpm: Int,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val beatMs = calculateBeatDurationMs(bpm)
    val infiniteTransition = rememberInfiniteTransition(label = "mini_player_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = beatMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    val dotAlpha = if (isPlaying) pulseAlpha else 0.4f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .drawBehind {
                drawLine(
                    color = StudioPalette.Accent.copy(alpha = 0.6f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .background(StudioPalette.SurfaceRaised)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = StudioPalette.Accent),
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pulsing LED dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = StudioPalette.Accent.copy(alpha = dotAlpha),
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = StudioPalette.Accent.copy(alpha = dotAlpha * 0.8f),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Text content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatMiniPlayerPrimaryText(pattern, bpm),
                color = StudioPalette.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatMiniPlayerSecondaryText(pattern),
                color = StudioPalette.TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Mini Play/Pause button
        IconButton(
            onClick = onTogglePlay,
            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pausar ritmo" else "Reproduzir ritmo",
                tint = StudioPalette.Accent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(name = "Playing Mini Player", showBackground = true)
@Composable
private fun StudioDrumMiniPlayerPlayingPreview() {
    StudioDrumMiniPlayer(
        pattern = DrumPattern(id = 0, name = "Classic Rock 1", genre = DrumGenre.ROCK, timeSignature = "4/4"),
        bpm = 120,
        isPlaying = true,
        onTogglePlay = {},
        onClick = {}
    )
}

@Preview(name = "Paused Mini Player", showBackground = true)
@Composable
private fun StudioDrumMiniPlayerPausedPreview() {
    StudioDrumMiniPlayer(
        pattern = DrumPattern(id = 45, name = "Pop 1", genre = DrumGenre.POP, timeSignature = "4/4"),
        bpm = 95,
        isPlaying = false,
        onTogglePlay = {},
        onClick = {}
    )
}
