package com.valeton.editor.core.codec

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SysExCodecTest {

    @Test
    fun `nibbleEncode and nibbleDecode roundtrip`() {
        val original = byteArrayOf(0x00, 0x7F, 0xFF.toByte(), 0x42, 0xAB.toByte())
        val encoded = SysExCodec.nibbleEncode(original)
        assertEquals(original.size * 2, encoded.size)
        val decoded = SysExCodec.nibbleDecode(encoded)
        assertArrayEquals(original, decoded)
    }

    @Test
    fun `slot to label conversion works accurately`() {
        assertEquals("1A", SysExCodec.slotToLabel(0))
        assertEquals("1B", SysExCodec.slotToLabel(1))
        assertEquals("1D", SysExCodec.slotToLabel(3))
        assertEquals("2A", SysExCodec.slotToLabel(4))
        assertEquals("64D", SysExCodec.slotToLabel(255))

        assertEquals(0, SysExCodec.labelToSlot("1A"))
        assertEquals(1, SysExCodec.labelToSlot("1B"))
        assertEquals(255, SysExCodec.labelToSlot("64D"))
    }

    @Test
    fun `buildReadRequest returns 46 bytes with correct framing`() {
        val req = SysExCodec.buildReadRequest(0)
        assertEquals(46, req.size)
        assertEquals(0xF0.toByte(), req[0])
        assertEquals(0x11.toByte(), req[8])
        assertEquals(0x10.toByte(), req[9])
        assertEquals(0xF7.toByte(), req[45])
    }

    @Test
    fun `buildNameReadRequest returns 46 bytes with sub 0x20`() {
        val req = SysExCodec.buildNameReadRequest(0)
        assertEquals(46, req.size)
        assertEquals(0x20.toByte(), req[9])
    }

    @Test
    fun `buildToggle returns 46 bytes with correct block index and state`() {
        val toggleOn = SysExCodec.buildToggle(3, true)
        assertEquals(46, toggleOn.size)
        assertEquals(0x12.toByte(), toggleOn[8])
        assertEquals(0x10.toByte(), toggleOn[9])
        assertEquals(3.toByte(), toggleOn[38])
        assertEquals(1.toByte(), toggleOn[40])
        assertEquals(0xF7.toByte(), toggleOn[45])

        val toggleOff = SysExCodec.buildToggle(3, false)
        assertEquals(0.toByte(), toggleOff[40])
    }

    @Test
    fun `buildParamChange returns 62 bytes with logarithmic display value`() {
        val paramMsg = SysExCodec.buildParamChange(2, 0, 117440565, 50.0f)
        assertEquals(62, paramMsg.size)
        assertEquals(0xF0.toByte(), paramMsg[0])
        assertEquals(0x12.toByte(), paramMsg[8])
        assertEquals(0x18.toByte(), paramMsg[9])
        assertEquals(0xF7.toByte(), paramMsg[61])
    }

    @Test
    fun `buildReorder returns 78 bytes with 11 blocks`() {
        val order = listOf(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val reorderMsg = SysExCodec.buildReorder(order, send = 4, ret = 4)
        assertEquals(78, reorderMsg.size)
        assertEquals(0x12.toByte(), reorderMsg[8])
        assertEquals(0x20.toByte(), reorderMsg[9])
        assertEquals(0xF7.toByte(), reorderMsg[77])
    }

    @Test
    fun `buildSaveCommit returns 62 bytes`() {
        val commitMsg = SysExCodec.buildSaveCommit("MyRockPatch", 1) // 1B (sub-slot 1)
        assertEquals(62, commitMsg.size)
        assertEquals(0x12.toByte(), commitMsg[8])
        assertEquals(0x18.toByte(), commitMsg[9])
        assertEquals(0xF7.toByte(), commitMsg[61])
    }

    @Test
    fun `buildSaveCommit encodes absolute slot number correctly for slots outside bank 1`() {
        val slot26A = 100 // 26A is (26 - 1) * 4 + 0 = 100 (0x64)
        val commitMsg = SysExCodec.buildSaveCommit("Patch 26A", slot26A)
        assertEquals(62, commitMsg.size)
        val nibbles = commitMsg.copyOfRange(13, 61)
        val decoded = SysExCodec.nibbleDecode(nibbles)
        assertEquals(100, decoded[4].toInt() and 0xFF)
    }

    @Test
    fun `buildEnterEditorMode returns 14 bytes with 0x11 0x12`() {
        val msg = SysExCodec.buildEnterEditorMode()
        assertEquals(14, msg.size)
        assertEquals(0xF0.toByte(), msg[0])
        assertEquals(0x11.toByte(), msg[8])
        assertEquals(0x12.toByte(), msg[9])
        assertEquals(0xF7.toByte(), msg[13])
    }

    @Test
    fun `buildStateDumpRequest returns 22 bytes with 0x11 0x04`() {
        val msg = SysExCodec.buildStateDumpRequest()
        assertEquals(22, msg.size)
        assertEquals(0xF0.toByte(), msg[0])
        assertEquals(0x11.toByte(), msg[8])
        assertEquals(0x04.toByte(), msg[9])
        assertEquals(0xF7.toByte(), msg[21])
    }

    @Test
    fun `isSysEx correctly identifies header and cmd sub`() {
        val enterMsg = SysExCodec.buildEnterEditorMode()
        assertTrue(SysExCodec.isSysEx(enterMsg, 0x11, 0x12))
        assertFalse(SysExCodec.isSysEx(enterMsg, 0x12, 0x10))
        assertFalse(SysExCodec.isSysEx(byteArrayOf(0x00, 0x01), 0x11, 0x12))
    }

    @Test
    fun `parseKnobNotification extracts block param and float32 value`() {
        // Construct a knob notification frame: 0x12 0x10, 46 bytes, [29..36]=0
        val msg = ByteArray(46) { 0 }
        msg[0] = 0xF0.toByte()
        msg[1] = 0x21; msg[2] = 0x25; msg[3] = 0x7E
        msg[4] = 0x47; msg[5] = 0x50; msg[6] = 0x2D; msg[7] = 0x32
        msg[8] = 0x12; msg[9] = 0x10
        msg[22] = 3 // Block AMP
        msg[24] = 1 // Param Gain
        // Float 50.0f is 0x42480000 -> bytes in LE: 0x00, 0x00, 0x48, 0x42
        // Nibbles: 0x0, 0x0, 0x0, 0x0, 0x4, 0x8, 0x4, 0x2
        msg[37] = 0x0; msg[38] = 0x0
        msg[39] = 0x0; msg[40] = 0x0
        msg[41] = 0x4; msg[42] = 0x8
        msg[43] = 0x4; msg[44] = 0x2
        msg[45] = 0xF7.toByte()

        val knob = SysExCodec.parseKnobNotification(msg)
        assertNotNull(knob)
        assertEquals(3, knob!!.first)
        assertEquals(1, knob.second)
        assertEquals(50.0f, knob.third, 0.001f)
    }

    @Test
    fun `parseToggleNotification extracts block and enabled state`() {
        val toggleMsg = SysExCodec.buildToggle(blockIndex = 2, enabled = true)
        val toggle = SysExCodec.parseToggleNotification(toggleMsg)
        assertNotNull(toggle)
        assertEquals(2, toggle!!.first)
        assertTrue(toggle.second)
    }

    @Test
    fun `parseSlotChangeNotification extracts active slot`() {
        // CMD=0x12, sub=0x08, data[14]=0x08, slot at [25:26]
        val msg = ByteArray(30) { 0 }
        msg[0] = 0xF0.toByte()
        msg[1] = 0x21; msg[2] = 0x25; msg[3] = 0x7E
        msg[4] = 0x47; msg[5] = 0x50; msg[6] = 0x2D; msg[7] = 0x32
        msg[8] = 0x12; msg[9] = 0x08
        msg[14] = 0x08
        msg[25] = 0x01 // Slot 0x1A (26) -> high nibble 1
        msg[26] = 0x0A // low nibble 10
        msg[29] = 0xF7.toByte()

        val slot = SysExCodec.parseSlotChangeNotification(msg)
        assertNotNull(slot)
        assertEquals(26, slot)
    }

    @Test
    fun `parseStateDumpSlot parses active slot correctly`() {
        val nibbles = byteArrayOf(
            0x00, 0x07, 0x01, 0x00, 0x04, 0x08, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x06, 0x00, 0x01, 0x00, 0x00, 0x00, 0x0D, 0x00, 0x00
        )
        val header = byteArrayOf(
            0xF0.toByte(), 0x21, 0x25, 0x7E, 0x47, 0x50, 0x2D, 0x32, 0x12, 0x4E,
            0x06, 0x00, 0x00 // byte[10]=0x06, offset=0
        )
        val chunk = header + nibbles + byteArrayOf(0xF7.toByte())
        val slot = SysExCodec.parseStateDumpSlot(listOf(chunk))
        assertEquals(13, slot)
    }

    @Test
    fun `buildPresetChange returns 30 bytes with slot nibbles`() {
        val msg = SysExCodec.buildPresetChange(26) // 0x1A -> sh=1, sl=10
        assertEquals(30, msg.size)
        assertEquals(0xF0.toByte(), msg[0])
        assertEquals(0x12.toByte(), msg[8])
        assertEquals(0x08.toByte(), msg[9])
        assertEquals(1.toByte(), msg[25])
        assertEquals(10.toByte(), msg[26])
        assertEquals(0xF7.toByte(), msg[29])
    }

    @Test
    fun `buildDrumParam returns 30 bytes with 0x1001 command and nibble encoded payload`() {
        // Test BPM 120 (target = 2, value = 120 = 0x0078)
        val msgBpm = SysExCodec.buildDrumBpm(120)
        assertEquals(30, msgBpm.size)
        // Standard header
        assertEquals(0xF0.toByte(), msgBpm[0])
        assertEquals(0x12.toByte(), msgBpm[8]) // CMD = SET
        assertEquals(0x08.toByte(), msgBpm[9]) // Length = 8
        assertEquals(0x00.toByte(), msgBpm[10])
        assertEquals(0x00.toByte(), msgBpm[11]) // Offset = 0
        assertEquals(0x00.toByte(), msgBpm[12])
        // Nibbles 13..28 decode to 8 bytes payload
        val nibbles = msgBpm.copyOfRange(13, 29)
        val decoded = SysExCodec.nibbleDecode(nibbles)
        assertEquals(8, decoded.size)
        assertEquals(0x01.toByte(), decoded[0])
        assertEquals(0x10.toByte(), decoded[1]) // 0x1001 LE (Drum Parameter Update)
        assertEquals(0x04.toByte(), decoded[2])
        assertEquals(0x00.toByte(), decoded[3]) // 0x0004 LE
        assertEquals(0x02.toByte(), decoded[4]) // Target = 2 (BPM)
        assertEquals(0x00.toByte(), decoded[5])
        assertEquals(0x78.toByte(), decoded[6]) // Value = 120 (0x78)
        assertEquals(0x00.toByte(), decoded[7])
        assertEquals(0xF7.toByte(), msgBpm[29])

        // Test Volume 85 (target = 1, value = 85 = 0x55)
        val msgVol = SysExCodec.buildDrumVolume(85)
        val decVol = SysExCodec.nibbleDecode(msgVol.copyOfRange(13, 29))
        assertEquals(0x01.toByte(), decVol[4]) // Target = 1 (Volume)
        assertEquals(85.toByte(), decVol[6])

        // Test Pattern 42 (target = 0, value = 42 = 0x2A)
        val msgPat = SysExCodec.buildDrumPattern(42)
        val decPat = SysExCodec.nibbleDecode(msgPat.copyOfRange(13, 29))
        assertEquals(0x00.toByte(), decPat[4]) // Target = 0 (Pattern)
        assertEquals(42.toByte(), decPat[6])

        // Test Sync ON (0x1011 LE, target = 2, value = 1)
        val msgSyncOn = SysExCodec.buildDrumSync(true)
        val decSyncOn = SysExCodec.nibbleDecode(msgSyncOn.copyOfRange(13, 29))
        assertEquals(0x11.toByte(), decSyncOn[0])
        assertEquals(0x10.toByte(), decSyncOn[1]) // 0x1011 LE
        assertEquals(0x02.toByte(), decSyncOn[4]) // Target = 2 (Sync)
        assertEquals(0x01.toByte(), decSyncOn[6]) // Value = 1

        // Test Sync OFF (0x1011 LE, target = 2, value = 0)
        val msgSyncOff = SysExCodec.buildDrumSync(false)
        val decSyncOff = SysExCodec.nibbleDecode(msgSyncOff.copyOfRange(13, 29))
        assertEquals(0x11.toByte(), decSyncOff[0])
        assertEquals(0x10.toByte(), decSyncOff[1])
        assertEquals(0x02.toByte(), decSyncOff[4]) // Target = 2 (Sync)
        assertEquals(0x00.toByte(), decSyncOff[6]) // Value = 0

        // Test Patch Setting Param (0x1008 LE, target = 0x19, value = 1)
        val msgPatchSync = SysExCodec.buildPatchSettingParam(target = 0x19, value = 1)
        val decPatchSync = SysExCodec.nibbleDecode(msgPatchSync.copyOfRange(13, 29))
        assertEquals(0x08.toByte(), decPatchSync[0])
        assertEquals(0x10.toByte(), decPatchSync[1]) // 0x1008 LE
        assertEquals(0x19.toByte(), decPatchSync[4]) // Target = 0x19 (25)
        assertEquals(0x01.toByte(), decPatchSync[6]) // Value = 1
    }

    @Test
    fun `buildDrumPlayStop returns 30 bytes with 0x1011 command and play flag`() {
        val playMsg = SysExCodec.buildDrumPlayStop(true)
        assertEquals(30, playMsg.size)
        val decPlay = SysExCodec.nibbleDecode(playMsg.copyOfRange(13, 29))
        assertEquals(0x11.toByte(), decPlay[0])
        assertEquals(0x10.toByte(), decPlay[1]) // 0x1011 LE
        assertEquals(0x01.toByte(), decPlay[6]) // value = 1 (Play)

        val stopMsg = SysExCodec.buildDrumPlayStop(false)
        val decStop = SysExCodec.nibbleDecode(stopMsg.copyOfRange(13, 29))
        assertEquals(0x11.toByte(), decStop[0])
        assertEquals(0x10.toByte(), decStop[1])
        assertEquals(0x00.toByte(), decStop[6]) // value = 0 (Stop)
    }

    @Test
    fun `buildPatchSetting properly encodes target 0x19 for drum sync`() {
        // Target 0x19 (25) = Drum Sync in patch setting
        val syncOn = SysExCodec.buildPatchSetting(target = 0x19, value = 1)
        assertEquals(46, syncOn.size)
        val dec = SysExCodec.nibbleDecode(syncOn.copyOfRange(13, 45))
        assertEquals(16, dec.size)
        assertEquals(0x19.toByte(), dec[12]) // Target = 0x19
        assertEquals(0x00.toByte(), dec[13])
        assertEquals(0x01.toByte(), dec[14]) // Value = 1
        assertEquals(0x00.toByte(), dec[15])
    }

    @Test
    fun `buildProgramChange generates Bank Select CC0 and Program Change for Bank 1 and Bank 2`() {
        // Slot 0 (Bank 01-A): CC0=0, PC=0
        val msgs0 = SysExCodec.buildProgramChange(0, channel = 0)
        assertEquals(2, msgs0.size)
        // CC 0: status 0xB0, controller 0, value 0
        assertArrayEquals(byteArrayOf(0xB0.toByte(), 0x00, 0x00), msgs0[0])
        // PC: status 0xC0, program 0
        assertArrayEquals(byteArrayOf(0xC0.toByte(), 0x00), msgs0[1])

        // Slot 61 (Bank 16-B): CC0=0, PC=61
        val msgs61 = SysExCodec.buildProgramChange(61, channel = 0)
        assertArrayEquals(byteArrayOf(0xB0.toByte(), 0x00, 0x00), msgs61[0])
        assertArrayEquals(byteArrayOf(0xC0.toByte(), 61.toByte()), msgs61[1])

        // Slot 127 (Bank 32-D): CC0=0, PC=127
        val msgs127 = SysExCodec.buildProgramChange(127, channel = 0)
        assertArrayEquals(byteArrayOf(0xB0.toByte(), 0x00, 0x00), msgs127[0])
        assertArrayEquals(byteArrayOf(0xC0.toByte(), 127.toByte()), msgs127[1])

        // Slot 128 (Bank 33-A): CC0=1, PC=0
        val msgs128 = SysExCodec.buildProgramChange(128, channel = 0)
        assertArrayEquals(byteArrayOf(0xB0.toByte(), 0x00, 0x01), msgs128[0])
        assertArrayEquals(byteArrayOf(0xC0.toByte(), 0x00), msgs128[1])

        // Slot 255 (Bank 64-D): CC0=1, PC=127
        val msgs255 = SysExCodec.buildProgramChange(255, channel = 0)
        assertArrayEquals(byteArrayOf(0xB0.toByte(), 0x00, 0x01), msgs255[0])
        assertArrayEquals(byteArrayOf(0xC0.toByte(), 127.toByte()), msgs255[1])
    }

    @Test
    fun `buildPatchName encodes address 0x0B60 and ascii string`() {
        val msg = SysExCodec.buildPatchName("Solo Lead")
        assertTrue(msg.size >= 14)
        assertEquals(0xF0.toByte(), msg[0])
        assertEquals(0x12.toByte(), msg[8])
        assertEquals(0xF7.toByte(), msg[msg.size - 1])

        // Decode nibbles back
        val nibbles = msg.copyOfRange(13, msg.size - 1)
        val decoded = SysExCodec.nibbleDecode(nibbles)
        assertEquals(32, decoded.size)
        assertEquals(0x09.toByte(), decoded[8]) // string field
        assertEquals(0x60.toByte(), decoded[14]) // 0x0B60 address
        assertEquals(0x0B.toByte(), decoded[15])
        val nameString = String(decoded, 16, 9, java.nio.charset.StandardCharsets.US_ASCII)
        assertEquals("Solo Lead", nameString)
    }

    @Test
    fun `buildAuthorName encodes address 0x0B70 and ascii string`() {
        val msg = SysExCodec.buildAuthorName("Hendrix")
        assertEquals(78, msg.size)
        assertEquals(0xF0.toByte(), msg[0])
        assertEquals(0x12.toByte(), msg[8])
        assertEquals(0x20.toByte(), msg[9])
        assertEquals(0xF7.toByte(), msg[msg.size - 1])

        val nibbles = msg.copyOfRange(13, msg.size - 1)
        val decoded = SysExCodec.nibbleDecode(nibbles)
        assertEquals(32, decoded.size)
        assertEquals(0x04.toByte(), decoded[2])
        assertEquals(0x01.toByte(), decoded[6])
        assertEquals(0x09.toByte(), decoded[8]) // author msg type
        assertEquals(0x70.toByte(), decoded[14]) // 0x0B70 address
        assertEquals(0x0B.toByte(), decoded[15])
        val authorString = String(decoded, 16, 7, java.nio.charset.StandardCharsets.US_ASCII)
        assertEquals("Hendrix", authorString)
    }

    @Test
    fun `buildEffectChange produces 54 bytes with exact protocol constants and encoding`() {
        // Block 3 (AMP), effectId = 0x07000055
        val msg = SysExCodec.buildEffectChange(blockIndex = 3, effectId = 0x07000055)
        assertEquals(54, msg.size)
        assertEquals(0xF0.toByte(), msg[0])
        assertEquals(0x12.toByte(), msg[8]) // CMD=SET
        assertEquals(0x14.toByte(), msg[9]) // sub=EFFECT_CHANGE
        assertEquals(0x01.toByte(), msg[29])
        assertEquals(0x06.toByte(), msg[30]) // Must be 0x06, NOT 0x05 (which was toggle!)
        assertEquals(0x08.toByte(), msg[34]) // Must be 0x08, NOT 0x04
        assertEquals(3.toByte(), msg[38])    // blockIndex
        assertEquals(0.toByte(), msg[39])
        assertEquals(0.toByte(), msg[40])
        assertEquals(0x07.toByte(), msg[41]) // Must be 0x07, NOT 0x09
        assertEquals(0x06.toByte(), msg[42]) // Must be 0x06, NOT 0x0C
        assertEquals(0x00.toByte(), msg[43])
        assertEquals(0x02.toByte(), msg[44])
        // Variant = 0x55 -> vh = 0x05, vl = 0x05
        assertEquals(0x05.toByte(), msg[45])
        assertEquals(0x05.toByte(), msg[46])
        // Module type at byte 52 -> 0x07
        assertEquals(0x07.toByte(), msg[52])
        assertEquals(0xF7.toByte(), msg[53])
    }

    @Test
    fun `buildEffectChange encodes variant and module correctly for various blocks`() {
        // DST (module 0x03, variant 0x0A): AC Boost on block 2
        val dstMsg = SysExCodec.buildEffectChange(blockIndex = 2, effectId = 0x0300000A)
        assertEquals(54, dstMsg.size)
        assertEquals(2.toByte(), dstMsg[38])
        assertEquals(0x00.toByte(), dstMsg[45]) // high nibble of 0x0A
        assertEquals(0x0A.toByte(), dstMsg[46]) // low nibble of 0x0A
        assertEquals(0x03.toByte(), dstMsg[52]) // module type DST

        // CAB (module 0x0A, variant 0x7C): block 5
        val cabMsg = SysExCodec.buildEffectChange(blockIndex = 5, effectId = 0x0A00007C)
        assertEquals(54, cabMsg.size)
        assertEquals(5.toByte(), cabMsg[38])
        assertEquals(0x07.toByte(), cabMsg[45]) // high nibble of 0x7C
        assertEquals(0x0C.toByte(), cabMsg[46]) // low nibble of 0x7C
        assertEquals(0x0A.toByte(), cabMsg[52]) // module type CAB
    }

    @Test
    fun `buildControlChange returns standard 3-byte MIDI message`() {
        val volMsg = SysExCodec.buildControlChange(cc = 7, value = 80, channel = 0)
        assertEquals(3, volMsg.size)
        assertEquals(0xB0.toByte(), volMsg[0])
        assertEquals(7.toByte(), volMsg[1])
        assertEquals(80.toByte(), volMsg[2])

        val ctrl1Msg = SysExCodec.buildControlChange(cc = 69, value = 127, channel = 0)
        assertEquals(3, ctrl1Msg.size)
        assertEquals(0xB0.toByte(), ctrl1Msg[0])
        assertEquals(69.toByte(), ctrl1Msg[1])
        assertEquals(127.toByte(), ctrl1Msg[2])
    }

    @Test
    fun `buildPatchSetting generates 46-byte SysEx for Volume and Pan`() {
        // Volume: target 0x00, value 75 (0x4B)
        val volSysEx = SysExCodec.buildPatchSetting(target = 0x00, value = 75)
        assertEquals(46, volSysEx.size)
        assertEquals(0xF0.toByte(), volSysEx[0])
        assertEquals(0x12.toByte(), volSysEx[8])
        assertEquals(0x10.toByte(), volSysEx[9])
        assertEquals(0x00.toByte(), volSysEx[38]) // target = 0 (VOL)
        assertEquals(4.toByte(), volSysEx[41])    // 0x4B high nibble
        assertEquals(11.toByte(), volSysEx[42])   // 0x4B low nibble
        assertEquals(0xF7.toByte(), volSysEx[45])

        // Pan: target 0x06, value 20
        val panSysEx = SysExCodec.buildPatchSetting(target = 0x06, value = 20)
        assertEquals(46, panSysEx.size)
        assertEquals(0x06.toByte(), panSysEx[38]) // target = 6 (PAN)
        assertEquals(1.toByte(), panSysEx[41])    // 20 = 0x14
        assertEquals(4.toByte(), panSysEx[42])
    }

    @Test
    fun `buildTempoControlChange generates MSB and LSB pairs accurately`() {
        // 120 BPM: CC73 = 0, CC74 = 120
        val tempo120 = SysExCodec.buildTempoControlChange(120)
        assertEquals(2, tempo120.size)
        assertArrayEquals(byteArrayOf(0xB0.toByte(), 73, 0), tempo120[0])
        assertArrayEquals(byteArrayOf(0xB0.toByte(), 74, 120), tempo120[1])

        // 140 BPM: CC73 = 1, CC74 = 12 (140 - 128)
        val tempo140 = SysExCodec.buildTempoControlChange(140)
        assertEquals(2, tempo140.size)
        assertArrayEquals(byteArrayOf(0xB0.toByte(), 73, 1), tempo140[0])
        assertArrayEquals(byteArrayOf(0xB0.toByte(), 74, 12), tempo140[1])
    }

    @Test
    fun `buildTapTempoControlChange generates CC 75 message`() {
        val tapMsg = SysExCodec.buildTapTempoControlChange()
        assertArrayEquals(byteArrayOf(0xB0.toByte(), 75, 127), tapMsg)
    }

    @Test
    fun `parsePresetFromDecoded parses patchTempo from bytes 14 and 15`() {
        val decoded = ByteArray(912) { 0 }
        // Bytes 14..15 = 135 (0x0087)
        decoded[14] = 0x87.toByte()
        decoded[15] = 0x00.toByte()

        val preset = SysExCodec.parsePresetFromDecoded(decoded)
        assertEquals(135, preset.patchTempo)
    }
    private fun fullPresetChunks(): List<ByteArray> = (0 until 1176 step 185).map { offset ->
        val length = minOf(185, 1176 - offset)
        val decoded = ByteArray(1176)
        for (slot in 0..10) decoded[120 + slot * 72 + 4] = slot.toByte()
        val frame = SysExCodec.buildFrame(0x12, decoded.copyOfRange(offset, offset + length), chunkOffset = offset)
        frame[9] = 0x18
        frame
    }

    @Test
    fun `read chunks require complete contiguous payload`() {
        val chunks = fullPresetChunks()
        assertEquals(11, SysExCodec.parseReadChunks(chunks.reversed()).effects.size)
        for (missing in listOf(0, 3, 6)) {
            assertThrows(IllegalArgumentException::class.java) {
                SysExCodec.parseReadChunks(chunks.filterIndexed { i, _ -> i != missing })
            }
        }
    }

    @Test
    fun `read chunks reject duplicates and invalid nibbles`() {
        val chunks = fullPresetChunks()
        assertThrows(IllegalArgumentException::class.java) { SysExCodec.parseReadChunks(chunks + chunks[2]) }
        val corrupt = chunks.map { it.clone() }
        corrupt[2][13] = 16
        assertThrows(IllegalArgumentException::class.java) { SysExCodec.parseReadChunks(corrupt) }
    }

}
