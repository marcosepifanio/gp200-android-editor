package com.valeton.editor.usb

import com.valeton.editor.core.codec.SysExCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidMidiDriverTest {

    private var lastControlChange: Pair<Int, Int>? = null
    private var lastSentSysEx: ByteArray? = null
    private val allSentMessages = mutableListOf<ByteArray>()

    private val driver = AndroidMidiDriver(
        coroutineScope = CoroutineScope(Dispatchers.Unconfined),
        rawSender = { bytes: ByteArray ->
            allSentMessages.add(bytes.clone())
            if (bytes.size == 3 && (bytes[0].toInt() and 0xF0) == 0xB0) {
                val cc = bytes[1].toInt() and 0xFF
                val value = bytes[2].toInt() and 0xFF
                lastControlChange = Pair(cc, value)
            } else if (bytes.isNotEmpty() && bytes[0] == 0xF0.toByte()) {
                lastSentSysEx = bytes.clone()
            }
        }
    )

    private fun createFakePresetChunks(slot: Int, name: String, hardwareStyle: Boolean = true): List<ByteArray> {
        val decoded = ByteArray(1176) { 0 }
        val nameBytes = name.toByteArray(java.nio.charset.StandardCharsets.US_ASCII)
        for (i in 0 until minOf(16, nameBytes.size)) {
            decoded[28 + i] = nameBytes[i]
        }
        decoded[14] = 135.toByte() // Tempo 135
        decoded[15] = 0.toByte()
        for (b in 0 until 11) {
            val base = 120 + b * 72
            decoded[base + 4] = b.toByte()
            decoded[base + 5] = 1.toByte()
            decoded[base + 8] = (b + 1).toByte()
        }

        val nibbles = SysExCodec.nibbleEncode(decoded)
        val chunkNibbleLengths = intArrayOf(370, 370, 370, 370, 370, 370, 132)
        val chunkOffsets = intArrayOf(0, 313, 626, 1067, 1380, 1821, 2134)
        val chunks = mutableListOf<ByteArray>()
        var pos = 0
        for (i in 0 until 7) {
            val slice = nibbles.copyOfRange(pos, pos + chunkNibbleLengths[i])
            val header = byteArrayOf(
                0xF0.toByte(), 0x21, 0x25, 0x7E, 0x47, 0x50, 0x2D, 0x32,
                0x12, 0x18,
                if (hardwareStyle) 0x00.toByte() else slot.toByte(),
                (chunkOffsets[i] and 0xFF).toByte(),
                ((chunkOffsets[i] shr 8) and 0xFF).toByte()
            )
            chunks.add(header + slice + byteArrayOf(0xF7.toByte()))
            pos += chunkNibbleLengths[i]
        }
        return chunks
    }

    @Test
    fun `handleIncomingMidi decodes chunks when byte 10 is 00 as in real hardware`() = runTest {
        var presetLoadedEvent: PedalEvent.PresetLoaded? = null
        val testDriver = AndroidMidiDriver(
            coroutineScope = this,
            rawSender = {}
        )
        val job = launch {
            testDriver.pedalEvents.collect {
                if (it is PedalEvent.PresetLoaded) presetLoadedEvent = it
            }
        }

        val chunks = createFakePresetChunks(slot = 42, name = "Hardware Patch", hardwareStyle = true)
        testDriver.requestReadPreset(42)

        for (c in chunks) {
            testDriver.handleIncomingMidi(c)
        }

        testScheduler.advanceUntilIdle()

        assertTrue(presetLoadedEvent != null, "PresetLoaded must be emitted for slot 42 even if byte 10 is 0x00")
        assertEquals(42, presetLoadedEvent!!.slot)
        assertEquals("Hardware Patch", presetLoadedEvent!!.preset.patchName)

        job.cancel()
    }

    @Test
    fun `sendPresetChange sends CC0 Bank Select, Program Change, and SysEx change`() {
        allSentMessages.clear()
        driver.sendPresetChange(130) // Slot 130 -> Bank MSB = 1, PC = 2

        // CC 0 Bank Select: 0xB0, 0x00, 0x01
        val cc0 = allSentMessages.find { it.size == 3 && it[0] == 0xB0.toByte() && it[1] == 0x00.toByte() }
        assertTrue(cc0 != null, "Must send Bank Select CC 0")
        assertEquals(1.toByte(), cc0!![2], "Bank MSB for slot 130 must be 1")

        // Program Change: 0xC0, 0x02
        val pc = allSentMessages.find { it.size == 2 && it[0] == 0xC0.toByte() }
        assertTrue(pc != null, "Must send Program Change")
        assertEquals(2.toByte(), pc!![1], "Program Change for slot 130 must be 2")
    }

    @Test
    fun `sendDrumMenu sends CC 92 with 127 on and 0 off`() {
        driver.sendDrumMenu(true)
        assertEquals(Pair(92, 127), lastControlChange)

        driver.sendDrumMenu(false)
        assertEquals(Pair(92, 0), lastControlChange)
    }

    @Test
    fun `sendDrumPlay sends CC 93 and SysEx play stop`() {
        allSentMessages.clear()
        driver.sendDrumPlay(true)
        assertEquals(Pair(93, 127), lastControlChange)
        val playSysex = allSentMessages.firstOrNull { it.isNotEmpty() && it[0] == 0xF0.toByte() && it.size == 30 }
        assertTrue(playSysex != null, "Must send 30-byte play SysEx")
        val decPlay = SysExCodec.nibbleDecode(playSysex!!.copyOfRange(13, 29))
        assertEquals(0x11.toByte(), decPlay[0])
        assertEquals(0x10.toByte(), decPlay[1])
        assertEquals(0x01.toByte(), decPlay[6])

        allSentMessages.clear()
        driver.sendDrumPlay(false)
        assertEquals(Pair(93, 0), lastControlChange)
        val stopSysex = allSentMessages.firstOrNull { it.isNotEmpty() && it[0] == 0xF0.toByte() && it.size == 30 }
        assertTrue(stopSysex != null, "Must send 30-byte stop SysEx")
        val decStop = SysExCodec.nibbleDecode(stopSysex!!.copyOfRange(13, 29))
        assertEquals(0x00.toByte(), decStop[6])
    }

    @Test
    fun `sendDrumPattern sends CC 94 clamped between 0 and 99 and SysEx pattern`() {
        allSentMessages.clear()
        driver.sendDrumPattern(42)
        assertEquals(Pair(94, 42), lastControlChange)
        val patSysex = allSentMessages.firstOrNull { it.isNotEmpty() && it[0] == 0xF0.toByte() && it.size == 30 }
        assertTrue(patSysex != null, "Must send 30-byte pattern SysEx")
        val dec = SysExCodec.nibbleDecode(patSysex!!.copyOfRange(13, 29))
        assertEquals(0x00.toByte(), dec[4]) // Target = 0 (Pattern)
        assertEquals(42.toByte(), dec[6]) // Value = 42

        driver.sendDrumPattern(-5)
        assertEquals(Pair(94, 0), lastControlChange)

        driver.sendDrumPattern(150)
        assertEquals(Pair(94, 99), lastControlChange)
    }

    @Test
    fun `sendDrumVolume sends CC 95 clamped between 0 and 100 and SysEx volume`() {
        allSentMessages.clear()
        driver.sendDrumVolume(80)
        assertEquals(Pair(95, 80), lastControlChange)
        val volSysex = allSentMessages.firstOrNull { it.isNotEmpty() && it[0] == 0xF0.toByte() && it.size == 30 }
        assertTrue(volSysex != null, "Must send 30-byte volume SysEx")
        val dec = SysExCodec.nibbleDecode(volSysex!!.copyOfRange(13, 29))
        assertEquals(0x01.toByte(), dec[4]) // Target = 1 (Volume)
        assertEquals(80.toByte(), dec[6]) // Value = 80

        driver.sendDrumVolume(120)
        assertEquals(Pair(95, 100), lastControlChange)
    }

    @Test
    fun `sendDrumSync sends Patch Setting SysEx and Drum Sync SysEx`() = runTest {
        val sent = mutableListOf<ByteArray>()
        val testDriver = AndroidMidiDriver(
            coroutineScope = this,
            rawSender = { sent.add(it.clone()) }
        )

        testDriver.sendDrumSync(enabled = true, bpm = 120)
        advanceTimeBy(200L)

        // Verify CC 20 is NOT sent (CC 20 corrupts Quick Access Para 3)
        val hasCc20 = sent.any {
            it.size == 3 && (it[0].toInt() and 0xF0) == 0xB0 && it[1].toInt() == 20
        }
        assertTrue(!hasCc20, "CC 20 must NOT be sent")

        // 30-byte Patch Setting Param SysEx (0x1008 LE, target = 0x19)
        val patchSyncParamOn = sent.firstOrNull {
            it.size == 30 && SysExCodec.nibbleDecode(it.copyOfRange(13, 29))[0] == 0x08.toByte()
        }
        assertTrue(patchSyncParamOn != null, "Patch Setting Param SysEx (30 bytes, 0x1008) must be sent")
        val decPatchParamOn = SysExCodec.nibbleDecode(patchSyncParamOn!!.copyOfRange(13, 29))
        assertEquals(0x19.toByte(), decPatchParamOn[4]) // Target = 0x19 (25)
        assertEquals(0x01.toByte(), decPatchParamOn[6]) // Value = 1

        // 30-byte Drum Sync SysEx (0x1011 LE, target = 2)
        val drumSyncOn = sent.firstOrNull {
            it.size == 30 && SysExCodec.nibbleDecode(it.copyOfRange(13, 29))[0] == 0x11.toByte()
        }
        assertTrue(drumSyncOn != null, "Drum Sync SysEx (30 bytes, 0x1011) must be sent")
        val decDrumOn = SysExCodec.nibbleDecode(drumSyncOn!!.copyOfRange(13, 29))
        assertEquals(0x02.toByte(), decDrumOn[4]) // Target = 2 (Drum Sync)
        assertEquals(0x01.toByte(), decDrumOn[6]) // Value = 1

        sent.clear()
        testDriver.sendDrumSync(enabled = false, bpm = 120)
        advanceTimeBy(200L)

        val drumSyncOff = sent.firstOrNull {
            it.size == 30 && SysExCodec.nibbleDecode(it.copyOfRange(13, 29))[0] == 0x11.toByte()
        }
        assertTrue(drumSyncOff != null, "Drum Sync SysEx (30 bytes) must be sent for sync OFF")
        val decDrumOff = SysExCodec.nibbleDecode(drumSyncOff!!.copyOfRange(13, 29))
        assertEquals(0x02.toByte(), decDrumOff[4])
        assertEquals(0x00.toByte(), decDrumOff[6]) // Value = 0
    }

    @Test
    fun `sendDrumBpm sends CC 73 and 74, CC 16, Patch Setting SysEx and Drum Param SysEx`() {
        allSentMessages.clear()
        driver.sendDrumBpm(bpm = 140, isSyncWithPatch = false)

        // 30-byte Drum Param SysEx with target 2 (BPM) and value 140 (0x8C)
        val drumBpm = allSentMessages.firstOrNull { it.size == 30 }
        assertTrue(drumBpm != null, "Drum Param SysEx (30 bytes) must be sent")
        val decBpm = SysExCodec.nibbleDecode(drumBpm!!.copyOfRange(13, 29))
        assertEquals(0x02.toByte(), decBpm[4]) // Target = 2 (BPM)
        assertEquals(140.toByte(), decBpm[6]) // Value = 140

        // 46-byte Patch Setting SysEx with target 0x01 (Patch Tempo)
        val patchTempo = allSentMessages.firstOrNull { it.size == 46 }
        assertTrue(patchTempo != null, "Patch Setting SysEx (46 bytes) must be sent")
        val decPatch = SysExCodec.nibbleDecode(patchTempo!!.copyOfRange(13, 45))
        assertEquals(0x01.toByte(), decPatch[12]) // Target = 0x01
        assertEquals(140.toByte(), decPatch[14]) // Value = 140

        // Verify CC 16 was sent for Quick Access Knob 1
        val scaledExpected = ((140 - 40) * 100 / 210).coerceIn(0, 100)
        val hasCc16 = allSentMessages.any {
            it.size == 3 && (it[0].toInt() and 0xF0) == 0xB0 && it[1].toInt() == 16 && it[2].toInt() == scaledExpected
        }
        assertTrue(hasCc16, "CC 16 with scaled value must be sent")

        // Verify CC 73 and 74 (Tempo MSB/LSB for 140 BPM: CC 73=1, CC 74=12)
        val hasCc73 = allSentMessages.any {
            it.size == 3 && (it[0].toInt() and 0xF0) == 0xB0 && it[1].toInt() == 73 && it[2].toInt() == 1
        }
        val hasCc74 = allSentMessages.any {
            it.size == 3 && (it[0].toInt() and 0xF0) == 0xB0 && it[1].toInt() == 74 && it[2].toInt() == 12
        }
        assertTrue(hasCc73, "CC 73 MSB must be sent")
        assertTrue(hasCc74, "CC 74 LSB must be sent")
    }

    @Test
    fun `syncCurrentPatch sends buildReadRequest directly without duplicate nameReq`() {
        driver.syncCurrentPatch(14)
        val sent = lastSentSysEx
        assertTrue(sent != null)
        assertTrue(SysExCodec.isSysEx(sent!!, 0x11, 0x10)) // Opcode 0x11, sub 0x10 (full read)
        assertEquals(0x10.toByte(), sent[9]) // 0x10, NOT 0x20
    }

    @Test
    fun `handleIncomingMidi chunk 0 emits PresetNameLoaded and 7 chunks emit PresetLoaded`() {
        val events = mutableListOf<PedalEvent>()
        val job = CoroutineScope(Dispatchers.Unconfined).launch {
            driver.pedalEvents.collect { events.add(it) }
        }

        val chunks = createFakePresetChunks(slot = 15, name = "Crunch Classic")
        driver.requestReadPreset(15)

        // Deliver chunk 0
        driver.handleIncomingMidi(chunks[0])
        val nameLoaded = events.filterIsInstance<PedalEvent.PresetNameLoaded>().firstOrNull()
        assertTrue(nameLoaded != null, "PresetNameLoaded must be emitted immediately on chunk 0")
        assertEquals(15, nameLoaded!!.slot)
        assertEquals("Crunch Classic", nameLoaded.name)

        // Deliver remaining chunks
        for (i in 1 until 7) {
            driver.handleIncomingMidi(chunks[i])
        }

        val presetLoaded = events.filterIsInstance<PedalEvent.PresetLoaded>().firstOrNull()
        assertTrue(presetLoaded != null, "PresetLoaded must be emitted after assembling 7 chunks")
        assertEquals(15, presetLoaded!!.slot)
        assertEquals("Crunch Classic", presetLoaded.preset.patchName)
        assertEquals(135, presetLoaded.preset.patchTempo)
        assertEquals(11, presetLoaded.preset.effects.size)

        job.cancel()
    }

    @Test
    fun `handleIncomingMidi rejects missing trailing chunk until it arrives`() = runTest {
        var presetLoadedEvent: PedalEvent.PresetLoaded? = null
        val testDriver = AndroidMidiDriver(
            coroutineScope = this,
            rawSender = {}
        )
        val job = launch {
            testDriver.pedalEvents.collect {
                if (it is PedalEvent.PresetLoaded) presetLoadedEvent = it
            }
        }

        val chunks = createFakePresetChunks(slot = 20, name = "Modern HighGain")
        testDriver.requestReadPreset(20)

        // Deliver only first 6 chunks
        for (i in 0 until 6) {
            testDriver.handleIncomingMidi(chunks[i])
        }

        // Advance time to allow debounce timer (150ms) to fire
        advanceTimeBy(200L)

        assertEquals(null, presetLoadedEvent, "Incomplete preset must not be emitted")
        testDriver.handleIncomingMidi(chunks[6])
        testScheduler.runCurrent()
        assertTrue(presetLoadedEvent != null)
        assertEquals(20, presetLoadedEvent!!.slot)
        assertEquals("Modern HighGain", presetLoadedEvent!!.preset.patchName)
        assertEquals(11, presetLoadedEvent!!.preset.effects.size)

        job.cancel()
    }

    @Test
    fun `handleIncomingMidi rejects missing middle and first chunks`() = runTest {
        for (missing in listOf(0, 3)) {
            val events = mutableListOf<PedalEvent.PresetLoaded>()
            val testDriver = AndroidMidiDriver(coroutineScope = this, rawSender = {})
            val collector = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
                testDriver.pedalEvents.collect { if (it is PedalEvent.PresetLoaded) events.add(it) }
            }
            testDriver.requestReadPreset(20)
            val chunks = createFakePresetChunks(20, "Complete")
            chunks.filterIndexed { index, _ -> index != missing }.forEach(testDriver::handleIncomingMidi)
            advanceTimeBy(200)
            testScheduler.runCurrent()
            assertTrue(events.isEmpty(), "Missing chunk $missing must be rejected")
            testDriver.handleIncomingMidi(chunks[missing])
            testScheduler.runCurrent()
            assertEquals("Complete", events.single().preset.patchName)
            collector.cancel()
        }
    }

    @Test
    fun `startFullLibrarySync progresses across slots and emits FullLibrarySynced`() = runTest {
        val syncedEvents = mutableListOf<PedalEvent>()
        val progressList = mutableListOf<FullSyncProgress>()
        lateinit var testDriver: AndroidMidiDriver
        testDriver = AndroidMidiDriver(
            coroutineScope = this,
            rawSender = { bytes ->
                if (bytes.size >= 27 && SysExCodec.isSysEx(bytes, 0x11, 0x10)) {
                    val sh = bytes[25].toInt() and 0x0F
                    val sl = bytes[26].toInt() and 0x0F
                    val slot = (sh shl 4) or sl
                    val chunks = createFakePresetChunks(slot, "P$slot")
                    for (c in chunks) {
                        testDriver.handleIncomingMidi(c)
                    }
                }
            },
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )

        val progressJob = launch {
            testDriver.fullSyncProgress.collect { progress ->
                if (progress != null) progressList.add(progress)
            }
        }
        val eventJob = launch {
            testDriver.pedalEvents.collect {
                if (it is PedalEvent.FullLibrarySynced) syncedEvents.add(it)
            }
        }

        testDriver.startFullLibrarySync()
        testScheduler.advanceUntilIdle()

        assertEquals(1, syncedEvents.size, "Should emit FullLibrarySynced once")
        val fullSynced = syncedEvents[0] as PedalEvent.FullLibrarySynced
        assertEquals(256, fullSynced.presets.size)
        assertEquals("P0", fullSynced.presets[0]?.patchName)
        assertEquals("P255", fullSynced.presets[255]?.patchName)

        val lastProgress = testDriver.fullSyncProgress.value
        assertTrue(lastProgress != null)
        assertTrue(lastProgress!!.isCompleted)
        assertEquals(256, lastProgress.currentSlot)
        assertEquals("Concluído", lastProgress.currentPatchName)
        assertEquals(1f, lastProgress.progressFraction)

        progressJob.cancel()
        eventJob.cancel()
    }

    @Test
    fun `startFullLibrarySync preserves currentActiveSlot and does not emit PresetLoaded`() = runTest {
        val presetLoadedEvents = mutableListOf<PedalEvent.PresetLoaded>()
        lateinit var testDriver: AndroidMidiDriver
        testDriver = AndroidMidiDriver(
            coroutineScope = this,
            rawSender = { bytes ->
                if (bytes.size >= 27 && SysExCodec.isSysEx(bytes, 0x11, 0x10)) {
                    val sh = bytes[25].toInt() and 0x0F
                    val sl = bytes[26].toInt() and 0x0F
                    val slot = (sh shl 4) or sl
                    val chunks = createFakePresetChunks(slot, "P$slot")
                    for (c in chunks) {
                        testDriver.handleIncomingMidi(c)
                    }
                }
            },
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )
        testDriver.currentActiveSlot = 12

        val eventJob = launch {
            testDriver.pedalEvents.collect {
                if (it is PedalEvent.PresetLoaded) presetLoadedEvents.add(it)
            }
        }

        testDriver.startFullLibrarySync()
        testScheduler.advanceUntilIdle()

        assertEquals(12, testDriver.currentActiveSlot, "Active slot must remain unchanged during full sync")
        assertTrue(presetLoadedEvents.isEmpty(), "PresetLoaded events must be suppressed during full sync")

        eventJob.cancel()
    }

    @Test
    fun `startFullLibrarySync leaves failed slots absent and unconfirmed`() = runTest {
        val syncedEvents = mutableListOf<PedalEvent.FullLibrarySynced>()
        lateinit var testDriver: AndroidMidiDriver
        testDriver = AndroidMidiDriver(
            coroutineScope = this,
            rawSender = { bytes ->
                if (bytes.size >= 27 && SysExCodec.isSysEx(bytes, 0x11, 0x10)) {
                    val sh = bytes[25].toInt() and 0x0F
                    val sl = bytes[26].toInt() and 0x0F
                    val slot = (sh shl 4) or sl
                    // Drop chunks for slot 5 to simulate timeout
                    if (slot != 5) {
                        val chunks = createFakePresetChunks(slot, "P$slot")
                        for (c in chunks) {
                            testDriver.handleIncomingMidi(c)
                        }
                    }
                }
            },
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )

        val eventJob = launch {
            testDriver.pedalEvents.collect {
                if (it is PedalEvent.FullLibrarySynced) syncedEvents.add(it)
            }
        }

        testDriver.startFullLibrarySync()
        testScheduler.advanceUntilIdle()

        assertEquals(1, syncedEvents.size)
        val fullSynced = syncedEvents[0]
        assertEquals(255, fullSynced.presets.size)
        assertEquals(null, fullSynced.presets[5])
        assertEquals(255, testDriver.namesSyncProgress.value)
        assertEquals(false, testDriver.fullSyncProgress.value?.isCompleted)
        assertEquals("P0", fullSynced.presets[0]?.patchName)
        assertEquals("P6", fullSynced.presets[6]?.patchName)

        eventJob.cancel()
    }

    @Test
    fun `startFullLibrarySync does not run when rawSender is null and device is not connected`() = runTest {
        val testDriver = AndroidMidiDriver(
            coroutineScope = this,
            rawSender = null,
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )

        testDriver.startFullLibrarySync()
        testScheduler.advanceUntilIdle()

        assertEquals(null, testDriver.fullSyncProgress.value)
    }

    @Test
    fun `cancelFullLibrarySync cancels sync and resets fullSyncProgress to null`() = runTest {
        lateinit var testDriver: AndroidMidiDriver
        testDriver = AndroidMidiDriver(
            coroutineScope = this,
            rawSender = {},
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )

        testDriver.startFullLibrarySync()
        testScheduler.runCurrent()

        testDriver.cancelFullLibrarySync()
        testScheduler.runCurrent()

        assertEquals(null, testDriver.fullSyncProgress.value)
    }

    @Test
    fun `startFullLibrarySync completes namesSyncProgress to 256 upon completion`() = runTest {
        lateinit var testDriver: AndroidMidiDriver
        testDriver = AndroidMidiDriver(
            coroutineScope = this,
            rawSender = { bytes ->
                if (bytes.size >= 27 && SysExCodec.isSysEx(bytes, 0x11, 0x10)) {
                    val sh = bytes[25].toInt() and 0x0F
                    val sl = bytes[26].toInt() and 0x0F
                    val slot = (sh shl 4) or sl
                    val chunks = createFakePresetChunks(slot, "P$slot")
                    for (c in chunks) {
                        testDriver.handleIncomingMidi(c)
                    }
                }
            },
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )

        testDriver.startFullLibrarySync()
        testScheduler.advanceUntilIdle()

        assertEquals(256, testDriver.namesSyncProgress.value, "namesSyncProgress must be 256 after full sync completes")
    }

    @Test
    fun `requestReadPreset is ignored while full sync is running`() = runTest {
        var requestedCount = 0
        lateinit var testDriver: AndroidMidiDriver
        testDriver = AndroidMidiDriver(
            coroutineScope = this,
            rawSender = { bytes ->
                if (bytes.size >= 27 && SysExCodec.isSysEx(bytes, 0x11, 0x10)) {
                    requestedCount++
                }
            },
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )

        testDriver.startFullLibrarySync()
        testScheduler.runCurrent()

        val initialCount = requestedCount
        testDriver.requestReadPreset(42)
        testScheduler.runCurrent()

        assertEquals(initialCount, requestedCount, "requestReadPreset must be ignored during full sync")
        testDriver.cancelFullLibrarySync()
    }

    @Test
    fun `preloadCachedNames marks slots confirmed, sets namesSyncProgress to 256, and skips startPresetNamesScan`() = runTest {
        var namesScanRequested = false
        val testDriver = AndroidMidiDriver(
            coroutineScope = this,
            rawSender = { bytes ->
                if (SysExCodec.isSysEx(bytes, 0x11, 0x14)) {
                    namesScanRequested = true
                }
            }
        )

        val cachedMap = (0..255).associateWith { "Cached $it" }
        testDriver.preloadCachedNames(cachedMap)

        assertEquals(256, testDriver.namesSyncProgress.value)
        assertEquals("Cached 0", testDriver.presetNamesState.value[0])
        assertEquals("Cached 255", testDriver.presetNamesState.value[255])

        testDriver.startPresetNamesScan()
        testScheduler.advanceUntilIdle()

        org.junit.jupiter.api.Assertions.assertFalse(namesScanRequested, "startPresetNamesScan should do nothing when all 256 slots are preloaded")
    }

    @Test
    fun `startFullLibrarySync resets confirmed slots and namesSyncProgress to 0`() = runTest {
        val testDriver = AndroidMidiDriver(
            coroutineScope = this,
            rawSender = {},
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )

        val cachedMap = (0..255).associateWith { "Cached $it" }
        testDriver.preloadCachedNames(cachedMap)
        assertEquals(256, testDriver.namesSyncProgress.value)

        testDriver.startFullLibrarySync()
        testScheduler.runCurrent()

        assertEquals(0, testDriver.namesSyncProgress.value, "namesSyncProgress must reset to 0 when starting full resync")
        testDriver.cancelFullLibrarySync()
    }

    @Test
    fun `sendSaveCommit updates activeSlot and suppresses echoes`() = runTest {
        var requestedReadSlot: Int? = null
        val testDriver = AndroidMidiDriver(
            coroutineScope = this,
            rawSender = { bytes ->
                if (SysExCodec.isSysEx(bytes, 0x11, 0x00)) {
                    requestedReadSlot = bytes[10].toInt() and 0xFF
                }
            }
        )

        testDriver.sendSaveCommit("Patch 26C", 102)
        testScheduler.advanceUntilIdle()

        // Verify active slot was updated
        assertEquals(102, testDriver.currentActiveSlot)

        // Simulate an echo of state dump arriving during echo suppression window
        // decoded[0:10] = [07 10 48 00 00 06 01 00 66 00] (slot 102 = 0x66)
        val nibbles = byteArrayOf(
            0x00, 0x07, 0x01, 0x00, 0x04, 0x08, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x06, 0x00, 0x01, 0x00, 0x00, 0x00, 0x06, 0x00, 0x06
        )
        val stateDumpChunk = byteArrayOf(
            0xF0.toByte(), 0x21, 0x25, 0x7E, 0x47, 0x50, 0x2D, 0x32, 0x12, 0x4E,
            0x06, 0x00, 0x00
        ) + nibbles + byteArrayOf(0xF7.toByte())

        testDriver.handleIncomingMidi(stateDumpChunk)
        testScheduler.advanceUntilIdle()

        // Read must NOT have been requested because it's suppressed during save
        assertEquals(null, requestedReadSlot, "State dump echo during save must not trigger read request")
    }

    @Test
    fun `sendAuthorName sends 78-byte SysEx message with address 0x0B70`() = runTest {
        var sentSysEx: ByteArray? = null
        val testDriver = AndroidMidiDriver(
            coroutineScope = this,
            rawSender = { bytes ->
                sentSysEx = bytes
            }
        )

        testDriver.sendAuthorName("SRV")
        testScheduler.advanceUntilIdle()

        assertTrue(sentSysEx != null)
        assertEquals(78, sentSysEx!!.size)
        assertEquals(0xF0.toByte(), sentSysEx!![0])
        assertEquals(0x12.toByte(), sentSysEx!![8])
        assertEquals(0x20.toByte(), sentSysEx!![9])
        assertEquals(0xF7.toByte(), sentSysEx!![77])

        val nibbles = sentSysEx!!.copyOfRange(13, 77)
        val decoded = SysExCodec.nibbleDecode(nibbles)
        assertEquals(0x70.toByte(), decoded[14]) // 0x0B70 address
        val author = String(decoded, 16, 3, java.nio.charset.StandardCharsets.US_ASCII)
        assertEquals("SRV", author)
    }
    @Test
    fun `savePreset waits for complete readback after commit`() = runTest {
        val sent = mutableListOf<ByteArray>()
        val testDriver = AndroidMidiDriver(coroutineScope = this, rawSender = { sent.add(it) })
        val pending = async {
            testDriver.savePreset(com.valeton.editor.core.model.GP200Preset(patchName = "Verified", effects = emptyList()), 42)
        }
        testScheduler.runCurrent()
        while (sent.none { SysExCodec.isSysEx(it, 0x11, 0x10) } && testScheduler.currentTime < 10000) {
            advanceTimeBy(40)
            testScheduler.runCurrent()
        }
        assertTrue(sent.any { SysExCodec.isSysEx(it, 0x11, 0x10) }, "Save must request readback")
        assertEquals(false, pending.isCompleted)
        val chunks = createFakePresetChunks(42, "Verified")
        chunks.dropLast(1).forEach(testDriver::handleIncomingMidi)
        advanceTimeBy(200)
        testScheduler.runCurrent()
        assertEquals(false, pending.isCompleted)
        testDriver.handleIncomingMidi(chunks.last())
        testScheduler.runCurrent()
        assertEquals("Verified", pending.await()?.patchName)
    }

    @Test
    fun `verification retry only reads and never writes or commits`() = runTest {
        val sent = mutableListOf<ByteArray>()
        val testDriver = AndroidMidiDriver(coroutineScope = this, rawSender = { sent.add(it) })
        val pending = async { testDriver.readPresetForVerification(42) }
        advanceTimeBy(300)
        testScheduler.runCurrent()
        assertTrue(sent.isNotEmpty())
        assertTrue(sent.all { SysExCodec.isSysEx(it, 0x11, 0x10) })
        createFakePresetChunks(42, "Verified").forEach(testDriver::handleIncomingMidi)
        testScheduler.runCurrent()
        assertEquals("Verified", pending.await()?.patchName)
    }

    @Test
    fun `savePreset returns null when readback times out`() = runTest {
        val testDriver = AndroidMidiDriver(coroutineScope = this, rawSender = {})
        val pending = async {
            testDriver.savePreset(com.valeton.editor.core.model.GP200Preset(patchName = "Timeout", effects = emptyList()), 3)
        }
        testScheduler.advanceUntilIdle()
        assertEquals(null, pending.await())
    }

    @Test
    fun `save flushes pending parameter updates before commit`() = runTest {
        val sent = mutableListOf<ByteArray>()
        val testDriver = AndroidMidiDriver(coroutineScope = this, rawSender = { sent.add(it) })
        for (param in 0..5) testDriver.sendParamChange(0, param, 1, param.toFloat())
        val save = async {
            testDriver.savePreset(com.valeton.editor.core.model.GP200Preset(patchName = "Flush", effects = emptyList()), 0)
        }
        testScheduler.advanceUntilIdle()
        val commitIndex = sent.indexOfFirst { it.contentEquals(SysExCodec.buildSaveCommit("Flush", 0)) }
        assertTrue(commitIndex >= 0)
        val parameterMessages = sent.take(commitIndex).filter { it.size == 62 && !it.contentEquals(SysExCodec.buildSaveCommit("Flush", 0)) }
        assertEquals(6, parameterMessages.size)
        save.await()
    }

    @Test
    fun `older read timeout cannot discard save verification chunks for same slot`() = runTest {
        val testDriver = AndroidMidiDriver(coroutineScope = this, rawSender = {})
        testDriver.requestReadPreset(3)
        testScheduler.runCurrent()
        advanceTimeBy(100)
        val save = async {
            testDriver.savePreset(com.valeton.editor.core.model.GP200Preset(patchName = "Verified", effects = emptyList()), 3)
        }
        advanceTimeBy(1300)
        testScheduler.runCurrent()
        val chunks = createFakePresetChunks(3, "Verified")
        chunks.dropLast(1).forEach(testDriver::handleIncomingMidi)
        advanceTimeBy(200)
        testScheduler.runCurrent()
        testDriver.handleIncomingMidi(chunks.last())
        testScheduler.advanceUntilIdle()
        assertEquals("Verified", save.await()?.patchName)
    }

    @Test
    fun `save sends known mappings before commit and preserves other CTRL records`() = runTest {
        val preset = com.valeton.editor.core.codec.PRSTDecoder(
            java.io.File("../core-protocol/src/test/resources/fixtures/clean.prst").readBytes()
        ).decode()
        val sent = mutableListOf<ByteArray>()
        val testDriver = AndroidMidiDriver(coroutineScope = this, rawSender = { sent.add(it) })
        val save = async { testDriver.savePreset(preset, 0) }
        testScheduler.advanceUntilIdle()
        val commit = sent.indexOfFirst { it.contentEquals(SysExCodec.buildSaveCommit(preset.patchName, 0)) }
        val records = sent.take(commit).mapNotNull { frame ->
            if (frame.size < 46 || frame[8] != 0x12.toByte()) null else
                SysExCodec.nibbleDecode(frame.copyOfRange(13, frame.lastIndex)).takeIf {
                    it.size >= 16 && (it[8] == 0x10.toByte() || it[8] == 0xf.toByte())
                }
        }
        assertEquals(11, records.size)
        val expectedTail = java.io.File("../core-protocol/src/test/resources/fixtures/clean.prst").readBytes().copyOfRange(0x448, 0x4c0)
        val actualTail = records.flatMap { it.drop(8) }.toByteArray()
        org.junit.jupiter.api.Assertions.assertArrayEquals(expectedTail, actualTail)
        save.await()
    }

    @Test
    fun `quick assignment message is never treated as a stomp toggle`() = runTest {
        val events = mutableListOf<PedalEvent>()
        val testDriver = AndroidMidiDriver(coroutineScope = this, rawSender = {})
        val collector = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            testDriver.pedalEvents.collect { events.add(it) }
        }
        testDriver.handleIncomingMidi(SysExCodec.buildQuickKnobAssignment(0, com.valeton.editor.core.model.QuickKnobAssignment(3, 2)))
        testScheduler.runCurrent()
        assertTrue(events.filterIsInstance<PedalEvent.ToggleChanged>().isEmpty())
        collector.cancel()
    }

    @Test
    fun `save replays pending effect state before commit using last confirmed device read`() = runTest {
        val bytes = java.io.File("../core-protocol/src/test/resources/fixtures/clean.prst").readBytes()
        val decoded = bytes.copyOfRange(0x28, 0x4c0)
        val baseline = SysExCodec.parsePresetFromDecoded(decoded)
        val sent = mutableListOf<ByteArray>()
        val testDriver = AndroidMidiDriver(coroutineScope = this, rawSender = { sent.add(it) })
        testDriver.requestReadPreset(0)
        testScheduler.runCurrent()
        for (offset in 0 until decoded.size step 185) {
            val chunk = SysExCodec.buildFrame(0x12, decoded.copyOfRange(offset, minOf(offset + 185, decoded.size)), offset)
            chunk[9] = 0x18
            chunk[10] = 9
            testDriver.handleIncomingMidi(chunk)
        }
        testScheduler.runCurrent()
        val changed = baseline.copy(effects = baseline.effects.map { slot ->
            if (slot.slotIndex == 0) slot.copy(enabled = !slot.enabled,
                params = slot.params.toMutableList().apply { set(0, get(0) + 7f) }) else slot
        })
        sent.clear()
        val save = async { testDriver.savePreset(changed, 0) }
        testScheduler.advanceUntilIdle()
        val commit = sent.indexOfFirst { it.contentEquals(SysExCodec.buildSaveCommit(changed.patchName, 0)) }
        assertTrue(commit > 0)
        assertTrue(sent.take(commit).any { it.contentEquals(SysExCodec.buildToggle(0, changed.effects.find { it.slotIndex == 0 }!!.enabled)) }, "Save must reapply intended enabled state")
        val expected = changed.effects.find { it.slotIndex == 0 }!!
        assertTrue(sent.take(commit).any { it.contentEquals(SysExCodec.buildParamChange(0, 0, expected.effectId, expected.params[0])) }, "Save must reapply pending parameter")
        assertEquals(1, sent.count { it.contentEquals(SysExCodec.buildSaveCommit(changed.patchName, 0)) })
        save.await()
    }

}
