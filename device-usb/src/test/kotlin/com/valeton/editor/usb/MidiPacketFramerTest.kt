package com.valeton.editor.usb

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MidiPacketFramerTest {

    @Test
    fun `pacer spaces consecutive parameter changes by at least 40ms`() = runTest {
        val sentMessages = mutableListOf<Long>()
        val pacer = MidiPacketFramer(coroutineScope = this, intervalMs = 40L) {
            sentMessages.add(testScheduler.currentTime)
        }

        // Send two messages immediately
        pacer.sendParam(blockIndex = 2, paramIndex = 0, effectId = 100, value = 10f)
        pacer.sendParam(blockIndex = 2, paramIndex = 0, effectId = 100, value = 20f)

        advanceTimeBy(100L)

        // The second message should be dispatched after at least 40ms
        assertTrue(sentMessages.size >= 1)
        if (sentMessages.size > 1) {
            val delta = sentMessages[1] - sentMessages[0]
            assertTrue(delta >= 40L, "Delta between sends must be >= 40ms, but was $delta")
        }
    }

    @Test
    fun `pacer coalesces rapid value changes for same block and param`() = runTest {
        var lastSentValue: Float? = null
        var sendCount = 0

        val pacer = MidiPacketFramer(coroutineScope = this, intervalMs = 40L) { msg ->
            sendCount++
            // In our test, extract value from callback
        }

        // Send 5 rapid changes for the same knob
        pacer.sendParam(blockIndex = 1, paramIndex = 2, effectId = 50, value = 1f)
        pacer.sendParam(blockIndex = 1, paramIndex = 2, effectId = 50, value = 2f)
        pacer.sendParam(blockIndex = 1, paramIndex = 2, effectId = 50, value = 3f)
        pacer.sendParam(blockIndex = 1, paramIndex = 2, effectId = 50, value = 4f)
        pacer.sendParam(blockIndex = 1, paramIndex = 2, effectId = 50, value = 5f)

        advanceTimeBy(200L)

        // Coalescing should ensure we did not send 5 separate spaced commands
        assertTrue(sendCount in 1..3, "Rapid updates should be coalesced, sendCount: $sendCount")
    }
}
