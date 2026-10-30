package com.valeton.editor.usb

import com.valeton.editor.core.codec.SysExCodec
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.core.model.UserModelKind
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidMidiDriverUserModelNamesTest {

    private val allNameRequests = UserModelKind.entries.flatMap { kind -> (0 until kind.slotCount).map { kind to it } }

    private fun nameRequest(bytes: ByteArray): Pair<UserModelKind, Int>? {
        if (!SysExCodec.isSysEx(bytes, 0x11, 0x1C)) return null
        val payload = SysExCodec.nibbleDecode(bytes.copyOfRange(13, bytes.lastIndex))
        val recordId = (payload[0].toInt() and 0xFF) or ((payload[1].toInt() and 0xFF) shl 8)
        val kind = UserModelKind.entries.first { it.recordId == recordId }
        return kind to (payload[4].toInt() and 0xFF)
    }

    private fun nameResponse(kind: UserModelKind, index: Int, name: String): ByteArray {
        val payload = ByteArray(28)
        payload[0] = (kind.recordId and 0xFF).toByte()
        payload[1] = (kind.recordId shr 8).toByte()
        payload[2] = 0x18
        payload[4] = index.toByte()
        name.toByteArray(Charsets.US_ASCII).copyInto(payload, 12)
        return SysExCodec.buildFrame(0x12, payload)
    }

    private fun defaultName(kind: UserModelKind, index: Int) = "${kind.name.lowercase()}$index"

    /** Driver whose fake pedal answers each name request with [answer] (null = no response). */
    private fun TestScope.driverAnswering(
        sent: MutableList<ByteArray>,
        answer: (AndroidMidiDriver, UserModelKind, Int) -> Unit = { d, k, i -> d.handleIncomingMidi(nameResponse(k, i, defaultName(k, i))) }
    ): AndroidMidiDriver {
        lateinit var driver: AndroidMidiDriver
        driver = AndroidMidiDriver(
            coroutineScope = this,
            rawSender = { bytes ->
                sent.add(bytes.clone())
                nameRequest(bytes)?.let { (kind, index) -> answer(driver, kind, index) }
            },
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )
        return driver
    }

    @Test
    fun `readUserModelNames requests 20 IR then 10 SnapTone, one at a time`() = runTest {
        val sent = mutableListOf<ByteArray>()
        val driver = driverAnswering(sent) { _, _, _ -> }
        driver.startUserModelNamesRead()
        testScheduler.runCurrent()
        assertEquals(1, sent.count { nameRequest(it) != null }, "Next request must wait for the response")
        allNameRequests.forEach { (kind, index) ->
            driver.handleIncomingMidi(nameResponse(kind, index, defaultName(kind, index)))
            testScheduler.runCurrent()
        }
        testScheduler.advanceUntilIdle()
        assertEquals(allNameRequests, sent.mapNotNull(::nameRequest))
    }

    @Test
    fun `readUserModelNames publishes names keyed by effectId`() = runTest {
        val sent = mutableListOf<ByteArray>()
        val driver = driverAnswering(sent) { d, kind, index ->
            val name = when {
                kind == UserModelKind.IR && index == 0 -> "ir1"
                kind == UserModelKind.SNAPTONE && index == 5 -> "nam1"
                else -> defaultName(kind, index)
            }
            d.handleIncomingMidi(nameResponse(kind, index, name))
        }
        driver.startUserModelNamesRead()
        testScheduler.advanceUntilIdle()
        assertEquals("ir1", driver.userModelNames.value[168820736])
        assertEquals("nam1", driver.userModelNames.value[251658245])
        assertEquals(30, driver.userModelNames.value.size)
    }

    @Test
    fun `missing response is skipped after timeout and reading continues`() = runTest {
        val sent = mutableListOf<ByteArray>()
        val driver = driverAnswering(sent) { d, kind, index ->
            if (!(kind == UserModelKind.IR && index == 3)) d.handleIncomingMidi(nameResponse(kind, index, defaultName(kind, index)))
        }
        driver.startUserModelNamesRead()
        testScheduler.advanceUntilIdle()
        assertEquals(30, sent.count { nameRequest(it) != null })
        assertEquals(29, driver.userModelNames.value.size)
        assertFalse(driver.userModelNames.value.containsKey(UserModelKind.IR.effectId(3)))
    }

    @Test
    fun `late response is stored under its own index`() = runTest {
        val sent = mutableListOf<ByteArray>()
        val driver = driverAnswering(sent) { d, kind, index ->
            when {
                kind == UserModelKind.IR && index == 0 -> Unit
                kind == UserModelKind.IR && index == 1 -> {
                    d.handleIncomingMidi(nameResponse(UserModelKind.IR, 0, "late0"))
                    d.handleIncomingMidi(nameResponse(UserModelKind.IR, 1, "one"))
                }
                else -> d.handleIncomingMidi(nameResponse(kind, index, defaultName(kind, index)))
            }
        }
        driver.startUserModelNamesRead()
        testScheduler.advanceUntilIdle()
        assertEquals("late0", driver.userModelNames.value[UserModelKind.IR.effectId(0)])
        assertEquals("one", driver.userModelNames.value[UserModelKind.IR.effectId(1)])
    }

    @Test
    fun `disconnect during reading stops it and keeps names already read`() = runTest {
        val sent = mutableListOf<ByteArray>()
        val driver = driverAnswering(sent) { d, kind, index ->
            if (index == 4 && kind == UserModelKind.IR) d.disconnect()
            else d.handleIncomingMidi(nameResponse(kind, index, defaultName(kind, index)))
        }
        driver.startUserModelNamesRead()
        testScheduler.advanceUntilIdle()
        assertEquals(5, sent.count { nameRequest(it) != null })
        assertEquals(4, driver.userModelNames.value.size)

        driver.startUserModelNamesRead()
        testScheduler.runCurrent()
        assertEquals(UserModelKind.IR to 0, sent.mapNotNull(::nameRequest)[5], "A new connection restarts the read")
    }

    @Test
    fun `full library sync waits for user model names to finish`() = runTest {
        val sent = mutableListOf<ByteArray>()
        val driver = driverAnswering(sent) { _, _, _ -> }
        driver.startUserModelNamesRead()
        driver.startFullLibrarySync()
        testScheduler.advanceUntilIdle()
        val lastName = sent.indexOfLast { nameRequest(it) != null }
        val firstPresetRead = sent.indexOfFirst { SysExCodec.isSysEx(it, 0x11, 0x10) }
        assertTrue(firstPresetRead > lastName, "Preset reads started at $firstPresetRead before name read ended at $lastName")
        driver.cancelFullLibrarySync()
    }

    @Test
    fun `savePreset waits for user model names to finish`() = runTest {
        val sent = mutableListOf<ByteArray>()
        val driver = driverAnswering(sent) { _, _, _ -> }
        driver.startUserModelNamesRead()
        val save = async { driver.savePreset(GP200Preset(patchName = "Wait", effects = emptyList()), 3) }
        testScheduler.advanceUntilIdle()
        save.await()
        val lastName = sent.indexOfLast { nameRequest(it) != null }
        val firstOther = sent.indexOfFirst { nameRequest(it) == null }
        assertTrue(firstOther > lastName, "Save traffic started at $firstOther before name read ended at $lastName")
    }

    @Test
    fun `full sync triggered by the Connected state still waits for the name read`() = runTest {
        val sent = mutableListOf<ByteArray>()
        val driver = driverAnswering(sent) { _, _, _ -> }
        // Mirrors PresetViewModel: start a full sync as soon as the state turns Connected.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            driver.connectionState.collect { if (it is MidiConnectionState.Connected) driver.startFullLibrarySync() }
        }
        driver.markConnected("GP-200", 0)
        testScheduler.advanceUntilIdle()
        val lastName = sent.indexOfLast { nameRequest(it) != null }
        val firstPresetRead = sent.indexOfFirst { SysExCodec.isSysEx(it, 0x11, 0x10) }
        assertTrue(lastName >= 0, "Name read must run")
        assertTrue(firstPresetRead > lastName, "Preset reads started at $firstPresetRead before name read ended at $lastName")
        driver.cancelFullLibrarySync()
    }

    @Test
    fun `a pedal that never answers stops the read after three timeouts`() = runTest {
        val sent = mutableListOf<ByteArray>()
        val driver = driverAnswering(sent) { _, _, _ -> }
        driver.startUserModelNamesRead()
        testScheduler.advanceUntilIdle()
        assertEquals(3, sent.count { nameRequest(it) != null })
    }

    @Test
    fun `names are published once per read`() = runTest {
        val sent = mutableListOf<ByteArray>()
        val driver = driverAnswering(sent)
        val emissions = mutableListOf<Map<Int, String>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { driver.userModelNames.collect { emissions.add(it) } }
        driver.startUserModelNamesRead()
        testScheduler.advanceUntilIdle()
        assertEquals(listOf(0, 30), emissions.map { it.size })
    }

    @Test
    fun `restoreSlotName puts the confirmed name back in the preset name list`() = runTest {
        val driver = driverAnswering(mutableListOf())
        driver.sendPatchName("Temp")
        driver.restoreSlotName(0, "Lead Rock")
        assertEquals("Lead Rock", driver.presetNamesState.value[0])
    }
}
