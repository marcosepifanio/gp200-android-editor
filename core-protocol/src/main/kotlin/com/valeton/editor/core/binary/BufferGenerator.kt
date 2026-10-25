package com.valeton.editor.core.binary

import java.nio.charset.StandardCharsets

class BufferGenerator(val size: Int) {
    val buffer: ByteArray = ByteArray(size)

    private fun assertBounds(offset: Int, len: Int) {
        if (offset < 0 || offset + len > size) {
            throw IndexOutOfBoundsException(
                "BufferGenerator: out of bounds write at offset $offset (size $len, buffer $size)"
            )
        }
    }

    fun writeUint8(offset: Int, value: Int) {
        assertBounds(offset, 1)
        buffer[offset] = (value and 0xFF).toByte()
    }

    fun writeUint16LE(offset: Int, value: Int) {
        assertBounds(offset, 2)
        buffer[offset] = (value and 0xFF).toByte()
        buffer[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }

    fun writeUint32LE(offset: Int, value: Long) {
        assertBounds(offset, 4)
        buffer[offset] = (value and 0xFF).toByte()
        buffer[offset + 1] = ((value shr 8) and 0xFF).toByte()
        buffer[offset + 2] = ((value shr 16) and 0xFF).toByte()
        buffer[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    fun writeFloat32LE(offset: Int, value: Float) {
        assertBounds(offset, 4)
        val validValue = if (value.isFinite()) value else 0.0f
        val intBits = java.lang.Float.floatToIntBits(validValue)
        writeUint32LE(offset, intBits.toLong() and 0xFFFFFFFFL)
    }

    fun writeAscii(offset: Int, value: String, fieldLength: Int) {
        assertBounds(offset, fieldLength)
        val asciiBytes = value.toByteArray(StandardCharsets.US_ASCII)
        for (i in 0 until fieldLength) {
            buffer[offset + i] = if (i < asciiBytes.size) asciiBytes[i] else 0
        }
    }

    fun writeBytes(offset: Int, bytes: ByteArray) {
        assertBounds(offset, bytes.size)
        System.arraycopy(bytes, 0, buffer, offset, bytes.size)
    }

    fun toByteArray(): ByteArray = buffer.copyOf()
}
