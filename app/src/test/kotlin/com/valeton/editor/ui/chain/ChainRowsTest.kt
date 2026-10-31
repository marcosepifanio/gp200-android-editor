package com.valeton.editor.ui.chain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ChainRowsTest {
    private fun pedals(range: IntRange) = range.map { ChainRow.Pedal(it) }

    @Test
    fun `send comes before return when both share a gap`() {
        assertEquals(
            pedals(0..3) + ChainRow.FxSend + ChainRow.FxReturn + pedals(4..10),
            ChainRows.build(chainSize = 11, fxLoopSend = 4, fxLoopReturn = 4)
        )
    }

    @Test
    fun `send and return in different gaps wrap the blocks inside the loop`() {
        assertEquals(
            pedals(0..1) + ChainRow.FxSend + pedals(2..6) + ChainRow.FxReturn + pedals(7..10),
            ChainRows.build(chainSize = 11, fxLoopSend = 2, fxLoopReturn = 7)
        )
    }

    @Test
    fun `a position after the last block is still drawn`() {
        assertEquals(
            pedals(0..9) + ChainRow.FxSend + ChainRow.Pedal(10) + ChainRow.FxReturn,
            ChainRows.build(chainSize = 11, fxLoopSend = 10, fxLoopReturn = 11)
        )
    }
}
