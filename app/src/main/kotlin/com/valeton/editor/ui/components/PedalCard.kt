package com.valeton.editor.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.ui.theme.AmberAccent
import com.valeton.editor.ui.theme.BorderDark
import com.valeton.editor.ui.theme.SurfaceDark
import com.valeton.editor.ui.theme.TextPrimary
import com.valeton.editor.ui.theme.TextSecondary

@Composable
fun PedalCard(
    slot: EffectSlot,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    val effectDef = EffectCatalog.getEffectById(slot.effectId)
    val moduleType = effectDef?.module ?: EffectCatalog.getModuleName(slot.effectId)
    val moduleColor = try {
        Color(android.graphics.Color.parseColor(moduleType.colorHex))
    } catch (_: Exception) {
        AmberAccent
    }

    val cardBorderColor = if (isSelected) AmberAccent else BorderDark

    Box(
        modifier = modifier
            .width(88.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceDark)
            .border(if (isSelected) 2.dp else 1.dp, cardBorderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Module badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(moduleColor.copy(alpha = if (slot.enabled) 0.85f else 0.35f))
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = moduleType.code,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Effect Name
            Text(
                text = effectDef?.name ?: "Bypass",
                color = if (slot.enabled) TextPrimary else TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            // Stomp switch with bypass LED
            StompSwitch(
                enabled = slot.enabled,
                onToggle = onToggle
            )
        }
    }
}
