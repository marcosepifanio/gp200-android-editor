package com.valeton.editor.core.model

/** Physical quick-access destinations. Unknown values are retained when reading a patch. */
data class QuickKnobAssignment(val blockIndex: Int, val paramIndex: Int) {
    init {
        require(blockIndex in 0..255)
        require(paramIndex in 0..65535)
    }
    companion object {
        val OFF = QuickKnobAssignment(255, 255)
        val PATCH_VOLUME = QuickKnobAssignment(11, 255)
        val PATCH_TEMPO = QuickKnobAssignment(12, 255)
    }
}

/** Preserve opaque fields and mask bits; only bits 0..10 identify effect targets. */
data class CtrlAssignment(val flags: Int, val reserved: Int, val targetMask: Long) {
    init {
        require(flags in 0..255 && reserved in 0..65535 && targetMask in 0..0xffffffffL)
    }
    val effectBlocks: List<Int> get() = (0..10).filter { targetMask and (1L shl it) != 0L }

    // The hardware rewrites opaque metadata; only the target mask is stable across readbacks.
    fun matchesDevice(actual: CtrlAssignment): Boolean = targetMask == actual.targetMask
}

data class ControlMappings(val knobs: List<QuickKnobAssignment>, val ctrls: List<CtrlAssignment>) {
    init { require(knobs.size == 3 && ctrls.size == 8) }

    fun withKnob(index: Int, assignment: QuickKnobAssignment): ControlMappings {
        require(index in 0..2)
        return copy(knobs = knobs.mapIndexed { i, old -> if (i == index) assignment else old })
    }

    fun withCtrl1Block(blockIndex: Int?): ControlMappings {
        require(blockIndex == null || blockIndex in 0..10)
        val old = ctrls[0]
        val mask = (old.targetMask and 0xfffff800L) or (blockIndex?.let { 1L shl it } ?: 0L)
        return copy(ctrls = listOf(old.copy(targetMask = mask)) + ctrls.drop(1))
    }

    fun withCtrlTarget(ctrlIndex: Int, blockIndex: Int, enabled: Boolean): ControlMappings {
        require(ctrlIndex in 0..3)
        require(blockIndex in 0..10)
        val bit = 1L shl blockIndex
        val ctrl = ctrls[ctrlIndex]
        val targetMask = if (enabled) ctrl.targetMask or bit else ctrl.targetMask and bit.inv()
        return copy(ctrls = ctrls.mapIndexed { index, assignment ->
            if (index == ctrlIndex) assignment.copy(targetMask = targetMask) else assignment
        })
    }

    fun matchesDevice(actual: ControlMappings): Boolean = ctrls.zip(actual.ctrls).all { (expected, received) -> expected.matchesDevice(received) } && knobs.zip(actual.knobs).all { (expected, received) ->
        expected.blockIndex == received.blockIndex &&
            (expected.blockIndex in listOf(11, 12, 255) || expected.paramIndex == received.paramIndex)
    }
}
