package com.valeton.editor.ui.chain

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.BuildConfig
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.ui.theme.StudioPalette

@Composable
fun StudioChainScreen(
    preset: GP200Preset,
    onToggleSlot: (Int) -> Unit,
    onOpenSlot: (Int) -> Unit,
    onMoveSlot: (fromIndex: Int, toIndex: Int) -> Unit,
    onVolumeChange: (Int) -> Unit = {},
    onPanChange: (Int) -> Unit = {},
    onTempoChange: (Int) -> Unit = {},
    onTapTempo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val chain = preset.effects

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioPalette.Case)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            // 1. Header Information
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CADEIA DE SINAL",
                        color = StudioPalette.TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Toque para editar",
                        color = StudioPalette.TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // 2. IN Terminal Marker
            item {
                TerminalMarker(label = "IN")
                CableLine()
            }

        // 3. Pedal Cards with Interconnecting Cables
        itemsIndexed(chain, key = { _, slot -> slot.slotIndex }) { index, slot ->
            // FX Loop SEND Marker if positioned before this slot
            if (preset.fxLoopSend == index) {
                FxLoopMarker(label = "↗ FX SEND")
                CableLine()
            }

            StudioPedalCard(
                slot = slot,
                position = index,
                chainLength = chain.size,
                onToggle = { onToggleSlot(slot.slotIndex) },
                onClick = { onOpenSlot(slot.slotIndex) },
                onMoveUp = if (index > 0) { { onMoveSlot(index, index - 1) } } else null,
                onMoveDown = if (index < chain.size - 1) { { onMoveSlot(index, index + 1) } } else null
            )

            // FX Loop RETURN Marker if positioned after this slot
            if (preset.fxLoopReturn == index + 1) {
                CableLine()
                FxLoopMarker(label = "↙ FX RETURN")
            }

            // Patch Cable between pedal cards
            if (index < chain.size - 1) {
                CableLine()
            }
        }

        // 3. OUT Terminal Marker & Master Output (Volume & Pan)
        item {
            CableLine()
            TerminalMarker(label = "OUT")
            Spacer(modifier = Modifier.height(16.dp))

            StudioMasterBar(
                volume = preset.patchVolume,
                pan = preset.patchPan,
                tempo = preset.patchTempo,
                onVolumeChange = onVolumeChange,
                onPanChange = onPanChange,
                onTempoChange = onTempoChange,
                onTapTempo = onTapTempo
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Valeton GP-200 Editor v${BuildConfig.VERSION_NAME}",
                color = StudioPalette.TextMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun TerminalMarker(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(StudioPalette.SurfaceRaised)
            .border(1.dp, StudioPalette.Border, RoundedCornerShape(4.dp))
            .padding(horizontal = 14.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = StudioPalette.TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun CableLine() {
    Box(
        modifier = Modifier
            .width(2.dp)
            .height(12.dp)
            .background(StudioPalette.BorderActive)
    )
}

@Composable
private fun FxLoopMarker(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(StudioPalette.SurfaceElevated)
            .border(1.dp, StudioPalette.AccentDim.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = StudioPalette.Accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
