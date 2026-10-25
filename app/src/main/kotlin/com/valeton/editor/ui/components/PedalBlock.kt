package com.valeton.editor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.ModuleType
import com.valeton.editor.ui.theme.*

@Composable
fun PedalBlock(
    slot: EffectSlot,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggleBypass: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectDef = EffectCatalog.getEffectById(slot.effectId)
    val moduleType = ModuleType.fromSlotIndex(slot.slotIndex).takeIf { it != ModuleType.UNKNOWN }
        ?: effectDef?.module
        ?: EffectCatalog.getModuleName(slot.effectId)

    val baseColor = when (moduleType) {
        ModuleType.NR -> ModuleNrColor
        ModuleType.PRE -> ModulePreColor
        ModuleType.DST -> ModuleDstColor
        ModuleType.AMP -> ModuleAmpColor
        ModuleType.CAB -> ModuleCabColor
        ModuleType.EQ -> ModuleEqColor
        ModuleType.MOD -> ModuleModColor
        ModuleType.DLY -> ModuleDlyColor
        ModuleType.RVB -> ModuleRvbColor
        ModuleType.WAH -> ModuleWahColor
        ModuleType.VOL -> ModuleVolColor
        ModuleType.UNKNOWN -> ModuleNrColor
    }

    val blockColor = if (slot.enabled) baseColor else Color(0xFF2B303A)
    val codeText = moduleType.code

    Box(
        modifier = modifier
            .width(58.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(blockColor)
            .then(
                if (isSelected) {
                    Modifier.border(2.5.dp, ValetonBlue, RoundedCornerShape(10.dp))
                } else {
                    Modifier.border(1.dp, BorderDark, RoundedCornerShape(10.dp))
                }
            )
            .clickable { onClick() }
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Graphic Area (Pedal/Amp/EQ Canvas illustration)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                PedalGraphic(
                    moduleType = moduleType,
                    enabled = slot.enabled,
                    modifier = Modifier.fillMaxSize()
                )

                // Red Prohibition Slash for Bypassed State
                if (!slot.enabled) {
                    BypassOverlay(modifier = Modifier.size(34.dp))
                }
            }

            // Bottom Module Code Label
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = codeText,
                    color = if (slot.enabled) Color.White else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun BypassOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f * 0.85f
        val center = Offset(size.width / 2f, size.height / 2f)
        val strokeWidth = 3.dp.toPx()

        // Red outer circle
        drawCircle(
            color = BypassRed,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // Diagonal slash through circle
        val offset = (radius * 0.7071f) // cos(45 deg)
        drawLine(
            color = BypassRed,
            start = Offset(center.x - offset, center.y - offset),
            end = Offset(center.x + offset, center.y + offset),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
private fun PedalGraphic(
    moduleType: ModuleType,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val elementColor = if (enabled) Color.White else Color(0xFF6B7280)

    Canvas(modifier = modifier) {
        when (moduleType) {
            ModuleType.EQ -> drawEqSliders(elementColor)
            ModuleType.AMP -> drawAmpHead(elementColor)
            ModuleType.CAB -> drawSpeakerCabinet(elementColor)
            ModuleType.DST, ModuleType.MOD, ModuleType.DLY, ModuleType.RVB -> drawMultiKnobs(elementColor)
            ModuleType.PRE, ModuleType.NR, ModuleType.WAH, ModuleType.VOL, ModuleType.UNKNOWN -> drawSingleKnob(elementColor)
        }
    }
}

private fun DrawScope.drawEqSliders(color: Color) {
    val sliderCount = 5
    val spacing = size.width / (sliderCount + 1)
    val sliderWidth = 1.5.dp.toPx()
    val notchWidth = 5.dp.toPx()
    val notchHeight = 2.dp.toPx()

    val heights = listOf(0.4f, 0.7f, 0.3f, 0.8f, 0.5f)

    for (i in 0 until sliderCount) {
        val x = spacing * (i + 1)
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = Offset(x, size.height * 0.15f),
            end = Offset(x, size.height * 0.85f),
            strokeWidth = sliderWidth
        )

        val notchY = size.height * (0.85f - heights[i] * 0.7f)
        drawRoundRect(
            color = color,
            topLeft = Offset(x - notchWidth / 2f, notchY - notchHeight / 2f),
            size = Size(notchWidth, notchHeight),
            cornerRadius = CornerRadius(1.dp.toPx())
        )
    }
}

private fun DrawScope.drawAmpHead(color: Color) {
    val w = size.width
    val h = size.height

    // Amp head body
    drawRoundRect(
        color = color.copy(alpha = 0.4f),
        topLeft = Offset(w * 0.1f, h * 0.2f),
        size = Size(w * 0.8f, h * 0.65f),
        cornerRadius = CornerRadius(2.dp.toPx())
    )

    // Grill line
    drawLine(
        color = color,
        start = Offset(w * 0.15f, h * 0.6f),
        end = Offset(w * 0.85f, h * 0.6f),
        strokeWidth = 2.dp.toPx()
    )

    // Mini knobs on amp panel
    val knobY = h * 0.38f
    for (i in 1..4) {
        drawCircle(
            color = color,
            radius = 2.dp.toPx(),
            center = Offset(w * (0.15f + i * 0.14f), knobY)
        )
    }
}

private fun DrawScope.drawSpeakerCabinet(color: Color) {
    val w = size.width
    val h = size.height

    // Outer cab box
    drawRoundRect(
        color = color.copy(alpha = 0.3f),
        topLeft = Offset(w * 0.15f, h * 0.15f),
        size = Size(w * 0.7f, h * 0.7f),
        cornerRadius = CornerRadius(3.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
    )

    // Speaker cone circle
    drawCircle(
        color = color.copy(alpha = 0.7f),
        radius = h * 0.25f,
        center = Offset(w * 0.5f, h * 0.5f),
        style = Stroke(width = 1.5.dp.toPx())
    )
    drawCircle(
        color = color,
        radius = h * 0.08f,
        center = Offset(w * 0.5f, h * 0.5f)
    )
}

private fun DrawScope.drawMultiKnobs(color: Color) {
    val w = size.width
    val h = size.height
    val r = 3.5.dp.toPx()

    // 3 stomp knobs in triangle formation
    drawCircle(color = color, radius = r, center = Offset(w * 0.32f, h * 0.35f))
    drawCircle(color = color, radius = r, center = Offset(w * 0.68f, h * 0.35f))
    drawCircle(color = color, radius = r, center = Offset(w * 0.5f, h * 0.68f))
}

private fun DrawScope.drawSingleKnob(color: Color) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.3f

    drawCircle(color = Color.Black.copy(alpha = 0.4f), radius = r, center = center)
    drawCircle(color = color, radius = r, center = center, style = Stroke(width = 2.dp.toPx()))

    // Pointer line
    drawLine(
        color = color,
        start = center,
        end = Offset(center.x, center.y - r),
        strokeWidth = 2.dp.toPx()
    )
}
