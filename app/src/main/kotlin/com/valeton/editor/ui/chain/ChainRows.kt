package com.valeton.editor.ui.chain

/** One line of the chain list: a pedal card or an FX loop marker sitting in a gap between cards. */
internal sealed interface ChainRow {
    data class Pedal(val index: Int) : ChainRow
    data object FxSend : ChainRow
    data object FxReturn : ChainRow
}

internal object ChainRows {
    /**
     * Lays the FX loop markers into the chain. A position is the number of blocks before the marker,
     * and the signal runs top to bottom, so in a shared gap the Send comes before the Return.
     */
    fun build(chainSize: Int, fxLoopSend: Int, fxLoopReturn: Int): List<ChainRow> = buildList {
        for (gap in 0..chainSize) {
            if (fxLoopSend == gap) add(ChainRow.FxSend)
            if (fxLoopReturn == gap) add(ChainRow.FxReturn)
            if (gap < chainSize) add(ChainRow.Pedal(gap))
        }
    }
}
