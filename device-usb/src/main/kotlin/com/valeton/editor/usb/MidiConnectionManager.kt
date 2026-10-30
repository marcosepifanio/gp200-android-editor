package com.valeton.editor.usb

import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.core.model.QuickKnobAssignment
import com.valeton.editor.core.model.CtrlAssignment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

data class FullSyncProgress(
    val currentSlot: Int,
    val totalSlots: Int = 256,
    val currentPatchName: String,
    val progressFraction: Float,
    val isCompleted: Boolean = false
)

sealed interface PedalEvent {
    data class PresetLoaded(val preset: GP200Preset, val slot: Int) : PedalEvent
    data class ParamChanged(val blockIndex: Int, val paramIndex: Int, val value: Float) : PedalEvent
    data class ToggleChanged(val blockIndex: Int, val enabled: Boolean) : PedalEvent
    data class EffectChanged(val blockIndex: Int, val effectId: Int) : PedalEvent
    data class SlotChanged(val slot: Int) : PedalEvent
    data class PresetNameLoaded(val slot: Int, val name: String) : PedalEvent
    data class FullLibrarySynced(val presets: Map<Int, GP200Preset>) : PedalEvent
    data class DrumSyncChanged(val enabled: Boolean) : PedalEvent
    data class DrumBpmChanged(val bpm: Int) : PedalEvent
    data class DrumVolumeChanged(val volume: Int) : PedalEvent
    data class DrumPatternChanged(val patternId: Int) : PedalEvent
    data class DrumPlayChanged(val playing: Boolean) : PedalEvent
}

interface MidiConnectionManager {
    val connectionState: StateFlow<MidiConnectionState>
    val pedalEvents: SharedFlow<PedalEvent>
    val presetNamesState: StateFlow<List<String>>
    val namesSyncProgress: StateFlow<Int>
    val fullSyncProgress: StateFlow<FullSyncProgress?> get() = MutableStateFlow(null)
    /** Names of user IRs and SnapTones read from the pedal, keyed by catalog effectId. */
    val userModelNames: StateFlow<Map<Int, String>> get() = MutableStateFlow(emptyMap())
    fun startFullLibrarySync() {}
    fun cancelFullLibrarySync() {}

    fun sendParamChange(blockIndex: Int, paramIndex: Int, effectId: Int, value: Float)
    fun sendToggle(blockIndex: Int, enabled: Boolean)
    fun sendEffectChange(blockIndex: Int, effectId: Int)
    fun sendReorder(order: List<Int>, send: Int = 4, ret: Int = 4)
    suspend fun savePreset(preset: GP200Preset, slot: Int, forceReplay: Boolean = false): GP200Preset? = null
    suspend fun readPresetForVerification(slot: Int): GP200Preset? = null
    fun sendQuickKnobAssignment(index: Int, assignment: QuickKnobAssignment) {}
    fun sendCtrlAssignment(index: Int, assignment: CtrlAssignment) {}
    fun sendSaveCommit(presetName: String, slot: Int)
    fun sendPatchName(name: String)
    /** Puts a slot's confirmed name back after an unsaved local rename ("save as" leaves the source untouched). */
    fun restoreSlotName(slot: Int, name: String) {}
    fun sendAuthorName(author: String) {}
    fun sendPresetChange(slot: Int)
    fun sendControlChange(cc: Int, value: Int, channel: Int = 0)
    fun sendPatchVolume(volume: Int)
    fun sendPatchPan(pan: Int)
    fun sendPatchTempo(bpm: Int)
    fun sendTapTempo()
    fun triggerCtrl(ctrlNumber: Int, active: Boolean)
    fun sendDrumMenu(enabled: Boolean) {}
    fun sendDrumPlay(playing: Boolean) {}
    fun sendDrumPattern(patternId: Int) {}
    fun sendDrumVolume(volume: Int) {}
    fun sendDrumSync(enabled: Boolean, bpm: Int = 120) {}
    fun sendDrumBpm(bpm: Int, isSyncWithPatch: Boolean = false) {}
    fun requestReadPreset(slot: Int)
    fun requestPresetName(slot: Int)
    fun startPresetNamesScan()
    fun pausePresetNamesScan()
    fun scanDevices()
    fun disconnect()
    fun syncCurrentPatch(slot: Int = -1) {}
    fun preloadCachedNames(names: Map<Int, String>) {}
}
