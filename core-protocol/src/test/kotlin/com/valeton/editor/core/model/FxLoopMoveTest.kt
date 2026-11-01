package com.valeton.editor.core.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FxLoopMoveTest {
    private fun allowed(send: Int, ret: Int, newSend: Int, newRet: Int) = GP200Preset.fxLoopMoveAllowed(send, ret, newSend, newRet)

    @Test
    fun `send may reach the return but not pass it`() {
        assertTrue(allowed(3, 5, 4, 5))
        assertTrue(allowed(4, 5, 5, 5))
        assertFalse(allowed(5, 5, 6, 5))
    }

    @Test
    fun `positions stay inside the editable range`() {
        assertFalse(allowed(1, 5, 0, 5))
        assertFalse(allowed(3, 10, 3, 11))
    }

    @Test
    fun `a send after the return can be walked back from either marker`() {
        assertTrue(allowed(8, 3, 7, 3))
        assertTrue(allowed(8, 3, 8, 4))
        assertFalse(allowed(8, 3, 9, 3))
        assertFalse(allowed(8, 3, 8, 2))
    }

    @Test
    fun `a return the pedal reported at 11 does not block moving the send`() {
        assertTrue(allowed(5, 11, 4, 11))
        assertTrue(allowed(5, 11, 5, 10))
    }

    @Test
    fun `exactly one marker moves`() {
        assertFalse(allowed(3, 5, 3, 5))
        assertFalse(allowed(3, 5, 4, 6))
    }
}
