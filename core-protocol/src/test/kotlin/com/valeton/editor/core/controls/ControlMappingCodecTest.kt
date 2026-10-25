package com.valeton.editor.core.controls

import com.valeton.editor.core.codec.ControlMappingCodec
import com.valeton.editor.core.codec.PRSTDecoder
import com.valeton.editor.core.codec.PRSTEncoder
import com.valeton.editor.core.codec.SysExCodec
import com.valeton.editor.core.model.QuickKnobAssignment
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ControlMappingCodecTest {
    private fun fixture() = javaClass.getResourceAsStream("/fixtures/clean.prst")!!.readBytes()

    @Test
    fun `fixture assignments are decoded from PRST and live payload`() {
        val bytes = fixture()
        val file = PRSTDecoder(bytes).decode().controlMappings!!
        val live = SysExCodec.parsePresetFromDecoded(bytes.copyOfRange(0x28, 0x4c0)).controlMappings
        assertEquals(file, live)
        assertEquals(QuickKnobAssignment(1, 1), file.knobs[0])
        assertEquals(QuickKnobAssignment(11, 0), file.knobs[2])
        assertEquals(0, file.ctrls[0].flags)
        assertEquals(0x82a8, file.ctrls[0].reserved)
        assertEquals(0x1f286804L, file.ctrls[0].targetMask)
    }

    @Test
    fun `changing CTRL1 replaces low effect bits while preserving other fields`() {
        val bytes = fixture()
        // Controlled opaque fields and mask, independent of the fixture's saved CTRL state.
        bytes[0x465] = 7
        bytes[0x466] = 0x34
        bytes[0x467] = 0x12
        byteArrayOf(5, 0, 0, 0x80.toByte()).copyInto(bytes, 0x468)
        val controls = PRSTDecoder(bytes).decode().controlMappings!!
        val changed = controls.withCtrl1Block(8)
        assertEquals(0x80000100L, changed.ctrls[0].targetMask)
        assertEquals(controls.ctrls.drop(1), changed.ctrls.drop(1))
        assertEquals(7, changed.ctrls[0].flags)
        assertEquals(0x1234, changed.ctrls[0].reserved)
        assertEquals(0x80000000L, changed.withCtrl1Block(null).ctrls[0].targetMask)
    }

    @Test
    fun `edited assignments roundtrip without changing unrelated PRST bytes`() {
        val original = PRSTDecoder(fixture()).decode()
        val controls = original.controlMappings!!
        val changed = controls.copy(knobs = controls.knobs.toMutableList().apply { set(1, QuickKnobAssignment(3, 2)) }).withCtrl1Block(9)
        val before = PRSTEncoder().encode(original)
        val after = PRSTEncoder().encode(original.copy(controlMappings = changed))
        assertEquals(changed, PRSTDecoder(after).decode().controlMappings)
        for (index in before.indices) {
            if (index !in 0x450..0x457 && index !in 0x460..0x46b && index !in 0x4c6..0x4c7) {
                assertEquals(before[index], after[index], "Unrelated byte $index changed")
            }
        }
    }

    @Test
    fun `missing or malformed mapping records stay unknown`() {
        assertNull(ControlMappingCodec.decode(ByteArray(119)))
        val records = fixture().copyOfRange(0x448, 0x4c0)
        records[0] = 0
        assertNull(ControlMappingCodec.decode(records))
        assertNull(SysExCodec.parsePresetFromDecoded(ByteArray(912)).controlMappings)
    }
    @Test
    fun `quick write includes active edit context and exact assignment record`() {
        val frame = SysExCodec.buildQuickKnobAssignment(1, QuickKnobAssignment(3, 2))
        assertEquals(46, frame.size)
        assertArrayEquals(byteArrayOf(0, 0, 4, 0, 0, 0, 0, 0, 16, 0, 4, 0, 1, 3, 2, 0),
            SysExCodec.nibbleDecode(frame.copyOfRange(13, frame.lastIndex)))
        assertEquals(0x12.toByte(), frame[8])
        assertEquals(16.toByte(), frame[9])
        assertEquals(0xf7.toByte(), frame.last())
    }

    @Test
    fun `CTRL write retains opaque fields and all mask bits`() {
        val ctrl = com.valeton.editor.core.model.CtrlAssignment(7, 0x1234, 0x80000100L)
        val frame = SysExCodec.buildCtrlAssignment(0, ctrl)
        assertEquals(54, frame.size)
        assertArrayEquals(byteArrayOf(0, 0, 4, 0, 0, 0, 0, 0, 15, 0, 8, 0, 0, 7, 0x34, 0x12, 0, 1, 0, 0x80.toByte()),
            SysExCodec.nibbleDecode(frame.copyOfRange(13, frame.lastIndex)))
    }

    @Test
    fun `hardware CTRL metadata may normalize without changing assignments`() {
        val preset = PRSTDecoder(fixture()).decode()
        val expected = preset.controlMappings!!.copy(ctrls = preset.controlMappings!!.ctrls.map {
            it.copy(flags = 0, reserved = 0xb7c7, targetMask = 0)
        })
        val readback = expected.copy(ctrls = expected.ctrls.map { it.copy(reserved = 0xb86c) })
        assertTrue(expected.matchesDevice(readback), "Captured CTRL padding changes must not report a failed save")
        assertFalse(expected.matchesDevice(readback.withCtrl1Block(3)))
        val activeExpected = expected.withCtrl1Block(1)
        val activeReadback = activeExpected.copy(ctrls = activeExpected.ctrls.toMutableList().apply {
            set(0, get(0).copy(flags = 1))
        })
        assertTrue(activeExpected.matchesDevice(activeReadback))
    }

    @Test
    fun `inactive CTRL records accept hardware normalized opaque flags`() {
        val expected = com.valeton.editor.core.model.ControlMappings(
            knobs = List(3) { QuickKnobAssignment.OFF },
            ctrls = List(8) { index ->
                com.valeton.editor.core.model.CtrlAssignment(
                    flags = if (index < 4) 0 else 0xaa + index,
                    reserved = 0,
                    targetMask = if (index == 0) 1 else 0
                )
            }
        )
        val readback = expected.copy(ctrls = expected.ctrls.map { it.copy(flags = 0, reserved = 0x9999) })

        assertTrue(expected.matchesDevice(readback))
        assertFalse(expected.matchesDevice(readback.copy(ctrls = readback.ctrls.toMutableList().apply {
            set(0, get(0).copy(targetMask = 2))
        })))
    }

}
