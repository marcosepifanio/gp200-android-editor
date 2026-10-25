package com.valeton.editor.ui.chain

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.ui.components.PedalBlock
import com.valeton.editor.ui.theme.BackgroundDark

@Composable
fun SignalChainScreen(
    preset: GP200Preset,
    selectedSlotIndex: Int,
    onSelectSlot: (Int) -> Unit,
    onToggleSlot: (Int) -> Unit,
    onMoveSlot: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sort effect slots according to the pedal's routing order
    val orderedSlots: List<EffectSlot> = preset.routingOrder.mapNotNull { slotIdx ->
        preset.effects.find { it.slotIndex == slotIdx }
    }

    val row1Count = if (orderedSlots.size > 5) 5 else orderedSlots.size
    val row1Slots = orderedSlots.take(row1Count)
    val row2Slots = orderedSlots.drop(row1Count)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row1Slots.forEachIndexed { index, slot ->
                    PedalBlock(
                        slot = slot,
                        isSelected = slot.slotIndex == selectedSlotIndex,
                        onClick = { onSelectSlot(slot.slotIndex) },
                        onToggleBypass = { onToggleSlot(slot.slotIndex) }
                    )

                    // Draw cable connector pin between blocks
                    if (index < row1Slots.size - 1) {
                        CableConnector(modifier = Modifier.weight(1f).height(12.dp))
                    }
                }
            }

            // Row 2 (if exists)
            if (row2Slots.isNotEmpty()) {
                // Loop cable running from right to left
                ConnectingLoopCable(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row2Slots.forEachIndexed { index, slot ->
                        PedalBlock(
                            slot = slot,
                            isSelected = slot.slotIndex == selectedSlotIndex,
                            onClick = { onSelectSlot(slot.slotIndex) },
                            onToggleBypass = { onToggleSlot(slot.slotIndex) }
                        )

                        // Draw cable connector pin between blocks
                        if (index < row2Slots.size - 1) {
                            CableConnector(modifier = Modifier.weight(1f).height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CableConnector(modifier: Modifier = Modifier) {
    val cableColor = Color(0xFF3E4756)
    val pinColor = Color(0xFF6B7280)

    Canvas(modifier = modifier) {
        val y = size.height / 2f
        val w = size.width

        // Cable wire
        drawLine(
            color = cableColor,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 2.dp.toPx()
        )

        // Small jack pin square in the middle
        if (w >= 12.dp.toPx()) {
            val pinSize = 3.dp.toPx()
            drawRect(
                color = pinColor,
                topLeft = Offset(w / 2f - pinSize / 2f, y - pinSize / 2f),
                size = androidx.compose.ui.geometry.Size(pinSize, pinSize)
            )
        }
    }
}

@Composable
private fun ConnectingLoopCable(modifier: Modifier = Modifier) {
    val cableColor = Color(0xFF3E4756)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            // Loop from top right, curve down, go left, and curve down to bottom left
            moveTo(w - 18.dp.toPx(), 0f)
            lineTo(w - 10.dp.toPx(), 0f)
            cubicTo(
                w - 2.dp.toPx(), 0f,
                w - 2.dp.toPx(), h * 0.5f,
                w - 10.dp.toPx(), h * 0.5f
            )
            lineTo(18.dp.toPx(), h * 0.5f)
            cubicTo(
                6.dp.toPx(), h * 0.5f,
                6.dp.toPx(), h,
                14.dp.toPx(), h
            )
        }

        drawPath(
            path = path,
            color = cableColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
