package com.valeton.editor.core.binary

import java.nio.charset.StandardCharsets

class BinaryParser(private val buffer: ByteArray) {

    val byteLength: Int get() = buffer.size

    private fun assertBounds(offset: Int, size: Int) {
        if (offset < 0 || offset + size > buffer.size) {
            throw IndexOutOfBoundsException(
                "BinaryParser: out of bounds read at offset $offset (size $size, buffer ${buffer.size})"
            )
        }
    }

    fun readUint8(offset: Int): Int {
        assertBounds(offset, 1)
        return buffer[offset].toInt() and 0xFF
    }

    fun readUint16LE(offset: Int): Int {
        assertBounds(offset, 2)
        return (readUint8(offset)) or (readUint8(offset + 1) shl 8)
    }

    fun readInt16LE(offset: Int): Int {
        assertBounds(offset, 2)
        val low = buffer[offset].toInt() and 0xFF
        val high = buffer[offset + 1].toInt()
        return (high shl 8) or low
    }

    fun readUint16BE(offset: Int): Int {
        assertBounds(offset, 2)
        return (readUint8(offset) shl 8) or (readUint8(offset + 1))
    }

    fun readUint32LE(offset: Int): Long {
        assertBounds(offset, 4)
        val b0 = readUint8(offset).toLong()
        val b1 = readUint8(offset + 1).toLong()
        val b2 = readUint8(offset + 2).toLong()
        val b3 = readUint8(offset + 3).toLong()
        return (b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)) and 0xFFFFFFFFL
    }

    fun readFloat32LE(offset: Int): Float {
        assertBounds(offset, 4)
        val intBits = readUint32LE(offset).toInt()
        val floatVal = java.lang.Float.intBitsToFloat(intBits)
        return if (floatVal.isFinite()) floatVal else 0.0f
    }

    fun readBytes(offset: Int, length: Int): ByteArray {
        assertBounds(offset, length)
        return buffer.copyOfRange(offset, offset + length)
    }

    fun readAscii(offset: Int, length: Int): String {
        assertBounds(offset, length)
        var actualLen = 0
        while (actualLen < length && buffer[offset + actualLen] != 0.toByte()) {
            actualLen++
        }
        return String(buffer, offset, actualLen, StandardCharsets.US_ASCII)
    }
}
