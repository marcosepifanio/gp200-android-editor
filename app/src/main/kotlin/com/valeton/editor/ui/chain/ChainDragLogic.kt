package com.valeton.editor.ui.chain

/** Pure rules for dragging pedal cards in the chain; the composable feeds them LazyColumn layout info. */
internal object ChainDragLogic {

    data class ItemBounds(val key: Any, val start: Int, val end: Int)

    /** The card whose bounds contain the dragged card's center, other than the dragged card itself. */
    fun dropTarget(draggedKey: Any, center: Float, items: List<ItemBounds>): Any? =
        items.firstOrNull { it.key != draggedKey && center >= it.start && center <= it.end }?.key

    fun <T> move(order: List<T>, fromIndex: Int, toIndex: Int): List<T> {
        if (fromIndex !in order.indices || toIndex !in order.indices || fromIndex == toIndex) return order
        return order.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
    }

    /**
     * Keeps the dragged card inside the pedal area: between the top of the first card and the bottom of
     * the last one when they are on screen, and always inside the viewport.
     */
    fun clampTop(top: Float, size: Int, areaStart: Int?, areaEnd: Int?, viewportStart: Int, viewportEnd: Int): Float {
        val min = maxOf(viewportStart, areaStart ?: viewportStart).toFloat()
        val max = (minOf(viewportEnd, areaEnd ?: viewportEnd) - size).toFloat()
        return if (max < min) min else top.coerceIn(min, max)
    }

    /** Scroll step while a dragged card sits within [threshold] of the viewport edge; negative scrolls up. */
    fun edgeScroll(
        itemStart: Float,
        itemEnd: Float,
        viewportStart: Int,
        viewportEnd: Int,
        threshold: Float,
        maxSpeed: Float
    ): Float {
        val intoTop = viewportStart + threshold - itemStart
        val intoBottom = itemEnd - (viewportEnd - threshold)
        return when {
            intoTop > 0f -> -maxSpeed * (intoTop / threshold).coerceAtMost(1f)
            intoBottom > 0f -> maxSpeed * (intoBottom / threshold).coerceAtMost(1f)
            else -> 0f
        }
    }
}
