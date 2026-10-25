package com.valeton.editor.core.codec

import com.valeton.editor.core.binary.BufferGenerator
import com.valeton.editor.core.model.GP200Preset

class PRSTEncoder {

    companion object {
        const val PRST_MAGIC = "TSRP"
        const val DEVICE_ID = "2-PG"
        const val TOTAL_SIZE = 1224
        const val OFFSET_MAGIC = 0x00
        const val OFFSET_DEVICE_ID = 0x10
        const val OFFSET_FW_VERSION = 0x14
        const val OFFSET_TIMESTAMP = 0x1C
        const val OFFSET_MRAP = 0x28
        const val OFFSET_MRAP_SIZE = 0x2C
        const val MRAP_CONTENT_SIZE = 1172

        const val OFFSET_PRE_META = 0x30
        const val OFFSET_PATCH_NAME = 0x44
        const val PATCH_NAME_MAX = 16
        const val OFFSET_AUTHOR = 0x54
        const val AUTHOR_MAX = 16

        const val OFFSET_ROUTING = 0x8C
        const val OFFSET_FX_SEND = 0x92
        const val OFFSET_FX_RETURN = 0x93
        const val OFFSET_ROUTING_ORDER = 0x94

        const val EFFECT_BLOCK_COUNT = 11
        const val EFFECT_BLOCK_START = 0xA0
        const val EFFECT_BLOCK_SIZE = 0x48
        const val PARAMS_OFFSET = 0x0C
        const val PARAMS_COUNT = 15

        const val OFFSET_CHECKSUM = 0x4C6
    }

    fun encode(preset: GP200Preset): ByteArray {
        val gen = BufferGenerator(TOTAL_SIZE)
        val hasRaw = preset.rawSource != null && preset.rawSource.size >= TOTAL_SIZE

        if (hasRaw) {
            gen.writeBytes(0, preset.rawSource!!.copyOfRange(0, TOTAL_SIZE))
        }

        if (!hasRaw) {
            gen.writeAscii(OFFSET_MAGIC, PRST_MAGIC, 4)
            gen.writeUint8(0x0B, 0x06)
            gen.writeAscii(OFFSET_DEVICE_ID, DEVICE_ID, 4)
            val verNum = preset.version.toIntOrNull() ?: 1
            gen.writeUint8(OFFSET_FW_VERSION, 0x00)
            gen.writeUint8(OFFSET_FW_VERSION + 1, verNum and 0xFF)
            gen.writeUint8(OFFSET_FW_VERSION + 2, 0x01)
            gen.writeUint8(OFFSET_FW_VERSION + 3, 0x00)
            gen.writeUint32LE(OFFSET_TIMESTAMP, (System.currentTimeMillis() / 1000) and 0xFFFFFFFFL)
            gen.writeUint16LE(0x24, 0x0494)
            gen.writeAscii(OFFSET_MRAP, "MRAP", 4)
            gen.writeUint32LE(OFFSET_MRAP_SIZE, MRAP_CONTENT_SIZE.toLong())

            gen.writeUint8(OFFSET_PRE_META, 0x02)
            gen.writeUint8(OFFSET_PRE_META + 2, 0x58)
            gen.writeUint8(OFFSET_PRE_META + 6, 0x78)
            gen.writeUint8(OFFSET_PRE_META + 8, 0x32)
        } else {
            val verNum = preset.version.toIntOrNull() ?: 1
            gen.writeUint8(OFFSET_FW_VERSION + 1, verNum and 0xFF)
        }

        // Patch name & Author
        gen.writeAscii(OFFSET_PATCH_NAME, preset.patchName, PATCH_NAME_MAX)
        gen.writeAscii(OFFSET_AUTHOR, preset.author, AUTHOR_MAX)
        gen.writeUint16LE(0x36, preset.patchTempo)
        gen.writeUint16LE(0x38, preset.patchVolume)
        gen.writeUint16LE(0x3A, preset.patchPan)

        // Routing section
        if (!hasRaw) {
            gen.writeUint8(OFFSET_ROUTING + 0, 0x08)
            gen.writeUint8(OFFSET_ROUTING + 2, 0x10)
            gen.writeUint8(OFFSET_ROUTING + 4, 0x04)
            gen.writeUint8(OFFSET_ROUTING + 5, 0x04)
        }
        gen.writeUint8(OFFSET_FX_SEND, preset.fxLoopSend)
        gen.writeUint8(OFFSET_FX_RETURN, preset.fxLoopReturn)

        for (i in 0 until EFFECT_BLOCK_COUNT) {
            val slotIdx = if (i < preset.routingOrder.size) preset.routingOrder[i] else i
            gen.writeUint8(OFFSET_ROUTING_ORDER + i, slotIdx)
        }

        // Effect blocks: always stored in slotIndex order (0..10)
        val written = mutableSetOf<Int>()
        for (slot in preset.effects) {
            val base = EFFECT_BLOCK_START + slot.slotIndex * EFFECT_BLOCK_SIZE
            if (!hasRaw) {
                gen.writeUint8(base + 0, 0x14)
                gen.writeUint8(base + 2, 0x44)
                gen.writeUint8(base + 7, 0x0F)
            }
            gen.writeUint8(base + 4, slot.slotIndex)
            gen.writeUint8(base + 5, if (slot.enabled) 1 else 0)
            gen.writeUint32LE(base + 8, slot.effectId.toLong() and 0xFFFFFFFFL)
            for (p in 0 until PARAMS_COUNT) {
                val valParam = if (p < slot.params.size) slot.params[p] else 0f
                gen.writeFloat32LE(base + PARAMS_OFFSET + p * 4, valParam)
            }
            written.add(slot.slotIndex)
        }

        if (!hasRaw) {
            for (i in 0 until EFFECT_BLOCK_COUNT) {
                if (written.contains(i)) continue
                val base = EFFECT_BLOCK_START + i * EFFECT_BLOCK_SIZE
                gen.writeUint8(base + 0, 0x14)
                gen.writeUint8(base + 2, 0x44)
                gen.writeUint8(base + 4, i)
                gen.writeUint8(base + 7, 0x0F)
            }
        }

        preset.controlMappings?.let { gen.writeBytes(ControlMappingCodec.PRST_OFFSET, ControlMappingCodec.encode(it)) }

        // Checksum calculation: sum(bytes[0..0x4C5]) & 0xFFFF as BE16
        val bytes = gen.buffer
        var sum = 0
        for (i in 0 until OFFSET_CHECKSUM) {
            sum += bytes[i].toInt() and 0xFF
        }
        val checksum = sum and 0xFFFF
        bytes[OFFSET_CHECKSUM] = ((checksum shr 8) and 0xFF).toByte()
        bytes[OFFSET_CHECKSUM + 1] = (checksum and 0xFF).toByte()

        return bytes.copyOf()
    }
}
