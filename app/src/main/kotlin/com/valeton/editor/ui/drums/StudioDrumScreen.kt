package com.valeton.editor.ui.drums

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.core.catalog.DrumCatalog
import com.valeton.editor.core.model.DrumGenre
import com.valeton.editor.core.model.DrumPattern
import com.valeton.editor.ui.components.StudioTempoControl
import com.valeton.editor.ui.theme.StudioPalette
import com.valeton.editor.viewmodel.DrumUiState
import kotlin.math.roundToInt

/**
 * Pure filtering logic for drum patterns given a selected [genre] and search [query].
 */
fun filterPatterns(
    patterns: List<DrumPattern>,
    genre: DrumGenre,
    query: String
): List<DrumPattern> {
    val trimmed = query.trim()
    return patterns.filter { pattern ->
        val matchesGenre = genre == DrumGenre.ALL || pattern.genre == genre
        val matchesQuery = trimmed.isEmpty() ||
            pattern.name.contains(trimmed, ignoreCase = true) ||
            pattern.timeSignature.contains(trimmed, ignoreCase = true)
        matchesGenre && matchesQuery
    }
}

/**
 * Calculates item counts for [DrumGenre.ALL] and each individual [DrumGenre] based on matches with [query].
 */
fun calculateGenreCounts(
    patterns: List<DrumPattern>,
    query: String
): Map<DrumGenre, Int> {
    val trimmed = query.trim()
    val filteredByQuery = if (trimmed.isEmpty()) {
        patterns
    } else {
        patterns.filter {
            it.name.contains(trimmed, ignoreCase = true) ||
                it.timeSignature.contains(trimmed, ignoreCase = true)
        }
    }
    val countsByGenre = filteredByQuery.groupingBy { it.genre }.eachCount()
    return DrumGenre.entries.associateWith { genre ->
        if (genre == DrumGenre.ALL) filteredByQuery.size else (countsByGenre[genre] ?: 0)
    }
}

@Composable
fun StudioDrumScreen(
    drumState: DrumUiState,
    tempo: Int,
    onTogglePlay: () -> Unit,
    onSelectPattern: (Int) -> Unit,
    onVolumeChange: (Int) -> Unit,
    onTempoChange: (Int) -> Unit,
    onGenreChange: (DrumGenre) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allPatterns = remember { DrumCatalog.getAllPatterns() }
    val activePattern = remember(drumState.selectedPatternId) {
        DrumCatalog.getPatternById(drumState.selectedPatternId) ?: allPatterns.first()
    }

    val filteredPatterns = remember(allPatterns, drumState.selectedGenre, drumState.searchQuery) {
        filterPatterns(allPatterns, drumState.selectedGenre, drumState.searchQuery)
    }

    val genreCounts = remember(allPatterns, drumState.searchQuery) {
        calculateGenreCounts(allPatterns, drumState.searchQuery)
    }

    val listState = rememberLazyListState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioPalette.Case)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Hero Player Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioPalette.Surface),
            border = BorderStroke(1.dp, StudioPalette.BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pattern Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = "#${activePattern.id.toString().padStart(2, '0')}",
                            color = StudioPalette.Accent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = activePattern.name.uppercase(),
                            color = StudioPalette.TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Genre & Time Signature Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PatternBadge(text = activePattern.genre.displayName)
                        PatternBadge(text = activePattern.timeSignature)
                    }
                }

                // Controls Row: Circular Play/Stop (64.dp) + Volume Touch Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PlayStopButton(
                        isPlaying = drumState.isPlaying,
                        onClick = onTogglePlay
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "VOLUME ${drumState.volume}%",
                            color = StudioPalette.TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        VolumeBar(
                            volume = drumState.volume,
                            onVolumeChange = onVolumeChange
                        )
                    }
                }

                // Tempo Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TEMPO · BPM DO RITMO",
                        color = StudioPalette.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                // Integrated Tempo Control
                StudioTempoControl(
                    tempo = tempo,
                    onTempoChange = onTempoChange,
                    compact = false,
                    showTap = false
                )
            }
        }

        // 2. Search Input
        OutlinedTextField(
            value = drumState.searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "Buscar ritmo ou compasso...",
                    color = StudioPalette.TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar ritmos",
                    tint = StudioPalette.TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (drumState.searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpar busca",
                            tint = StudioPalette.TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = StudioPalette.TextPrimary,
                unfocusedTextColor = StudioPalette.TextPrimary,
                focusedBorderColor = StudioPalette.Accent,
                unfocusedBorderColor = StudioPalette.BorderSubtle,
                focusedContainerColor = StudioPalette.Surface,
                unfocusedContainerColor = StudioPalette.Surface,
                cursorColor = StudioPalette.Accent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )

        // 3. Genre Filter Rail
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items = DrumGenre.entries, key = { it.name }) { genre ->
                val isSelected = genre == drumState.selectedGenre
                val count = genreCounts[genre] ?: 0

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) StudioPalette.Accent else StudioPalette.SurfaceRaised)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) StudioPalette.Accent else StudioPalette.BorderSubtle,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onGenreChange(genre) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = genre.displayName,
                            color = if (isSelected) StudioPalette.Case else StudioPalette.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "($count)",
                            color = if (isSelected) StudioPalette.Case.copy(alpha = 0.8f) else StudioPalette.TextMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 4. Pattern List (or Empty State)
        if (filteredPatterns.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Nenhum ritmo encontrado",
                        color = StudioPalette.TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Tente outro nome ou fórmula de compasso",
                        color = StudioPalette.TextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredPatterns, key = { it.id }) { pattern ->
                    val isSelected = pattern.id == drumState.selectedPatternId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) StudioPalette.SurfaceRaised else StudioPalette.Surface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) StudioPalette.Accent else StudioPalette.BorderSubtle,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectPattern(pattern.id) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = "#${pattern.id.toString().padStart(2, '0')}",
                                color = if (isSelected) StudioPalette.Accent else StudioPalette.TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = pattern.name,
                                color = if (isSelected) StudioPalette.TextPrimary else StudioPalette.TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PatternBadge(text = pattern.genre.displayName)
                            PatternBadge(text = pattern.timeSignature)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayStopButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderStroke: BorderStroke
    val buttonBg: androidx.compose.ui.graphics.Color

    if (isPlaying) {
        val infiniteTransition = rememberInfiniteTransition(label = "hero_play_pulse")
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "hero_pulse_alpha"
        )
        borderStroke = BorderStroke(2.dp, StudioPalette.Accent.copy(alpha = pulseAlpha))
        buttonBg = StudioPalette.Accent.copy(alpha = 0.15f * pulseAlpha)
    } else {
        borderStroke = BorderStroke(1.dp, StudioPalette.Border)
        buttonBg = StudioPalette.SurfaceRaised
    }

    Box(
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(buttonBg)
            .border(borderStroke, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = StudioPalette.Accent, bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Parar ritmo" else "Reproduzir ritmo",
            tint = if (isPlaying) StudioPalette.Accent else StudioPalette.TextPrimary,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun VolumeBar(
    volume: Int,
    onVolumeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val clampedVolume = volume.coerceIn(0, 100)
    val fraction = clampedVolume / 100f
    val currentOnVolumeChange by rememberUpdatedState(onVolumeChange)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Step Down (48dp touch target)
        Box(
            modifier = Modifier
                .size(48.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Diminuir volume"
                }
                .clickable(
                    role = Role.Button,
                    onClick = { currentOnVolumeChange((clampedVolume - 5).coerceAtLeast(0)) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StudioPalette.SurfaceRaised)
                    .border(1.dp, StudioPalette.Border, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("–", color = StudioPalette.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Slider Track (0..100)
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val f = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        currentOnVolumeChange((f * 100).roundToInt().coerceIn(0, 100))
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            val f = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            currentOnVolumeChange((f * 100).roundToInt().coerceIn(0, 100))
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            val f = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                            currentOnVolumeChange((f * 100).roundToInt().coerceIn(0, 100))
                        }
                    )
                }
        ) {
            if (fraction > 0f) {
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
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(StudioPalette.AccentDim, StudioPalette.Accent)))
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioPalette.SurfaceRaised)
                        .border(1.dp, StudioPalette.Border, RoundedCornerShape(4.dp))
                )
            }
        }

        // Step Up (48dp touch target)
        Box(
            modifier = Modifier
                .size(48.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Aumentar volume"
                }
                .clickable(
                    role = Role.Button,
                    onClick = { currentOnVolumeChange((clampedVolume + 5).coerceAtMost(100)) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StudioPalette.SurfaceRaised)
                    .border(1.dp, StudioPalette.Border, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = StudioPalette.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PatternBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(StudioPalette.SurfaceRaised)
            .border(1.dp, StudioPalette.BorderSubtle, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = StudioPalette.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Preview(name = "StudioDrumScreen Preview", showBackground = true, widthDp = 380, heightDp = 720)
@Composable
private fun StudioDrumScreenPreview() {
    StudioDrumScreen(
        drumState = DrumUiState(
            isPlaying = true,
            selectedPatternId = 0,
            volume = 65,
            selectedGenre = DrumGenre.ALL,
            searchQuery = ""
        ),
        tempo = 120,
        onTogglePlay = {},
        onSelectPattern = {},
        onVolumeChange = {},
        onTempoChange = {},
        onGenreChange = {},
        onSearchQueryChange = {}
    )
}
