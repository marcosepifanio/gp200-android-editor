package com.valeton.editor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.ui.theme.StudioPalette

enum class StudioTab(val label: String, val icon: ImageVector) {
    CHAIN("CADEIA", Icons.Default.GraphicEq),
    PATCHES("PRESETS", Icons.AutoMirrored.Filled.List),
    DRUMS("RITMOS", Icons.Default.MusicNote),
    CONTROLS("CONTROLES", Icons.Default.Tune),
    ABOUT("SOBRE", Icons.Default.Info)
}

/**
 * The largest label size, from [base] down to [min] in [step]s, whose width fits in [available]. When even
 * [min] does not fit, returns [min] and the label falls back to an ellipsis (BL-09).
 */
internal fun fittingLabelSize(
    widthAt: (Float) -> Float,
    available: Float,
    base: Float = 10f,
    min: Float = 8f,
    step: Float = 0.5f
): Float {
    var size = base
    while (size > min) {
        if (widthAt(size) <= available) return size
        size -= step
    }
    return min
}

@Composable
fun StudioTabBar(
    activeTab: StudioTab,
    onTabSelected: (StudioTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(StudioPalette.ChromeBg)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val measurer = rememberTextMeasurer()
        StudioTab.entries.forEach { tab ->
            val isActive = tab == activeTab
            val color = if (isActive) StudioPalette.Accent else StudioPalette.TextMuted

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = StudioPalette.Accent),
                        onClick = { onTabSelected(tab) }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                BoxWithConstraints(contentAlignment = Alignment.Center) {
                    val style = TextStyle(
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    val available = constraints.maxWidth.toFloat()
                    val size = fittingLabelSize(
                        widthAt = { sp ->
                            measurer.measure(tab.label, style.copy(fontSize = sp.sp), maxLines = 1, softWrap = false)
                                .size.width.toFloat()
                        },
                        available = available
                    )
                    Text(
                        text = tab.label,
                        color = color,
                        style = style,
                        fontSize = size.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
