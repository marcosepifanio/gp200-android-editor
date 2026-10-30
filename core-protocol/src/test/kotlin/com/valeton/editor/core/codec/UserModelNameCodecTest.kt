package com.valeton.editor.core.codec

import com.valeton.editor.core.model.UserModelKind
import com.valeton.editor.core.model.UserModelName
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/** Payloads below are decoded frames from a GP-200 LT usbmon capture (desktop editor V1.8.1). */
class UserModelNameCodecTest {

    private fun hex(s: String) = s.split(" ").map { it.toInt(16).toByte() }.toByteArray()

    private fun decodedPayload(msg: ByteArray) = SysExCodec.nibbleDecode(msg.copyOfRange(13, msg.lastIndex))

    private fun response(payload: String) = SysExCodec.buildFrame(0x12, hex(payload))

    @Test
    fun `IR request for index 0 has record 1009, length 28 and zero tail`() {
        val msg = SysExCodec.buildUserModelNameRequest(UserModelKind.IR, 0)
        assertEquals(0x11, msg[8].toInt() and 0xFF)
        assertEquals(28, msg[9].toInt() or (msg[10].toInt() shl 7))
        assertArrayEquals(hex("09 10 18 00 00 00 01 00") + ByteArray(20), decodedPayload(msg))
    }

    @Test
    fun `SnapTone request for index 7 uses record 1012`() {
        val msg = SysExCodec.buildUserModelNameRequest(UserModelKind.SNAPTONE, 7)
        assertArrayEquals(hex("12 10 18 00 07 00 01 00") + ByteArray(20), decodedPayload(msg))
    }

    @Test
    fun `request with captured tail copies it after the header`() {
        val msg = SysExCodec.buildUserModelNameRequest(UserModelKind.IR, 1, SysExCodec.USER_MODEL_CAPTURED_TAIL)
        assertArrayEquals(
            hex("09 10 18 00 01 00 01 00 5e 9e 67 00 ec 4e 1c 25 6c 5d 78 02 b0 f9 92 02 01 fa d1 07"),
            decodedPayload(msg)
        )
    }

    @Test
    fun `parses captured IR response and drops stale bytes after NUL`() {
        val r = SysExCodec.parseUserModelNameResponse(
            response("09 10 18 00 00 00 00 00 00 00 00 00 69 72 31 00 6c 69 64 00 a8 ac 3f 80 25 77 02 80")
        )
        assertEquals(UserModelName(UserModelKind.IR, 0, "ir1"), r)
    }

    @Test
    fun `parses captured SnapTone response nam2 at index 1`() {
        val r = SysExCodec.parseUserModelNameResponse(
            response("12 10 18 00 01 00 00 00 00 00 00 00 6e 61 6d 32 00 03 20 00 00 00 00 00 a8 ac 3f 80")
        )
        assertEquals(UserModelName(UserModelKind.SNAPTONE, 1, "nam2"), r)
    }

    @Test
    fun `parses factory name APP-5153-Ivory-`() {
        val r = SysExCodec.parseUserModelNameResponse(
            response("12 10 18 00 00 00 00 00 00 00 00 00 41 50 50 2d 35 31 35 33 2d 49 76 6f 72 79 2d 00")
        )
        assertEquals("APP-5153-Ivory-", r?.name)
    }

    @Test
    fun `16-character name without NUL is kept whole`() {
        val r = SysExCodec.parseUserModelNameResponse(
            response("09 10 18 00 02 00 00 00 00 00 00 00 41 42 43 44 45 46 47 48 49 4a 4b 4c 4d 4e 4f 50")
        )
        assertEquals("ABCDEFGHIJKLMNOP", r?.name)
    }

    @Test
    fun `non-printable bytes are removed from the name`() {
        val r = SysExCodec.parseUserModelNameResponse(
            response("09 10 18 00 03 00 00 00 00 00 00 00 41 01 42 ff 43 00 00 00 00 00 00 00 00 00 00 00")
        )
        assertEquals("ABC", r?.name)
    }

    @Test
    fun `rejects unknown record id, wrong length and out-of-range index`() {
        // Rhythm name record (0x000a) from the same capture.
        assertNull(SysExCodec.parseUserModelNameResponse(
            response("0a 00 14 00 01 00 3f 80 4d 65 74 61 6c 00 00 00 a8 ac 3f 80 a8 ac 3f 80")
        ))
        assertNull(SysExCodec.parseUserModelNameResponse(
            response("09 10 18 00 14 00 00 00 00 00 00 00 69 72 31 00 00 00 00 00 00 00 00 00 00 00 00 00")
        ))
        assertNull(SysExCodec.parseUserModelNameResponse(
            response("12 10 18 00 0a 00 00 00 00 00 00 00 6e 61 6d 31 00 00 00 00 00 00 00 00 00 00 00 00")
        ))
        assertNull(SysExCodec.parseUserModelNameResponse(
            response("09 10 18 00 00 00 00 00 00 00 00 00 69 72 31 00 00 00 00 00 00 00 00 00")
        ))
    }

    @Test
    fun `effectId maps IR 19 to 168820755 and SnapTone 5 to 251658245`() {
        assertEquals(168820755, UserModelKind.IR.effectId(19))
        assertEquals(251658245, UserModelKind.SNAPTONE.effectId(5))
    }
}
