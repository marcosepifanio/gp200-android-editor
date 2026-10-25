package com.valeton.editor.ui.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.valeton.editor.ui.chain.StudioMasterBar
import com.valeton.editor.ui.theme.StudioPalette

@Composable
fun PatchMetadataDialog(
    isOpen: Boolean,
    currentName: String,
    currentAuthor: String = "Valeton",
    currentVolume: Int = 50,
    currentPan: Int = 0,
    onDismiss: () -> Unit,
    onSaveLocal: (name: String, author: String, volume: Int, pan: Int) -> Unit,
    onSaveToDevice: (name: String, author: String, volume: Int, pan: Int) -> Unit
) {
    if (!isOpen) return

    var tempName by remember(currentName) { mutableStateOf(currentName) }
    var tempAuthor by remember(currentAuthor) { mutableStateOf(currentAuthor) }
    var tempVolume by remember(currentVolume) { mutableStateOf(currentVolume) }
    var tempPan by remember(currentPan) { mutableStateOf(currentPan) }

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

                // Master Output Section
                StudioMasterBar(
                    volume = tempVolume,
                    pan = tempPan,
                    onVolumeChange = { tempVolume = it },
                    onPanChange = { tempPan = it }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Actions: Save to device / Apply live / Cancel
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 1. Primary: Save to Pedal Flash
                    Button(
                        onClick = {
                            onSaveToDevice(tempName, tempAuthor, tempVolume, tempPan)
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
                                onSaveLocal(tempName, tempAuthor, tempVolume, tempPan)
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
}
