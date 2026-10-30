package com.valeton.editor.viewmodel

import com.valeton.editor.core.model.DrumGenre
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.usb.FullSyncProgress

data class DrumUiState(
    val isPlaying: Boolean = false,
    val selectedPatternId: Int = 0,
    val volume: Int = 50,
    val selectedGenre: DrumGenre = DrumGenre.ALL,
    val searchQuery: String = "",
    val bpm: Int = 120,
    val isSyncWithPatch: Boolean = false
)

data class PresetUiState(
    val preset: GP200Preset? = null,
    val selectedSlotIndex: Int = 0,
    val currentSlotNumber: Int = 0,
    val presetNames: List<String> = emptyList(),
    val namesSyncProgress: Int = 0,
    val isSaving: Boolean = false,
    val isImporting: Boolean = false,
    val saveSuccessCount: Int = 0,
    val hasUnsavedChanges: Boolean = false,
    val errorMessage: String? = null,
    val drum: DrumUiState = DrumUiState(),
    val confirmedSlots: Set<Int> = emptySet(),
    val isSyncingCurrentPreset: Boolean = false,
    val isPerformingFullSync: Boolean = false,
    val fullSyncProgress: FullSyncProgress? = null,
    val cachedLibrary: Map<Int, GP200Preset> = emptyMap(),
    /** User IR/SnapTone names from the pedal, keyed by effectId; never part of the preset. */
    val userModelNames: Map<Int, String> = emptyMap()
)

