package com.valeton.editor.ui.device

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.ui.theme.*
import com.valeton.editor.usb.MidiConnectionState

@Composable
fun TopConnectionBar(
    slotNumber: Int,
    patchName: String,
    connectionState: MidiConnectionState,
    hasUnsavedChanges: Boolean,
    onOpenManage: () -> Unit,
    onSaveClick: () -> Unit,
    onExportClick: () -> Unit,
    onRenameClick: () -> Unit,
    onHomeClick: () -> Unit = {},
    onReconnectClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Home / Connection Status Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceVariantDark)
                        .clickable { onHomeClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Tela de Conexão",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Status indicator (clickable to reconnect)
                val ledColor = when (connectionState) {
                    is MidiConnectionState.Connected -> LedActive
                    is MidiConnectionState.Connecting, is MidiConnectionState.Handshaking -> ValetonBlue
                    else -> LedInactive
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(ledColor)
                        .clickable { onReconnectClick() }
                )
            }

            // Central Preset Selector Pill [ 00 | Patch Name ]
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariantDark)
                    .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
                    .clickable { onOpenManage() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "%02d".format(slotNumber),
                    color = TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "  |  ",
                    color = BorderDark,
                    fontSize = 14.sp
                )

                Text(
                    text = patchName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            // Right Action Buttons: Save & 3-Dots Menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Save Button (Floppy Disk icon)
                IconButton(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceVariantDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Salvar na Pedaleira",
                        tint = if (hasUnsavedChanges) ValetonBlue else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 3-Dots Menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceVariantDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opções",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Renomear Patch", color = TextPrimary) },
                            onClick = {
                                menuExpanded = false
                                onRenameClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Exportar .prst", color = TextPrimary) },
                            onClick = {
                                menuExpanded = false
                                onExportClick()
                            }
                        )
                    }
                }
            }
        }
    }
}
