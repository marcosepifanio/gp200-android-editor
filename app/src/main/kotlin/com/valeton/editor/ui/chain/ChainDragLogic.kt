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
