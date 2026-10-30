package com.valeton.editor.viewmodel

import com.valeton.editor.DebugLog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valeton.editor.core.codec.SysExCodec
import com.valeton.editor.core.codec.PresetSaveVerifier
import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.DrumGenre
import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.core.model.QuickKnobAssignment
import com.valeton.editor.core.model.KnobParam
import kotlin.math.roundToInt
import com.valeton.editor.repository.PresetLibraryRepository
import com.valeton.editor.repository.UserModelNamesRepository
import com.valeton.editor.repository.PresetNamesRepository
import com.valeton.editor.usb.MidiConnectionManager
import com.valeton.editor.usb.MidiConnectionState
import com.valeton.editor.usb.PedalEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

class PresetViewModel(
    private val midiManager: MidiConnectionManager,
    private val repository: PresetNamesRepository? = null,
    private val libraryRepository: PresetLibraryRepository? = null,
    private val userModelNamesRepository: UserModelNamesRepository? = null,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope
    private val ioDispatcher: CoroutineDispatcher =
        coroutineScope?.coroutineContext?.get(kotlin.coroutines.ContinuationInterceptor) as? CoroutineDispatcher
            ?: Dispatchers.IO
    var midiDelayProvider: suspend (Long) -> Unit = { kotlinx.coroutines.delay(it) }
    private var lastSavedSlot: Int? = null
    private var lastSavedTimestamp: Long = 0L
    /** Name a slot had before its first unsaved local rename, so "save as" can restore the source. */
    private var nameBeforeLocalRename: Pair<Int, String>? = null
    private val _uiState = MutableStateFlow(PresetUiState())
    val uiState: StateFlow<PresetUiState> = _uiState.asStateFlow()

    init {
        val cachedLib = if (libraryRepository != null && libraryRepository.isLibraryCached()) {
            libraryRepository.loadFullLibrary()
        } else null

        if (cachedLib != null && cachedLib.size == 256 && cachedLib.values.all { it.effects.isNotEmpty() }) {
            val names = (0..255).map { idx -> cachedLib[idx]?.patchName ?: "Patch %02d".format(idx) }
            _uiState.update { current ->
                val active = cachedLib[current.currentSlotNumber]?.takeIf { it.effects.isNotEmpty() } ?: current.preset
                current.copy(
                    cachedLibrary = cachedLib,
                    presetNames = names,
                    confirmedSlots = (0..255).toSet(),
                    namesSyncProgress = 256,
                    preset = active
                )
            }
            midiManager.preloadCachedNames((0..255).associateWith { names[it] })
        } else {
            val initialNames = repository?.loadCachedNames() ?: midiManager.presetNamesState.value
            val confirmedFromCache = initialNames.mapIndexedNotNull { idx, name ->
                if (name.isNotBlank() && !name.matches(Regex("""^Patch \d+$"""))) idx else null
            }.toSet()
            val allConfirmed = confirmedFromCache.size == 256
            _uiState.update {
                it.copy(
                    presetNames = initialNames,
                    confirmedSlots = confirmedFromCache,
                    namesSyncProgress = if (allConfirmed) 256 else it.namesSyncProgress
                )
            }
            if (allConfirmed) {
                midiManager.preloadCachedNames(initialNames.mapIndexed { idx, name -> idx to name }.toMap())
            }
        }

        userModelNamesRepository?.load()?.takeIf { it.isNotEmpty() }?.let { cached ->
            _uiState.update { it.copy(userModelNames = cached) }
        }

        scope.launch {
            midiManager.userModelNames.collect { received ->
                if (received.isEmpty()) return@collect
                val merged = _uiState.updateAndGet { it.copy(userModelNames = it.userModelNames + received) }.userModelNames
                scope.launch(ioDispatcher) { userModelNamesRepository?.save(merged) }
            }
        }

        scope.launch {
            midiManager.pedalEvents.collect { event ->
                handlePedalEvent(event)
            }
        }

        scope.launch {
            midiManager.presetNamesState.collect { names ->
                if (names.isNotEmpty()) {
                    _uiState.update { current ->
                        val newConfirmed = current.confirmedSlots.toMutableSet()
                        val merged = names.mapIndexed { idx, name ->
                            if (current.confirmedSlots.contains(idx) ||
                                (idx == current.currentSlotNumber && (current.hasUnsavedChanges || current.isSaving || current.isImporting))) {
                                current.presetNames.getOrElse(idx) { name }
                            } else {
                                if (name.isNotBlank() && !name.matches(Regex("""^Patch \d+$"""))) {
                                    newConfirmed.add(idx)
                                }
                                name
                            }
                        }
                        current.copy(presetNames = merged, confirmedSlots = newConfirmed)
                    }
                    repository?.saveCachedNames(names)
                }
            }
        }

        scope.launch {
            midiManager.namesSyncProgress.collect { progress ->
                _uiState.update { it.copy(namesSyncProgress = progress) }
            }
        }

        scope.launch {
            midiManager.fullSyncProgress.collect { progress ->
                _uiState.update { it.copy(fullSyncProgress = progress) }
            }
        }

        scope.launch {
            midiManager.connectionState.collect { connection ->
                when (connection) {
                    is MidiConnectionState.Connected -> {
                        checkAndTriggerFullSync()
                    }
                    is MidiConnectionState.Disconnected, is MidiConnectionState.Error -> {
                        _uiState.update {
                            it.copy(
                                isPerformingFullSync = false,
                                fullSyncProgress = null
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun checkAndTriggerFullSync() {
        if (libraryRepository == null || _uiState.value.isImporting || _uiState.value.isSaving || _uiState.value.hasUnsavedChanges) return
        val isFull = _uiState.value.cachedLibrary.size == 256 && _uiState.value.cachedLibrary.values.all { it.effects.isNotEmpty() }
        if (!isFull && !_uiState.value.isPerformingFullSync) {
            _uiState.update { it.copy(isPerformingFullSync = true) }
            midiManager.startFullLibrarySync()
        }
    }

    fun handlePedalEvent(event: PedalEvent) {
        if ((_uiState.value.isImporting || _uiState.value.isSaving) &&
            event !is PedalEvent.DrumPlayChanged && event !is PedalEvent.DrumVolumeChanged && event !is PedalEvent.DrumPatternChanged) return
        when (event) {
            is PedalEvent.PresetLoaded -> {
                val currentSlot = _uiState.value.currentSlotNumber
                val isRecentSave = (event.slot == lastSavedSlot && (System.currentTimeMillis() - lastSavedTimestamp < 3000L || _uiState.value.isSaving))
                if (event.slot == currentSlot) {
                    if ((_uiState.value.hasUnsavedChanges || isRecentSave) && !_uiState.value.isSyncingCurrentPreset) {
                        return
                    }
                } else if (isRecentSave && !_uiState.value.isSyncingCurrentPreset) {
                    return
                }
                loadPreset(event.preset, event.slot)
                updatePresetNameLocally(event.slot, event.preset.patchName)
                _uiState.update {
                    it.copy(
                        confirmedSlots = it.confirmedSlots + event.slot,
                        isSyncingCurrentPreset = false,
                        cachedLibrary = it.cachedLibrary + (event.slot to event.preset)
                    )
                }
                scope.launch(ioDispatcher) {
                    libraryRepository?.updatePreset(event.slot, event.preset)
                }
            }
            is PedalEvent.ParamChanged -> {
                applyParamFromDevice(event.blockIndex, event.paramIndex, event.value)
            }
            is PedalEvent.ToggleChanged -> {
                applyToggleFromDevice(event.blockIndex, event.enabled)
            }
            is PedalEvent.EffectChanged -> {
                applyEffectChangeFromDevice(event.blockIndex, event.effectId)
            }
            is PedalEvent.SlotChanged -> {
                val currentSlot = _uiState.value.currentSlotNumber
                if (event.slot == currentSlot && (_uiState.value.hasUnsavedChanges || _uiState.value.isSaving)) {
                    return
                }
                val cachedPreset = _uiState.value.cachedLibrary[event.slot]
                if (cachedPreset != null) {
                    _uiState.update {
                        val isSync = cachedPreset.drumSync || it.drum.isSyncWithPatch
                        val newDrumBpm = if (isSync) cachedPreset.patchTempo else it.drum.bpm
                        it.copy(
                            currentSlotNumber = event.slot,
                            preset = cachedPreset,
                            selectedSlotIndex = cachedPreset.effects.firstOrNull()?.slotIndex ?: 0,
                            hasUnsavedChanges = false,
                            drum = it.drum.copy(isSyncWithPatch = isSync, bpm = newDrumBpm)
                        )
                    }
                } else {
                    val isConfirmed = _uiState.value.confirmedSlots.contains(event.slot)
                    val rawCachedName = _uiState.value.presetNames.getOrNull(event.slot)?.takeIf { it.isNotBlank() }
                    val cachedName = if (isConfirmed || (rawCachedName != null && !rawCachedName.matches(Regex("""^Patch \d+$""")))) {
                        rawCachedName
                    } else null

                    _uiState.update {
                        it.copy(
                            currentSlotNumber = event.slot,
                            preset = it.preset?.copy(patchName = cachedName ?: "Carregando...")
                        )
                    }
                }
            }
            is PedalEvent.PresetNameLoaded -> {
                if (event.slot == _uiState.value.currentSlotNumber &&
                    (_uiState.value.isSaving || _uiState.value.hasUnsavedChanges)) return
                updatePresetNameLocally(event.slot, event.name)
                _uiState.update {
                    it.copy(confirmedSlots = it.confirmedSlots + event.slot)
                }
            }
            is PedalEvent.FullLibrarySynced -> {
                val validPresets = event.presets.filter { (slot, preset) -> slot in 0..255 && preset.effects.isNotEmpty() }
                val missingCount = 256 - validPresets.size
                _uiState.update { current ->
                    val names = (0..255).map { idx ->
                        validPresets[idx]?.patchName ?: current.presetNames.getOrElse(idx) { "Patch ${SysExCodec.slotToLabel(idx)}" }
                    }
                    current.copy(
                        cachedLibrary = validPresets,
                        presetNames = names,
                        confirmedSlots = validPresets.keys,
                        isPerformingFullSync = false,
                        preset = validPresets[current.currentSlotNumber] ?: current.preset,
                        namesSyncProgress = validPresets.size,
                        errorMessage = if (missingCount == 0) null else
                            "Não foi possível ler $missingCount patches. Reconecte a pedaleira ou sincronize novamente."
                    )
                }
                scope.launch(ioDispatcher) { libraryRepository?.saveFullLibrary(validPresets) }
            }
            is PedalEvent.DrumSyncChanged -> {
                DebugLog.d("ValetonDrum") { "DrumSyncChanged received: enabled=${event.enabled}" }
                _uiState.update { current ->
                    val bpm = if (event.enabled && current.preset != null) current.preset.patchTempo else current.drum.bpm
                    current.copy(
                        preset = current.preset?.copy(drumSync = event.enabled),
                        drum = current.drum.copy(
                            isSyncWithPatch = event.enabled,
                            bpm = bpm
                        )
                    )
                }
            }
            is PedalEvent.DrumBpmChanged -> {
                DebugLog.d("ValetonDrum") { "DrumBpmChanged received: bpm=${event.bpm}" }
                _uiState.update { current ->
                    val updatedPreset = if (current.drum.isSyncWithPatch && current.preset != null) {
                        current.preset.copy(patchTempo = event.bpm)
                    } else {
                        current.preset
                    }
                    current.copy(
                        preset = updatedPreset,
                        drum = current.drum.copy(bpm = event.bpm)
                    )
                }
            }
            is PedalEvent.DrumVolumeChanged -> {
                DebugLog.d("ValetonDrum") { "DrumVolumeChanged received: volume=${event.volume}" }
                _uiState.update { current ->
                    current.copy(drum = current.drum.copy(volume = event.volume))
                }
            }
            is PedalEvent.DrumPatternChanged -> {
                DebugLog.d("ValetonDrum") { "DrumPatternChanged received: patternId=${event.patternId}" }
                _uiState.update { current ->
                    current.copy(drum = current.drum.copy(selectedPatternId = event.patternId))
                }
            }
            is PedalEvent.DrumPlayChanged -> {
                DebugLog.d("ValetonDrum") { "DrumPlayChanged received: playing=${event.playing}" }
                _uiState.update { current ->
                    current.copy(drum = current.drum.copy(isPlaying = event.playing))
                }
            }
        }
    }


    private fun applyParamFromDevice(slotIndex: Int, paramIndex: Int, value: Float) {
        val currentPreset = _uiState.value.preset ?: return
        val currentSlot = currentPreset.effects.find { it.slotIndex == slotIndex } ?: return
        val newParams = currentSlot.params.toMutableList()
        while (newParams.size <= paramIndex) {
            newParams.add(0f)
        }
        newParams[paramIndex] = value

        val updatedEffects = currentPreset.effects.map { slot ->
            if (slot.slotIndex == slotIndex) {
                slot.copy(params = newParams)
            } else {
                slot
            }
        }

        _uiState.update {
            it.copy(preset = currentPreset.copy(effects = updatedEffects))
        }
    }

    private fun applyToggleFromDevice(slotIndex: Int, enabled: Boolean) {
        val currentPreset = _uiState.value.preset ?: return
        val updatedEffects = currentPreset.effects.map { slot ->
            if (slot.slotIndex == slotIndex) {
                slot.copy(enabled = enabled)
            } else {
                slot
            }
        }
        _uiState.update {
            it.copy(preset = currentPreset.copy(effects = updatedEffects))
        }
    }

    private fun getInitialParamsForEffect(effectId: Int): List<Float> {
        val effectDef = EffectCatalog.getEffectById(effectId)
        val params = MutableList(15) { 0f }
        effectDef?.params?.forEach { p ->
            if (p.index in 0 until 15) {
                params[p.index] = p.defaultValue
            }
        }
        return params
    }

    private fun presetAfterEffectChange(preset: GP200Preset, block: Int, effectId: Int): Pair<GP200Preset, List<Int>> {
        val effects = preset.effects.map { if (it.slotIndex == block) it.copy(effectId = effectId, params = getInitialParamsForEffect(effectId)) else it }
        val definition = EffectCatalog.getEffectById(effectId)
        val validParams = definition?.params?.map { it.index }.orEmpty()
        val invalidKnobs = if (definition == null) emptyList() else preset.controlMappings?.knobs?.mapIndexedNotNull { index, assignment ->
            index.takeIf { assignment.blockIndex == block && assignment.paramIndex !in validParams }
        }.orEmpty()
        var controls = preset.controlMappings
        invalidKnobs.forEach { controls = controls?.withKnob(it, QuickKnobAssignment.OFF) }
        return preset.copy(effects = effects, controlMappings = controls) to invalidKnobs
    }

    private fun invalidateDeviceKnobs(indices: List<Int>) {
        if (indices.isEmpty()) return
        _uiState.update { it.copy(
            hasUnsavedChanges = true,
            errorMessage = "O novo efeito não possui o parâmetro de ${indices.joinToString { index -> "Knob ${index + 1}" }}. Selecione novamente na aba CONTROLES."
        ) }
        if (midiManager.connectionState.value is MidiConnectionState.Connected) {
            indices.forEach { midiManager.sendQuickKnobAssignment(it, QuickKnobAssignment.OFF) }
        }
    }

    private fun applyEffectChangeFromDevice(slotIndex: Int, newEffectId: Int) {
        val preset = _uiState.value.preset ?: return
        val (updated, invalidKnobs) = presetAfterEffectChange(preset, slotIndex, newEffectId)
        _uiState.update { it.copy(preset = updated) }
        invalidateDeviceKnobs(invalidKnobs)
    }

    fun loadPreset(preset: GP200Preset, slot: Int = _uiState.value.currentSlotNumber) {
        _uiState.update {
            val isSync = preset.drumSync || it.drum.isSyncWithPatch
            val newDrumBpm = if (isSync) preset.patchTempo else it.drum.bpm
            it.copy(
                preset = preset,
                currentSlotNumber = slot,
                selectedSlotIndex = preset.effects.firstOrNull()?.slotIndex ?: 0,
                hasUnsavedChanges = false,
                errorMessage = null,
                drum = it.drum.copy(isSyncWithPatch = isSync, bpm = newDrumBpm)
            )
        }
        nameBeforeLocalRename = null
    }

    fun importPreset(preset: GP200Preset, targetSlot: Int, saveToPedal: Boolean) {
        if (_uiState.value.isPerformingFullSync) return
        if (_uiState.value.isSaving || _uiState.value.isImporting) return
        if (saveToPedal && midiManager.connectionState.value !is MidiConnectionState.Connected) {
            _uiState.update { it.copy(errorMessage = "Conecte a pedaleira antes de salvar.") }
            return
        }
        val clampedSlot = targetSlot.coerceIn(0, 255)
        val shouldSwitchSlot = _uiState.value.currentSlotNumber != clampedSlot

        _uiState.update {
            it.copy(
                preset = preset,
                presetNames = it.presetNames.mapIndexed { index, name -> if (index == clampedSlot) preset.patchName else name },
                currentSlotNumber = clampedSlot,
                selectedSlotIndex = preset.effects.firstOrNull()?.slotIndex ?: 0,
                hasUnsavedChanges = true,
                isSaving = saveToPedal,
                isImporting = true,
                errorMessage = null
            )
        }

        scope.launch {
            try {
                midiManager.pausePresetNamesScan()

                // 1. If targetSlot != currentSlot, switch slot first and wait for device to settle
                if (shouldSwitchSlot) {
                    midiManager.sendPresetChange(clampedSlot)
                    midiDelayProvider(200)
                }

                if (saveToPedal) {
                    // The save transaction replays the whole imported snapshot in wire order.
                    _uiState.update { it.copy(isSaving = false, isImporting = false, hasUnsavedChanges = true) }
                    saveToDevice(clampedSlot, forceReplay = true)
                    return@launch
                }

                midiManager.sendPatchTempo(preset.patchTempo)
                midiDelayProvider(30)
                midiManager.sendDrumSync(preset.drumSync, preset.patchTempo)
                midiDelayProvider(120)

                // 2. Send live audition parameters to the pedal with safe throttling
                for (slot in preset.effects) {
                    midiManager.sendEffectChange(slot.slotIndex, slot.effectId)
                    midiDelayProvider(25)
                    EffectCatalog.liveParameterIndices(slot.effectId, slot.params.size).forEach { pIdx ->
                        midiManager.sendParamChange(slot.slotIndex, pIdx, slot.effectId, slot.params[pIdx])
                        midiDelayProvider(10)
                    }
                    midiManager.sendToggle(slot.slotIndex, slot.enabled)
                    midiDelayProvider(15)
                }

                midiDelayProvider(30)
                midiManager.sendReorder(preset.routingOrder, preset.fxLoopSend, preset.fxLoopReturn)
                midiDelayProvider(30)

                midiManager.sendPatchName(preset.patchName)
                midiDelayProvider(20)
                if (preset.author.isNotBlank()) {
                    midiManager.sendAuthorName(preset.author.take(16))
                    midiDelayProvider(20)
                }
                midiManager.sendPatchVolume(preset.patchVolume)
                midiDelayProvider(20)
                midiManager.sendPatchPan(preset.patchPan)
                midiDelayProvider(20)
                preset.controlMappings?.let { mappings ->
                    mappings.knobs.forEachIndexed { index, assignment ->
                        midiManager.sendQuickKnobAssignment(index, assignment)
                        midiDelayProvider(25)
                    }
                    mappings.ctrls.forEachIndexed { index, assignment ->
                        midiManager.sendCtrlAssignment(index, assignment)
                        midiDelayProvider(25)
                    }
                }
                _uiState.update { it.copy(isSaving = false, isImporting = false, hasUnsavedChanges = true) }
                if (saveToPedal) saveToDevice(clampedSlot)
            } catch (cancelled: CancellationException) {
                _uiState.update { it.copy(isImporting = false, isSaving = false, hasUnsavedChanges = true) }
                throw cancelled
            } catch (_: Exception) {
                _uiState.update { it.copy(
                    isImporting = false, isSaving = false, hasUnsavedChanges = true,
                    errorMessage = "Não foi possível concluir a importação. As alterações continuam pendentes."
                ) }
            }
        }
    }

    fun selectPresetSlot(slot: Int) {
        if (_uiState.value.isPerformingFullSync || _uiState.value.isSaving || _uiState.value.isImporting) return
        val targetSlot = slot.coerceIn(0, 255)
        val cachedPreset = _uiState.value.cachedLibrary[targetSlot]
        if (cachedPreset != null) {
            _uiState.update {
                val newDrumBpm = if (it.drum.isSyncWithPatch) cachedPreset.patchTempo else it.drum.bpm
                it.copy(
                    currentSlotNumber = targetSlot,
                    preset = cachedPreset,
                    selectedSlotIndex = cachedPreset.effects.firstOrNull()?.slotIndex ?: 0,
                    hasUnsavedChanges = false,
                    drum = it.drum.copy(bpm = newDrumBpm)
                )
            }
        } else {
            val isConfirmed = _uiState.value.confirmedSlots.contains(targetSlot)
            val rawCachedName = _uiState.value.presetNames.getOrNull(targetSlot)?.takeIf { it.isNotBlank() }
            val cachedName = if (isConfirmed || (rawCachedName != null && !rawCachedName.matches(Regex("""^Patch \d+$""")))) {
                rawCachedName
            } else null

            _uiState.update {
                it.copy(
                    currentSlotNumber = targetSlot,
                    preset = it.preset?.copy(patchName = cachedName ?: "Carregando...")
                )
            }
        }
        midiManager.sendPresetChange(targetSlot)
    }

    fun forceFullLibraryResync() {
        if (_uiState.value.isSaving || _uiState.value.isImporting || _uiState.value.isPerformingFullSync) return
        _uiState.update {
            it.copy(
                isPerformingFullSync = true,
                cachedLibrary = emptyMap(),
                confirmedSlots = emptySet(),
                namesSyncProgress = 0
            )
        }
        scope.launch(ioDispatcher) {
            libraryRepository?.clearCache()
        }
        midiManager.startFullLibrarySync()
    }

    fun selectSlot(slotIndex: Int) {
        if (editingBusy()) return
        _uiState.update { it.copy(selectedSlotIndex = slotIndex) }
    }

    fun toggleSlot(slotIndex: Int) {
        if (editingBusy()) return
        val currentPreset = _uiState.value.preset ?: return
        val currentSlot = currentPreset.effects.find { it.slotIndex == slotIndex } ?: return
        val newEnabled = !currentSlot.enabled

        val updatedEffects = currentPreset.effects.map { slot ->
            if (slot.slotIndex == slotIndex) {
                slot.copy(enabled = newEnabled)
            } else {
                slot
            }
        }

        _uiState.update {
            it.copy(
                preset = currentPreset.copy(effects = updatedEffects),
                hasUnsavedChanges = true
            )
        }

        midiManager.sendToggle(slotIndex, newEnabled)
    }

    fun updateParam(slotIndex: Int, paramIndex: Int, value: Float) {
        if (editingBusy()) return
        val currentPreset = _uiState.value.preset ?: return
        val currentSlot = currentPreset.effects.find { it.slotIndex == slotIndex } ?: return
        if (!value.isFinite()) return
        val param = EffectCatalog.getEffectById(currentSlot.effectId)?.params
            ?.find { it.index == paramIndex } as? KnobParam
        // Keep UI state and MIDI on the same catalog step; rounding only the label hides fractions.
        val editedValue = if (param != null) {
            val bounded = value.coerceIn(param.min, param.max)
            if (param.step > 0f) {
                (param.min + ((bounded - param.min) / param.step).roundToInt() * param.step)
                    .coerceIn(param.min, param.max)
            } else bounded
        } else value

        val newParams = currentSlot.params.toMutableList()
        while (newParams.size <= paramIndex) {
            newParams.add(0f)
        }
        newParams[paramIndex] = editedValue

        val updatedEffects = currentPreset.effects.map { slot ->
            if (slot.slotIndex == slotIndex) {
                slot.copy(params = newParams)
            } else {
                slot
            }
        }

        _uiState.update {
            it.copy(
                preset = currentPreset.copy(effects = updatedEffects),
                hasUnsavedChanges = true
            )
        }

        midiManager.sendParamChange(slotIndex, paramIndex, currentSlot.effectId, editedValue)
    }

    private fun editingBusy(): Boolean = _uiState.value.let {
        it.isSaving || it.isImporting || it.isPerformingFullSync
    }

    private fun controlsBusy(): Boolean = editingBusy() || _uiState.value.isSyncingCurrentPreset

    fun updateQuickKnob(index: Int, blockIndex: Int, paramIndex: Int) {
        if (controlsBusy() || index !in 0..2) return
        val preset = _uiState.value.preset ?: return
        val controls = preset.controlMappings ?: return
        val valid = if (blockIndex in listOf(11, 12, 255)) paramIndex == 255 else {
            val slot = preset.effects.find { it.slotIndex == blockIndex }
            blockIndex in 0..9 && slot != null && EffectCatalog.getEffectById(slot.effectId)?.params?.any { it.index == paramIndex } == true
        }
        if (!valid) return
        val assignment = QuickKnobAssignment(blockIndex, paramIndex)
        if (assignment == controls.knobs[index]) return
        _uiState.update { it.copy(preset = preset.copy(controlMappings = controls.withKnob(index, assignment)), hasUnsavedChanges = true) }
        if (midiManager.connectionState.value is MidiConnectionState.Connected) midiManager.sendQuickKnobAssignment(index, assignment)
    }

    fun setCtrl1Effect(blockIndex: Int?) {
        if (controlsBusy()) return
        val preset = _uiState.value.preset ?: return
        val controls = preset.controlMappings ?: return
        if (blockIndex != null && preset.effects.none { it.slotIndex == blockIndex }) return
        if (blockIndex != null && blockIndex !in 0..10) return
        val updated = controls.withCtrl1Block(blockIndex)
        if (updated == controls) return
        _uiState.update { it.copy(preset = preset.copy(controlMappings = updated), hasUnsavedChanges = true) }
        if (midiManager.connectionState.value is MidiConnectionState.Connected) midiManager.sendCtrlAssignment(0, updated.ctrls[0])
    }

    fun setCtrlTarget(ctrlIndex: Int, blockIndex: Int, enabled: Boolean) {
        if (controlsBusy() || ctrlIndex !in 0..3 || blockIndex !in 0..10) return
        val preset = _uiState.value.preset ?: return
        if (preset.effects.none { it.slotIndex == blockIndex }) return
        val controls = preset.controlMappings ?: return
        val updated = controls.withCtrlTarget(ctrlIndex, blockIndex, enabled)
        if (updated == controls) return
        _uiState.update { it.copy(preset = preset.copy(controlMappings = updated), hasUnsavedChanges = true) }
        if (midiManager.connectionState.value is MidiConnectionState.Connected) {
            midiManager.sendCtrlAssignment(ctrlIndex, updated.ctrls[ctrlIndex])
        }
    }

    fun changeEffect(slotIndex: Int, newEffectId: Int) {
        if (controlsBusy()) return
        val preset = _uiState.value.preset ?: return
        if (preset.effects.none { it.slotIndex == slotIndex }) return
        val (updated, invalidKnobs) = presetAfterEffectChange(preset, slotIndex, newEffectId)
        _uiState.update { it.copy(preset = updated, hasUnsavedChanges = true) }
        midiManager.sendEffectChange(slotIndex, newEffectId)
        invalidateDeviceKnobs(invalidKnobs)
    }

    fun reorderSlots(fromIndex: Int, toIndex: Int) {
        if (editingBusy()) return
        val currentPreset = _uiState.value.preset ?: return
        val currentRouting = currentPreset.routingOrder.toMutableList()
        if (fromIndex !in currentRouting.indices || toIndex !in currentRouting.indices) return

        val item = currentRouting.removeAt(fromIndex)
        currentRouting.add(toIndex, item)

        val updatedEffects = currentRouting.mapNotNull { slotIdx ->
            currentPreset.effects.find { it.slotIndex == slotIdx }
        }

        _uiState.update {
            it.copy(
                preset = currentPreset.copy(
                    effects = updatedEffects,
                    routingOrder = currentRouting
                ),
                hasUnsavedChanges = true
            )
        }

        midiManager.sendReorder(currentRouting, currentPreset.fxLoopSend, currentPreset.fxLoopReturn)
    }

    fun renamePatch(newName: String) {
        if (editingBusy()) return
        val currentPreset = _uiState.value.preset ?: return
        val currentSlot = _uiState.value.currentSlotNumber
        val cleanName = newName.take(16)
        if (nameBeforeLocalRename?.first != currentSlot) {
            nameBeforeLocalRename = currentSlot to (_uiState.value.presetNames.getOrNull(currentSlot) ?: currentPreset.patchName)
        }
        _uiState.update {
            it.copy(
                preset = currentPreset.copy(patchName = cleanName),
                hasUnsavedChanges = true
            )
        }
        updatePresetNameLocally(currentSlot, cleanName)
        midiManager.sendPatchName(cleanName)
    }

    /**
     * Saves the edited patch with [name], [author] and [volume] to [targetSlot]. Another slot reuses the
     * import path: switch the pedal to the target, replay the whole preset, commit and verify. The source
     * slot is left as the pedal has it.
     */
    fun saveAs(targetSlot: Int, name: String, author: String, volume: Int) {
        if (editingBusy()) return
        val edited = _uiState.value.preset ?: return
        if (midiManager.connectionState.value !is MidiConnectionState.Connected) {
            _uiState.update { it.copy(errorMessage = "Conecte a pedaleira antes de salvar.") }
            return
        }
        val target = targetSlot.coerceIn(0, 255)
        val source = _uiState.value.currentSlotNumber
        if (target == source) {
            renamePatch(name)
            updatePatchAuthor(author)
            updatePatchVolume(volume)
            saveToDevice()
            return
        }
        val sourceName = _uiState.value.cachedLibrary[source]?.patchName
            ?: nameBeforeLocalRename?.takeIf { it.first == source }?.second
        nameBeforeLocalRename = null
        val preset = edited.copy(patchName = name.take(16), author = author.take(16), patchVolume = volume)
        _uiState.update {
            it.copy(
                preset = preset,
                presetNames = it.presetNames.mapIndexed { index, current ->
                    when {
                        index == source && sourceName != null -> sourceName
                        index == target -> preset.patchName
                        else -> current
                    }
                },
                currentSlotNumber = target,
                hasUnsavedChanges = true,
                isSaving = true,
                errorMessage = null
            )
        }
        if (sourceName != null) {
            midiManager.restoreSlotName(source, sourceName)
            repository?.updateSlotName(source, sourceName)
        }
        scope.launch {
            try {
                midiManager.pausePresetNamesScan()
                midiManager.sendPresetChange(target)
                midiDelayProvider(200)
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
            saveToDevice(target, forceReplay = true)
        }
    }

    fun renameSlot(slot: Int, newName: String) {
        if (editingBusy()) return
        if (_uiState.value.isPerformingFullSync) return
        if (slot == _uiState.value.currentSlotNumber) {
            renamePatch(newName)
        } else {
            val cleanName = newName.take(16)
            updatePresetNameLocally(slot, cleanName)
        }
    }

    fun updatePatchAuthor(author: String) {
        if (editingBusy()) return
        val currentPreset = _uiState.value.preset ?: return
        val clean = author.take(16)
        _uiState.update {
            it.copy(
                preset = currentPreset.copy(author = clean),
                hasUnsavedChanges = true
            )
        }
        midiManager.sendAuthorName(clean)
    }

    fun updatePatchVolume(volume: Int) {
        if (editingBusy()) return
        val currentPreset = _uiState.value.preset ?: return
        val clamped = volume.coerceIn(0, 100)
        _uiState.update {
            it.copy(
                preset = currentPreset.copy(patchVolume = clamped),
                hasUnsavedChanges = true
            )
        }
        midiManager.sendPatchVolume(clamped)
    }

    fun updatePatchPan(pan: Int) {
        if (editingBusy()) return
        val currentPreset = _uiState.value.preset ?: return
        val clamped = pan.coerceIn(-50, 50)
        _uiState.update {
            it.copy(
                preset = currentPreset.copy(patchPan = clamped),
                hasUnsavedChanges = true
            )
        }
        midiManager.sendPatchPan(clamped)
    }

    private val tapHistory = mutableListOf<Long>()

    fun updatePatchTempo(bpm: Int) {
        if (editingBusy()) return
        val currentPreset = _uiState.value.preset ?: return
        val clamped = bpm.coerceIn(40, 250)
        _uiState.update {
            val newDrumBpm = if (it.drum.isSyncWithPatch) clamped else it.drum.bpm
            it.copy(
                preset = currentPreset.copy(patchTempo = clamped),
                hasUnsavedChanges = true,
                drum = it.drum.copy(bpm = newDrumBpm)
            )
        }
        midiManager.sendPatchTempo(clamped)
    }

    fun tapTempo() {
        if (editingBusy()) return
        val now = System.currentTimeMillis()
        tapHistory.removeAll { now - it > 2500 }
        tapHistory.add(now)

        if (tapHistory.size >= 2) {
            val intervals = (1 until tapHistory.size).map { tapHistory[it] - tapHistory[it - 1] }
            val avgInterval = intervals.average()
            if (avgInterval > 0) {
                val calculatedBpm = kotlin.math.round((60000.0 / avgInterval)).toInt().coerceIn(40, 250)
                updatePatchTempo(calculatedBpm)
            }
        }
        midiManager.sendTapTempo()
    }

    fun triggerCtrl(ctrlNumber: Int, active: Boolean) {
        if (editingBusy()) return
        midiManager.triggerCtrl(ctrlNumber, active)
    }

    fun updatePatchName(name: String) {
        renamePatch(name)
    }

    fun saveToDevice(slot: Int = _uiState.value.currentSlotNumber, forceReplay: Boolean = false) {
        if (_uiState.value.isPerformingFullSync || _uiState.value.isSaving || _uiState.value.isImporting) return
        val currentPreset = _uiState.value.preset ?: return
        if (midiManager.connectionState.value !is MidiConnectionState.Connected) {
            _uiState.update { it.copy(errorMessage = "Conecte a pedaleira antes de salvar.") }
            return
        }
        val targetSlot = slot.coerceIn(0, 255)
        val sourceSlot = _uiState.value.currentSlotNumber
        lastSavedSlot = targetSlot
        lastSavedTimestamp = System.currentTimeMillis()
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        scope.launch {
            try {
                var readback = midiManager.savePreset(currentPreset, targetSlot, forceReplay)
                var differences = readback?.let { PresetSaveVerifier.differences(currentPreset, it) }.orEmpty()
                var retries = 0
                while ((readback == null || differences.isNotEmpty()) && retries < 2 &&
                    midiManager.connectionState.value is MidiConnectionState.Connected) {
                    DebugLog.d("ValetonSave") { "slot=$targetSlot readback=${retries + 1} differences=${if (readback == null) "no response" else differences.joinToString()}" }
                    val newer = midiManager.readPresetForVerification(targetSlot) ?: break
                    readback = newer
                    differences = PresetSaveVerifier.differences(currentPreset, newer)
                    retries++
                }
                val confirmed = readback
                if (confirmed == null || differences.isNotEmpty()) {
                    val detail = differences.firstOrNull()?.let { " ($it)" }.orEmpty()
                    _uiState.update { it.copy(
                        isSaving = false,
                        hasUnsavedChanges = true,
                        errorMessage = "Não foi possível confirmar o salvamento$detail. Suas alterações continuam pendentes."
                    ) }
                    return@launch
                }
                _uiState.update { current ->
                    current.copy(
                        isSaving = false,
                        hasUnsavedChanges = if (current.currentSlotNumber == sourceSlot && current.preset == currentPreset)
                            false else current.hasUnsavedChanges,
                        cachedLibrary = current.cachedLibrary + (targetSlot to confirmed),
                        confirmedSlots = current.confirmedSlots + targetSlot,
                        presetNames = current.presetNames.mapIndexed { index, name ->
                            if (index == targetSlot && (current.currentSlotNumber != sourceSlot || current.preset == currentPreset))
                                confirmed.patchName else name
                        },
                        saveSuccessCount = current.saveSuccessCount + 1
                    )
                }
                scope.launch(ioDispatcher) {
                    libraryRepository?.updatePreset(targetSlot, confirmed)
                    repository?.updateSlotName(targetSlot, confirmed.patchName)
                }
            } catch (cancelled: CancellationException) {
                _uiState.update { it.copy(isSaving = false, hasUnsavedChanges = true) }
                throw cancelled
            } catch (_: Exception) {
                _uiState.update { it.copy(
                    isSaving = false, hasUnsavedChanges = true,
                    errorMessage = "Falha ao salvar na pedaleira. Suas alterações continuam pendentes."
                ) }
            }
        }
    }

    fun syncCurrentPatch() {
        if (editingBusy()) return
        val slot = _uiState.value.currentSlotNumber
        _uiState.update { it.copy(isSyncingCurrentPreset = true) }
        midiManager.syncCurrentPatch(slot)
        scope.launch {
            midiDelayProvider(2500)
            _uiState.update { it.copy(isSyncingCurrentPreset = false) }
        }
    }

    private fun updatePresetNameLocally(slot: Int, name: String) {
        if (slot !in 0..255) return
        val currentNames = _uiState.value.presetNames.toMutableList()
        while (currentNames.size <= slot) {
            currentNames.add("Patch %02d".format(currentNames.size))
        }
        currentNames[slot] = name
        _uiState.update {
            it.copy(
                presetNames = currentNames,
                preset = if (slot == it.currentSlotNumber && it.preset != null) {
                    it.preset.copy(patchName = name)
                } else it.preset
            )
        }
        repository?.updateSlotName(slot, name)
    }

    fun toggleDrumPlay() {
        if (editingBusy()) return
        val current = _uiState.value.drum.isPlaying
        setDrumPlaying(!current)
    }

    fun setDrumPlaying(playing: Boolean) {
        if (editingBusy()) return
        _uiState.update { it.copy(drum = it.drum.copy(isPlaying = playing)) }
        if (playing) {
            midiManager.sendDrumMenu(true)
            midiManager.sendDrumPlay(true)
        } else {
            midiManager.sendDrumPlay(false)
        }
    }

    fun selectDrumPattern(patternId: Int) {
        if (editingBusy()) return
        val clamped = patternId.coerceIn(0, 99)
        _uiState.update { it.copy(drum = it.drum.copy(selectedPatternId = clamped)) }
        midiManager.sendDrumPattern(clamped)
    }

    fun updateDrumVolume(volume: Int) {
        if (editingBusy()) return
        val clamped = volume.coerceIn(0, 100)
        _uiState.update { it.copy(drum = it.drum.copy(volume = clamped)) }
        midiManager.sendDrumVolume(clamped)
    }

    fun setDrumGenre(genre: DrumGenre) {
        if (editingBusy()) return
        _uiState.update { it.copy(drum = it.drum.copy(selectedGenre = genre)) }
    }

    fun setDrumSearchQuery(query: String) {
        if (editingBusy()) return
        _uiState.update { it.copy(drum = it.drum.copy(searchQuery = query)) }
    }

    fun updateDrumBpm(bpm: Int) {
        if (editingBusy()) return
        val clamped = bpm.coerceIn(40, 250)
        val isSync = _uiState.value.drum.isSyncWithPatch
        _uiState.update { current ->
            val updatedPreset = if (isSync && current.preset != null) {
                current.preset.copy(patchTempo = clamped)
            } else {
                current.preset
            }
            val hasUnsaved = if (isSync && current.preset != null && current.preset.patchTempo != clamped) {
                true
            } else {
                current.hasUnsavedChanges
            }
            current.copy(
                preset = updatedPreset,
                hasUnsavedChanges = hasUnsaved,
                drum = current.drum.copy(bpm = clamped)
            )
        }
        midiManager.sendDrumBpm(clamped, isSync)
    }

    fun toggleDrumSync() {
        if (editingBusy()) return
        setDrumSync(!_uiState.value.drum.isSyncWithPatch)
    }

    fun setDrumSync(sync: Boolean) {
        if (editingBusy()) return
        val currentBpm = _uiState.value.drum.bpm
        val newBpm = if (sync && _uiState.value.preset != null) {
            _uiState.value.preset!!.patchTempo
        } else {
            currentBpm
        }
        val currentPreset = _uiState.value.preset
        val updatedPreset = currentPreset?.copy(drumSync = sync)
        val hasUnsaved = if (currentPreset != null && currentPreset.drumSync != sync) true else _uiState.value.hasUnsavedChanges
        DebugLog.d("ValetonDrum") { "setDrumSync: sync=$sync, newBpm=$newBpm" }
        _uiState.update { current ->
            current.copy(
                preset = updatedPreset,
                hasUnsavedChanges = hasUnsaved,
                drum = current.drum.copy(
                    isSyncWithPatch = sync,
                    bpm = newBpm
                )
            )
        }
        midiManager.sendDrumSync(sync, newBpm)
    }

    fun openDrumsMenu() {
        if (editingBusy()) return
        midiManager.sendDrumMenu(true)
    }

    fun closeDrumsMenu() {
        if (editingBusy()) return
        midiManager.sendDrumMenu(false)
    }
}

