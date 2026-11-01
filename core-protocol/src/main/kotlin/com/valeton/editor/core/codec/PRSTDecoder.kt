package com.valeton.editor.core.codec

import com.valeton.editor.core.binary.BinaryParser
import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.GP200Preset
import java.io.InputStream

class PRSTDecoder(private val buffer: ByteArray) {
    private val parser = BinaryParser(buffer)

    companion object {
        const val PRST_MAGIC = "TSRP"
        const val OFFSET_MAGIC = 0x00
        const val OFFSET_VERSION = 0x15
        const val OFFSET_PATCH_TEMPO = 0x36
        const val OFFSET_PATCH_VOLUME = 0x38
        const val OFFSET_PATCH_PAN = 0x3A
        const val OFFSET_PATCH_NAME = 0x44
        const val PATCH_NAME_MAX = 16
        const val OFFSET_AUTHOR = 0x54
        const val AUTHOR_MAX = 16
        const val OFFSET_CHECKSUM = 0x4C6

        const val EFFECT_BLOCK_COUNT = 11
        const val EFFECT_BLOCK_START = 0xA0
        const val EFFECT_BLOCK_SIZE = 0x48
        const val OFFSET_FX_SEND = 0x92
        const val OFFSET_FX_RETURN = 0x93
        const val OFFSET_ROUTING_ORDER = 0x94

        const val SLOT_OFFSET = 4
        const val ACTIVE_OFFSET = 5
        const val MODEL_OFFSET = 8
        const val PARAMS_OFFSET = 0x0C
        const val PARAMS_COUNT = 15
        const val MAX_FILE_SIZE = 1224

        /** Reads a .prst stream, refusing anything larger than the format before it is buffered. */
        fun readFile(input: InputStream): ByteArray {
            // InputStream.readNBytes needs Android API 33; the app supports API 26.
            val buffer = ByteArray(MAX_FILE_SIZE + 1)
            var size = 0
            while (size < buffer.size) {
                val read = input.read(buffer, size, buffer.size - size)
                if (read < 0) break
                size += read
            }
            require(size <= MAX_FILE_SIZE) { "Invalid .prst file: larger than $MAX_FILE_SIZE bytes" }
            return buffer.copyOf(size)
        }
    }

    fun hasMagic(): Boolean {
        if (parser.byteLength < 4) return false
        return parser.readAscii(OFFSET_MAGIC, 4) == PRST_MAGIC
    }

    fun decode(): GP200Preset {
        val len = parser.byteLength
        if (len != 1224 && len != 1176) {
            throw IllegalArgumentException("Invalid .prst file: expected 1224 or 1176 bytes, got $len")
        }
        if (!hasMagic()) {
            throw IllegalArgumentException("Invalid .prst file: magic header not found")
        }

        val version = parser.readUint8(OFFSET_VERSION).toString()
        val patchName = parser.readAscii(OFFSET_PATCH_NAME, PATCH_NAME_MAX).trim()
        val author = parser.readAscii(OFFSET_AUTHOR, AUTHOR_MAX).trim()

        val byBlock = mutableListOf<EffectSlot>()
        for (i in 0 until EFFECT_BLOCK_COUNT) {
            val base = EFFECT_BLOCK_START + i * EFFECT_BLOCK_SIZE
            val slotIndex = parser.readUint8(base + SLOT_OFFSET)
            val enabled = parser.readUint8(base + ACTIVE_OFFSET) == 1
            val effectId = parser.readUint32LE(base + MODEL_OFFSET).toInt()
            val params = mutableListOf<Float>()
            for (p in 0 until PARAMS_COUNT) {
                val raw = parser.readFloat32LE(base + PARAMS_OFFSET + p * 4)
                params.add(if (raw.isFinite()) raw else 0f)
            }
            byBlock.add(EffectSlot(slotIndex, effectId, enabled, params))
        }

        val routing = mutableListOf<Int>()
        for (i in 0 until EFFECT_BLOCK_COUNT) {
            val slotIdx = parser.readUint8(OFFSET_ROUTING_ORDER + i)
            if (slotIdx in 0 until EFFECT_BLOCK_COUNT && !routing.contains(slotIdx)) {
                routing.add(slotIdx)
            }
        }
        for (i in 0 until EFFECT_BLOCK_COUNT) {
            if (!routing.contains(i)) {
                routing.add(i)
            }
        }

        val effectsInRoutingOrder = routing.mapNotNull { slotIdx ->
            byBlock.find { it.slotIndex == slotIdx }
        }

        val rawSend = parser.readUint8(OFFSET_FX_SEND)
        val rawReturn = parser.readUint8(OFFSET_FX_RETURN)
        val fxLoopSend = if (rawSend in 1..10) rawSend else 4
        val fxLoopReturn = if (rawReturn in 1..10) rawReturn else 4

        val checksum = if (len >= 1224) parser.readUint16BE(OFFSET_CHECKSUM) else 0
        val patchVolume = parser.readUint8(OFFSET_PATCH_VOLUME)
        val patchPan = parser.readInt16LE(OFFSET_PATCH_PAN)
        val rawTempo = parser.readUint16LE(OFFSET_PATCH_TEMPO)
        val patchTempo = if (rawTempo in 40..250) rawTempo else 120

        return GP200Preset(
            version = version,
            patchName = patchName,
            author = author,
            patchVolume = patchVolume,
            patchPan = patchPan,
            patchTempo = patchTempo,
            effects = effectsInRoutingOrder,
            routingOrder = routing,
            fxLoopSend = fxLoopSend,
            fxLoopReturn = fxLoopReturn,
            checksum = checksum,
            rawSource = buffer.copyOf(),
            controlMappings = ControlMappingCodec.decode(buffer, ControlMappingCodec.PRST_OFFSET)
        )
    }
}
