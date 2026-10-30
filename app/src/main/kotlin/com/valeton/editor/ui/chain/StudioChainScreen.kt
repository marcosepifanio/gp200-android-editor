package com.valeton.editor.ui.chain

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.valeton.editor.BuildConfig
import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.ui.theme.StudioPalette
import kotlinx.coroutines.delay

@Composable
fun StudioChainScreen(
    preset: GP200Preset,
    onToggleSlot: (Int) -> Unit,
    onOpenSlot: (Int) -> Unit,
    onMoveSlot: (fromIndex: Int, toIndex: Int) -> Unit,
    onVolumeChange: (Int) -> Unit = {},
    onPanChange: (Int) -> Unit = {},
    onTempoChange: (Int) -> Unit = {},
    onTapTempo: () -> Unit = {},
    reorderEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val drag = remember { ChainDragState(listState) }
    val haptics = LocalHapticFeedback.current
    val currentOnMoveSlot by rememberUpdatedState(onMoveSlot)
    val currentEffects by rememberUpdatedState(preset.effects)
    val chain = drag.order ?: preset.effects

    val edgeThreshold = with(LocalDensity.current) { 64.dp.toPx() }
    LaunchedEffect(drag.draggingKey) {
        while (drag.draggingKey != null) {
            val step = drag.edgeScroll(edgeThreshold, maxSpeed = edgeThreshold / 4f)
            if (step != 0f) {
                listState.scrollBy(step)
                drag.updateTarget()
            }
            delay(16)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(StudioPalette.Case)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            // 1. Header Information
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CADEIA DE SINAL",
                        color = StudioPalette.TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Toque para editar",
                        color = StudioPalette.TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // 2. IN Terminal Marker
            item {
                TerminalMarker(label = "IN")
                CableLine()
            }

        // 3. Pedal Cards with Interconnecting Cables
        itemsIndexed(chain, key = { _, slot -> slot.slotIndex }) { index, slot ->
            val isDragging = drag.draggingKey == slot.slotIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = if (isDragging) {
                    Modifier
                        .zIndex(1f)
                        .graphicsLayer { translationY = drag.draggedOffset() }
                } else {
                    Modifier.animateItem()
                }
            ) {
                // FX Loop SEND Marker if positioned before this slot
                if (preset.fxLoopSend == index) {
                    FxLoopMarker(label = "↗ FX SEND")
                    CableLine()
                }

                StudioPedalCard(
                    slot = slot,
                    position = index,
                    chainLength = chain.size,
                    onToggle = { onToggleSlot(slot.slotIndex) },
                    onClick = { onOpenSlot(slot.slotIndex) },
                    onMoveUp = if (index > 0) { { onMoveSlot(index, index - 1) } } else null,
                    onMoveDown = if (index < chain.size - 1) { { onMoveSlot(index, index + 1) } } else null,
                    modifier = if (isDragging) Modifier.border(2.dp, StudioPalette.Accent, RoundedCornerShape(8.dp)) else Modifier,
                    dragHandle = if (!reorderEnabled) Modifier else Modifier.pointerInput(slot.slotIndex) {
                        detectDragGestures(
                            onDragStart = {
                                drag.start(slot.slotIndex, currentEffects)
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                drag.dragBy(amount.y)
                            },
                            onDragEnd = {
                                drag.finish()?.let { (from, to) ->
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    currentOnMoveSlot(from, to)
                                }
                            },
                            onDragCancel = { drag.finish() }
                        )
                    }
                )

                // FX Loop RETURN Marker if positioned after this slot
                if (preset.fxLoopReturn == index + 1) {
                    CableLine()
                    FxLoopMarker(label = "↙ FX RETURN")
                }

                // Patch Cable between pedal cards
                if (index < chain.size - 1) {
                    CableLine()
                }
            }
        }

        // 3. OUT Terminal Marker & Master Output (Volume & Pan)
        item {
            CableLine()
            TerminalMarker(label = "OUT")
            Spacer(modifier = Modifier.height(16.dp))

            StudioMasterBar(
                volume = preset.patchVolume,
                pan = preset.patchPan,
                tempo = preset.patchTempo,
                onVolumeChange = onVolumeChange,
                onPanChange = onPanChange,
                onTempoChange = onTempoChange,
                onTapTempo = onTapTempo
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Valeton GP-200 Editor v${BuildConfig.VERSION_NAME}",
                color = StudioPalette.TextMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    }
}

/**
 * Drag state for the chain. While a card is dragged only the local [order] changes; the view model
 * gets a single move when the finger lifts, so the pedal receives one reorder message.
 */
private class ChainDragState(private val listState: LazyListState) {
    var order by mutableStateOf<List<EffectSlot>?>(null)
        private set
    var draggingKey by mutableStateOf<Int?>(null)
        private set
    private var startIndex = -1
    private var initialOffset = 0
    private var dragDelta by mutableFloatStateOf(0f)

    private fun draggedItem() = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == draggingKey }

    fun start(key: Int, effects: List<EffectSlot>) {
        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key } ?: return
        order = effects
        draggingKey = key
        startIndex = effects.indexOfFirst { it.slotIndex == key }
        initialOffset = item.offset
        dragDelta = 0f
    }

    /** Translation that keeps the dragged card under the finger while the list re-lays out around it. */
    fun draggedOffset(): Float = draggedItem()?.let { initialOffset + dragDelta - it.offset } ?: 0f

    fun dragBy(amount: Float) {
        dragDelta += amount
        updateTarget()
    }

    fun updateTarget() {
        val key = draggingKey ?: return
        val item = draggedItem() ?: return
        val current = order ?: return
        val center = item.offset + draggedOffset() + item.size / 2f
        val cards = listState.layoutInfo.visibleItemsInfo
            .filter { it.key is Int }
            .map { ChainDragLogic.ItemBounds(it.key, it.offset, it.offset + it.size) }
        val target = ChainDragLogic.dropTarget(key, center, cards) ?: return
        order = ChainDragLogic.move(
            current,
            fromIndex = current.indexOfFirst { it.slotIndex == key },
            toIndex = current.indexOfFirst { it.slotIndex == target }
        )
    }

    fun edgeScroll(threshold: Float, maxSpeed: Float): Float {
        val item = draggedItem() ?: return 0f
        val start = item.offset + draggedOffset()
        val info = listState.layoutInfo
        return ChainDragLogic.edgeScroll(start, start + item.size, info.viewportStartOffset, info.viewportEndOffset, threshold, maxSpeed)
    }

    /** Ends the drag and returns the (from, to) chain move, or null when the card did not change place. */
    fun finish(): Pair<Int, Int>? {
        val key = draggingKey
        val endIndex = order?.indexOfFirst { it.slotIndex == key } ?: -1
        val move = if (key != null && startIndex >= 0 && endIndex >= 0 && endIndex != startIndex) startIndex to endIndex else null
        order = null
        draggingKey = null
        dragDelta = 0f
        return move
    }
}

@Composable
private fun TerminalMarker(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(StudioPalette.SurfaceRaised)
            .border(1.dp, StudioPalette.Border, RoundedCornerShape(4.dp))
            .padding(horizontal = 14.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = StudioPalette.TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun CableLine() {
    Box(
        modifier = Modifier
            .width(2.dp)
            .height(12.dp)
            .background(StudioPalette.BorderActive)
    )
}

@Composable
private fun FxLoopMarker(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(StudioPalette.SurfaceElevated)
            .border(1.dp, StudioPalette.AccentDim.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = StudioPalette.Accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
