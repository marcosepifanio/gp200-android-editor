package com.valeton.editor.core.codec

import com.valeton.editor.core.binary.BinaryParser
import com.valeton.editor.core.binary.BufferGenerator
import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.CtrlAssignment
import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.core.model.QuickKnobAssignment
import com.valeton.editor.core.model.UserModelKind
import com.valeton.editor.core.model.UserModelName
import java.nio.charset.StandardCharsets

object SysExCodec {

    private val SYSEX_HEADER = byteArrayOf(
        0xF0.toByte(), 0x21, 0x25, 0x7E, 0x47, 0x50, 0x2D, 0x32
    )

    fun nibbleDecode(data: ByteArray): ByteArray {
        val out = ByteArray(data.size / 2)
        for (i in out.indices) {
            val hi = data[2 * i].toInt() and 0x0F
            val lo = data[2 * i + 1].toInt() and 0x0F
            out[i] = ((hi shl 4) or lo).toByte()
        }
        return out
    }

    fun nibbleEncode(data: ByteArray): ByteArray {
        val out = ByteArray(data.size * 2)
        for (i in data.indices) {
            val b = data[i].toInt() and 0xFF
            out[2 * i] = ((b shr 4) and 0x0F).toByte()
            out[2 * i + 1] = (b and 0x0F).toByte()
        }
        return out
    }

    fun slotToLabel(slot: Int): String {
        val bank = (slot / 4) + 1
        val letter = "ABCD"[slot % 4]
        return "$bank$letter"
    }

    fun labelToSlot(label: String): Int {
        val match = Regex("""^(\d+)([ABCD])$""").find(label)
            ?: throw IllegalArgumentException("Invalid slot label: $label")
        val bank = match.groupValues[1].toInt()
        val letter = "ABCD".indexOf(match.groupValues[2])
        return (bank - 1) * 4 + letter
    }

    fun encodeDisplayValue(value: Float): Pair<Byte, Byte> {
        var u16 = 0
        if (value > 0f) {
            val log2Val = Math.log(value.toDouble()) / Math.log(2.0)
            u16 = Math.round(16367.0 + 16.0 * log2Val).toInt()
            if (u16 < 0) u16 = 0
            if (u16 > 0xFFFF) u16 = 0xFFFF
        }
        return Pair((u16 and 0xFF).toByte(), ((u16 shr 8) and 0xFF).toByte())
    }

    fun buildReadRequest(slot: Int): ByteArray {
        val sh = ((slot shr 4) and 0x0F).toByte()
        val sl = (slot and 0x0F).toByte()
        return byteArrayOf(
            0xF0.toByte(), 0x21, 0x25, 0x7E, 0x47, 0x50, 0x2D, 0x32,
            0x11, 0x10,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x04, 0x00, 0x00, 0x00,
            0x01, 0x00, 0x00,
            sh, sl,
            0x00, 0x00, 0x00,
            0x01, 0x00, 0x00, 0x00,
            0x04, 0x00, 0x00,
            sh, sl,
            0x00, 0x00,
            sh, sl,
            0x00, 0x00,
            0xF7.toByte()
        )
    }

    fun buildNameReadRequest(slot: Int): ByteArray {
        val msg = buildReadRequest(slot)
        msg[9] = 0x20
        return msg
    }

    fun buildPresetChange(slot: Int): ByteArray {
        val sh = ((slot shr 4) and 0x0F).toByte()
        val sl = (slot and 0x0F).toByte()
        return byteArrayOf(
            0xF0.toByte(), 0x21, 0x25, 0x7E, 0x47, 0x50, 0x2D, 0x32,
            0x12, 0x08,
            0x00, 0x00, 0x00, 0x00,
            0x08, 0x01,
            0x00, 0x00,
            0x04, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00,
            sh,
            sl,
            0x00, 0x00,
            0xF7.toByte()
        )
    }

    /**
     * Build Drum Machine Parameter SysEx frame (CMD=0x12, length=8 bytes).
     *
     * Wire protocol reverse-engineered from GP-200.exe:
     *   Command ID = 0x1001 LE (0x01, 0x10)
     *   Subcode    = 0x0004 LE (0x04, 0x00)
     *   Target     = uint16 LE (0 = Pattern, 1 = Volume, 2 = BPM, 3 = Sync)
     *   Value      = uint16 LE
     *
     * Transmitted as standard frame buildFrame(0x12, payload):
     *   Header: 8 bytes (F0 21 25 7E 47 50 2D 32)
     *   CMD: 0x12 (byte 8)
     *   Length: 0x08, 0x00 (bytes 9, 10 - 7-bit length)
     *   Offset: 0x00, 0x00 (bytes 11, 12 - 7-bit chunk offset)
     *   Payload: 16 nibbles (bytes 13..28)
     *   End: 0xF7 (byte 29)
     *   Total size: exactly 30 bytes!
     */
    fun buildDrumParam(target: Int, value: Int): ByteArray {
        val payload = ByteArray(8)
        payload[0] = 0x01
        payload[1] = 0x10 // 0x1001 LE (Drum Parameter Update)
        payload[2] = 0x04
        payload[3] = 0x00 // 0x0004 LE
        payload[4] = (target and 0xFF).toByte()
        payload[5] = ((target shr 8) and 0xFF).toByte()
        payload[6] = (value and 0xFF).toByte()
        payload[7] = ((value shr 8) and 0xFF).toByte()
        return buildFrame(0x12, payload)
    }

    fun buildDrumPattern(patternId: Int): ByteArray =
        buildDrumParam(target = 0, value = patternId.coerceIn(0, 99))

    fun buildDrumVolume(volume: Int): ByteArray =
        buildDrumParam(target = 1, value = volume.coerceIn(0, 100))

    fun buildDrumBpm(bpm: Int): ByteArray =
        buildDrumParam(target = 2, value = bpm.coerceIn(40, 250))

    /**
     * Build Drum Sync toggle SysEx frame (CMD=0x12, length=8 bytes).
     *
     * Wire protocol reverse-engineered from GP-200.exe:
     *   Command ID = 0x1011 LE (0x11, 0x10)
     *   Subcode    = 0x0004 LE (0x04, 0x00)
     *   Target     = uint16 LE: 0 = Play/Stop, 1 = Mute, 2 = Drum Sync
     *   Value      = uint16 LE: 1 = ON, 0 = OFF
     *
     * Dispatched to PresetManager slot 49 (0x1bc170) which updates patch offset 0x7E:
     *   patch[0x7E] = value (Drum Sync)
     */
    fun buildDrumSync(sync: Boolean): ByteArray {
        val payload = ByteArray(8)
        payload[0] = 0x11
        payload[1] = 0x10 // 0x1011 LE
        payload[2] = 0x04
        payload[3] = 0x00 // 0x0004 LE
        payload[4] = 0x02 // Target = 2 (Drum Sync)
        payload[5] = 0x00
        payload[6] = (if (sync) 0x01 else 0x00).toByte()
        payload[7] = 0x00
        return buildFrame(0x12, payload)
    }

    /**
     * Build Patch Setting Parameter SysEx frame (CMD=0x12, length=8 bytes).
     * Command ID = 0x1008 LE (0x08, 0x10)
     * Target 0x19 (25) = Drum Sync in patch
     */
    fun buildPatchSettingParam(target: Int, value: Int): ByteArray {
        val payload = ByteArray(8)
        payload[0] = 0x08
        payload[1] = 0x10 // 0x1008 LE
        payload[2] = 0x04
        payload[3] = 0x00 // 0x0004 LE
        payload[4] = (target and 0xFF).toByte()
        payload[5] = ((target shr 8) and 0xFF).toByte()
        payload[6] = (value and 0xFF).toByte()
        payload[7] = ((value shr 8) and 0xFF).toByte()
        return buildFrame(0x12, payload)
    }

    /**
     * Build Drum Play/Stop toggle SysEx frame (CMD=0x12, length=8 bytes).
     * Uses Command ID 0x1011 LE with target 0, value 1 (play) or 0 (stop).
     */
    fun buildDrumPlayStop(play: Boolean): ByteArray {
        val payload = ByteArray(8)
        payload[0] = 0x11
        payload[1] = 0x10 // 0x1011 LE
        payload[2] = 0x04
        payload[3] = 0x00
        payload[4] = 0x00
        payload[5] = 0x00
        payload[6] = (if (play) 0x01 else 0x00).toByte()
        payload[7] = 0x00
        return buildFrame(0x12, payload)
    }

    /**
     * Legacy buildDrumControl compatibility method.
     */
    fun buildDrumControl(mode: Int = 0x00, sync: Boolean, value: Int): ByteArray {
        return buildDrumBpm(value)
    }

    fun buildProgramChange(slot: Int, channel: Int = 0): List<ByteArray> {
        val clampedSlot = slot.coerceIn(0, 255)
        val bankMsb = clampedSlot / 128
        val programNumber = clampedSlot % 128
        val bankMsg = buildControlChange(cc = 0, value = bankMsb, channel = channel)
        val pcStatus = (0xC0 or (channel and 0x0F)).toByte()
        val pcMsg = byteArrayOf(pcStatus, (programNumber and 0x7F).toByte())
        return listOf(bankMsg, pcMsg)
    }

    fun createDefaultEffectSlots(): List<EffectSlot> = listOf(
        EffectSlot(slotIndex = 0, effectId = 0, enabled = false),
        EffectSlot(slotIndex = 1, effectId = 83886081, enabled = false),
        EffectSlot(slotIndex = 2, effectId = 50331648, enabled = false),
        EffectSlot(slotIndex = 3, effectId = 117440565, enabled = true),
        EffectSlot(slotIndex = 4, effectId = 27, enabled = true),
        EffectSlot(slotIndex = 5, effectId = 167772182, enabled = true),
        EffectSlot(slotIndex = 6, effectId = 16777269, enabled = false),
        EffectSlot(slotIndex = 7, effectId = 16777257, enabled = false),
        EffectSlot(slotIndex = 8, effectId = 184549376, enabled = false),
        EffectSlot(slotIndex = 9, effectId = 201326592, enabled = false),
        EffectSlot(slotIndex = 10, effectId = 100663299, enabled = true)
    )

    fun buildFrame(cmd: Int, decoded: ByteArray, chunkOffset: Int = 0): ByteArray {
        val nibbles = nibbleEncode(decoded)
        val msg = ByteArray(14 + nibbles.size)
        System.arraycopy(SYSEX_HEADER, 0, msg, 0, 8)
        msg[8] = (cmd and 0xFF).toByte()
        msg[9] = (decoded.size and 0x7F).toByte()
        msg[10] = ((decoded.size shr 7) and 0x7F).toByte()
        msg[11] = (chunkOffset and 0x7F).toByte()
        msg[12] = ((chunkOffset shr 7) and 0x7F).toByte()
        System.arraycopy(nibbles, 0, msg, 13, nibbles.size)
        msg[msg.size - 1] = 0xF7.toByte()
        return msg
    }

    fun buildQuickKnobAssignment(index: Int, assignment: QuickKnobAssignment): ByteArray =
        buildFrame(0x12, byteArrayOf(0, 0, 4, 0, 0, 0, 0, 0) + ControlMappingCodec.knobRecord(index, assignment))

    fun buildCtrlAssignment(index: Int, assignment: CtrlAssignment): ByteArray =
        buildFrame(0x12, byteArrayOf(0, 0, 4, 0, 0, 0, 0, 0) + ControlMappingCodec.ctrlRecord(index, assignment))

    /** Recognize mapping records before generic 0x12/0x10 stomp-toggle parsing. */
    fun isControlAssignmentMessage(msg: ByteArray): Boolean {
        if (msg.size !in listOf(46, 54) || msg[8] != 0x12.toByte() || msg.last() != 0xf7.toByte()) return false
        if (!isSysEx(msg, 0x12, if (msg.size == 46) 16 else 20)) return false
        val nibbles = msg.copyOfRange(13, msg.lastIndex)
        if (nibbles.any { (it.toInt() and 255) > 15 }) return false
        val payload = nibbleDecode(nibbles)
        if (!payload.copyOfRange(0, 4).contentEquals(byteArrayOf(0, 0, 4, 0))) return false
        val record = payload.copyOfRange(8, payload.size)
        val quick = record.size == 8 && record.copyOfRange(0, 4).contentEquals(byteArrayOf(16, 0, 4, 0)) && (record[4].toInt() and 255) in 0..2
        val ctrl = record.size == 12 && record.copyOfRange(0, 4).contentEquals(byteArrayOf(15, 0, 8, 0)) && (record[4].toInt() and 255) in 0..7
        return quick || ctrl
    }

    fun buildPatchName(name: String): ByteArray {
        val decoded = ByteArray(32)
        decoded[2] = 0x04
        decoded[6] = 0x01
        decoded[8] = 0x09 // msg type: string field
        decoded[10] = 0x14
        decoded[12] = 0x01
        decoded[14] = 0x60 // field address 0x0B60
        decoded[15] = 0x0B
        val bytes = name.toByteArray(StandardCharsets.US_ASCII)
        for (i in 0 until minOf(16, bytes.size)) {
            decoded[16 + i] = bytes[i]
        }
        return buildFrame(0x12, decoded)
    }

    fun buildAuthorName(author: String): ByteArray {
        val decoded = ByteArray(32)
        decoded[2] = 0x04
        decoded[6] = 0x01
        decoded[8] = 0x09 // msg type: author
        decoded[10] = 0x14
        decoded[12] = 0x01
        decoded[14] = 0x70 // field address 0x0B70
        decoded[15] = 0x0B
        val bytes = author.toByteArray(StandardCharsets.US_ASCII)
        for (i in 0 until minOf(16, bytes.size)) {
            decoded[16 + i] = bytes[i]
        }
        return buildFrame(0x12, decoded)
    }

    fun buildToggle(blockIndex: Int, enabled: Boolean): ByteArray {
        return byteArrayOf(
            0xF0.toByte(), 0x21, 0x25, 0x7E, 0x47, 0x50, 0x2D, 0x32,
            0x12, 0x10,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x04, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00,
            0x00, 0x00,
            0x00, 0x00,
            0x01, 0x05,
            0x00, 0x00, 0x00,
            0x04, 0x00, 0x00, 0x00,
            (blockIndex and 0x0F).toByte(),
            0x00,
            (if (enabled) 0x01 else 0x00).toByte(),
            0x09, 0x0C,
            0x00, 0x02,
            0xF7.toByte()
        )
    }

    /** Effect change record [16 00 08 00 block 00 76 02][effectId LE32]; all four id bytes matter (User IR is 0x0A10xxxx). */
    fun buildEffectChange(blockIndex: Int, effectId: Int): ByteArray {
        val payload = byteArrayOf(
            0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x16, 0x00, 0x08, 0x00, (blockIndex and 0x0F).toByte(), 0x00, 0x76, 0x02,
            (effectId and 0xFF).toByte(), ((effectId shr 8) and 0xFF).toByte(),
            ((effectId shr 16) and 0xFF).toByte(), ((effectId shr 24) and 0xFF).toByte()
        )
        return buildFrame(0x12, payload)
    }

    fun buildParamChange(blockIndex: Int, paramIndex: Int, effectId: Int, value: Float): ByteArray {
        val gen = BufferGenerator(24)
        gen.writeUint8(2, 0x04)
        gen.writeUint8(8, 0x05)
        gen.writeUint8(10, 0x0C)
        gen.writeUint8(12, blockIndex)
        gen.writeUint8(13, paramIndex)
        val (dispLo, dispHi) = encodeDisplayValue(value)
        gen.writeUint8(14, dispLo.toInt() and 0xFF)
        gen.writeUint8(15, dispHi.toInt() and 0xFF)
        gen.writeUint32LE(16, effectId.toLong() and 0xFFFFFFFFL)
        gen.writeFloat32LE(20, value)

        val nibbles = nibbleEncode(gen.buffer)
        val msg = ByteArray(62)
        System.arraycopy(SYSEX_HEADER, 0, msg, 0, 8)
        msg[8] = 0x12
        msg[9] = 0x18
        msg[10] = 0x00
        msg[11] = 0x00
        msg[12] = 0x00
        System.arraycopy(nibbles, 0, msg, 13, nibbles.size)
        msg[61] = 0xF7.toByte()
        return msg
    }

    fun buildReorder(order: List<Int>, send: Int = 4, ret: Int = 4): ByteArray {
        val gen = BufferGenerator(32)
        gen.writeUint8(2, 0x04)
        gen.writeUint8(8, 0x08)
        gen.writeUint8(10, 0x10)
        gen.writeUint8(14, send and 0xFF)
        gen.writeUint8(15, ret and 0xFF)
        for (i in 0 until 11) {
            val slot = if (i < order.size) order[i] else i
            gen.writeUint8(16 + i, slot)
        }
        gen.writeUint8(27, 0x44)

        val nibbles = nibbleEncode(gen.buffer)
        val msg = ByteArray(78)
        System.arraycopy(SYSEX_HEADER, 0, msg, 0, 8)
        msg[8] = 0x12
        msg[9] = 0x20
        msg[10] = 0x00
        msg[11] = 0x00
        msg[12] = 0x00
        System.arraycopy(nibbles, 0, msg, 13, nibbles.size)
        msg[77] = 0xF7.toByte()
        return msg
    }

    fun buildSaveCommit(presetName: String, slot: Int): ByteArray {
        val clampedSlot = slot.coerceIn(0, 255)
        val gen = BufferGenerator(24)
        gen.writeUint8(0, 0x03)
        gen.writeUint8(1, 0x20)
        gen.writeUint8(2, 0x14)
        gen.writeUint8(4, clampedSlot) // full absolute slot number (0..255)
        gen.writeAscii(8, presetName, 16)

        val nibbles = nibbleEncode(gen.buffer)
        val msg = ByteArray(62)
        System.arraycopy(SYSEX_HEADER, 0, msg, 0, 8)
        msg[8] = 0x12
        msg[9] = 0x18
        msg[10] = 0x00
        msg[11] = 0x00
        msg[12] = 0x00
        System.arraycopy(nibbles, 0, msg, 13, nibbles.size)
        msg[61] = 0xF7.toByte()
        return msg
    }

    fun isSysEx(data: ByteArray, cmd: Int, sub: Int): Boolean {
        if (data.size < 10) return false
        for (i in 0 until 8) {
            if (data[i] != SYSEX_HEADER[i]) return false
        }
        return (data[8].toInt() and 0xFF) == (cmd and 0xFF) &&
                (data[9].toInt() and 0xFF) == (sub and 0xFF)
    }

    fun buildEnterEditorMode(): ByteArray {
        return byteArrayOf(
            0xF0.toByte(), 0x21, 0x25, 0x7E, 0x47, 0x50, 0x2D, 0x32,
            0x11, 0x12,
            0x00, 0x00, 0x00,
            0xF7.toByte()
        )
    }

    fun buildControlChange(cc: Int, value: Int, channel: Int = 0): ByteArray {
        val status = (0xB0 or (channel and 0x0F)).toByte()
        return byteArrayOf(status, (cc and 0x7F).toByte(), (value and 0x7F).toByte())
    }

    fun buildTempoControlChange(bpm: Int, channel: Int = 0): List<ByteArray> {
        val clamped = bpm.coerceIn(40, 250)
        return if (clamped <= 127) {
            listOf(
                buildControlChange(73, 0, channel),
                buildControlChange(74, clamped, channel)
            )
        } else {
            listOf(
                buildControlChange(73, 1, channel),
                buildControlChange(74, clamped - 128, channel)
            )
        }
    }

    fun buildTapTempoControlChange(channel: Int = 0): ByteArray {
        return buildControlChange(75, 127, channel)
    }

    fun buildPatchSetting(target: Int, value: Int): ByteArray {
        val decoded = ByteArray(16)
        decoded[2] = 0x04
        decoded[8] = 0x06
        decoded[10] = 0x04
        decoded[12] = (target and 0xFF).toByte()
        decoded[13] = ((target shr 8) and 0xFF).toByte()

        val shortVal = if (target == 0x06 && value > 127) {
            (value - 256).toShort()
        } else {
            value.toShort()
        }
        decoded[14] = (shortVal.toInt() and 0xFF).toByte()
        decoded[15] = ((shortVal.toInt() shr 8) and 0xFF).toByte()
        return buildFrame(0x12, decoded)
    }

    fun buildStateDumpRequest(): ByteArray {
        return byteArrayOf(
            0xF0.toByte(), 0x21, 0x25, 0x7E, 0x47, 0x50, 0x2D, 0x32,
            0x11, 0x04,
            0x00, 0x00, 0x00, 0x00, 0x06, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00,
            0xF7.toByte()
        )
    }

    fun assembleChunks(chunks: List<ByteArray>): ByteArray {
        val sorted = chunks.sortedBy { chunk ->
            if (chunk.size >= 13) {
                val offLo = chunk[11].toInt() and 0xFF
                val offHi = chunk[12].toInt() and 0xFF
                offLo or (offHi shl 8)
            } else 0
        }
        val allNibbles = sorted.flatMap { chunk ->
            if (chunk.size >= 14) {
                chunk.slice(13 until (chunk.size - 1))
            } else emptyList()
        }.toByteArray()
        return nibbleDecode(allNibbles)
    }

    fun parsePresetFromDecoded(decoded: ByteArray, fallbackName: String = ""): GP200Preset {
        val parser = BinaryParser(decoded)
        var patchName = ""
        if (decoded.size > 43) {
            var len = 0
            while (len < 16 && (28 + len) < decoded.size && decoded[28 + len] != 0.toByte()) {
                len++
            }
            patchName = String(decoded, 28, len, StandardCharsets.US_ASCII).trim()
        }
        if (patchName.isEmpty()) patchName = fallbackName

        var author = ""
        if (decoded.size > 59) {
            var len = 0
            while (len < 16 && (44 + len) < decoded.size && decoded[44 + len] != 0.toByte()) {
                len++
            }
            author = String(decoded, 44, len, StandardCharsets.US_ASCII).trim()
        }

        val rawSend = if (decoded.size > 106) decoded[106].toInt() and 0xFF else 4
        val rawReturn = if (decoded.size > 107) decoded[107].toInt() and 0xFF else 4
        val fxLoopSend = if (rawSend in 1..11) rawSend else 4
        val fxLoopReturn = if (rawReturn in 1..11) rawReturn else 4

        val effectsBySlot = mutableListOf<EffectSlot>()
        for (b in 0 until 11) {
            val base = 120 + b * 72
            if (base + 72 > decoded.size) {
                effectsBySlot.add(EffectSlot(slotIndex = b, effectId = 0, enabled = false))
                continue
            }
            val slotIndex = decoded[base + 4].toInt() and 0xFF
            val enabled = (decoded[base + 5].toInt() and 0xFF) == 1
            val effectId = parser.readUint32LE(base + 8).toInt()
            val params = mutableListOf<Float>()
            for (p in 0 until 15) {
                val raw = parser.readFloat32LE(base + 12 + p * 4)
                params.add(if (raw.isFinite()) raw else 0f)
            }
            effectsBySlot.add(EffectSlot(slotIndex = slotIndex, effectId = effectId, enabled = enabled, params = params))
        }

        val routing = mutableListOf<Int>()
        val seen = mutableSetOf<Int>()
        if (decoded.size > 118) {
            for (i in 0 until 11) {
                val v = decoded[108 + i].toInt() and 0xFF
                if (v < 11 && !seen.contains(v)) {
                    routing.add(v)
                    seen.add(v)
                }
            }
        }
        for (si in 0 until 11) {
            if (!seen.contains(si)) routing.add(si)
        }

        val orderedEffects = routing.mapNotNull { si ->
            effectsBySlot.find { it.slotIndex == si }
        }

        val rawVol = if (decoded.size > 16) decoded[16].toInt() and 0xFF else 50
        val patchVolume = if (rawVol in 0..100) rawVol else 50

        // The device's live preset dump stores patch pan big-endian (unlike the .prst file format).
        val rawPan = if (decoded.size > 19) ((decoded[18].toInt() and 0xFF) shl 8) or (decoded[19].toInt() and 0xFF) else 0
        val panSigned = if (rawPan > 0x7FFF) rawPan - 0x10000 else rawPan
        val patchPan = if (panSigned in -50..50) panSigned else 0

        val rawTempo = if (decoded.size > 15) (decoded[14].toInt() and 0xFF) or ((decoded[15].toInt() and 0xFF) shl 8) else 120
        val patchTempo = if (rawTempo in 40..250) rawTempo else 120

        val rawDrumSync = if (decoded.size > 0x7E) (decoded[0x7E].toInt() and 0xFF) == 1 else false

        return GP200Preset(
            patchName = patchName,
            author = author,
            effects = orderedEffects,
            routingOrder = routing,
            fxLoopSend = fxLoopSend,
            fxLoopReturn = fxLoopReturn,
            patchVolume = patchVolume,
            patchPan = patchPan,
            patchTempo = patchTempo,
            checksum = 0,
            drumSync = rawDrumSync,
            controlMappings = ControlMappingCodec.decode(decoded, ControlMappingCodec.LIVE_OFFSET)
        )
    }

    fun parseReadChunks(chunks: List<ByteArray>): GP200Preset {
        require(chunks.isNotEmpty()) { "Preset read has no chunks" }
        val sorted = chunks.sortedBy { chunk ->
            require(isSysEx(chunk, 0x12, 0x18) && chunk.size >= 16 && chunk.last() == 0xF7.toByte()) {
                "Invalid preset read frame"
            }
            require((chunk[11].toInt() and 0xFF) < 128 && (chunk[12].toInt() and 0xFF) < 128)
            (chunk[11].toInt() and 0x7F) or ((chunk[12].toInt() and 0x7F) shl 7)
        }
        var expectedOffset = 0
        for (chunk in sorted) {
            val offset = (chunk[11].toInt() and 0x7F) or ((chunk[12].toInt() and 0x7F) shl 7)
            require(offset == expectedOffset) { "Missing, duplicate or overlapping preset chunk" }
            val payload = chunk.copyOfRange(13, chunk.lastIndex)
            require(payload.size % 2 == 0 && payload.all { (it.toInt() and 0xFF) <= 15 }) {
                "Invalid preset nibbles"
            }
            expectedOffset += payload.size / 2
        }
        require(expectedOffset == 1176) { "Incomplete preset read: $expectedOffset of 1176 bytes" }
        return parsePresetFromDecoded(assembleChunks(sorted))
    }

    fun parseStateDumpSlot(chunks: List<ByteArray>): Int {
        if (chunks.isEmpty()) return 0
        val decoded = assembleChunks(chunks)
        if (decoded.size >= 10) {
            val rawSlot = (decoded[8].toInt() and 0xFF) or ((decoded[9].toInt() and 0xFF) shl 8)
            if (rawSlot in 0..255) return rawSlot
        }
        return 0
    }

    fun parseKnobNotification(msg: ByteArray): Triple<Int, Int, Float>? {
        if (!isSysEx(msg, 0x12, 0x10) || msg.size < 45) return null
        for (i in 29..36) {
            if (msg[i] != 0.toByte()) return null
        }
        val blockId = msg[22].toInt() and 0xFF
        val paramIdx = msg[24].toInt() and 0xFF
        if (blockId !in 0..10) return null

        val hi0 = msg[37].toInt() and 0x0F
        val lo0 = msg[38].toInt() and 0x0F
        val hi1 = msg[39].toInt() and 0x0F
        val lo1 = msg[40].toInt() and 0x0F
        val hi2 = msg[41].toInt() and 0x0F
        val lo2 = msg[42].toInt() and 0x0F
        val hi3 = msg[43].toInt() and 0x0F
        val lo3 = msg[44].toInt() and 0x0F

        val b0 = (hi0 shl 4) or lo0
        val b1 = (hi1 shl 4) or lo1
        val b2 = (hi2 shl 4) or lo2
        val b3 = (hi3 shl 4) or lo3
        val bits = b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
        val value = java.lang.Float.intBitsToFloat(bits)
        return Triple(blockId, paramIdx, value)
    }

    fun parseToggleNotification(msg: ByteArray): Pair<Int, Boolean>? {
        if (isSysEx(msg, 0x12, 0x10) && msg.size >= 41) {
            val isKnob = (29..36).all { it < msg.size && msg[it] == 0.toByte() }
            if (!isKnob && msg.size >= 45) {
                val blockId = msg[38].toInt() and 0xFF
                val state = (msg[40].toInt() and 0xFF) != 0
                if (blockId in 0..10) return Pair(blockId, state)
            }
        }
        // 0x05 reports the persistent effect state; 0x01 is the pedal release event.
        if (isSysEx(msg, 0x12, 0x08) && msg.size >= 25 && msg[14] == 0x05.toByte()) {
            val blockId = msg[22].toInt() and 0xFF
            val stateValue = msg[24].toInt() and 0xFF
            if (blockId in 0..10 && stateValue in 0..1) return Pair(blockId, stateValue == 1)
        }
        return null
    }

    fun parseEffectChangeNotification(msg: ByteArray): Pair<Int, Int>? {
        if (!isSysEx(msg, 0x12, 0x0C) || msg.size < 38) return null
        val payload = msg.copyOfRange(10, msg.size)
        val blockIndex = payload[12].toInt() and 0xFF
        val moduleType = payload[26].toInt() and 0xFF
        val variant = ((payload[19].toInt() and 0x0F) shl 4) or (payload[20].toInt() and 0x0F)
        val effectId = (moduleType shl 24) or variant
        // Ignore unrelated 0x12/0x0C events that happen to decode as an unknown model ID.
        if (blockIndex in 0..10 && EffectCatalog.getEffectById(effectId) != null) {
            return Pair(blockIndex, effectId)
        }
        return null
    }

    fun parseSlotChangeNotification(msg: ByteArray): Int? {
        if (!isSysEx(msg, 0x12, 0x08) || msg.size < 28) return null
        if (msg[14] == 0x08.toByte() && msg[21] == 0.toByte() && msg[22] == 0.toByte()) {
            val slot = ((msg[25].toInt() and 0x0F) shl 4) or (msg[26].toInt() and 0x0F)
            if (slot in 0..255) return slot
        }
        return null
    }

    sealed interface DrumNotification {
        data class Sync(val enabled: Boolean) : DrumNotification
        data class Pattern(val patternId: Int) : DrumNotification
        data class Volume(val volume: Int) : DrumNotification
        data class Bpm(val bpm: Int) : DrumNotification
        data class PlayStop(val playing: Boolean) : DrumNotification
    }

    fun parseDrumNotification(msg: ByteArray): DrumNotification? {
        if (msg.size != 30 || msg[0] != 0xF0.toByte() || msg[msg.size - 1] != 0xF7.toByte()) {
            return null
        }
        if (msg[8] != 0x12.toByte() || msg[9] != 0x08.toByte()) {
            return null
        }
        val decoded = nibbleDecode(msg.copyOfRange(13, 29))
        if (decoded.size < 8) return null
        val cmd = (decoded[0].toInt() and 0xFF) or ((decoded[1].toInt() and 0xFF) shl 8)
        val target = (decoded[4].toInt() and 0xFF) or ((decoded[5].toInt() and 0xFF) shl 8)
        val value = (decoded[6].toInt() and 0xFF) or ((decoded[7].toInt() and 0xFF) shl 8)

        return when {
            cmd == 0x1008 && target == 0x19 -> DrumNotification.Sync(value != 0)
            cmd == 0x1011 && target == 2 -> DrumNotification.Sync(value != 0)
            cmd == 0x1001 && target == 3 -> DrumNotification.Sync(value != 0)
            cmd == 0x1001 && target == 0 -> DrumNotification.Pattern(value)
            cmd == 0x1001 && target == 1 -> DrumNotification.Volume(value)
            cmd == 0x1001 && target == 2 -> DrumNotification.Bpm(value)
            cmd == 0x1011 && target == 0 -> DrumNotification.PlayStop(value != 0)
            else -> null
        }
    }

    fun parsePresetName(sysexMsg: ByteArray): String {
        // [F0 header(10B)][slot(1B)][off_lo(1B)][off_hi(1B)][nibbles...][F7]
        if (sysexMsg.size < 46) return ""
        val nibbles = sysexMsg.copyOfRange(13, sysexMsg.size - 1)
        val decoded = nibbleDecode(nibbles)
        if (decoded.size < 44) return ""
        var len = 0
        while (len < 16 && (28 + len) < decoded.size && decoded[28 + len] != 0.toByte()) {
            len++
        }
        return String(decoded, 28, len, StandardCharsets.US_ASCII).trim()
    }

    /** Tail the desktop editor sent with name reads (uninitialized stack); fallback if zeros are rejected. */
    val USER_MODEL_CAPTURED_TAIL: ByteArray
        get() = byteArrayOf(
            0x5e, 0x9e.toByte(), 0x67, 0x00, 0xec.toByte(), 0x4e, 0x1c, 0x25, 0x6c, 0x5d,
            0x78, 0x02, 0xb0.toByte(), 0xf9.toByte(), 0x92.toByte(), 0x02, 0x01, 0xfa.toByte(), 0xd1.toByte(), 0x07
        )

    /** Read request for one user IR/SnapTone name: [recordId LE16][18 00][index LE16][01 00][tail 20B]. */
    fun buildUserModelNameRequest(kind: UserModelKind, index: Int, tail: ByteArray = ByteArray(20)): ByteArray {
        val payload = ByteArray(28)
        payload[0] = (kind.recordId and 0xFF).toByte()
        payload[1] = ((kind.recordId shr 8) and 0xFF).toByte()
        payload[2] = 0x18
        payload[4] = (index and 0xFF).toByte()
        payload[5] = ((index shr 8) and 0xFF).toByte()
        payload[6] = 0x01
        System.arraycopy(tail, 0, payload, 8, minOf(20, tail.size))
        return buildFrame(0x11, payload)
    }

    /** Response: [recordId LE16][18 00][index LE16][6 zero bytes][name 16B, NUL-terminated; bytes after NUL are stale]. */
    fun parseUserModelNameResponse(msg: ByteArray): UserModelName? {
        if (!isSysEx(msg, 0x12, 0x1C) || msg.size != 14 + 56) return null
        val nibbles = msg.copyOfRange(13, msg.lastIndex)
        if (nibbles.any { (it.toInt() and 0xFF) > 15 }) return null
        val payload = nibbleDecode(nibbles)
        val recordId = (payload[0].toInt() and 0xFF) or ((payload[1].toInt() and 0xFF) shl 8)
        val kind = UserModelKind.entries.firstOrNull { it.recordId == recordId } ?: return null
        if (payload[2] != 0x18.toByte() || payload[3] != 0.toByte()) return null
        val index = (payload[4].toInt() and 0xFF) or ((payload[5].toInt() and 0xFF) shl 8)
        if (index !in 0 until kind.slotCount) return null
        val name = buildString {
            for (i in 12 until 28) {
                val c = payload[i].toInt() and 0xFF
                if (c == 0) break
                if (c in 0x20..0x7E) append(c.toChar())
            }
        }.trim()
        return UserModelName(kind, index, name)
    }
}
