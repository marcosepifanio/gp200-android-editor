package com.valeton.editor.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.*
import com.valeton.editor.ui.components.HorizontalSlider
import com.valeton.editor.ui.components.SegmentedPill
import com.valeton.editor.ui.theme.*

@Composable
fun PedalEditorSheet(
    slot: EffectSlot,
    onParamChange: (paramIndex: Int, value: Float) -> Unit,
    onChangeEffect: (newEffectId: Int) -> Unit,
    onToggleBypass: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectDef = remember(slot.effectId) { EffectCatalog.getEffectById(slot.effectId) }
    val moduleType = remember(slot.slotIndex, slot.effectId) {
        ModuleType.fromSlotIndex(slot.slotIndex).takeIf { it != ModuleType.UNKNOWN }
            ?: effectDef?.module
            ?: EffectCatalog.getModuleName(slot.effectId)
    }

    var showEffectPicker by remember(slot.slotIndex) { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(scrollState)
    ) {
        // Top Header: Module badge with Power toggle (left) and Model Picker card (right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Module Box with Power Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariantDark)
                    .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClickLabel = "Alternar bypass") { onToggleBypass() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = moduleType.code,
                    color = if (slot.enabled) TextPrimary else TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Ligar/Desligar",
                    tint = if (slot.enabled) ValetonBlue else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Right: Model Selection Card
            Box(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceVariantDark)
                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClickLabel = "Selecionar modelo") { showEffectPicker = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = moduleType.displayName,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = effectDef?.name ?: "Bypass / Desligado",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (effectDef != null && effectDef.basedOn.isNotBlank()) {
                            Text(
                                text = effectDef.basedOn,
                                color = TextMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Selecionar modelo",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        EffectPickerSheet(
            isOpen = showEffectPicker,
            module = moduleType,
            currentEffectId = slot.effectId,
            onSelectEffect = { newEffectId ->
                showEffectPicker = false
                onChangeEffect(newEffectId)
            },
            onDismiss = { showEffectPicker = false }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Parameter Controls List
        if (effectDef != null && effectDef.params.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                effectDef.params.forEach { param ->
                    when (param) {
                        is KnobParam -> {
                            val currentValue = if (param.index < slot.params.size) {
                                slot.params[param.index]
                            } else {
                                param.defaultValue
                            }

                            HorizontalSlider(
                                value = currentValue,
                                min = param.min,
                                max = param.max,
                                step = param.step,
                                label = param.name,
                                onValueChange = { newVal ->
                                    onParamChange(param.index, newVal)
                                }
                            )
                        }

                        is SwitchParam -> {
                            val currentValue = if (param.index < slot.params.size) {
                                slot.params[param.index]
                            } else {
                                param.defaultValue
                            }
                            val isSwitchOn = currentValue > 0f

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceVariantDark)
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = param.name,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Switch(
                                    checked = isSwitchOn,
                                    onCheckedChange = { checked ->
                                        onParamChange(param.index, if (checked) 1f else 0f)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = ValetonBlue,
                                        uncheckedThumbColor = TextSecondary,
                                        uncheckedTrackColor = Color(0xFF2C3240)
                                    )
                                )
                            }
                        }

                        is ComboBoxParam -> {
                            val currentValue = if (param.index < slot.params.size) {
                                slot.params[param.index]
                            } else {
                                param.defaultValue
                            }

                            if (param.options.size in 2..3) {
                                // Render as Segmented Pill buttons
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceVariantDark)
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = param.name,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    val currentOption = param.options.find { it.id == currentValue.toInt() }
                                        ?: param.options.first()

                                    SegmentedPill(
                                        items = param.options,
                                        selectedItem = currentOption,
                                        onItemSelected = { selected ->
                                            onParamChange(param.index, selected.id.toFloat())
                                        },
                                        itemLabel = { it.name },
                                        modifier = Modifier.width(180.dp)
                                    )
                                }
                            } else {
                                // Dropdown row with chevron
                                var comboExpanded by remember { mutableStateOf(false) }
                                val currentOptName = param.options.firstOrNull { it.id == currentValue.toInt() }?.name
                                    ?: "Selecionar"

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceVariantDark)
                                            .clickable { comboExpanded = true }
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = param.name,
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = currentOptName,
                                                color = ValetonBlueLight,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = "Opções",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = comboExpanded,
                                        onDismissRequest = { comboExpanded = false },
                                        modifier = Modifier.background(SurfaceDark)
                                    ) {
                                        param.options.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option.name, color = TextPrimary) },
                                                onClick = {
                                                    onParamChange(param.index, option.id.toFloat())
                                                    comboExpanded = false
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
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum parâmetro ajustável para este bloco.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}
