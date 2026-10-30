package com.valeton.editor.ui.manage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.BuildConfig
import com.valeton.editor.core.codec.SysExCodec
import com.valeton.editor.ui.theme.StudioPalette
import com.valeton.editor.ui.theme.StudioTypography

data class SlotItem(
    val slotNumber: Int,
    val label: String,
    val name: String
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun StudioPatchManagerScreen(
    currentSlot: Int,
    presetNames: List<String>,
    syncProgress: Int,
    onSelectSlot: (Int) -> Unit,
    onRenameSlot: (Int, String) -> Unit,
    onSaveExportSlot: (Int) -> Unit = {},
    onShareExportSlot: (Int) -> Unit = {},
    onImportClick: (() -> Unit)? = null,
    onResyncAllClick: (() -> Unit)? = null,
    resyncEnabled: Boolean = true,
    isPerformingFullSync: Boolean = false,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var renameDialogSlot by remember { mutableStateOf<SlotItem?>(null) }
    var tempRenameText by remember { mutableStateOf("") }
    var showResyncConfirmDialog by remember { mutableStateOf(false) }
    var exportSheetSlot by remember { mutableStateOf<SlotItem?>(null) }

    LaunchedEffect(isPerformingFullSync) {
        if (isPerformingFullSync) {
            renameDialogSlot = null
            showResyncConfirmDialog = false
        }
    }

    val allSlots = remember(presetNames) {
        (0 until 256).map { slotIndex ->
            val label = try {
                SysExCodec.slotToLabel(slotIndex)
            } catch (_: Exception) {
                "%02d".format(slotIndex)
            }
            val name = presetNames.getOrElse(slotIndex) { "Patch %02d".format(slotIndex) }
            SlotItem(slotIndex, label, name)
        }
    }

    val filteredSlots = remember(allSlots, searchQuery) {
        if (searchQuery.isBlank()) allSlots
        else allSlots.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.label.contains(searchQuery, ignoreCase = true) ||
            "%02d".format(it.slotNumber).contains(searchQuery)
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(currentSlot) {
        if (searchQuery.isBlank()) {
            val target = (currentSlot - 2).coerceAtLeast(0)
            listState.animateScrollToItem(target)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioPalette.Case)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "PRESETS",
                color = StudioPalette.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onResyncAllClick != null) {
                    OutlinedButton(
                        onClick = { showResyncConfirmDialog = true },
                        enabled = resyncEnabled && !isPerformingFullSync,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioPalette.AmberLed)
                    ) {
                        Text(
                            text = "Ressincronizar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (onImportClick != null) {
                    OutlinedButton(
                        onClick = onImportClick,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioPalette.Accent)
                    ) {
                        Text(
                            text = "Importar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 1. Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Buscar preset ou banco (ex.: 01-A, lead)...",
                    color = StudioPalette.TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = StudioPalette.TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = StudioPalette.TextPrimary,
                unfocusedTextColor = StudioPalette.TextPrimary,
                focusedBorderColor = StudioPalette.Accent,
                unfocusedBorderColor = StudioPalette.Border,
                focusedContainerColor = StudioPalette.Surface,
                unfocusedContainerColor = StudioPalette.Surface
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )

        // 2. Sync Progress Notice
        AnimatedVisibility(visible = !isPerformingFullSync && syncProgress in 1..255) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sincronizando patches com a pedaleira...",
                        color = StudioPalette.TextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "$syncProgress/256",
                        color = StudioPalette.Accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = { syncProgress / 256f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = StudioPalette.Accent,
                    trackColor = StudioPalette.Border
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Patches List (256 Slots) fills all remaining vertical space
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredSlots, key = { it.slotNumber }) { item ->
                val isSelected = item.slotNumber == currentSlot

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) StudioPalette.SurfaceRaised else StudioPalette.Surface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) StudioPalette.Accent else StudioPalette.Border,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onSelectSlot(item.slotNumber) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Slot Label Badge (e.g. "01-A")
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) StudioPalette.AccentGlow else StudioPalette.SurfaceElevated)
                            .padding(vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.label,
                            color = if (isSelected) StudioPalette.Accent else StudioPalette.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Patch Name
                    Text(
                        text = item.name,
                        color = if (isSelected) StudioPalette.TextPrimary else StudioPalette.TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Active dot
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(StudioPalette.Accent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    var showRowMenu by remember(item.slotNumber) { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showRowMenu = true },
                            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Mais opções para ${item.label} ${item.name}",
                                tint = StudioPalette.TextMuted
                            )
                        }
                        DropdownMenu(
                            expanded = showRowMenu,
                            onDismissRequest = { showRowMenu = false },
                            containerColor = StudioPalette.SurfaceElevated
                        ) {
                            DropdownMenuItem(
                                text = { Text("Exportar preset") },
                                leadingIcon = { Icon(Icons.Default.SaveAlt, contentDescription = null) },
                                onClick = {
                                    showRowMenu = false
                                    exportSheetSlot = item
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Renomear") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    showRowMenu = false
                                    renameDialogSlot = item
                                    tempRenameText = item.name
                                }
                            )
                        }
                    }
                }
            }

            item {
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

    exportSheetSlot?.let { slot ->
        ModalBottomSheet(
            onDismissRequest = { exportSheetSlot = null },
            containerColor = StudioPalette.SurfaceRaised,
            contentColor = StudioPalette.TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Exportar preset", style = StudioTypography.Section)
                Text("${slot.label} · ${slot.name}", color = StudioPalette.TextSecondary)
                OutlinedButton(
                    onClick = {
                        exportSheetSlot = null
                        onSaveExportSlot(slot.slotNumber)
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioPalette.Accent)
                ) {
                    Icon(Icons.Default.SaveAlt, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Salvar arquivo")
                }
                Button(
                    onClick = {
                        exportSheetSlot = null
                        onShareExportSlot(slot.slotNumber)
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPalette.Accent, contentColor = StudioPalette.Case)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Compartilhar")
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // Quick Rename Dialog
    if (renameDialogSlot != null && !isPerformingFullSync) {
        val target = renameDialogSlot!!
        AlertDialog(
            onDismissRequest = { renameDialogSlot = null },
            title = {
                Text(
                    text = "Renomear Slot ${target.label}",
                    color = StudioPalette.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                OutlinedTextField(
                    value = tempRenameText,
                    onValueChange = { tempRenameText = it.take(16) },
                    singleLine = true,
                    label = { Text("Nome do Patch (máx 16)", color = StudioPalette.TextMuted, fontSize = 12.sp) },
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
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRenameSlot(target.slotNumber, tempRenameText)
                        renameDialogSlot = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioPalette.Accent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Salvar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { renameDialogSlot = null }) {
                    Text("Cancelar", color = StudioPalette.TextMuted)
                }
            },
            containerColor = StudioPalette.SurfaceRaised,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showResyncConfirmDialog && resyncEnabled && !isPerformingFullSync) {
        AlertDialog(
            onDismissRequest = { showResyncConfirmDialog = false },
            title = {
                Text(
                    text = "RESSINCRONIZAR BIBLIOTECA",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Text(
                    text = "Deseja recarregar todos os 256 patches diretamente da pedaleira? Isso atualizará o cache local permanente."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResyncConfirmDialog = false
                        onResyncAllClick?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPalette.AmberLed)
                ) {
                    Text("RESSINCRONIZAR", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResyncConfirmDialog = false }) {
                    Text("CANCELAR", color = StudioPalette.TextSecondary)
                }
            },
            containerColor = StudioPalette.SurfaceElevated,
            titleContentColor = StudioPalette.TextPrimary,
            textContentColor = StudioPalette.TextSecondary
        )
    }
}
