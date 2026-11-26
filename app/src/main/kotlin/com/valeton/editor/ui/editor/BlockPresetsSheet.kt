package com.valeton.editor.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.ModuleType
import com.valeton.editor.repository.BlockPreset
import com.valeton.editor.repository.BlockPresetResult
import com.valeton.editor.repository.BlockPresetRules
import com.valeton.editor.repository.BlockPresetsRepository
import com.valeton.editor.ui.components.LocalUserModelNames
import com.valeton.editor.ui.components.effectDisplayName
import com.valeton.editor.ui.theme.StudioPalette

val LocalBlockPresetsRepository = staticCompositionLocalOf<BlockPresetsRepository?> { null }

private sealed interface BlockPresetDialog {
    data object Save : BlockPresetDialog
    data class Rename(val preset: BlockPreset) : BlockPresetDialog
    data class Replace(val preset: BlockPreset) : BlockPresetDialog
    data class Delete(val preset: BlockPreset) : BlockPresetDialog
}

private fun BlockPresetResult.errorMessage(): String? = when (this) {
    is BlockPresetResult.Ok -> null
    BlockPresetResult.EmptyName -> "Dê um nome ao preset."
    BlockPresetResult.Duplicate -> "Já existe um preset com esse nome."
    BlockPresetResult.LimitReached -> "Limite de ${BlockPresetRules.MAX_PER_BLOCK} presets."
    BlockPresetResult.NotFound -> "O preset não existe mais."
    BlockPresetResult.WriteFailed -> "Não foi possível gravar o preset."
    else -> null
}

@Composable
fun rememberBlockPresetsRepository(): BlockPresetsRepository {
    val override = LocalBlockPresetsRepository.current
    if (override != null) return override
    val filesDir = LocalContext.current.applicationContext.filesDir
    return remember(filesDir) { BlockPresetsRepository(filesDir) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockPresetsSheet(
    isOpen: Boolean,
    module: ModuleType,
    slot: EffectSlot,
    busy: Boolean,
    repository: BlockPresetsRepository,
    onApplyPreset: (BlockPreset) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val userNames = LocalUserModelNames.current
    var presets by remember(module, repository) { mutableStateOf(repository.list(module)) }
    var damaged by remember(module, repository) { mutableStateOf(repository.hasDamagedEntries()) }
    var dialog by remember { mutableStateOf<BlockPresetDialog?>(null) }
    var dialogError by remember { mutableStateOf<String?>(null) }

    fun applyResult(result: BlockPresetResult): Boolean {
        if (result is BlockPresetResult.Ok) {
            presets = result.presets
            damaged = repository.hasDamagedEntries()
            dialogError = null
            return true
        }
        dialogError = result.errorMessage()
        return false
    }

    val limitReached = presets.size >= BlockPresetRules.MAX_PER_BLOCK

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StudioPalette.Surface,
        contentColor = StudioPalette.TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PRESETS DO ${module.code}",
                        color = StudioPalette.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${presets.size}/${BlockPresetRules.MAX_PER_BLOCK} salvos neste aparelho",
                        color = StudioPalette.TextMuted,
                        fontSize = 12.sp
                    )
                }
                Button(
                    onClick = {
                        dialogError = null
                        dialog = BlockPresetDialog.Save
                    },
                    enabled = !busy && !limitReached,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioPalette.Accent,
                        contentColor = StudioPalette.Case
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Salvar atual", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (damaged) {
                Surface(
                    color = StudioPalette.SurfaceRaised,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, StudioPalette.AmberLed.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Alguns presets salvos estavam danificados e foram ignorados.",
                        color = StudioPalette.AmberLed,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth().padding(10.dp)
                    )
                }
            }

            if (presets.isEmpty()) {
                Surface(
                    color = StudioPalette.SurfaceRaised,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, StudioPalette.Border)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Nenhum preset salvo para ${module.code}",
                            color = StudioPalette.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Toque em \"Salvar atual\" para guardar o modelo e os parâmetros deste efeito.",
                            color = StudioPalette.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presets, key = { it.name }) { item ->
                        val isMatch = BlockPresetRules.matches(item, slot)
                        val modelLabel = EffectCatalog.getEffectById(item.effectId)
                            ?.let { effectDisplayName(it, userNames) }
                            ?: "Modelo ${item.effectId}"
                        var menuOpen by remember(item.name) { mutableStateOf(false) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isMatch) StudioPalette.SurfaceRaised else StudioPalette.Case)
                                .border(
                                    width = 1.dp,
                                    color = if (isMatch) StudioPalette.Accent else StudioPalette.Border,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = !busy) {
                                    onApplyPreset(item)
                                    onDismiss()
                                }
                                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isMatch) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Ativo",
                                            tint = StudioPalette.Accent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = item.name,
                                        color = if (isMatch) StudioPalette.Accent else StudioPalette.TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = modelLabel,
                                    color = StudioPalette.TextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { menuOpen = true },
                                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Opções de ${item.name}",
                                        tint = StudioPalette.TextMuted
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuOpen,
                                    onDismissRequest = { menuOpen = false },
                                    containerColor = StudioPalette.SurfaceElevated
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Aplicar") },
                                        enabled = !busy,
                                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
                                        onClick = {
                                            menuOpen = false
                                            onApplyPreset(item)
                                            onDismiss()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Substituir pelo atual") },
                                        enabled = !busy,
                                        leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                                        onClick = {
                                            menuOpen = false
                                            dialogError = null
                                            dialog = BlockPresetDialog.Replace(item)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Renomear") },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                        onClick = {
                                            menuOpen = false
                                            dialogError = null
                                            dialog = BlockPresetDialog.Rename(item)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Excluir", color = StudioPalette.AccentRed) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = StudioPalette.AccentRed)
                                        },
                                        onClick = {
                                            menuOpen = false
                                            dialogError = null
                                            dialog = BlockPresetDialog.Delete(item)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    when (val d = dialog) {
        BlockPresetDialog.Save -> {
            val defaultName = EffectCatalog.getEffectById(slot.effectId)
                ?.let { effectDisplayName(it, userNames) }
                ?.take(BlockPresetRules.MAX_NAME)
                .orEmpty()
            PresetNameDialog(
                title = "Salvar preset de ${module.code}",
                initialName = defaultName,
                confirmLabel = "Salvar",
                error = dialogError,
                onConfirm = { name ->
                    if (applyResult(repository.save(module, name, slot.effectId, slot.params))) {
                        dialog = null
                    }
                },
                onDismiss = { dialog = null }
            )
        }
        is BlockPresetDialog.Rename -> {
            PresetNameDialog(
                title = "Renomear preset",
                initialName = d.preset.name,
                confirmLabel = "Salvar",
                error = dialogError,
                onConfirm = { name ->
                    if (applyResult(repository.rename(module, d.preset.name, name))) {
                        dialog = null
                    }
                },
                onDismiss = { dialog = null }
            )
        }
        is BlockPresetDialog.Replace -> {
            AlertDialog(
                onDismissRequest = { dialog = null },
                containerColor = StudioPalette.SurfaceRaised,
                title = { Text("Substituir preset?", color = StudioPalette.TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "\"${d.preset.name}\" será atualizado com o modelo e os parâmetros atuais de ${module.code}.",
                            color = StudioPalette.TextSecondary
                        )
                        if (dialogError != null) {
                            Text(dialogError!!, color = StudioPalette.AccentRed, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (applyResult(repository.replace(module, d.preset.name, slot.effectId, slot.params))) {
                                dialog = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPalette.Accent, contentColor = StudioPalette.Case)
                    ) { Text("Substituir", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { dialog = null }) { Text("Cancelar", color = StudioPalette.TextMuted) }
                }
            )
        }
        is BlockPresetDialog.Delete -> {
            AlertDialog(
                onDismissRequest = { dialog = null },
                containerColor = StudioPalette.SurfaceRaised,
                title = { Text("Excluir preset?", color = StudioPalette.TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Remover \"${d.preset.name}\" de ${module.code}?", color = StudioPalette.TextSecondary)
                        if (dialogError != null) {
                            Text(dialogError!!, color = StudioPalette.AccentRed, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (applyResult(repository.delete(module, d.preset.name))) {
                                dialog = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPalette.AccentRed, contentColor = StudioPalette.TextPrimary)
                    ) { Text("Excluir", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { dialog = null }) { Text("Cancelar", color = StudioPalette.TextMuted) }
                }
            )
        }
        null -> Unit
    }
}

@Composable
private fun PresetNameDialog(
    title: String,
    initialName: String,
    confirmLabel: String,
    error: String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioPalette.SurfaceRaised,
        title = { Text(title, color = StudioPalette.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.take(BlockPresetRules.MAX_NAME) },
                    singleLine = true,
                    label = { Text("Nome (máx ${BlockPresetRules.MAX_NAME})") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StudioPalette.TextPrimary,
                        unfocusedTextColor = StudioPalette.TextPrimary,
                        focusedBorderColor = StudioPalette.Accent,
                        unfocusedBorderColor = StudioPalette.Border
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(error, color = StudioPalette.AccentRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
                colors = ButtonDefaults.buttonColors(containerColor = StudioPalette.Accent, contentColor = StudioPalette.Case)
            ) { Text(confirmLabel, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = StudioPalette.TextMuted) }
        }
    )
}
