package com.valeton.editor.ui.controls

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.core.model.ModuleType
import com.valeton.editor.ui.theme.StudioPalette

@Composable
fun StudioControlsScreen(
    preset: GP200Preset?,
    currentSlot: Int,
    connected: Boolean,
    busy: Boolean,
    hasUnsavedChanges: Boolean,
    onQuickKnobChange: (Int, Int, Int) -> Unit,
    onCtrlTargetChange: (Int, Int, Boolean) -> Unit,
    onReload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxSize().background(StudioPalette.Case).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("CONTROLES", style = MaterialTheme.typography.titleLarge, color = StudioPalette.TextPrimary, fontWeight = FontWeight.Bold)
        Text("Configure os três knobs físicos e os CTRL 1 a 4 deste patch.", color = StudioPalette.TextSecondary)
        if (!connected) {
            Surface(color = StudioPalette.SurfaceRaised, shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, StudioPalette.AmberLed.copy(alpha = 0.35f))) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Modo offline", color = StudioPalette.AmberLed, fontWeight = FontWeight.SemiBold)
                    Text("As alterações ficam no patch deste dispositivo. Conecte a GP-200 e salve para aplicar as associações.", color = StudioPalette.TextSecondary)
                }
            }
        }
        if (busy) {
            Surface(color = StudioPalette.Surface, shape = RoundedCornerShape(10.dp)) {
                Text("Aguarde a operação em andamento para editar os controles.", Modifier.padding(12.dp), color = StudioPalette.Accent)
            }
        }
        val controls = preset?.controlMappings
        if (preset == null || controls == null) {
            ControlCard("Mapeamentos indisponíveis") {
                Text("As associações deste patch ainda não foram lidas da pedaleira.", color = StudioPalette.TextSecondary)
                if (hasUnsavedChanges) {
                    Text("Salve ou descarte as alterações do patch antes de ler novamente.", color = StudioPalette.TextMuted)
                }
                Button(
                    onClick = onReload,
                    enabled = connected && !busy && !hasUnsavedChanges,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPalette.Accent, contentColor = StudioPalette.Case)
                ) {
                    Text("Ler da pedaleira")
                }
            }
            return@Column
        }
        ControlCard("KNOBS FÍSICOS") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Spacer(Modifier.width(46.dp))
                    Text(
                        "DESTINO",
                        modifier = Modifier.weight(1.05f),
                        color = StudioPalette.TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                    Text(
                        "PARÂMETRO",
                        modifier = Modifier.weight(0.95f),
                        color = StudioPalette.TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                }
                for (index in 0..2) {
                    key(currentSlot, index) {
                        val assignment = controls.knobs[index]
                        val effectIdentity = preset.effects.map { it.slotIndex to it.effectId }
                        var draftBlock by remember(currentSlot, assignment, effectIdentity) {
                            mutableIntStateOf(assignment.blockIndex)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                modifier = Modifier.width(46.dp).heightIn(min = 48.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = StudioPalette.SurfaceRaised,
                                border = BorderStroke(1.dp, StudioPalette.BorderSubtle)
                            ) {
                                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    Text(
                                        "K${index + 1}",
                                        color = StudioPalette.TextSecondary,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            MappingSelect(
                                label = "Destino",
                                value = ControlMappingOptions.knobTargetLabel(preset, draftBlock),
                                options = ControlMappingOptions.knobTargets(preset),
                                enabled = !busy,
                                showLabel = false,
                                compact = true,
                                modifier = Modifier.weight(1.05f),
                                accessibilityLabel = "Knob ${index + 1}, destino: ${ControlMappingOptions.knobTargetLabel(preset, draftBlock)}",
                                onSelect = { block ->
                                    draftBlock = block
                                    if (block in listOf(11, 12, 255)) onQuickKnobChange(index, block, 255)
                                }
                            )
                            if (draftBlock in 0..9) {
                                val parameters = ControlMappingOptions.parameters(preset, draftBlock)
                                val selectedParam = assignment.paramIndex.takeIf { assignment.blockIndex == draftBlock }
                                val paramLabel = parameters.find { it.id == selectedParam }?.label
                                    ?: if (selectedParam != null) "Parâmetro $selectedParam · selecione novamente"
                                    else "Selecione um parâmetro"
                                MappingSelect(
                                    label = "Parâmetro",
                                    value = paramLabel,
                                    options = parameters,
                                    enabled = !busy && parameters.isNotEmpty(),
                                    showLabel = false,
                                    compact = true,
                                    modifier = Modifier.weight(0.95f),
                                    accessibilityLabel = "Knob ${index + 1}, parâmetro: $paramLabel"
                                ) { param -> onQuickKnobChange(index, draftBlock, param) }
                            } else {
                                Box(
                                    modifier = Modifier.weight(0.95f).heightIn(min = 48.dp),
                                    contentAlignment = androidx.compose.ui.Alignment.CenterStart
                                ) {
                                    Text("—", color = StudioPalette.TextMuted)
                                }
                            }
                        }
                        if (draftBlock != assignment.blockIndex) {
                            Text(
                                "Escolha o parâmetro para aplicar esta associação.",
                                color = StudioPalette.TextMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
        var selectedCtrlIndex by remember(currentSlot) { mutableIntStateOf(0) }
        ControlCard("CONTROLES CTRL") {
            Text("Marque os efeitos que cada CTRL pode desativar.", color = StudioPalette.TextSecondary)
            TabRow(selectedTabIndex = selectedCtrlIndex) {
                (0..3).forEach { index ->
                    Tab(
                        selected = selectedCtrlIndex == index,
                        onClick = { selectedCtrlIndex = index },
                        enabled = !busy,
                        text = { Text("CTRL ${index + 1}") }
                    )
                }
            }
            for (row in (0..10).toList().chunked(2)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { blockIndex ->
                        val bit = 1L shl blockIndex
                        val checked = controls.ctrls[selectedCtrlIndex].targetMask and bit != 0L
                        val fullLabel = ControlMappingOptions.effectLabel(preset, blockIndex)
                        val effectName = fullLabel.substringAfter(" · ", fullLabel)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .toggleable(
                                    value = checked,
                                    enabled = !busy,
                                    role = Role.Checkbox,
                                    onValueChange = { onCtrlTargetChange(selectedCtrlIndex, blockIndex, it) }
                                ),
                            color = if (checked) StudioPalette.SurfaceRaised else StudioPalette.Surface,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (checked) StudioPalette.Accent.copy(alpha = 0.7f) else StudioPalette.BorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Checkbox(checked = checked, onCheckedChange = null, enabled = !busy)
                                Column(Modifier.padding(start = 2.dp)) {
                                    Text(
                                        ModuleType.fromSlotIndex(blockIndex).code,
                                        color = StudioPalette.TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        effectName,
                                        color = StudioPalette.TextMuted,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        Text(
            if (hasUnsavedChanges) "Alterações pendentes. Use SALVAR para gravar neste patch." else "As associações são individuais para cada patch.",
            color = if (hasUnsavedChanges) StudioPalette.Accent else StudioPalette.TextMuted
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ControlCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(color = StudioPalette.Surface, shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, StudioPalette.BorderSubtle)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = StudioPalette.TextPrimary, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun MappingSelect(
    label: String,
    value: String,
    options: List<MappingOption>,
    enabled: Boolean,
    showLabel: Boolean = true,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
    accessibilityLabel: String = "$label: $value",
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(enabled) { if (!enabled) expanded = false }
    Column(modifier) {
        if (showLabel) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = StudioPalette.TextMuted)
        }
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().then(if (compact) Modifier.heightIn(min = 48.dp) else Modifier)
                    .semantics { contentDescription = accessibilityLabel },
                contentPadding = if (compact) PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                else ButtonDefaults.ContentPadding
            ) {
                Text(
                    value,
                    modifier = Modifier.weight(1f),
                    color = StudioPalette.TextPrimary,
                    style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge,
                    maxLines = if (compact) 1 else Int.MAX_VALUE,
                    overflow = if (compact) TextOverflow.Ellipsis else TextOverflow.Clip
                )
                Text("▾", color = StudioPalette.Accent, modifier = Modifier.padding(start = 4.dp))
            }
            DropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 320.dp)) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option.label) }, onClick = {
                        expanded = false
                        onSelect(option.id)
                    })
                }
            }
        }
    }
}
