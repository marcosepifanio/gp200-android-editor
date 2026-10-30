package com.valeton.editor.ui.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.valeton.editor.ui.chain.MasterVolumeControl
import com.valeton.editor.ui.theme.StudioPalette

/** Confirmation shown before saving over another slot; every slot always holds a preset. */
fun overwriteConfirmationText(targetSlot: Int, targetName: String?): String {
    val label = SysExCodec.slotToLabel(targetSlot)
    val name = targetName?.trim().orEmpty()
    return if (name.isEmpty()) "O preset do slot $label será substituído. Continuar?"
    else "O preset \"$name\" no slot $label será substituído. Continuar?"
}

@Composable
fun PatchMetadataDialog(
    isOpen: Boolean,
    connected: Boolean,
    currentName: String,
    currentAuthor: String = "Valeton",
    currentVolume: Int = 50,
    currentSlot: Int,
    presetNames: List<String>,
    onDismiss: () -> Unit,
    onApplyLocal: (name: String, author: String, volume: Int) -> Unit,
    onSaveToDevice: (targetSlot: Int, name: String, author: String, volume: Int) -> Unit
) {
    if (!isOpen) return

    var tempName by remember(currentName) { mutableStateOf(currentName) }
    var tempAuthor by remember(currentAuthor) { mutableStateOf(currentAuthor) }
    var tempVolume by remember(currentVolume) { mutableStateOf(currentVolume) }
    var targetSlot by remember(currentSlot) { mutableStateOf(currentSlot) }
    var showSlotPicker by remember { mutableStateOf(false) }
    var confirmOverwrite by remember { mutableStateOf(false) }
    val saveToDevice = {
        onSaveToDevice(targetSlot, tempName, tempAuthor, tempVolume)
        onDismiss()
    }

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
                // Title
                Text(
                    text = "CONFIGURAÇÕES DO PATCH",
                    color = StudioPalette.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                // Name Field
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it.take(16) },
                    label = { Text("Nome do Patch (máx 16)", color = StudioPalette.TextMuted, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StudioPalette.TextPrimary,
                        unfocusedTextColor = StudioPalette.TextPrimary,
                        focusedBorderColor = StudioPalette.Accent,
                        unfocusedBorderColor = StudioPalette.Border,
                        focusedContainerColor = StudioPalette.Surface,
                        unfocusedContainerColor = StudioPalette.Surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Author Field
                OutlinedTextField(
                    value = tempAuthor,
                    onValueChange = { tempAuthor = it.take(16) },
                    label = { Text("Autor (máx 16)", color = StudioPalette.TextMuted, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StudioPalette.TextPrimary,
                        unfocusedTextColor = StudioPalette.TextPrimary,
                        focusedBorderColor = StudioPalette.Accent,
                        unfocusedBorderColor = StudioPalette.Border,
                        focusedContainerColor = StudioPalette.Surface,
                        unfocusedContainerColor = StudioPalette.Surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                MasterVolumeControl(
                    volume = tempVolume,
                    onVolumeChange = { tempVolume = it }
                )

                // Patch location: target slot for "save to pedal"
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Local do patch", color = StudioPalette.TextMuted, fontSize = 12.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioPalette.Surface)
                            .border(1.dp, StudioPalette.Border, RoundedCornerShape(6.dp))
                            .clickable { showSlotPicker = true }
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SysExCodec.slotToLabel(targetSlot),
                            color = StudioPalette.Accent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (targetSlot == currentSlot) "Slot atual" else presetNames.getOrNull(targetSlot).orEmpty(),
                            color = StudioPalette.TextMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions: Save to device / Apply live / Cancel
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 1. Primary: Save to Pedal Flash
                    Button(
                        onClick = {
                            if (targetSlot == currentSlot) saveToDevice() else confirmOverwrite = true
                        },
                        enabled = connected,
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

                    // 2. Secondary Row: Apply Live or Cancel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioPalette.Border),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Cancelar", color = StudioPalette.TextMuted, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                onApplyLocal(tempName, tempAuthor, tempVolume)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StudioPalette.SurfaceElevated,
                                contentColor = StudioPalette.TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Aplicar agora", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    if (showSlotPicker) {
        SlotPickerModal(
            currentSlot = targetSlot,
            presetNames = presetNames,
            onSelectSlot = {
                targetSlot = it
                showSlotPicker = false
            },
            onDismiss = { showSlotPicker = false }
        )
    }

    if (confirmOverwrite) {
        AlertDialog(
            onDismissRequest = { confirmOverwrite = false },
            title = { Text("Substituir preset?") },
            text = { Text(overwriteConfirmationText(targetSlot, presetNames.getOrNull(targetSlot))) },
            confirmButton = {
                TextButton(onClick = {
                    confirmOverwrite = false
                    saveToDevice()
                }) { Text("Substituir") }
            },
            dismissButton = {
                TextButton(onClick = { confirmOverwrite = false }) { Text("Cancelar") }
            }
        )
    }
}
