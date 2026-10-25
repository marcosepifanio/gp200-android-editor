package com.valeton.editor.ui.chain

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.EffectDefinition
import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.ModuleType
import com.valeton.editor.ui.components.PedalArtwork
import com.valeton.editor.ui.theme.StudioPalette
import com.valeton.editor.ui.theme.getModuleSpec

@Composable
fun StudioPedalCard(
    slot: EffectSlot,
    position: Int,
    chainLength: Int,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val effectDef: EffectDefinition? = EffectCatalog.getEffectById(slot.effectId)
    val module = ModuleType.fromSlotIndex(slot.slotIndex)
    val spec = getModuleSpec(module.code)

    val cardBg = Brush.horizontalGradient(
        colors = listOf(StudioPalette.SurfaceRaised, StudioPalette.Surface)
    )

    val textColor = if (slot.enabled) StudioPalette.TextPrimary else StudioPalette.TextSecondary
    var showMoveMenu by remember(slot.slotIndex) { mutableStateOf(false) }
    val ledColor by animateColorAsState(
        targetValue = if (slot.enabled) spec.led else StudioPalette.LedOff,
        animationSpec = tween(150),
        label = "pedalLed"
    )

    val summary = summarizeParams(effectDef, slot.params)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(cardBg)
            .border(
                width = 1.dp,
                color = if (slot.enabled) spec.bodyDeep.copy(alpha = 0.8f) else StudioPalette.Border,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Grip / Reorder Column
        Box {
            IconButton(
                onClick = { showMoveMenu = true },
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${position + 1}", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Icon(Icons.Default.MoreVert, contentDescription = "Reordenar efeito", tint = StudioPalette.TextMuted, modifier = Modifier.size(16.dp))
                }
            }
            DropdownMenu(expanded = showMoveMenu, onDismissRequest = { showMoveMenu = false }, containerColor = StudioPalette.SurfaceElevated) {
                DropdownMenuItem(
                    text = { Text("Mover para cima") },
                    leadingIcon = { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null) },
                    enabled = onMoveUp != null,
                    onClick = { showMoveMenu = false; onMoveUp?.invoke() }
                )
                DropdownMenuItem(
                    text = { Text("Mover para baixo") },
                    leadingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) },
                    enabled = onMoveDown != null,
                    onClick = { showMoveMenu = false; onMoveDown?.invoke() }
                )
            }
        }

        // 2. Letterboxed Thumbnail Artwork
        Box(
            modifier = Modifier
                .size(48.dp, 36.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.15f))
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            PedalArtwork(
                svgFile = effectDef?.svgFile ?: "",
                name = effectDef?.name ?: module.code,
                accentColor = spec.bodyDeep,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // 3. Information Column: Name, Module Chip, and Parameter Summary
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = effectDef?.name ?: "Bypass",
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Module Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(spec.led.copy(alpha = 0.14f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = module.code,
                        color = spec.led,
                fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (summary.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = summary,
                        color = textColor.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 4. Status LED
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(ledColor.copy(alpha = if (slot.enabled) 0.35f else 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(ledColor)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 5. Bypass Switch
        Switch(
            checked = slot.enabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = StudioPalette.Accent,
                checkedTrackColor = StudioPalette.AccentGlow,
                uncheckedThumbColor = StudioPalette.TextMuted,
                uncheckedTrackColor = StudioPalette.BorderSubtle
            ),
            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        )
    }
}

internal fun summarizeParams(effectDef: EffectDefinition?, params: List<Float>): String {
    if (effectDef == null || effectDef.params.isEmpty()) return ""
    val primary = effectDef.params.take(2)
    return primary.map { p ->
        val v = params.getOrElse(p.index) { p.defaultValue }
        "${p.name} ${v.toInt()}"
    }.joinToString(" · ")
}
