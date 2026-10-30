package com.valeton.editor.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.ui.components.LocalUserModelNames
import com.valeton.editor.ui.components.effectDisplayName
import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.*
import com.valeton.editor.ui.components.PedalArtwork
import com.valeton.editor.ui.components.StudioKnob
import com.valeton.editor.ui.components.StudioStompSwitch
import com.valeton.editor.ui.components.StudioTempoControl
import com.valeton.editor.ui.theme.StudioPalette
import com.valeton.editor.ui.theme.getModuleSpec

@Composable
fun StudioPedalEditorScreen(
    slot: EffectSlot,
    index: Int,
    chainLength: Int = 11,
    tempo: Int = 120,
    onTempoChange: (Int) -> Unit = {},
    onTapTempo: () -> Unit = {},
    onBack: () -> Unit,
    onToggle: () -> Unit,
    onChangeEffect: (Int) -> Unit,
    onParamChange: (paramIndex: Int, value: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEffectPicker by remember { mutableStateOf(false) }

    val effectDef: EffectDefinition? = EffectCatalog.getEffectById(slot.effectId)
    val module = ModuleType.fromSlotIndex(slot.slotIndex)
    val spec = getModuleSpec(module.code)
    val effectsInDisplayOrder = remember(module) {
        EffectPickerLogic.effectsInDisplayOrder(
            effects = EffectCatalog.getEffectsByModule(module),
            categories = EffectCatalog.getCategoriesForModule(module)
        )
    }
    val previousEffectId = remember(effectsInDisplayOrder, slot.effectId) {
        EffectPickerLogic.adjacentEffectId(effectsInDisplayOrder, slot.effectId, step = -1)
    }
    val nextEffectId = remember(effectsInDisplayOrder, slot.effectId) {
        EffectPickerLogic.adjacentEffectId(effectsInDisplayOrder, slot.effectId, step = 1)
    }

    val knobParams = remember(effectDef) {
        effectDef?.params?.filterIsInstance<KnobParam>() ?: emptyList()
    }
    val switchParams = remember(effectDef) {
        effectDef?.params?.filter { it !is KnobParam } ?: emptyList()
    }

    var focusedParamIndex by remember(slot.effectId) {
        mutableStateOf(knobParams.firstOrNull()?.index)
    }

    val focusedKnob = remember(knobParams, focusedParamIndex) {
        knobParams.find { it.index == focusedParamIndex } ?: knobParams.firstOrNull()
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioPalette.Case)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 1. Editor Head [‹ CHAIN      DST · 3/11]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(StudioPalette.SurfaceRaised)
                    .border(1.dp, StudioPalette.Border, RoundedCornerShape(6.dp))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "‹ CADEIA",
                    color = StudioPalette.Accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = "${module.code} · ${index + 1}/$chainLength",
                color = StudioPalette.TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Hero Stage (Illuminated Platform + Artwork)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            spec.body.copy(alpha = 0.25f),
                            StudioPalette.SurfaceRaised,
                            StudioPalette.Surface
                        )
                    )
                )
                .border(1.dp, StudioPalette.Border, RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { previousEffectId?.let(onChangeEffect) },
                    enabled = previousEffectId != null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(StudioPalette.AccentGlow)
                        .border(1.dp, StudioPalette.Accent.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Efeito anterior",
                        tint = StudioPalette.Accent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                PedalArtwork(
                    svgFile = effectDef?.svgFile ?: "",
                    name = effectDef?.let { effectDisplayName(it, LocalUserModelNames.current) } ?: module.code,
                    accentColor = spec.bodyDeep,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                IconButton(
                    onClick = { nextEffectId?.let(onChangeEffect) },
                    enabled = nextEffectId != null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(StudioPalette.AccentGlow)
                        .border(1.dp, StudioPalette.Accent.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Próximo efeito",
                        tint = StudioPalette.Accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Identity Card [Name + based on ... | SWAP]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StudioPalette.SurfaceRaised)
                .border(1.dp, StudioPalette.Border, RoundedCornerShape(8.dp))
                .clickable { showEffectPicker = true }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = effectDef?.let { effectDisplayName(it, LocalUserModelNames.current) } ?: "Bypass",
                    color = StudioPalette.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!effectDef?.basedOn.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "baseado em ${effectDef!!.basedOn}",
                        color = StudioPalette.TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!effectDef?.category.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioPalette.AccentGlow)
                            .border(1.dp, StudioPalette.Accent.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = effectDef!!.category.uppercase(),
                            color = StudioPalette.Accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.4.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // SWAP Button Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(StudioPalette.AccentGlow)
                    .border(1.dp, StudioPalette.Accent, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "TROCAR",
                    color = StudioPalette.Accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // 3.5 Patch Tempo / BPM Control (Only for MOD and DLY modules)
        if (module == ModuleType.MOD || module == ModuleType.DLY) {
            Spacer(modifier = Modifier.height(10.dp))
            StudioTempoControl(
                tempo = tempo,
                onTempoChange = onTempoChange,
                onTapTempo = onTapTempo,
                compact = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Knobs Grid
        if (knobParams.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudioPalette.Surface)
                    .border(1.dp, StudioPalette.Border, RoundedCornerShape(10.dp))
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                // Chunk knobs in rows of up to 4 items
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    knobParams.chunked(4).forEach { rowKnobs ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowKnobs.forEach { param ->
                                val currentValue = slot.params.getOrElse(param.index) { param.defaultValue }
                                Box(
                                    modifier = Modifier.clickable { focusedParamIndex = param.index }
                                ) {
                                    StudioKnob(
                                        name = param.name,
                                        value = currentValue,
                                        min = param.min,
                                        max = param.max,
                                        onValueChange = { newValue ->
                                            focusedParamIndex = param.index
                                            onParamChange(param.index, newValue)
                                        },
                                        accentColor = if (focusedParamIndex == param.index) StudioPalette.Accent else spec.bodyDeep
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Switches & Discrete Controls
        if (switchParams.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioPalette.Surface)
                    .border(1.dp, StudioPalette.Border, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                switchParams.forEach { param ->
                    when (param) {
                        is KnobParam -> Unit
                        is SwitchParam -> {
                            val currentVal = slot.params.getOrElse(param.index) { param.defaultValue }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = param.name.uppercase(),
                                    color = StudioPalette.TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    param.options.forEach { opt ->
                                        val isSelected = currentVal.toInt() == opt.id
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isSelected) StudioPalette.Accent else StudioPalette.SurfaceRaised)
                                                .clickable { onParamChange(param.index, opt.id.toFloat()) }
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = opt.name,
                                                color = if (isSelected) Color.Black else StudioPalette.TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        is ComboBoxParam -> {
                            val currentVal = slot.params.getOrElse(param.index) { param.defaultValue }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = param.name.uppercase(),
                                    color = StudioPalette.TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )

                                if (param.options.size <= 5) {
                                    // Segmented interactive pill buttons (e.g. C-Chorus Modes 1, 2, 3, 4)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        param.options.forEach { opt ->
                                            val isSelected = currentVal.toInt() == opt.id
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (isSelected) StudioPalette.Accent else StudioPalette.SurfaceRaised)
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) StudioPalette.Accent else StudioPalette.Border,
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .clickable { onParamChange(param.index, opt.id.toFloat()) }
                                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                            ) {
                                                Text(
                                                    text = opt.name,
                                                    color = if (isSelected) Color.Black else StudioPalette.TextPrimary,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Dropdown Menu for larger option lists (scales, pitch offsets, etc.)
                                    var expanded by remember { mutableStateOf(false) }
                                    val activeOption = param.options.find { it.id == currentVal.toInt() }?.name
                                        ?: param.options.firstOrNull()?.name ?: "Option"

                                    Box {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(StudioPalette.SurfaceRaised)
                                                .border(1.dp, StudioPalette.Border, RoundedCornerShape(4.dp))
                                                .clickable { expanded = true }
                                                .padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = activeOption,
                                                color = StudioPalette.Accent,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Selecionar",
                                                tint = StudioPalette.TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false },
                                            modifier = Modifier.background(StudioPalette.SurfaceRaised)
                                        ) {
                                            param.options.forEach { opt ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = opt.name,
                                                            color = if (opt.id == currentVal.toInt()) StudioPalette.Accent else StudioPalette.TextPrimary,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 12.sp,
                                                            fontWeight = if (opt.id == currentVal.toInt()) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    },
                                                    onClick = {
                                                        onParamChange(param.index, opt.id.toFloat())
                                                        expanded = false
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
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Focus Rail (Slider de Precisão para o Knob Selecionado)
        if (focusedKnob != null) {
            val focusVal = slot.params.getOrElse(focusedKnob.index) { focusedKnob.defaultValue }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioPalette.SurfaceRaised)
                    .border(1.dp, StudioPalette.Border, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FOCO: ${focusedKnob.name.uppercase()}",
                        color = StudioPalette.TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (focusedKnob.max <= 10f) String.format("%.1f", focusVal) else focusVal.toInt().toString(),
                        color = StudioPalette.Accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Slider(
                    value = focusVal.coerceIn(focusedKnob.min, focusedKnob.max),
                    onValueChange = { onParamChange(focusedKnob.index, it) },
                    valueRange = focusedKnob.min..focusedKnob.max,
                    colors = SliderDefaults.colors(
                        thumbColor = StudioPalette.Accent,
                        activeTrackColor = StudioPalette.Accent,
                        inactiveTrackColor = StudioPalette.KnobTrack
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Mechanical Footswitch Stomp
        StudioStompSwitch(
            enabled = slot.enabled,
            onToggle = onToggle,
            ledColor = spec.led
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Effect Picker Modal
    EffectPickerSheet(
        isOpen = showEffectPicker,
        module = module,
        currentEffectId = slot.effectId,
        onSelectEffect = { newEffectId ->
            onChangeEffect(newEffectId)
            showEffectPicker = false
        },
        onDismiss = { showEffectPicker = false }
    )
}
