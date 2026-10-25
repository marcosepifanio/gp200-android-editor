package com.valeton.editor.core.codec

import com.valeton.editor.core.model.ControlMappings
import com.valeton.editor.core.model.CtrlAssignment
import com.valeton.editor.core.model.QuickKnobAssignment

/** Layout verified against GP-200.exe and the PRST fixtures. See docs/protocol/control-mapping.md. */
object ControlMappingCodec {
    const val PRST_OFFSET = 0x448
    const val LIVE_OFFSET = 0x420
    const val SIZE = 120

    fun decode(bytes: ByteArray, offset: Int = 0): ControlMappings? {
        if (offset < 0 || offset > bytes.size - SIZE) return null
        fun u8(index: Int) = bytes[offset + index].toInt() and 255
        fun u16(index: Int) = u8(index) or (u8(index + 1) shl 8)
        val knobs = (0..2).map { index ->
            val base = index * 8
            if (u16(base) != 0x10 || u16(base + 2) != 4 || u8(base + 4) != index) return null
            QuickKnobAssignment(u8(base + 5), u16(base + 6))
        }
        val ctrls = (0..7).map { index ->
            val base = 24 + index * 12
            if (u16(base) != 0xf || u16(base + 2) != 8 || u8(base + 4) != index) return null
            val mask = (0..3).fold(0L) { value, byte -> value or (u8(base + 8 + byte).toLong() shl (byte * 8)) }
            CtrlAssignment(u8(base + 5), u16(base + 6), mask)
        }
        return ControlMappings(knobs, ctrls)
    }

    fun knobRecord(index: Int, assignment: QuickKnobAssignment): ByteArray {
        require(index in 0..2)
        return byteArrayOf(0x10, 0, 4, 0, index.toByte(), assignment.blockIndex.toByte(),
            assignment.paramIndex.toByte(), (assignment.paramIndex shr 8).toByte())
    }

    fun ctrlRecord(index: Int, assignment: CtrlAssignment): ByteArray {
        require(index in 0..7)
        return byteArrayOf(0xf, 0, 8, 0, index.toByte(), assignment.flags.toByte(),
            assignment.reserved.toByte(), (assignment.reserved shr 8).toByte()) +
            ByteArray(4) { (assignment.targetMask shr (it * 8)).toByte() }
    }

    fun encode(mappings: ControlMappings): ByteArray =
        mappings.knobs.flatMapIndexed { index, knob -> knobRecord(index, knob).toList() }.toByteArray() +
            mappings.ctrls.flatMapIndexed { index, ctrl -> ctrlRecord(index, ctrl).toList() }.toByteArray()
}
