package com.valeton.editor.ui.chain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ChainDragLogicTest {

    private val items = listOf(
        ChainDragLogic.ItemBounds(key = 0, start = 0, end = 100),
        ChainDragLogic.ItemBounds(key = 1, start = 112, end = 212),
        ChainDragLogic.ItemBounds(key = 2, start = 224, end = 360)
    )

    @Test
    fun `drop target is the other item under the dragged card center`() {
        assertEquals(2, ChainDragLogic.dropTarget(draggedKey = 0, center = 300f, items = items))
    }

    @Test
    fun `no drop target while the center is over the dragged card itself`() {
        assertNull(ChainDragLogic.dropTarget(draggedKey = 1, center = 150f, items = items))
    }

    @Test
    fun `no drop target in the gap between cards`() {
        assertNull(ChainDragLogic.dropTarget(draggedKey = 0, center = 106f, items = items))
    }

    @Test
    fun `move puts the dragged key at the target position in one step`() {
        val order = listOf("PRE", "WAH", "DST", "AMP", "DLY")
        assertEquals(
            listOf("PRE", "DLY", "WAH", "DST", "AMP"),
            ChainDragLogic.move(order, fromIndex = 4, toIndex = 1)
        )
    }

    @Test
    fun `move ignores indices outside the list`() {
        val order = listOf("PRE", "WAH")
        assertEquals(order, ChainDragLogic.move(order, fromIndex = 0, toIndex = 5))
    }

    @Test
    fun `edge scroll is zero away from the edges and grows toward them`() {
        assertEquals(0f, ChainDragLogic.edgeScroll(itemStart = 300f, itemEnd = 400f, viewportStart = 0, viewportEnd = 1000, threshold = 100f, maxSpeed = 20f))
        assertEquals(-20f, ChainDragLogic.edgeScroll(itemStart = 0f, itemEnd = 100f, viewportStart = 0, viewportEnd = 1000, threshold = 100f, maxSpeed = 20f))
        assertEquals(10f, ChainDragLogic.edgeScroll(itemStart = 850f, itemEnd = 950f, viewportStart = 0, viewportEnd = 1000, threshold = 100f, maxSpeed = 20f))
    }
}
