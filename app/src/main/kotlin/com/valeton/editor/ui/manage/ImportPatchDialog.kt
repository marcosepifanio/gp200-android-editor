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

@Composable
fun ImportPatchDialog(
    isOpen: Boolean,
    preset: GP200Preset?,
    currentSlot: Int,
    connected: Boolean = true,
    presetNames: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onConfirmImport: (targetSlot: Int, saveToPedal: Boolean) -> Unit
) {
    if (!isOpen || preset == null) return

    var targetSlot by remember(currentSlot, preset) { mutableStateOf(currentSlot.coerceIn(0, 255)) }
    var showSlotPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StudioPalette.SurfaceRaised)
                .border(1.dp, StudioPalette.Border, RoundedCornerShape(12.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Header
                Column {
                    Text(
                        text = "IMPORTAR PRESET .PRST",
                        color = StudioPalette.TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = preset.patchName.ifBlank { "Sem Nome" },
                        color = StudioPalette.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (preset.author.isNotBlank()) {
                        Text(
                            text = "Autor: ${preset.author}",
                            color = StudioPalette.TextMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Patch Info Summary Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioPalette.Surface)
                        .border(1.dp, StudioPalette.Border, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "VOLUME",
                                color = StudioPalette.TextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${preset.patchVolume}%",
                                color = StudioPalette.Accent,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = "PAN",
                                color = StudioPalette.TextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = MasterControlsFormatter.formatPanValue(preset.patchPan),
                                color = StudioPalette.Accent,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = "BLOCOS",
                                color = StudioPalette.TextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${preset.effects.count { it.enabled }}/${preset.effects.size} ON",
                                color = StudioPalette.TextPrimary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Target Slot Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "SLOT DE DESTINO:",
                        color = StudioPalette.TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Slot Button
                        OutlinedButton(
                            onClick = { targetSlot = (targetSlot - 1 + 256) % 256 },
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioPalette.Border),
                            modifier = Modifier
                                .width(44.dp)
                                .heightIn(min = 48.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("‹", color = StudioPalette.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        // Center Slot Pill (Click to open list)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(StudioPalette.Surface)
                                .border(1.dp, StudioPalette.AccentDim.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .clickable { showSlotPicker = true }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val slotLabel = SysExCodec.slotToLabel(targetSlot)
                            val currentNameInSlot = presetNames.getOrNull(targetSlot)?.ifBlank { "Vazio" } ?: "Slot $targetSlot"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = slotLabel,
                                    color = StudioPalette.Accent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = currentNameInSlot,
                                    color = StudioPalette.TextMuted,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                        }

                        // Next Slot Button
                        OutlinedButton(
                            onClick = { targetSlot = (targetSlot + 1) % 256 },
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioPalette.Border),
                            modifier = Modifier
                                .width(44.dp)
                                .heightIn(min = 48.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("›", color = StudioPalette.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (!connected) {
                    Text("Conecte a pedaleira para salvar. O arquivo permanecerá aberto enquanto a conexão é estabelecida.", color = StudioPalette.TextMuted, fontSize = 12.sp)
                }
                // Actions
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 1. Primary: Save to Flash
                    Button(
                        enabled = connected,
                        onClick = {
                            onConfirmImport(targetSlot, true)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioPalette.Accent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = "SALVAR NA PEDALEIRA",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // 2. Secondary: Live Audition
                    Button(
                        onClick = {
                            onConfirmImport(targetSlot, false)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioPalette.SurfaceElevated,
                            contentColor = StudioPalette.TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioPalette.Border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = "Apenas testar (ao vivo)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // 3. Cancel
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioPalette.Border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text("Cancelar", color = StudioPalette.TextMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Modal Slot Selector List
    if (showSlotPicker) {
        SlotPickerModal(
            currentSlot = targetSlot,
            presetNames = presetNames,
            onSelectSlot = { selected ->
                targetSlot = selected
                showSlotPicker = false
            },
            onDismiss = { showSlotPicker = false }
        )
    }
}
