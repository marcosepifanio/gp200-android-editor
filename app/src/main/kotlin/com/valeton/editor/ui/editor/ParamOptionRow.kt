package com.valeton.editor.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.core.model.OptionItem
import com.valeton.editor.ui.theme.StudioPalette

/** Whether the label and every option fit on one line of [maxWidth], each at its natural width. */
internal fun optionsFitInline(labelWidth: Int, optionWidths: List<Int>, optionGap: Int, labelGap: Int, maxWidth: Int): Boolean {
    val options = optionWidths.sum() + optionGap * (optionWidths.size - 1).coerceAtLeast(0)
    return labelWidth + labelGap + options <= maxWidth
}

/**
 * A parameter name with its option buttons. Name and buttons share one line when they fit; otherwise the
 * name sits above and the buttons wrap below, so no button is ever squeezed (BL-05).
 */
@Composable
internal fun ParamOptionRow(
    name: String,
    options: List<OptionItem>,
    selectedId: Int,
    onSelect: (Int) -> Unit,
    bordered: Boolean,
    modifier: Modifier = Modifier
) {
    Layout(
        modifier = modifier,
        content = {
            Text(
                text = name,
                color = StudioPalette.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            options.forEach { opt ->
                val isSelected = selectedId == opt.id
                val shape = RoundedCornerShape(4.dp)
                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(if (isSelected) StudioPalette.Accent else StudioPalette.SurfaceRaised)
                        .then(
                            if (bordered) Modifier.border(1.dp, if (isSelected) StudioPalette.Accent else StudioPalette.Border, shape)
                            else Modifier
                        )
                        .clickable { onSelect(opt.id) }
                        .padding(horizontal = 10.dp, vertical = if (bordered) 5.dp else 4.dp)
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
    ) { measurables, constraints ->
        val optionGap = 6.dp.roundToPx()
        val labelGap = 12.dp.roundToPx()
        val rowGap = 8.dp.roundToPx()
        val free = Constraints()
        val label = measurables.first().measure(free)
        val chips = measurables.drop(1).map { it.measure(free) }
        val natural = label.width + labelGap + chips.sumOf { it.width } + optionGap * (chips.size - 1).coerceAtLeast(0)
        val maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else natural
        val inline = optionsFitInline(label.width, chips.map { it.width }, optionGap, labelGap, maxWidth)

        if (inline) {
            val height = maxOf(label.height, chips.maxOfOrNull { it.height } ?: 0)
            layout(maxWidth, height) {
                label.placeRelative(0, (height - label.height) / 2)
                var x = maxWidth - (chips.sumOf { it.width } + optionGap * (chips.size - 1).coerceAtLeast(0))
                chips.forEach { chip ->
                    chip.placeRelative(x, (height - chip.height) / 2)
                    x += chip.width + optionGap
                }
            }
        } else {
            // Wrap the buttons into lines under the name; a button wider than the line keeps its width.
            val lines = mutableListOf<MutableList<Int>>()
            var lineWidth = 0
            chips.forEachIndexed { i, chip ->
                val needed = if (lines.isEmpty() || lines.last().isEmpty()) chip.width else lineWidth + optionGap + chip.width
                if (lines.isEmpty() || needed > maxWidth && lines.last().isNotEmpty()) {
                    lines += mutableListOf(i)
                    lineWidth = chip.width
                } else {
                    lines.last() += i
                    lineWidth = needed
                }
            }
            val lineHeights = lines.map { line -> line.maxOf { chips[it].height } }
            val height = label.height + rowGap + lineHeights.sum() + optionGap * (lines.size - 1).coerceAtLeast(0)
            layout(maxWidth, height) {
                label.placeRelative(0, 0)
                var y = label.height + rowGap
                lines.forEachIndexed { li, line ->
                    var x = 0
                    line.forEach { i ->
                        chips[i].placeRelative(x, y + (lineHeights[li] - chips[i].height) / 2)
                        x += chips[i].width + optionGap
                    }
                    y += lineHeights[li] + optionGap
                }
            }
        }
    }
}
