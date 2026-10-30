package com.valeton.editor.viewmodel

import com.valeton.editor.core.model.DrumGenre
import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.repository.PresetLibraryRepository
import com.valeton.editor.repository.UserModelNamesRepository
import com.valeton.editor.usb.FullSyncProgress
import com.valeton.editor.usb.MidiConnectionManager
import com.valeton.editor.usb.MidiConnectionState
import com.valeton.editor.usb.PedalEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class FakeMidiManager : MidiConnectionManager {
    val _connectionState = MutableStateFlow<MidiConnectionState>(MidiConnectionState.Connected("GP-200"))
    override val connectionState: StateFlow<MidiConnectionState> = _connectionState

    val _fullSyncProgress = MutableStateFlow<FullSyncProgress?>(null)
    override val fullSyncProgress: StateFlow<FullSyncProgress?> = _fullSyncProgress

    val _userModelNames = MutableStateFlow<Map<Int, String>>(emptyMap())
    override val userModelNames: StateFlow<Map<Int, String>> = _userModelNames

    var fullSyncStarted = false
    var fullSyncCancelled = false
    override fun startFullLibrarySync() { fullSyncStarted = true }
    override fun cancelFullLibrarySync() { fullSyncCancelled = true }

    val _pedalEvents = MutableSharedFlow<PedalEvent>(extraBufferCapacity = 64)
    override val pedalEvents: SharedFlow<PedalEvent> = _pedalEvents

    override val presetNamesState: StateFlow<List<String>> =
        MutableStateFlow((0..255).map { "Patch %02d".format(it) })
    override val namesSyncProgress: StateFlow<Int> = MutableStateFlow(0)

    val quickAssignments = mutableListOf<Pair<Int, com.valeton.editor.core.model.QuickKnobAssignment>>()
    val ctrlAssignments = mutableListOf<Pair<Int, com.valeton.editor.core.model.CtrlAssignment>>()
    override fun sendQuickKnobAssignment(index: Int, assignment: com.valeton.editor.core.model.QuickKnobAssignment) { quickAssignments.add(index to assignment) }
    override fun sendCtrlAssignment(index: Int, assignment: com.valeton.editor.core.model.CtrlAssignment) { ctrlAssignments.add(index to assignment) }

    val toggles = mutableListOf<Pair<Int, Boolean>>()
    val params = mutableListOf<List<Any>>()
    val reorders = mutableListOf<List<Int>>()
    val saves = mutableListOf<Pair<String, Int>>()
    val effectChanges = mutableListOf<Pair<Int, Int>>()
    val requestedReads = mutableListOf<Int>()
    val patchNamesSent = mutableListOf<String>()
    val authorNamesSent = mutableListOf<String>()
    val presetChangesSent = mutableListOf<Int>()
    val controlChangesSent = mutableListOf<Triple<Int, Int, Int>>()
    val volumesSent = mutableListOf<Int>()
    val pansSent = mutableListOf<Int>()
    val ctrlsTriggered = mutableListOf<Pair<Int, Boolean>>()
    val temposSent = mutableListOf<Int>()
    var tapsSent = 0
    var drumMenuEnabled: Boolean? = null
    var drumPlaying: Boolean? = null
    val drumPatternsSent = mutableListOf<Int>()
    val drumVolumesSent = mutableListOf<Int>()
    val drumSyncsSent = mutableListOf<Pair<Boolean, Int>>()
    val drumBpmsSent = mutableListOf<Pair<Int, Boolean>>()

    override fun sendDrumMenu(enabled: Boolean) { drumMenuEnabled = enabled }
    override fun sendDrumPlay(playing: Boolean) { drumPlaying = playing }
    override fun sendDrumPattern(patternId: Int) { drumPatternsSent.add(patternId) }
    override fun sendDrumVolume(volume: Int) { drumVolumesSent.add(volume) }
    override fun sendDrumSync(enabled: Boolean, bpm: Int) { drumSyncsSent.add(Pair(enabled, bpm)) }
    override fun sendDrumBpm(bpm: Int, isSyncWithPatch: Boolean) { drumBpmsSent.add(Pair(bpm, isSyncWithPatch)) }

    override fun sendControlChange(cc: Int, value: Int, channel: Int) {
        controlChangesSent.add(Triple(cc, value, channel))
    }

    override fun sendPatchVolume(volume: Int) {
        volumesSent.add(volume)
    }

    override fun sendPatchPan(pan: Int) {
        pansSent.add(pan)
    }

    override fun sendPatchTempo(bpm: Int) {
        temposSent.add(bpm)
    }

    override fun sendTapTempo() {
        tapsSent++
    }

    override fun triggerCtrl(ctrlNumber: Int, active: Boolean) {
        ctrlsTriggered.add(Pair(ctrlNumber, active))
    }

    override fun sendParamChange(blockIndex: Int, paramIndex: Int, effectId: Int, value: Float) {
        params.add(listOf(blockIndex, paramIndex, effectId, value))
    }

    override fun sendToggle(blockIndex: Int, enabled: Boolean) {
        toggles.add(Pair(blockIndex, enabled))
    }

    override fun sendEffectChange(blockIndex: Int, effectId: Int) {
        effectChanges.add(Pair(blockIndex, effectId))
    }
    override fun sendReorder(order: List<Int>, send: Int, ret: Int) {
        reorders.add(order)
    }

    val verificationReads = mutableListOf<Int>()
    val verificationResponses = java.util.ArrayDeque<GP200Preset>()
    override suspend fun readPresetForVerification(slot: Int): GP200Preset? {
        verificationReads.add(slot)
        return verificationResponses.pollFirst()
    }

    var saveHandler: suspend (GP200Preset, Int) -> GP200Preset? = { preset, _ -> preset }
    val forceReplays = mutableListOf<Boolean>()
    override suspend fun savePreset(preset: GP200Preset, slot: Int, forceReplay: Boolean): GP200Preset? {
        forceReplays.add(forceReplay)
        if (forceReplay) sendDrumSync(preset.drumSync, preset.patchTempo)
        sendAuthorName(preset.author.take(16))
        sendSaveCommit(preset.patchName, slot)
        return saveHandler(preset, slot)
    }

    override fun sendSaveCommit(presetName: String, slot: Int) {
        saves.add(Pair(presetName, slot))
    }

    override fun sendPatchName(name: String) {
        patchNamesSent.add(name)
    }

    val restoredNames = mutableListOf<Pair<Int, String>>()
    override fun restoreSlotName(slot: Int, name: String) {
        restoredNames.add(slot to name)
    }

    override fun sendAuthorName(author: String) {
        authorNamesSent.add(author)
    }

    override fun sendPresetChange(slot: Int) {
        presetChangesSent.add(slot)
    }

    val requestedNames = mutableListOf<Int>()
    val syncPatchesCalled = mutableListOf<Int>()

    override fun startPresetNamesScan() {}
    override fun pausePresetNamesScan() {}
    override fun requestReadPreset(slot: Int) {
        requestedReads.add(slot)
    }
    override fun requestPresetName(slot: Int) {
        requestedNames.add(slot)
    }
    override fun syncCurrentPatch(slot: Int) {
        syncPatchesCalled.add(slot)
    }
    var preloadedNames: Map<Int, String>? = null
    override fun preloadCachedNames(names: Map<Int, String>) {
        preloadedNames = names
        // Mirror the real driver's confirmation progress so its initial zero cannot race cache initialization.
        if (names.size == 256) (namesSyncProgress as MutableStateFlow<Int>).value = 256
    }
    override fun scanDevices() {}
    override fun disconnect() {}
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PresetViewModelTest {

    @TempDir
    lateinit var tempDir: File

    private fun createDummyLibrary(): Map<Int, GP200Preset> {
        return (0..255).associateWith { idx ->
            GP200Preset(
                patchName = "Preset %03d".format(idx),
                author = "Author $idx",
                effects = listOf(EffectSlot(slotIndex = 0, effectId = 100 + idx, enabled = true)),
                routingOrder = (0..10).toList(),
                patchTempo = 120
            )
        }
    }

    private fun createInitialPreset(): GP200Preset {
        val slots = (0..10).map { i ->
            EffectSlot(slotIndex = i, effectId = 0, enabled = i % 2 == 0)
        }
        return GP200Preset(
            patchName = "Lead Rock",
            author = "Guitarist",
            effects = slots,
            routingOrder = (0..10).toList()
        )
    }

    @Test
    fun `toggleSlot updates state and notifies MIDI manager`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset())

        // Slot 0 is enabled in initial preset. Toggling it should disable it.
        viewModel.toggleSlot(0)

        val state = viewModel.uiState.value
        val slot0 = state.preset?.effects?.find { it.slotIndex == 0 }
        assertNotNull(slot0)
        assertFalse(slot0!!.enabled)
        assertTrue(state.hasUnsavedChanges)

        assertEquals(1, fakeMidi.toggles.size)
        assertEquals(Pair(0, false), fakeMidi.toggles[0])
    }

    @Test
    fun `updateParam updates parameter value and dispatches MIDI`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset())

        viewModel.updateParam(slotIndex = 2, paramIndex = 1, value = 75f)

        val state = viewModel.uiState.value
        val slot2 = state.preset?.effects?.find { it.slotIndex == 2 }
        assertNotNull(slot2)
        assertEquals(75f, slot2!!.params[1])

        assertEquals(1, fakeMidi.params.size)
    }

    @Test
    fun `integer knob edits display and transmit the same rounded value`() {
        val midi = FakeMidiManager()
        val vm = PresetViewModel(midi)
        vm.loadPreset(GP200Preset(patchName = "COMP", effects = listOf(EffectSlot(0, 0, true))))
        for ((raw, expected) in listOf(44.7f to 45f, 44.2f to 44f, 60f to 60f, -0.6f to 0f, 100.8f to 100f)) {
            vm.updateParam(0, 1, raw)
            assertEquals(expected, vm.uiState.value.preset!!.effects.single().params[1])
            assertEquals(expected, midi.params.last()[3])
        }
    }

    @Test
    fun `decimal knob edits retain their catalog step`() {
        val midi = FakeMidiManager()
        val definition = com.valeton.editor.core.catalog.EffectCatalog.getAllEffects().first { effect ->
            effect.params.any { it is com.valeton.editor.core.model.KnobParam && it.min == 0.1f && it.step == 0.1f }
        }
        val param = definition.params.filterIsInstance<com.valeton.editor.core.model.KnobParam>().first { it.min == 0.1f && it.step == 0.1f }
        val vm = PresetViewModel(midi)
        vm.loadPreset(GP200Preset(patchName = "Rate", effects = listOf(EffectSlot(definition.module.slotIndex, definition.effectId, true))))
        vm.updateParam(definition.module.slotIndex, param.index, 0.56f)
        assertEquals(0.6f, vm.uiState.value.preset!!.effects.single().params[param.index], 0.00001f)
        assertEquals(0.6f, midi.params.last()[3] as Float, 0.00001f)
    }

    @Test
    fun `reorderSlots updates chain order and dispatches MIDI reorder`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset())

        // Move item at index 0 to index 1
        viewModel.reorderSlots(0, 1)

        val state = viewModel.uiState.value
        val routing = state.preset?.routingOrder
        assertNotNull(routing)
        assertEquals(1, routing!![0])
        assertEquals(0, routing[1])

        assertEquals(1, fakeMidi.reorders.size)
    }

    @Test
    fun `changeEffect updates model id and dispatches MIDI`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset())

        viewModel.changeEffect(slotIndex = 3, newEffectId = 117440565)

        val state = viewModel.uiState.value
        val slot3 = state.preset?.effects?.find { it.slotIndex == 3 }
        assertNotNull(slot3)
        assertEquals(117440565, slot3!!.effectId)
        assertEquals(50f, slot3.params[0]) // Gain default 50f
        assertEquals(50f, slot3.params[1]) // Presence default 50f
        assertEquals(15, slot3.params.size)
        assertTrue(state.hasUnsavedChanges)

        assertEquals(1, fakeMidi.effectChanges.size)
        assertEquals(Pair(3, 117440565), fakeMidi.effectChanges[0])
    }

    @Test
    fun `handlePedalEvent EffectChanged resets params to effect defaults`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset())

        viewModel.handlePedalEvent(PedalEvent.EffectChanged(blockIndex = 3, effectId = 117440565))

        val slot3 = viewModel.uiState.value.preset?.effects?.find { it.slotIndex == 3 }
        assertNotNull(slot3)
        assertEquals(117440565, slot3!!.effectId)
        assertEquals(50f, slot3.params[0]) // Gain default 50f
    }

    @Test
    fun `handlePedalEvent ParamChanged updates param without echo MIDI dispatch`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset())

        // Hardware knob turned on pedal: block 2, param 0 -> 82.5f
        viewModel.handlePedalEvent(PedalEvent.ParamChanged(blockIndex = 2, paramIndex = 0, value = 82.5f))

        val slot2 = viewModel.uiState.value.preset?.effects?.find { it.slotIndex == 2 }
        assertNotNull(slot2)
        assertEquals(82.5f, slot2!!.params[0])

        // Crucial: no outgoing MIDI must be dispatched in response to incoming hardware event
        assertTrue(fakeMidi.params.isEmpty())
        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `handlePedalEvent ToggleChanged updates enabled without echo MIDI dispatch`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset())

        // Initial slot 0 is enabled. Pedal footswitch toggles it OFF.
        viewModel.handlePedalEvent(PedalEvent.ToggleChanged(blockIndex = 0, enabled = false))

        val slot0 = viewModel.uiState.value.preset?.effects?.find { it.slotIndex == 0 }
        assertNotNull(slot0)
        assertFalse(slot0!!.enabled)

        // No outgoing MIDI echo
        assertTrue(fakeMidi.toggles.isEmpty())
        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `handlePedalEvent EffectChanged updates model without echo MIDI dispatch`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset())

        viewModel.handlePedalEvent(PedalEvent.EffectChanged(blockIndex = 3, effectId = 50331648))

        val slot3 = viewModel.uiState.value.preset?.effects?.find { it.slotIndex == 3 }
        assertNotNull(slot3)
        assertEquals(50331648, slot3!!.effectId)

        // No outgoing MIDI echo
        assertTrue(fakeMidi.effectChanges.isEmpty())
        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `handlePedalEvent PresetLoaded replaces preset and updates slot`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset())

        val incomingPreset = GP200Preset(
            patchName = "Acoustic Stage",
            author = "Valeton",
            effects = listOf(EffectSlot(slotIndex = 0, effectId = 1, enabled = true)),
            routingOrder = listOf(0)
        )
        viewModel.handlePedalEvent(PedalEvent.PresetLoaded(incomingPreset, slot = 12))

        val state = viewModel.uiState.value
        assertEquals("Acoustic Stage", state.preset?.patchName)
        assertEquals(12, state.currentSlotNumber)
        assertFalse(state.hasUnsavedChanges)
    }

    @Test
    fun `handlePedalEvent SlotChanged updates active slot number and aligns cached preset name`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset().copy(patchName = "Old Patch"), slot = 0)
        viewModel.handlePedalEvent(PedalEvent.PresetNameLoaded(slot = 42, name = "ALX-Dist-3"))

        viewModel.handlePedalEvent(PedalEvent.SlotChanged(slot = 42))

        assertEquals(42, viewModel.uiState.value.currentSlotNumber)
        assertEquals("ALX-Dist-3", viewModel.uiState.value.preset?.patchName)
    }

    @Test
    fun `selectPresetSlot updates slot, aligns cached preset name and requests preset change`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset().copy(patchName = "Little Jazz"), slot = 0)
        viewModel.handlePedalEvent(PedalEvent.PresetNameLoaded(slot = 62, name = "ALX-Dist-3"))

        viewModel.selectPresetSlot(62)

        assertEquals(62, viewModel.uiState.value.currentSlotNumber)
        assertEquals("ALX-Dist-3", viewModel.uiState.value.preset?.patchName)
        assertEquals(listOf(62), fakeMidi.presetChangesSent)
    }

    @Test
    fun `selectPresetSlot sets Carregando when cached name is blank`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset().copy(patchName = "Old Patch"), slot = 0)
        viewModel.handlePedalEvent(PedalEvent.PresetNameLoaded(slot = 100, name = ""))

        viewModel.selectPresetSlot(100)

        assertEquals(100, viewModel.uiState.value.currentSlotNumber)
        assertEquals("Carregando...", viewModel.uiState.value.preset?.patchName)
        assertEquals(listOf(100), fakeMidi.presetChangesSent)
    }

    @Test
    fun `handlePedalEvent PresetNameLoaded updates active preset name if slot matches currentSlotNumber`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset().copy(patchName = "Carregando..."), slot = 50)

        viewModel.handlePedalEvent(PedalEvent.PresetNameLoaded(slot = 50, name = "Fresh From Pedal"))

        assertEquals("Fresh From Pedal", viewModel.uiState.value.preset?.patchName)
        assertEquals("Fresh From Pedal", viewModel.uiState.value.presetNames[50])
    }

    @Test
    fun `syncCurrentPatch sets isSyncingCurrentPreset and calls midiManager syncCurrentPatch`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset(), slot = 42)

        assertFalse(viewModel.uiState.value.isSyncingCurrentPreset)

        viewModel.syncCurrentPatch()

        assertTrue(viewModel.uiState.value.isSyncingCurrentPreset)
        assertEquals(listOf(42), fakeMidi.syncPatchesCalled)
    }

    @Test
    fun `selectPresetSlot dispatches sendPresetChange without redundant requestPresetName`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset(), slot = 0)

        assertFalse(viewModel.uiState.value.confirmedSlots.contains(88))

        viewModel.selectPresetSlot(88)

        assertEquals(listOf(88), fakeMidi.presetChangesSent)
        assertTrue(fakeMidi.requestedNames.isEmpty())
    }

    @Test
    fun `handlePedalEvent PresetLoaded updates all effect parameters and routing`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        val initialPreset = createInitialPreset().copy(patchTempo = 100)
        viewModel.loadPreset(initialPreset, slot = 0)

        val updatedPreset = createInitialPreset().copy(
            patchName = "Heavy Metal",
            patchTempo = 160,
            effects = listOf(
                com.valeton.editor.core.model.EffectSlot(slotIndex = 0, effectId = 999, enabled = true, params = listOf(75f, 25f))
            )
        )

        viewModel.handlePedalEvent(PedalEvent.PresetLoaded(updatedPreset, slot = 10))

        assertEquals("Heavy Metal", viewModel.uiState.value.preset?.patchName)
        assertEquals(160, viewModel.uiState.value.preset?.patchTempo)
        assertEquals(10, viewModel.uiState.value.currentSlotNumber)
        assertEquals(999, viewModel.uiState.value.preset?.effects?.firstOrNull()?.effectId)
        assertEquals(listOf(75f, 25f), viewModel.uiState.value.preset?.effects?.firstOrNull()?.params)
        assertTrue(viewModel.uiState.value.confirmedSlots.contains(10))
        assertFalse(viewModel.uiState.value.isSyncingCurrentPreset)
    }

    @Test
    fun `confirmedSlots tracks loaded names and presets`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)

        viewModel.handlePedalEvent(PedalEvent.PresetNameLoaded(slot = 12, name = "Echo Lead"))
        assertTrue(viewModel.uiState.value.confirmedSlots.contains(12))

        viewModel.handlePedalEvent(PedalEvent.PresetLoaded(createInitialPreset(), slot = 34))
        assertTrue(viewModel.uiState.value.confirmedSlots.contains(34))
        assertFalse(viewModel.uiState.value.isSyncingCurrentPreset)
    }

    @Test
    fun `renamePatch updates patch name, dispatches MIDI, and marks unsaved`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset(), slot = 3)

        viewModel.renamePatch("Super Crunch")

        assertEquals("Super Crunch", viewModel.uiState.value.preset?.patchName)
        assertEquals("Super Crunch", viewModel.uiState.value.presetNames[3])
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
        assertEquals(listOf("Super Crunch"), fakeMidi.patchNamesSent)
    }

    @Test
    fun `handlePedalEvent PresetNameLoaded updates slot name in presetNames list`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)

        viewModel.handlePedalEvent(PedalEvent.PresetNameLoaded(slot = 8, name = "Fender Clean"))

        assertEquals("Fender Clean", viewModel.uiState.value.presetNames[8])
    }

    @Test
    fun `saveToDevice commits and clears hasUnsavedChanges`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi, coroutineScope = CoroutineScope(Dispatchers.Unconfined))
        viewModel.loadPreset(createInitialPreset(), slot = 5)
        viewModel.renamePatch("Saved Patch")
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)

        viewModel.saveToDevice(5)

        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
        assertEquals(listOf(Pair("Saved Patch", 5)), fakeMidi.saves)
    }

    @Test
    fun `renameSlot for current slot renames patch and sends MIDI, other slot updates name locally`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset(), slot = 2)

        // Rename current slot (2)
        viewModel.renameSlot(2, "Slot 2 Name")
        assertEquals("Slot 2 Name", viewModel.uiState.value.preset?.patchName)
        assertEquals("Slot 2 Name", viewModel.uiState.value.presetNames[2])
        assertEquals(listOf("Slot 2 Name"), fakeMidi.patchNamesSent)

        // Rename another slot (10)
        viewModel.renameSlot(10, "Slot 10 Name")
        assertEquals("Slot 10 Name", viewModel.uiState.value.presetNames[10])
        // MIDI name is not sent for non-active slot
        assertEquals(1, fakeMidi.patchNamesSent.size)
    }

    @Test
    fun `updatePatchAuthor updates author and sets hasUnsavedChanges`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset(), slot = 0)

        viewModel.updatePatchAuthor("Hendrix")
        assertEquals("Hendrix", viewModel.uiState.value.preset?.author)
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
        assertEquals(listOf("Hendrix"), fakeMidi.authorNamesSent)
    }

    @Test
    fun `saveToDevice sends author name when author is present`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi, coroutineScope = CoroutineScope(Dispatchers.Unconfined))
        val preset = createInitialPreset().copy(author = "SRV")
        viewModel.loadPreset(preset, slot = 0)

        viewModel.saveToDevice(0)
        assertTrue(fakeMidi.authorNamesSent.contains("SRV"))
        assertEquals(listOf(Pair(preset.patchName, 0)), fakeMidi.saves)
    }

    @Test
    fun `updatePatchVolume updates preset, marks unsaved, and sends MIDI`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset())

        viewModel.updatePatchVolume(85)
        assertEquals(85, viewModel.uiState.value.preset?.patchVolume)
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
        assertEquals(listOf(85), fakeMidi.volumesSent)
    }

    @Test
    fun `updatePatchPan updates preset, marks unsaved, and sends MIDI`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset())

        viewModel.updatePatchPan(-20)
        assertEquals(-20, viewModel.uiState.value.preset?.patchPan)
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
        assertEquals(listOf(-20), fakeMidi.pansSent)
    }

    @Test
    fun `triggerCtrl dispatches to MIDI manager`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)

        viewModel.triggerCtrl(1, true)
        viewModel.triggerCtrl(1, false)
        viewModel.triggerCtrl(2, true)

        assertEquals(listOf(Pair(1, true), Pair(1, false), Pair(2, true)), fakeMidi.ctrlsTriggered)
    }

    @Test
    fun `importPreset with saveToPedal false sets state, marks unsaved and sends live midi`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi, coroutineScope = CoroutineScope(Dispatchers.Unconfined)).apply {
            midiDelayProvider = {}
        }
        viewModel.loadPreset(createInitialPreset(), slot = 0)

        val imported = GP200Preset(
            patchName = "The Sickness",
            author = "Disturbed",
            patchVolume = 82,
            patchPan = 0,
            effects = listOf(
                EffectSlot(slotIndex = 0, effectId = 50331649, enabled = true, params = listOf(50f)),
                EffectSlot(slotIndex = 3, effectId = 117440607, enabled = true, params = listOf(80f))
            ),
            routingOrder = listOf(0, 3)
        )

        viewModel.importPreset(imported, targetSlot = 5, saveToPedal = false)

        val state = viewModel.uiState.value
        assertEquals("The Sickness", state.preset?.patchName)
        assertEquals(5, state.currentSlotNumber)
        assertEquals(82, state.preset?.patchVolume)
        assertTrue(state.hasUnsavedChanges)
        assertTrue(fakeMidi.saves.isEmpty())
        assertTrue(fakeMidi.volumesSent.contains(82))
        assertEquals(listOf(5), fakeMidi.presetChangesSent)
    }

    @Test
    fun `live COMP import sends only its two supported parameters`() {
        val midi = FakeMidiManager()
        val vm = PresetViewModel(midi, coroutineScope = CoroutineScope(Dispatchers.Unconfined)).apply { midiDelayProvider = {} }
        vm.loadPreset(createInitialPreset(), slot = 0)
        val imported = GP200Preset(patchName = "COMP test", effects = listOf(
            EffectSlot(0, 0, true, listOf(89f, 60f) + List(13) { 0f })
        ))
        vm.importPreset(imported, targetSlot = 0, saveToPedal = false)
        assertEquals(listOf(listOf(0, 0, 0, 89f), listOf(0, 1, 0, 60f)), midi.params)
    }

    @Test
    fun `live Analog import sets Sync before restoring its Time`() {
        val midi = FakeMidiManager()
        val vm = PresetViewModel(midi, coroutineScope = CoroutineScope(Dispatchers.Unconfined)).apply { midiDelayProvider = {} }
        vm.loadPreset(createInitialPreset(), slot = 0)
        val imported = GP200Preset(patchName = "Analog", effects = listOf(
            EffectSlot(8, 184549377, true, listOf(20f, 400f, 30f, 0f, 0f) + List(10) { 0f })
        ))
        vm.importPreset(imported, targetSlot = 0, saveToPedal = false)
        val indices = midi.params.map { it[1] as Int }
        assertTrue(indices.indexOf(3) < indices.indexOf(1))
        assertEquals(400f, midi.params.first { it[1] == 1 }[3])
    }

    @Test
    fun `importPreset with saveToPedal true saves to pedal, updates names and clears unsaved changes`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi, coroutineScope = CoroutineScope(Dispatchers.Unconfined)).apply {
            midiDelayProvider = {}
        }
        viewModel.loadPreset(createInitialPreset(), slot = 0)

        val imported = GP200Preset(
            patchName = "JP OD 2024",
            author = "Gibexp Konx",
            patchVolume = 50,
            patchPan = 0,
            effects = listOf(
                EffectSlot(slotIndex = 2, effectId = 50331649, enabled = true, params = listOf(50f))
            ),
            routingOrder = listOf(2)
        )

        viewModel.importPreset(imported, targetSlot = 10, saveToPedal = true)

        val state = viewModel.uiState.value
        assertEquals("JP OD 2024", state.preset?.patchName)
        assertEquals(10, state.currentSlotNumber)
        assertFalse(state.hasUnsavedChanges)
        assertEquals(1, fakeMidi.saves.size)
        assertEquals(Pair("JP OD 2024", 10), fakeMidi.saves[0])
        assertEquals("JP OD 2024", state.presetNames[10])
    }

    @Test
    fun `updatePatchTempo updates preset, marks unsaved and sends MIDI`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset())

        viewModel.updatePatchTempo(145)
        assertEquals(145, viewModel.uiState.value.preset?.patchTempo)
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
        assertEquals(listOf(145), fakeMidi.temposSent)
    }

    @Test
    fun `tapTempo dispatches tap tempo message`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset())

        viewModel.tapTempo()
        assertEquals(1, fakeMidi.tapsSent)
    }

    @Test
    fun `toggleDrumPlay toggles playback state and dispatches MIDI`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)

        assertFalse(viewModel.uiState.value.drum.isPlaying)

        viewModel.toggleDrumPlay()
        assertTrue(viewModel.uiState.value.drum.isPlaying)
        assertEquals(true, fakeMidi.drumMenuEnabled)
        assertEquals(true, fakeMidi.drumPlaying)

        viewModel.toggleDrumPlay()
        assertFalse(viewModel.uiState.value.drum.isPlaying)
        assertEquals(false, fakeMidi.drumPlaying)
    }

    @Test
    fun `selectDrumPattern updates state and dispatches CC 94`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)

        viewModel.selectDrumPattern(15) // Punk 1
        assertEquals(15, viewModel.uiState.value.drum.selectedPatternId)
        assertEquals(listOf(15), fakeMidi.drumPatternsSent)
    }

    @Test
    fun `updateDrumVolume updates state clamped 0-100 and dispatches CC 95`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)

        viewModel.updateDrumVolume(75)
        assertEquals(75, viewModel.uiState.value.drum.volume)
        assertEquals(listOf(75), fakeMidi.drumVolumesSent)

        viewModel.updateDrumVolume(150)
        assertEquals(100, viewModel.uiState.value.drum.volume)
        assertEquals(listOf(75, 100), fakeMidi.drumVolumesSent)

        viewModel.updateDrumVolume(-20)
        assertEquals(0, viewModel.uiState.value.drum.volume)
        assertEquals(listOf(75, 100, 0), fakeMidi.drumVolumesSent)
    }

    @Test
    fun `setDrumGenre and setDrumSearchQuery update state`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)

        viewModel.setDrumGenre(DrumGenre.ROCK)
        assertEquals(DrumGenre.ROCK, viewModel.uiState.value.drum.selectedGenre)

        viewModel.setDrumSearchQuery("Metal")
        assertEquals("Metal", viewModel.uiState.value.drum.searchQuery)
    }

    @Test
    fun `when library is already fully cached, ViewModel loads cachedLibrary and does not trigger full sync`() {
        val fakeMidi = FakeMidiManager()
        val repository = PresetLibraryRepository(tempDir)
        val dummyLibrary = createDummyLibrary()
        repository.saveFullLibrary(dummyLibrary)

        val viewModel = PresetViewModel(fakeMidi, libraryRepository = repository)

        val state = viewModel.uiState.value
        assertFalse(state.isPerformingFullSync)
        assertFalse(fakeMidi.fullSyncStarted)
        assertEquals(256, state.cachedLibrary.size)
        assertEquals(256, state.presetNames.size)
        assertEquals("Preset 000", state.presetNames[0])
        assertEquals("Preset 255", state.presetNames[255])
        assertEquals((0..255).toSet(), state.confirmedSlots)
        assertEquals("Preset 000", state.preset?.patchName)
    }

    @Test
    fun `when library is not cached and pedal connects, isPerformingFullSync becomes true and startFullLibrarySync is called`() {
        val fakeMidi = FakeMidiManager()
        fakeMidi._connectionState.value = MidiConnectionState.Disconnected
        val repository = PresetLibraryRepository(tempDir)

        val viewModel = PresetViewModel(
            fakeMidi,
            libraryRepository = repository,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )

        assertFalse(viewModel.uiState.value.isPerformingFullSync)
        assertFalse(fakeMidi.fullSyncStarted)

        fakeMidi._connectionState.value = MidiConnectionState.Connected("GP-200")

        assertTrue(viewModel.uiState.value.isPerformingFullSync)
        assertTrue(fakeMidi.fullSyncStarted)
    }

    @Test
    fun `when FullLibrarySynced arrives, repository saves full library, cachedLibrary is populated and isPerformingFullSync is false`() {
        val fakeMidi = FakeMidiManager()
        val repository = PresetLibraryRepository(tempDir)
        val viewModel = PresetViewModel(
            fakeMidi,
            libraryRepository = repository,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )

        val dummyLibrary = createDummyLibrary()
        viewModel.handlePedalEvent(PedalEvent.FullLibrarySynced(dummyLibrary))

        val state = viewModel.uiState.value
        assertFalse(state.isPerformingFullSync)
        assertEquals(256, state.cachedLibrary.size)
        assertEquals(256, state.presetNames.size)
        assertEquals(256, state.namesSyncProgress)
        assertEquals((0..255).toSet(), state.confirmedSlots)
        assertEquals("Preset 000", state.preset?.patchName)

        assertTrue(repository.isLibraryCached())
        val loaded = repository.loadFullLibrary()
        assertNotNull(loaded)
        assertEquals(256, loaded!!.size)
        assertEquals("Preset 042", loaded[42]?.patchName)
    }

    @Test
    fun `selectPresetSlot immediately updates preset from cachedLibrary without waiting for MIDI roundtrip`() {
        val fakeMidi = FakeMidiManager()
        val repository = PresetLibraryRepository(tempDir)
        val dummyLibrary = createDummyLibrary()
        repository.saveFullLibrary(dummyLibrary)

        val viewModel = PresetViewModel(fakeMidi, libraryRepository = repository)

        // Before switching, slot is 0
        assertEquals(0, viewModel.uiState.value.currentSlotNumber)

        // Select slot 42
        viewModel.selectPresetSlot(42)

        val state = viewModel.uiState.value
        assertEquals(42, state.currentSlotNumber)
        assertNotNull(state.preset)
        assertEquals("Preset 042", state.preset?.patchName)
        assertEquals(dummyLibrary[42], state.preset)
        assertEquals(listOf(42), fakeMidi.presetChangesSent)
    }

    @Test
    fun `forceFullLibraryResync clears cache and starts full library sync`() {
        val fakeMidi = FakeMidiManager()
        val repository = PresetLibraryRepository(tempDir)
        repository.saveFullLibrary(createDummyLibrary())
        assertTrue(repository.isLibraryCached())

        val viewModel = PresetViewModel(
            fakeMidi,
            libraryRepository = repository,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
        assertEquals(256, viewModel.uiState.value.cachedLibrary.size)

        viewModel.forceFullLibraryResync()

        assertFalse(repository.isLibraryCached())
        assertTrue(viewModel.uiState.value.isPerformingFullSync)
        assertTrue(viewModel.uiState.value.cachedLibrary.isEmpty())
        assertTrue(fakeMidi.fullSyncStarted)
    }

    @Test
    fun `PresetLoaded updates cachedLibrary and repository`() {
        val fakeMidi = FakeMidiManager()
        val repository = PresetLibraryRepository(tempDir)
        val dummyLibrary = createDummyLibrary()
        repository.saveFullLibrary(dummyLibrary)

        val viewModel = PresetViewModel(
            fakeMidi,
            libraryRepository = repository,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )

        val updatedPreset = GP200Preset(
            patchName = "Super Crunch 10",
            author = "Rockstar",
            effects = listOf(EffectSlot(slotIndex = 0, effectId = 999, enabled = true)),
            routingOrder = (0..10).toList(),
            patchTempo = 140
        )

        viewModel.handlePedalEvent(PedalEvent.PresetLoaded(updatedPreset, slot = 10))

        assertEquals("Super Crunch 10", viewModel.uiState.value.cachedLibrary[10]?.patchName)
        val reloaded = repository.loadFullLibrary()
        assertEquals("Super Crunch 10", reloaded?.get(10)?.patchName)
    }

    @Test
    fun `fullSyncProgress updates uiState`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(
            fakeMidi,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )

        assertNull(viewModel.uiState.value.fullSyncProgress)

        val progress = FullSyncProgress(
            currentSlot = 50,
            totalSlots = 256,
            currentPatchName = "Patch 50",
            progressFraction = 50f / 256f,
            isCompleted = false
        )
        fakeMidi._fullSyncProgress.value = progress

        assertEquals(progress, viewModel.uiState.value.fullSyncProgress)
    }

    @Test
    fun `when connection becomes Disconnected, isPerformingFullSync and fullSyncProgress are reset`() {
        val fakeMidi = FakeMidiManager()
        val repository = PresetLibraryRepository(tempDir)

        val viewModel = PresetViewModel(
            fakeMidi,
            libraryRepository = repository,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )

        // Pedal connected without cache triggers full sync
        assertTrue(viewModel.uiState.value.isPerformingFullSync)
        fakeMidi._fullSyncProgress.value = FullSyncProgress(10, 256, "Patch 10", 10f / 256f)
        assertNotNull(viewModel.uiState.value.fullSyncProgress)

        // Pedal is disconnected
        fakeMidi._connectionState.value = MidiConnectionState.Disconnected

        assertFalse(viewModel.uiState.value.isPerformingFullSync)
        assertNull(viewModel.uiState.value.fullSyncProgress)
    }

    @Test
    fun `incomplete cache triggers full sync when pedal connects`() {
        val fakeMidi = FakeMidiManager()
        fakeMidi._connectionState.value = MidiConnectionState.Disconnected
        val repository = PresetLibraryRepository(tempDir)
        // Save only 100 presets (incomplete)
        val partialLibrary = (0..99).associateWith { idx ->
            GP200Preset(patchName = "Patch $idx", author = "Tester", effects = emptyList(), routingOrder = emptyList())
        }
        repository.saveFullLibrary(partialLibrary)

        val viewModel = PresetViewModel(
            fakeMidi,
            libraryRepository = repository,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )

        assertFalse(viewModel.uiState.value.isPerformingFullSync)
        assertFalse(fakeMidi.fullSyncStarted)

        fakeMidi._connectionState.value = MidiConnectionState.Connected("GP-200")

        assertTrue(viewModel.uiState.value.isPerformingFullSync)
        assertTrue(fakeMidi.fullSyncStarted)
    }

    @Test
    fun `selectPresetSlot clamps slot to 0-255`() {
        val fakeMidi = FakeMidiManager()
        val repository = PresetLibraryRepository(tempDir)
        val dummyLibrary = createDummyLibrary()
        repository.saveFullLibrary(dummyLibrary)

        val viewModel = PresetViewModel(fakeMidi, libraryRepository = repository)

        viewModel.selectPresetSlot(-10)
        assertEquals(0, viewModel.uiState.value.currentSlotNumber)
        assertEquals(listOf(0), fakeMidi.presetChangesSent)

        viewModel.selectPresetSlot(500)
        assertEquals(255, viewModel.uiState.value.currentSlotNumber)
        assertEquals(listOf(0, 255), fakeMidi.presetChangesSent)
    }

    @Test
    fun `mutators are ignored while isPerformingFullSync is true`() {
        val fakeMidi = FakeMidiManager()
        val repository = PresetLibraryRepository(tempDir)
        val dummyLibrary = createDummyLibrary()
        repository.saveFullLibrary(dummyLibrary)

        val viewModel = PresetViewModel(fakeMidi, libraryRepository = repository)
        viewModel.forceFullLibraryResync()

        assertTrue(viewModel.uiState.value.isPerformingFullSync)

        val previousSlot = viewModel.uiState.value.currentSlotNumber
        viewModel.selectPresetSlot(42)
        assertEquals(previousSlot, viewModel.uiState.value.currentSlotNumber)
        assertTrue(fakeMidi.presetChangesSent.isEmpty())

        viewModel.renameSlot(5, "ShouldNotRename")
        assertNotEquals("ShouldNotRename", viewModel.uiState.value.presetNames.getOrNull(5))

        viewModel.importPreset(GP200Preset(patchName = "Hacked", effects = emptyList()), targetSlot = 10, saveToPedal = false)
        assertNotEquals("Hacked", viewModel.uiState.value.preset?.patchName)
    }

    @Test
    fun `PresetViewModel preloads cached names into midiManager when full library is cached`() {
        val fakeMidi = FakeMidiManager()
        val repository = PresetLibraryRepository(tempDir)
        val dummyLibrary = createDummyLibrary()
        repository.saveFullLibrary(dummyLibrary)

        val viewModel = PresetViewModel(fakeMidi, libraryRepository = repository)

        assertEquals(256, viewModel.uiState.value.namesSyncProgress)
        assertNotNull(fakeMidi.preloadedNames)
        assertEquals(256, fakeMidi.preloadedNames?.size)
        assertEquals("Preset 042", fakeMidi.preloadedNames?.get(42))
    }

    @Test
    fun `PresetLoaded updates uiState, turns off isSyncingCurrentPreset, and persists into libraryRepository`() {
        val fakeMidi = FakeMidiManager()
        val repository = PresetLibraryRepository(tempDir)
        val dummyLibrary = createDummyLibrary()
        repository.saveFullLibrary(dummyLibrary)

        val viewModel = PresetViewModel(
            fakeMidi,
            libraryRepository = repository,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
        viewModel.syncCurrentPatch()
        assertTrue(viewModel.uiState.value.isSyncingCurrentPreset)

        val updatedPreset = GP200Preset(
            patchName = "Synced Patch",
            effects = listOf(EffectSlot(slotIndex = 0, enabled = true, effectId = 1, params = listOf(0.5f)))
        )

        viewModel.handlePedalEvent(PedalEvent.PresetLoaded(updatedPreset, 0))

        assertFalse(viewModel.uiState.value.isSyncingCurrentPreset)
        assertEquals("Synced Patch", viewModel.uiState.value.preset?.patchName)
        assertEquals("Synced Patch", viewModel.uiState.value.presetNames[0])
        val reloaded = repository.loadFullLibrary()
        assertEquals("Synced Patch", reloaded?.get(0)?.patchName)
    }

    @Test
    fun `verified saveToDevice updates cachedLibrary and libraryRepository`() {
        val fakeMidi = FakeMidiManager()
        val repository = PresetLibraryRepository(tempDir)
        val dummyLibrary = (0..255).associateWith { idx ->
            createInitialPreset().copy(patchName = "Preset %03d".format(idx))
        }
        repository.saveFullLibrary(dummyLibrary)

        val viewModel = PresetViewModel(
            midiManager = fakeMidi,
            libraryRepository = repository,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
        viewModel.midiDelayProvider = { }

        // Select slot 100 (26A)
        viewModel.selectPresetSlot(100)
        assertEquals("Preset 100", viewModel.uiState.value.preset?.patchName)

        // Alter amp (slotIndex = 3, newEffectId = 99999) and rename patch
        viewModel.changeEffect(slotIndex = 3, newEffectId = 99999)
        viewModel.renamePatch("New 26A")
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)

        fakeMidi.requestedReads.clear()

        // Save to device
        viewModel.saveToDevice(100)

        // Verify save commit sent
        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
        assertEquals(listOf(Pair("New 26A", 100)), fakeMidi.saves)

        // Verify cachedLibrary is updated with the altered amp and name
        val cached = viewModel.uiState.value.cachedLibrary[100]
        assertNotNull(cached)
        assertEquals("New 26A", cached?.patchName)
        assertEquals(99999, cached?.effects?.find { it.slotIndex == 3 }?.effectId)

        // Verify persistent repository is updated
        val diskLibrary = repository.loadFullLibrary()
        assertEquals("New 26A", diskLibrary?.get(100)?.patchName)
        assertEquals(99999, diskLibrary?.get(100)?.effects?.find { it.slotIndex == 3 }?.effectId)

        // Verify no redundant requestReadPreset was triggered during save
        assertTrue(fakeMidi.requestedReads.isEmpty(), "saveToDevice must not trigger requestReadPreset")

        // Switch to slot 0 and back to slot 100 - configs must NOT reset!
        viewModel.selectPresetSlot(0)
        viewModel.selectPresetSlot(100)
        assertEquals("New 26A", viewModel.uiState.value.preset?.patchName)
        assertEquals(99999, viewModel.uiState.value.preset?.effects?.find { it.slotIndex == 3 }?.effectId)
    }

    @Test
    fun `editing patch name on slot 26A and saving persists name without resetting`() {
        val fakeMidi = FakeMidiManager()
        val repository = PresetLibraryRepository(tempDir)
        val dummyLibrary = (0..255).associateWith { idx ->
            createInitialPreset().copy(patchName = "Preset %03d".format(idx))
        }
        repository.saveFullLibrary(dummyLibrary)

        val viewModel = PresetViewModel(
            midiManager = fakeMidi,
            libraryRepository = repository,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
        viewModel.midiDelayProvider = { }

        // Select slot 100 (26A)
        viewModel.selectPresetSlot(100)
        assertEquals("Preset 100", viewModel.uiState.value.preset?.patchName)

        // Rename patch
        viewModel.renamePatch("My Solo 26A")
        assertEquals("My Solo 26A", viewModel.uiState.value.preset?.patchName)
        assertEquals("My Solo 26A", viewModel.uiState.value.presetNames[100])
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)

        // Save
        viewModel.saveToDevice(100)

        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
        assertEquals(listOf(Pair("My Solo 26A", 100)), fakeMidi.saves)
        assertEquals("My Solo 26A", viewModel.uiState.value.cachedLibrary[100]?.patchName)
        assertEquals("My Solo 26A", repository.loadFullLibrary()?.get(100)?.patchName)

        // Switch to slot 0 and back to slot 100 - name must NOT reset!
        viewModel.selectPresetSlot(0)
        assertEquals("Preset 000", viewModel.uiState.value.preset?.patchName)
        viewModel.selectPresetSlot(100)
        assertEquals("My Solo 26A", viewModel.uiState.value.preset?.patchName)
    }

    @Test
    fun `incoming PresetLoaded for current slot while hasUnsavedChanges does not clobber edits`() {
        val fakeMidi = FakeMidiManager()
        val repository = PresetLibraryRepository(tempDir)
        val dummyLibrary = (0..255).associateWith { idx ->
            createInitialPreset().copy(patchName = "Preset %03d".format(idx))
        }
        repository.saveFullLibrary(dummyLibrary)

        val viewModel = PresetViewModel(
            midiManager = fakeMidi,
            libraryRepository = repository,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
        viewModel.midiDelayProvider = { }

        viewModel.selectPresetSlot(102)
        viewModel.changeEffect(slotIndex = 3, newEffectId = 77777)
        viewModel.renamePatch("Edited 26C")
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)

        // Stale preset arrives from device for slot 102
        val stalePreset = createInitialPreset().copy(patchName = "Old 26C")
        viewModel.handlePedalEvent(PedalEvent.PresetLoaded(stalePreset, slot = 102))

        // Edits must NOT be clobbered
        assertEquals("Edited 26C", viewModel.uiState.value.preset?.patchName)
        assertEquals(77777, viewModel.uiState.value.preset?.effects?.find { it.slotIndex == 3 }?.effectId)
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `incoming PresetLoaded for current slot while isSaving does not clobber freshly saved preset`() {
        val fakeMidi = FakeMidiManager()
        val repository = PresetLibraryRepository(tempDir)
        val dummyLibrary = (0..255).associateWith { idx ->
            createInitialPreset().copy(patchName = "Preset %03d".format(idx))
        }
        repository.saveFullLibrary(dummyLibrary)

        val viewModel = PresetViewModel(
            midiManager = fakeMidi,
            libraryRepository = repository,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
        viewModel.midiDelayProvider = { }

        viewModel.selectPresetSlot(102)
        viewModel.changeEffect(slotIndex = 3, newEffectId = 88888)
        viewModel.renamePatch("Saved 26C")

        // Trigger saveToDevice
        viewModel.saveToDevice(102)

        // Simulate stale PresetLoaded arriving right after save commit
        val stalePreset = createInitialPreset().copy(patchName = "Stale 26C")
        viewModel.handlePedalEvent(PedalEvent.PresetLoaded(stalePreset, slot = 102))

        // Preset must remain the saved 26C, not reverted to stale
        assertEquals("Saved 26C", viewModel.uiState.value.preset?.patchName)
        assertEquals(88888, viewModel.uiState.value.preset?.effects?.find { it.slotIndex == 3 }?.effectId)
    }

    @Test
    fun `incoming SlotChanged for current slot while hasUnsavedChanges does not clobber edits`() {
        val fakeMidi = FakeMidiManager()
        val repository = PresetLibraryRepository(tempDir)
        val dummyLibrary = (0..255).associateWith { idx ->
            createInitialPreset().copy(patchName = "Preset %03d".format(idx))
        }
        repository.saveFullLibrary(dummyLibrary)

        val viewModel = PresetViewModel(
            midiManager = fakeMidi,
            libraryRepository = repository,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
        viewModel.midiDelayProvider = { }

        viewModel.selectPresetSlot(102)
        viewModel.changeEffect(slotIndex = 3, newEffectId = 99999)
        viewModel.renamePatch("Unsaved 26C")
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)

        // SlotChanged event arrives for slot 102 (e.g. from footswitch press or save echo)
        viewModel.handlePedalEvent(PedalEvent.SlotChanged(slot = 102))

        // Edits must NOT be clobbered
        assertEquals("Unsaved 26C", viewModel.uiState.value.preset?.patchName)
        assertEquals(99999, viewModel.uiState.value.preset?.effects?.find { it.slotIndex == 3 }?.effectId)
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `updateDrumBpm updates drum bpm independently when sync is off`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset().copy(patchTempo = 100))

        assertFalse(viewModel.uiState.value.drum.isSyncWithPatch)
        assertEquals(120, viewModel.uiState.value.drum.bpm)

        viewModel.updateDrumBpm(145)

        assertEquals(145, viewModel.uiState.value.drum.bpm)
        assertEquals(100, viewModel.uiState.value.preset?.patchTempo) // patch tempo untouched
        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
        assertEquals(listOf(Pair(145, false)), fakeMidi.drumBpmsSent)
    }

    @Test
    fun `updateDrumBpm updates drum bpm and patch tempo when sync is on`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset().copy(patchTempo = 100))

        viewModel.setDrumSync(true)
        assertTrue(viewModel.uiState.value.drum.isSyncWithPatch)
        assertEquals(100, viewModel.uiState.value.drum.bpm) // adopted patch tempo

        fakeMidi.drumBpmsSent.clear()
        viewModel.updateDrumBpm(135)

        assertEquals(135, viewModel.uiState.value.drum.bpm)
        assertEquals(135, viewModel.uiState.value.preset?.patchTempo)
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
        assertEquals(listOf(Pair(135, true)), fakeMidi.drumBpmsSent)
    }

    @Test
    fun `toggleDrumSync enables sync and copies current patch tempo to drum bpm`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset().copy(patchTempo = 95))

        assertEquals(120, viewModel.uiState.value.drum.bpm)
        assertFalse(viewModel.uiState.value.drum.isSyncWithPatch)

        viewModel.toggleDrumSync()

        assertTrue(viewModel.uiState.value.drum.isSyncWithPatch)
        assertEquals(95, viewModel.uiState.value.drum.bpm)

        viewModel.toggleDrumSync()
        assertFalse(viewModel.uiState.value.drum.isSyncWithPatch)
        assertEquals(95, viewModel.uiState.value.drum.bpm) // keeps current bpm when turning sync off
    }

    @Test
    fun `loading preset preserves drum bpm when sync is off`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset().copy(patchTempo = 100))

        viewModel.updateDrumBpm(150)
        assertEquals(150, viewModel.uiState.value.drum.bpm)

        // Incoming new preset with patch tempo 80
        val newPreset = createInitialPreset().copy(patchTempo = 80)
        viewModel.handlePedalEvent(PedalEvent.PresetLoaded(newPreset, slot = 1))

        assertEquals(80, viewModel.uiState.value.preset?.patchTempo)
        assertEquals(150, viewModel.uiState.value.drum.bpm, "Drum BPM must remain 150 when sync is OFF")
    }

    @Test
    fun `loading preset updates drum bpm when sync is on`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset().copy(patchTempo = 100))

        viewModel.setDrumSync(true)
        assertEquals(100, viewModel.uiState.value.drum.bpm)

        // Incoming new preset with patch tempo 75
        val newPreset = createInitialPreset().copy(patchTempo = 75)
        viewModel.handlePedalEvent(PedalEvent.PresetLoaded(newPreset, slot = 1))

        assertEquals(75, viewModel.uiState.value.preset?.patchTempo)
        assertEquals(75, viewModel.uiState.value.drum.bpm, "Drum BPM must follow patch tempo 75 when sync is ON")
    }

    @Test
    fun `setDrumSync sends sendDrumSync to midiManager`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset().copy(patchTempo = 110))

        viewModel.setDrumSync(true)
        assertEquals(1, fakeMidi.drumSyncsSent.size)
        assertEquals(Pair(true, 110), fakeMidi.drumSyncsSent.last())

        viewModel.setDrumSync(false)
        assertEquals(2, fakeMidi.drumSyncsSent.size)
        assertEquals(Pair(false, 110), fakeMidi.drumSyncsSent.last())
    }

    @Test
    fun `updateDrumBpm sends sendDrumBpm to midiManager with sync flag`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)
        viewModel.loadPreset(createInitialPreset().copy(patchTempo = 120))

        // When Sync is false
        viewModel.updateDrumBpm(135)
        assertEquals(1, fakeMidi.drumBpmsSent.size)
        assertEquals(Pair(135, false), fakeMidi.drumBpmsSent.last())

        // When Sync is true
        viewModel.setDrumSync(true)
        viewModel.updateDrumBpm(145)
        assertEquals(2, fakeMidi.drumBpmsSent.size)
        assertEquals(Pair(145, true), fakeMidi.drumBpmsSent.last())
    }

    @Test
    fun `openDrumsMenu and closeDrumsMenu send sendDrumMenu true and false`() {
        val fakeMidi = FakeMidiManager()
        val viewModel = PresetViewModel(fakeMidi)

        viewModel.openDrumsMenu()
        assertEquals(true, fakeMidi.drumMenuEnabled)

        viewModel.closeDrumsMenu()
        assertEquals(false, fakeMidi.drumMenuEnabled)
    }
    @Test
    fun `offline save keeps edits dirty and does not update confirmed cache`() = kotlinx.coroutines.test.runTest {
        val midi = FakeMidiManager()
        midi._connectionState.value = MidiConnectionState.Disconnected
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.loadPreset(createInitialPreset())
        vm.renamePatch("Offline edit")
        vm.saveToDevice()
        testScheduler.runCurrent()
        assertTrue(vm.uiState.value.hasUnsavedChanges)
        assertFalse(vm.uiState.value.isSaving)
        assertNotNull(vm.uiState.value.errorMessage)
        assertTrue(midi.saves.isEmpty())
        assertTrue(vm.uiState.value.cachedLibrary.isEmpty())
    }

    @Test
    fun `save stays dirty until device responds and timeout preserves edits`() = kotlinx.coroutines.test.runTest {
        val midi = FakeMidiManager().apply {
            saveHandler = { _, _ -> kotlinx.coroutines.delay(3000); null }
        }
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.loadPreset(createInitialPreset())
        vm.renamePatch("Pending")
        vm.saveToDevice()
        testScheduler.runCurrent()
        assertTrue(vm.uiState.value.hasUnsavedChanges)
        assertTrue(vm.uiState.value.isSaving)
        assertTrue(vm.uiState.value.cachedLibrary.isEmpty())
        testScheduler.advanceTimeBy(5000)
        testScheduler.runCurrent()
        assertTrue(vm.uiState.value.hasUnsavedChanges)
        assertFalse(vm.uiState.value.isSaving)
        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `library with one empty preset is incomplete and retries on connection`() = kotlinx.coroutines.test.runTest {
        val repository = PresetLibraryRepository(tempDir)
        repository.saveFullLibrary(createDummyLibrary() + (5 to GP200Preset(patchName = "Missing", effects = emptyList())))
        val midi = FakeMidiManager()
        val vm = PresetViewModel(midi, libraryRepository = repository, coroutineScope = backgroundScope)
        testScheduler.runCurrent()
        assertTrue(midi.fullSyncStarted)
        assertTrue(vm.uiState.value.isPerformingFullSync)
    }

    @Test
    fun `partial library sync confirms only successful slots and reports error`() = kotlinx.coroutines.test.runTest {
        val midi = FakeMidiManager()
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.handlePedalEvent(PedalEvent.FullLibrarySynced(createDummyLibrary() - 5))
        val state = vm.uiState.value
        assertFalse(state.isPerformingFullSync)
        assertFalse(5 in state.confirmedSlots)
        assertEquals(255, state.namesSyncProgress)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `mismatching save readback keeps edits and previous cache`() = kotlinx.coroutines.test.runTest {
        val midi = FakeMidiManager().apply { saveHandler = { preset, _ -> preset.copy(patchVolume = 3) } }
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.loadPreset(createInitialPreset())
        vm.renamePatch("Expected")
        vm.saveToDevice()
        testScheduler.runCurrent()
        assertTrue(vm.uiState.value.hasUnsavedChanges)
        assertTrue(vm.uiState.value.cachedLibrary.isEmpty())
        assertEquals(0, vm.uiState.value.saveSuccessCount)
        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `edits made while saving are ignored until confirmation completes`() = kotlinx.coroutines.test.runTest {
        val response = kotlinx.coroutines.CompletableDeferred<GP200Preset?>()
        val midi = FakeMidiManager().apply { saveHandler = { _, _ -> response.await() } }
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.loadPreset(createInitialPreset())
        vm.renamePatch("Sent")
        val sent = vm.uiState.value.preset!!
        vm.saveToDevice()
        testScheduler.runCurrent()
        vm.renamePatch("New edit")
        response.complete(sent)
        testScheduler.runCurrent()
        assertEquals("Sent", vm.uiState.value.preset?.patchName)
        assertFalse(vm.uiState.value.hasUnsavedChanges)
        assertEquals("Sent", vm.uiState.value.cachedLibrary[0]?.patchName)
        assertEquals(1, vm.uiState.value.saveSuccessCount)
    }

    @Test
    fun `effect and patch controls ignore updates while saving`() = kotlinx.coroutines.test.runTest {
        val response = kotlinx.coroutines.CompletableDeferred<GP200Preset?>()
        val midi = FakeMidiManager().apply { saveHandler = { _, _ -> response.await() } }
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.loadPreset(createInitialPreset())
        val sent = vm.uiState.value.preset!!
        vm.saveToDevice()
        testScheduler.runCurrent()

        vm.toggleSlot(sent.effects.first().slotIndex)
        vm.updateParam(sent.effects.first().slotIndex, 0, 99f)
        vm.reorderSlots(0, 1)
        vm.updatePatchVolume(1)
        vm.updatePatchTempo(200)

        assertEquals(sent, vm.uiState.value.preset)
    }

    @Test
    fun `failed import save remains dirty and does not cache unverified data`() = kotlinx.coroutines.test.runTest {
        val midi = FakeMidiManager().apply { saveHandler = { _, _ -> null } }
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.midiDelayProvider = {}
        vm.loadPreset(createInitialPreset())
        vm.importPreset(createInitialPreset().copy(patchName = "Imported"), 0, true)
        testScheduler.runCurrent()
        assertTrue(vm.uiState.value.hasUnsavedChanges)
        assertFalse(vm.uiState.value.isSaving)
        assertNotNull(vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.cachedLibrary.isEmpty())
    }

    @Test
    fun `name notification during save preserves the sent patch`() = kotlinx.coroutines.test.runTest {
        val response = kotlinx.coroutines.CompletableDeferred<GP200Preset?>()
        val midi = FakeMidiManager().apply { saveHandler = { _, _ -> response.await() } }
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.loadPreset(createInitialPreset())
        vm.renamePatch("Sent")
        val sent = vm.uiState.value.preset!!
        vm.saveToDevice()
        testScheduler.runCurrent()
        vm.renamePatch("New edit")
        vm.handlePedalEvent(PedalEvent.PresetNameLoaded(0, "Sent"))
        response.complete(sent)
        testScheduler.runCurrent()
        assertEquals("Sent", vm.uiState.value.preset?.patchName)
        assertFalse(vm.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `import applies drum sync to pedal before save verification`() = kotlinx.coroutines.test.runTest {
        val midi = FakeMidiManager().apply {
            saveHandler = { preset, _ -> preset.copy(drumSync = drumSyncsSent.lastOrNull()?.first ?: false) }
        }
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.midiDelayProvider = {}
        vm.loadPreset(createInitialPreset())
        vm.importPreset(createInitialPreset().copy(drumSync = true), 0, true)
        testScheduler.runCurrent()
        assertFalse(vm.uiState.value.hasUnsavedChanges)
        assertEquals(1, vm.uiState.value.saveSuccessCount)
    }

    @Test
    fun `full resync cannot interrupt pending save`() = kotlinx.coroutines.test.runTest {
        val response = kotlinx.coroutines.CompletableDeferred<GP200Preset?>()
        val midi = FakeMidiManager().apply { saveHandler = { _, _ -> response.await() } }
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.loadPreset(createInitialPreset())
        vm.saveToDevice()
        testScheduler.runCurrent()
        vm.forceFullLibraryResync()
        assertFalse(vm.uiState.value.isPerformingFullSync)
        assertFalse(midi.fullSyncStarted)
    }

    private fun presetWithControls(): GP200Preset = com.valeton.editor.core.codec.PRSTDecoder(
        File("../core-protocol/src/test/resources/fixtures/clean.prst").readBytes()
    ).decode()

    @Test
    fun `knob assignment selects valid effect parameter and marks dirty`() = kotlinx.coroutines.test.runTest {
        val midi = FakeMidiManager()
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.loadPreset(presetWithControls())
        vm.updateQuickKnob(0, 0, 0)
        assertEquals(com.valeton.editor.core.model.QuickKnobAssignment(0, 0), vm.uiState.value.preset?.controlMappings?.knobs?.get(0))
        assertTrue(vm.uiState.value.hasUnsavedChanges)
        assertEquals(1, midi.quickAssignments.size)
        vm.updateQuickKnob(0, 0, 999)
        assertEquals(1, midi.quickAssignments.size)
    }

    @Test
    fun `CTRL1 replaces only its effect mask and never toggles the effect`() = kotlinx.coroutines.test.runTest {
        val midi = FakeMidiManager()
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        val original = presetWithControls()
        vm.loadPreset(original)
        vm.setCtrl1Effect(8)
        val mapped = vm.uiState.value.preset!!.controlMappings!!
        assertEquals(listOf(8), mapped.ctrls[0].effectBlocks)
        assertEquals(original.controlMappings!!.ctrls.drop(1), mapped.ctrls.drop(1))
        assertEquals(original.effects, vm.uiState.value.preset!!.effects)
        assertTrue(midi.toggles.isEmpty())
        assertEquals(1, midi.ctrlAssignments.size)
    }

    @Test
    fun `CTRL 2 to 4 edits update and send only the selected record`() = kotlinx.coroutines.test.runTest {
        val midi = FakeMidiManager()
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        val original = presetWithControls()
        vm.loadPreset(original)

        vm.setCtrlTarget(ctrlIndex = 2, blockIndex = 3, enabled = true)

        val updated = vm.uiState.value.preset!!.controlMappings!!
        assertTrue(updated.ctrls[2].targetMask and (1L shl 3) != 0L)
        assertEquals(original.controlMappings!!.ctrls.filterIndexed { index, _ -> index != 2 }, updated.ctrls.filterIndexed { index, _ -> index != 2 })
        assertEquals(listOf(2), midi.ctrlAssignments.map { it.first })
        assertEquals(updated.ctrls[2], midi.ctrlAssignments.single().second)
        assertTrue(vm.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `legacy unknown mappings are not overwritten by UI edits`() = kotlinx.coroutines.test.runTest {
        val midi = FakeMidiManager()
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.loadPreset(createInitialPreset())
        vm.updateQuickKnob(0, 0, 0)
        vm.setCtrl1Effect(0)
        assertNull(vm.uiState.value.preset!!.controlMappings)
        assertTrue(midi.quickAssignments.isEmpty())
        assertTrue(midi.ctrlAssignments.isEmpty())
    }

    @Test
    fun `offline mapping edits do not send device commands`() = kotlinx.coroutines.test.runTest {
        val midi = FakeMidiManager()
        midi._connectionState.value = MidiConnectionState.Disconnected
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.loadPreset(presetWithControls())
        vm.updateQuickKnob(0, 0, 0)
        vm.setCtrl1Effect(9)
        assertTrue(vm.uiState.value.hasUnsavedChanges)
        assertTrue(midi.quickAssignments.isEmpty())
        assertTrue(midi.ctrlAssignments.isEmpty())
    }

    @Test
    fun `save rejects different readback control assignments`() = kotlinx.coroutines.test.runTest {
        val preset = presetWithControls()
        val midi = FakeMidiManager().apply { saveHandler = { value, _ -> value.copy(controlMappings = value.controlMappings!!.withCtrl1Block(9)) } }
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.loadPreset(preset)
        vm.saveToDevice()
        testScheduler.runCurrent()
        assertTrue(vm.uiState.value.hasUnsavedChanges)
        assertEquals(0, vm.uiState.value.saveSuccessCount)
        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `reordering chain keeps mapping attached to block identity`() = kotlinx.coroutines.test.runTest {
        val vm = PresetViewModel(FakeMidiManager(), coroutineScope = backgroundScope)
        val original = presetWithControls()
        vm.loadPreset(original)
        vm.reorderSlots(0, 5)
        assertEquals(original.controlMappings, vm.uiState.value.preset!!.controlMappings)
    }

    @Test
    fun `changing effect clears only knob assignments whose parameter becomes invalid`() = kotlinx.coroutines.test.runTest {
        val midi = FakeMidiManager()
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        val preset = presetWithControls()
        val controls = preset.controlMappings!!.withKnob(0, com.valeton.editor.core.model.QuickKnobAssignment(0, 3))
            .withKnob(1, com.valeton.editor.core.model.QuickKnobAssignment(0, 0))
        vm.loadPreset(preset.copy(controlMappings = controls))
        vm.changeEffect(0, 0)
        assertEquals(com.valeton.editor.core.model.QuickKnobAssignment.OFF, vm.uiState.value.preset!!.controlMappings!!.knobs[0])
        assertEquals(controls.knobs[1], vm.uiState.value.preset!!.controlMappings!!.knobs[1])
        assertEquals(listOf(0), midi.quickAssignments.map { it.first })
    }

    @Test
    fun `mapping edits are blocked while live import is in progress`() = kotlinx.coroutines.test.runTest {
        val midi = FakeMidiManager()
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.loadPreset(presetWithControls())
        vm.importPreset(presetWithControls(), 1, false)
        testScheduler.runCurrent()
        assertTrue(vm.uiState.value.isImporting)
        vm.setCtrl1Effect(8)
        vm.updateQuickKnob(0, 0, 0)
        assertTrue(midi.ctrlAssignments.isEmpty())
        assertTrue(midi.quickAssignments.isEmpty())
    }

    @Test
    fun `unknown legacy mappings do not clear assignments returned by save readback`() = kotlinx.coroutines.test.runTest {
        val known = presetWithControls()
        val midi = FakeMidiManager().apply { saveHandler = { value, _ -> value.copy(controlMappings = known.controlMappings) } }
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.loadPreset(known.copy(controlMappings = null))
        vm.saveToDevice()
        testScheduler.runCurrent()
        assertEquals(1, vm.uiState.value.saveSuccessCount)
        assertEquals(known.controlMappings, vm.uiState.value.cachedLibrary[0]?.controlMappings)
    }

    @Test
    fun `failed import releases controls and preserves pending edits`() = kotlinx.coroutines.test.runTest {
        val vm = PresetViewModel(FakeMidiManager(), coroutineScope = backgroundScope)
        vm.midiDelayProvider = { throw IllegalStateException("Disconnected") }
        vm.loadPreset(presetWithControls())
        vm.importPreset(presetWithControls(), 1, false)
        testScheduler.runCurrent()
        assertFalse(vm.uiState.value.isImporting)
        assertNotNull(vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `pedal effect changes invalidate incompatible knob parameters before another save`() = kotlinx.coroutines.test.runTest {
        val midi = FakeMidiManager()
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        val preset = presetWithControls()
        val controls = preset.controlMappings!!.withKnob(0, com.valeton.editor.core.model.QuickKnobAssignment(0, 3))
        vm.loadPreset(preset.copy(controlMappings = controls))
        vm.handlePedalEvent(PedalEvent.EffectChanged(0, 0))
        assertEquals(com.valeton.editor.core.model.QuickKnobAssignment.OFF, vm.uiState.value.preset!!.controlMappings!!.knobs[0])
        assertTrue(vm.uiState.value.hasUnsavedChanges)
        assertNotNull(vm.uiState.value.errorMessage)
        assertTrue(midi.effectChanges.isEmpty())
    }

    @Test
    fun `import immediately updates confirmed list name while save remains pending`() = kotlinx.coroutines.test.runTest {
        val repository = PresetLibraryRepository(tempDir)
        repository.saveFullLibrary(createDummyLibrary())
        val response = kotlinx.coroutines.CompletableDeferred<GP200Preset?>()
        val midi = FakeMidiManager().apply { saveHandler = { _, _ -> response.await() } }
        val vm = PresetViewModel(midi, libraryRepository = repository, coroutineScope = backgroundScope)
        vm.midiDelayProvider = {}
        testScheduler.runCurrent()
        vm.importPreset(createInitialPreset().copy(patchName = "Imported name"), 5, true)
        testScheduler.runCurrent()
        assertEquals("Imported name", vm.uiState.value.presetNames[5])
        assertEquals("Imported name", vm.uiState.value.preset?.patchName)
        assertEquals("Preset 005", repository.loadFullLibrary()?.get(5)?.patchName)
    }

    @Test
    fun `reserved bytes changing in confirmed control readback do not fail save`() = kotlinx.coroutines.test.runTest {
        val original = presetWithControls()
        val midi = FakeMidiManager().apply { saveHandler = { value, _ ->
            value.copy(controlMappings = value.controlMappings!!.copy(ctrls = value.controlMappings!!.ctrls.map { it.copy(reserved = it.reserved xor 0xffff) }))
        } }
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.loadPreset(original)
        vm.saveToDevice()
        testScheduler.runCurrent()
        assertEquals(1, vm.uiState.value.saveSuccessCount)
        assertFalse(vm.uiState.value.hasUnsavedChanges)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `stale readback is reread without committing again and confirmed name is updated`() = kotlinx.coroutines.test.runTest {
        val expected = createInitialPreset().copy(patchName = "Fresh name")
        val stale = expected.copy(patchName = "Old name")
        val midi = FakeMidiManager().apply {
            saveHandler = { _, _ -> stale }
            verificationResponses.add(stale)
            verificationResponses.add(expected)
        }
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.loadPreset(expected)
        vm.saveToDevice()
        testScheduler.runCurrent()
        assertEquals(1, midi.saves.size)
        assertEquals(listOf(0, 0), midi.verificationReads)
        assertEquals(1, vm.uiState.value.saveSuccessCount)
        assertEquals("Fresh name", vm.uiState.value.presetNames[0])
        assertFalse(vm.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `permanent readback mismatch remains pending after bounded rereads`() = kotlinx.coroutines.test.runTest {
        val expected = createInitialPreset()
        val wrong = expected.copy(patchVolume = 3)
        val midi = FakeMidiManager().apply {
            saveHandler = { _, _ -> wrong }
            repeat(5) { verificationResponses.add(wrong) }
        }
        val vm = PresetViewModel(midi, coroutineScope = backgroundScope)
        vm.loadPreset(expected)
        vm.saveToDevice()
        testScheduler.runCurrent()
        assertEquals(1, midi.saves.size)
        assertEquals(2, midi.verificationReads.size)
        assertEquals(0, vm.uiState.value.saveSuccessCount)
        assertTrue(vm.uiState.value.hasUnsavedChanges)
        assertTrue(vm.uiState.value.errorMessage!!.contains("volume"))
    }


    @Test
    fun `cached user model names appear in state at startup`() {
        UserModelNamesRepository(tempDir).save(mapOf(168820736 to "ir1"))
        val vm = PresetViewModel(
            FakeMidiManager(),
            userModelNamesRepository = UserModelNamesRepository(tempDir),
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
        assertEquals(mapOf(168820736 to "ir1"), vm.uiState.value.userModelNames)
    }

    @Test
    fun `names from the driver merge over the cache and are persisted`() {
        UserModelNamesRepository(tempDir).save(mapOf(168820736 to "old", 168820737 to "keep"))
        val midi = FakeMidiManager()
        val vm = PresetViewModel(
            midi,
            userModelNamesRepository = UserModelNamesRepository(tempDir),
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
        midi._userModelNames.value = mapOf(168820736 to "ir1")
        val expected = mapOf(168820736 to "ir1", 168820737 to "keep")
        assertEquals(expected, vm.uiState.value.userModelNames)
        assertEquals(expected, UserModelNamesRepository(tempDir).load())
    }

    @Test
    fun `names stay after disconnect`() {
        val midi = FakeMidiManager()
        val vm = PresetViewModel(
            midi,
            userModelNamesRepository = UserModelNamesRepository(tempDir),
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
        midi._userModelNames.value = mapOf(251658245 to "nam1")
        midi._connectionState.value = MidiConnectionState.Disconnected
        assertEquals(mapOf(251658245 to "nam1"), vm.uiState.value.userModelNames)
    }

    private fun saveAsViewModel(midi: FakeMidiManager) =
        PresetViewModel(midi, coroutineScope = CoroutineScope(Dispatchers.Unconfined)).apply {
            midiDelayProvider = {}
            loadPreset(createInitialPreset(), slot = 0)
        }

    @Test
    fun `saveAs to current slot behaves like saveToDevice`() {
        val midi = FakeMidiManager()
        val vm = saveAsViewModel(midi)
        vm.saveAs(0, "Novo", "Autor", 70)
        assertTrue(midi.presetChangesSent.isEmpty())
        assertEquals(listOf(false), midi.forceReplays)
        assertEquals(listOf("Novo" to 0), midi.saves)
        assertEquals(70, vm.uiState.value.preset?.patchVolume)
        assertEquals("Autor", vm.uiState.value.preset?.author)
    }

    @Test
    fun `saveAs to another slot switches slot, replays and saves there`() {
        val midi = FakeMidiManager()
        val vm = saveAsViewModel(midi)
        vm.saveAs(10, "Novo", "Autor", 70)
        val state = vm.uiState.value
        assertEquals(listOf(10), midi.presetChangesSent)
        assertEquals(listOf(true), midi.forceReplays)
        assertEquals(listOf("Novo" to 10), midi.saves)
        assertEquals(10, state.currentSlotNumber)
        assertEquals("Novo", state.presetNames[10])
        assertEquals(70, state.cachedLibrary[10]?.patchVolume)
        assertFalse(state.hasUnsavedChanges)
    }

    @Test
    fun `saveAs to another slot leaves the source untouched`() {
        val midi = FakeMidiManager()
        val vm = saveAsViewModel(midi)
        val sourceName = vm.uiState.value.presetNames[0]
        val sourceCache = vm.uiState.value.cachedLibrary[0]
        vm.saveAs(10, "Novo", "Autor", 70)
        assertEquals(sourceName, vm.uiState.value.presetNames[0])
        assertEquals(sourceCache, vm.uiState.value.cachedLibrary[0])
        assertTrue(midi.patchNamesSent.isEmpty(), "Source edit buffer must not be renamed")
    }

    @Test
    fun `saveAs restores the source name after an earlier local rename`() {
        val midi = FakeMidiManager()
        val vm = saveAsViewModel(midi)
        val sourceName = vm.uiState.value.presetNames[0]
        vm.renamePatch("Temp")
        assertEquals("Temp", vm.uiState.value.presetNames[0])
        vm.saveAs(10, "Novo", "Autor", 70)
        assertEquals(sourceName, vm.uiState.value.presetNames[0])
        assertEquals("Novo", vm.uiState.value.presetNames[10])
        assertEquals(listOf(0 to sourceName), midi.restoredNames, "Driver name list must be restored too")
    }

    @Test
    fun `failed confirmation keeps changes pending on the target slot`() {
        val midi = FakeMidiManager().apply { saveHandler = { _, _ -> null } }
        val vm = saveAsViewModel(midi)
        vm.saveAs(10, "Novo", "Autor", 70)
        val state = vm.uiState.value
        assertEquals(10, state.currentSlotNumber)
        assertTrue(state.hasUnsavedChanges)
        assertTrue(state.errorMessage!!.startsWith("Não foi possível confirmar o salvamento"))
        assertEquals("Novo", state.preset?.patchName)
    }

    @Test
    fun `PresetLoaded for the target during saveAs does not replace the edited preset`() {
        val midi = FakeMidiManager()
        val vm = saveAsViewModel(midi)
        val other = GP200Preset(patchName = "Old Target", effects = emptyList())
        vm.midiDelayProvider = { midi._pedalEvents.tryEmit(PedalEvent.PresetLoaded(other, 10)) }
        vm.saveAs(10, "Novo", "Autor", 70)
        assertEquals("Novo", vm.uiState.value.preset?.patchName)
        assertEquals(listOf("Novo" to 10), midi.saves)
    }

    @Test
    fun `saveAs while disconnected shows error and changes nothing`() {
        val midi = FakeMidiManager()
        val vm = saveAsViewModel(midi)
        midi._connectionState.value = MidiConnectionState.Disconnected
        vm.saveAs(10, "Novo", "Autor", 70)
        val state = vm.uiState.value
        assertEquals("Conecte a pedaleira antes de salvar.", state.errorMessage)
        assertTrue(midi.presetChangesSent.isEmpty())
        assertEquals(0, state.currentSlotNumber)
        assertEquals("Lead Rock", state.preset?.patchName)
    }

    @Test
    fun `saveAs is ignored while busy`() {
        val midi = FakeMidiManager()
        val vm = saveAsViewModel(midi)
        vm.forceFullLibraryResync()
        assertTrue(vm.uiState.value.isPerformingFullSync)
        vm.saveAs(10, "Novo", "Autor", 70)
        assertTrue(midi.presetChangesSent.isEmpty())
        assertTrue(midi.saves.isEmpty())
    }
}



