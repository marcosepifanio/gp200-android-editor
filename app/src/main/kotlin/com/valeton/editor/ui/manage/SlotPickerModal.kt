package com.valeton.editor.ui.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.valeton.editor.core.codec.SysExCodec
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.ui.chain.MasterControlsFormatter
import com.valeton.editor.ui.theme.StudioPalette

/** Scrollable list of the pedal's 256 slots with their cached names. */
@Composable
internal fun SlotPickerModal(
    currentSlot: Int,
    connected: Boolean = true,
    presetNames: List<String>,
    onSelectSlot: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (currentSlot - 3).coerceAtLeast(0)
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(12.dp))
                .background(StudioPalette.SurfaceRaised)
                .border(1.dp, StudioPalette.Border, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "SELECIONAR SLOT DE DESTINO",
                    color = StudioPalette.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(256) { slotIdx ->
                        val isSelected = slotIdx == currentSlot
                        val slotLabel = SysExCodec.slotToLabel(slotIdx)
                        val name = presetNames.getOrNull(slotIdx)?.ifBlank { "Vazio" } ?: "Slot $slotIdx"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) StudioPalette.SurfaceElevated else StudioPalette.Surface
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) StudioPalette.Accent else StudioPalette.Border,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { onSelectSlot(slotIdx) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = slotLabel,
                                color = if (isSelected) StudioPalette.Accent else StudioPalette.TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = name,
                                color = if (isSelected) StudioPalette.TextPrimary else StudioPalette.TextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Fechar", color = StudioPalette.TextMuted)
                }
            }
        }
    }
}
