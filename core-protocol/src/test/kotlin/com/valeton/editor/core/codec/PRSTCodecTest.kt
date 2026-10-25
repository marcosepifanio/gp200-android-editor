package com.valeton.editor.core.codec

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PRSTCodecTest {

    @Test
    fun `decode maintainer clean prst file successfully`() {
        val stream = javaClass.classLoader.getResourceAsStream("fixtures/clean.prst")
        assertNotNull(stream, "Fixture clean.prst must exist in test resources")
        val bytes = stream!!.readAllBytes()
        assertEquals(1224, bytes.size, "Fixture must be 1224 bytes")

        val decoder = PRSTDecoder(bytes)
        assertTrue(decoder.hasMagic(), "Should have TSRP magic")

        val preset = decoder.decode()
        assertEquals("Clean", preset.patchName)
        assertEquals("", preset.author)
        assertEquals(50, preset.patchVolume)
        assertEquals(0, preset.patchPan)
        assertEquals(160, preset.patchTempo)
        assertEquals(11, preset.effects.size, "Must decode exactly 11 effect slots")
        assertEquals(11, preset.routingOrder.size, "Must decode 11 routing order indices")
    }

    @Test
    fun `roundtrip encode produces byte-identical output with matching checksum`() {
        val stream = javaClass.classLoader.getResourceAsStream("fixtures/clean.prst")
        val originalBytes = stream!!.readAllBytes()

        val decoder = PRSTDecoder(originalBytes)
        val preset = decoder.decode()

        val encoder = PRSTEncoder()
        val encodedBytes = encoder.encode(preset)

        assertEquals(1224, encodedBytes.size)
        assertArrayEquals(originalBytes, encodedBytes, "Encoded bytes must match original byte-for-byte")
    }

    @Test
    fun `decode maintainer clean fixture preserves amp and cabinet parameters`() {
        val stream = javaClass.classLoader.getResourceAsStream("fixtures/clean.prst")
        assertNotNull(stream)
        val bytes = stream!!.readAllBytes()
        assertEquals(1224, bytes.size)

        val preset = PRSTDecoder(bytes).decode()
        assertEquals((0..10).toList(), preset.routingOrder)
        val amp = preset.effects.single { it.slotIndex == 3 }
        assertEquals(117440513, amp.effectId)
        assertTrue(amp.enabled)
        assertEquals(listOf(30f, 73f, 50f, 40f, 62f, 66f), amp.params.take(6))
        val cabinet = preset.effects.single { it.slotIndex == 5 }
        assertEquals(167772217, cabinet.effectId)
        assertEquals(60f, cabinet.params[1])
        assertEquals(19f, cabinet.params[5])
        assertEquals(20001f, cabinet.params[6])
    }

    @Test
    fun `edited author volume signed pan and tempo roundtrip from clean fixture`() {
        val stream = javaClass.classLoader.getResourceAsStream("fixtures/clean.prst")
        assertNotNull(stream)
        val bytes = stream!!.readAllBytes()
        assertEquals(1224, bytes.size)

        val edited = PRSTDecoder(bytes).decode().copy(
            author = "Autor teste", patchVolume = 82, patchPan = -25, patchTempo = 125
        )
        val encoded = PRSTEncoder().encode(edited)
        // Independent wire-offset checks prevent matching encoder/decoder mistakes.
        assertArrayEquals("Autor teste".toByteArray(Charsets.US_ASCII).copyOf(16), encoded.copyOfRange(0x54, 0x64))
        assertArrayEquals(byteArrayOf(82, 0), encoded.copyOfRange(0x38, 0x3a))
        assertArrayEquals(byteArrayOf(0xe7.toByte(), 0xff.toByte()), encoded.copyOfRange(0x3a, 0x3c))
        assertArrayEquals(byteArrayOf(125, 0), encoded.copyOfRange(0x36, 0x38))
        val result = PRSTDecoder(encoded).decode()
        assertEquals("Autor teste", result.author)
        assertEquals(82, result.patchVolume)
        assertEquals(-25, result.patchPan)
        assertEquals(125, result.patchTempo)
        assertEquals(edited.effects, result.effects)
        assertEquals(edited.controlMappings, result.controlMappings)
    }
}
