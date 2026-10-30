package com.valeton.editor.usb

import android.content.Context
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiInputPort
import android.media.midi.MidiManager
import android.media.midi.MidiOutputPort
import android.media.midi.MidiReceiver
import android.os.Handler
import android.os.Looper
import com.valeton.editor.core.codec.SysExCodec
import com.valeton.editor.core.codec.PresetWritePlan
import com.valeton.editor.core.codec.PresetSaveVerifier
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.core.model.QuickKnobAssignment
import com.valeton.editor.core.model.CtrlAssignment
import com.valeton.editor.core.model.UserModelKind
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import java.io.ByteArrayOutputStream
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class AndroidMidiDriver internal constructor(
    private val context: Context? = null,
    private val coroutineScope: CoroutineScope,
    private val rawSender: ((ByteArray) -> Unit)? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MidiConnectionManager {

    constructor(
        context: Context,
        coroutineScope: CoroutineScope
    ) : this(context as Context?, coroutineScope, null, Dispatchers.IO)

    companion object {
        /** Well above the largest GP-200 reply (a preset read chunk is a few hundred bytes). */
        internal const val MAX_SYSEX_SIZE = 4096
    }

    private val midiManager: MidiManager? =
        context?.getSystemService(Context.MIDI_SERVICE) as? MidiManager

    private val _connectionState = MutableStateFlow<MidiConnectionState>(MidiConnectionState.Disconnected)
    override val connectionState: StateFlow<MidiConnectionState> = _connectionState.asStateFlow()

    private val _pedalEvents = MutableSharedFlow<PedalEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val pedalEvents: SharedFlow<PedalEvent> = _pedalEvents.asSharedFlow()

    private val defaultNames = (0..255).map { index ->
        val label = SysExCodec.slotToLabel(index)
        "Patch $label"
    }
    private val _presetNamesState = MutableStateFlow<List<String>>(defaultNames)
    override val presetNamesState: StateFlow<List<String>> = _presetNamesState.asStateFlow()

    private val _namesSyncProgress = MutableStateFlow(0)
    override val namesSyncProgress: StateFlow<Int> = _namesSyncProgress.asStateFlow()

    private val _fullSyncProgress = MutableStateFlow<FullSyncProgress?>(null)
    override val fullSyncProgress: StateFlow<FullSyncProgress?> = _fullSyncProgress.asStateFlow()
    private var fullSyncJob: Job? = null
    private var currentSyncSlotDeferred: CompletableDeferred<GP200Preset>? = null

    private val _userModelNames = MutableStateFlow<Map<Int, String>>(emptyMap())
    override val userModelNames: StateFlow<Map<Int, String>> = _userModelNames.asStateFlow()
    /** Zeros by default; switch to SysExCodec.USER_MODEL_CAPTURED_TAIL if the pedal ignores them. */
    internal var userModelNameRequestTail: ByteArray = ByteArray(20)
    private var userModelNamesJob: Job? = null
    @Volatile private var awaitedUserModel: Pair<UserModelKind, Int>? = null
    @Volatile private var userModelNameDeferred: CompletableDeferred<Unit>? = null
    /** Collects names during a read so they are published once, not 30 times. */
    @Volatile private var userModelReadBuffer: MutableMap<Int, String>? = null

    private var scannerJob: Job? = null
    private var scannerPausedUntilMs = 0L
    private var awaitingSlotName: Int? = null
    private var requestedPresetSlot: Int? = null
    private var saveReadDeferred: CompletableDeferred<GP200Preset>? = null
    private var saveReadSlot: Int? = null
    private val confirmedPresets = ConcurrentHashMap<Int, GP200Preset>()
    private val pendingEffectBlocks = ConcurrentHashMap.newKeySet<Int>()
    private val pendingPresetSettings = AtomicBoolean(false)

    private var activeDevice: MidiDevice? = null
    private var inputPort: MidiInputPort? = null
    private var outputPort: MidiOutputPort? = null
    private var framer: MidiPacketFramer? = null

    private val incomingSysExStream = ByteArrayOutputStream()
    private val pendingChunks = Collections.synchronizedList(mutableListOf<ByteArray>())
    private val confirmedSlotIndices = Collections.synchronizedSet(mutableSetOf<Int>())
    private var chunkCollectJob: Job? = null
    private var readTimeoutJob: Job? = null
    internal var currentActiveSlot: Int = 0
    private var suppressEchoUntilMs = 0L

    init {
        framer = MidiPacketFramer(coroutineScope, intervalMs = 40L) { bytes ->
            if (rawSender != null) {
                rawSender.invoke(bytes)
            } else {
                sendBytesDirect(bytes)
            }
        }
        if (context != null) {
            setupDeviceCallback()
            startConnectionWatchdog()
            scanForGP200()
        }
    }

    private var watchdogJob: Job? = null

    private fun startConnectionWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(2500)
                val currentState = _connectionState.value
                val isAttached = isDeviceAttached()
                if (currentState is MidiConnectionState.Connected || currentState is MidiConnectionState.Handshaking) {
                    if (!isAttached || inputPort == null || outputPort == null) {
                        disconnect()
                    }
                } else if (currentState is MidiConnectionState.Disconnected || currentState is MidiConnectionState.Error) {
                    if (isAttached) {
                        scanForGP200()
                    }
                }
            }
        }
    }

    fun isDeviceAttached(): Boolean {
        val devices = midiManager?.devices ?: return false
        return devices.any { isGP200(it) }
    }

    private fun setupDeviceCallback() {
        val handler = Handler(Looper.getMainLooper())
        midiManager?.registerDeviceCallback(object : MidiManager.DeviceCallback() {
            override fun onDeviceAdded(device: MidiDeviceInfo) {
                if (isGP200(device) && (_connectionState.value is MidiConnectionState.Disconnected || _connectionState.value is MidiConnectionState.Error)) {
                    connectDevice(device)
                }
            }

            override fun onDeviceRemoved(device: MidiDeviceInfo) {
                if (activeDevice?.info?.id == device.id) {
                    disconnect()
                }
            }
        }, handler)
    }

    override fun scanDevices() {
        scanForGP200()
    }

    fun scanForGP200() {
        val devices = midiManager?.devices ?: return
        val gp200 = devices.firstOrNull { isGP200(it) }
        if (gp200 != null) {
            connectDevice(gp200)
        } else {
            _connectionState.value = MidiConnectionState.Disconnected
        }
    }

    private fun isGP200(info: MidiDeviceInfo): Boolean {
        val name = info.properties.getString(MidiDeviceInfo.PROPERTY_NAME) ?: ""
        val product = info.properties.getString(MidiDeviceInfo.PROPERTY_PRODUCT) ?: ""
        val manufacturer = info.properties.getString(MidiDeviceInfo.PROPERTY_MANUFACTURER) ?: ""
        if (name.contains("GP-200", ignoreCase = true) ||
            product.contains("GP-200", ignoreCase = true) ||
            name.contains("GP200", ignoreCase = true) ||
            product.contains("GP200", ignoreCase = true) ||
            name.contains("Valeton", ignoreCase = true) ||
            product.contains("Valeton", ignoreCase = true) ||
            manufacturer.contains("Valeton", ignoreCase = true)
        ) {
            return true
        }
        val allDevices = midiManager?.devices ?: emptyArray()
        if (allDevices.size == 1 && (info.inputPortCount > 0 && info.outputPortCount > 0)) {
            return true
        }
        return false
    }

    private fun connectDevice(info: MidiDeviceInfo) {
        _connectionState.value = MidiConnectionState.Connecting
        val handler = Handler(Looper.getMainLooper())
        midiManager?.openDevice(info, { device ->
            if (device == null) {
                _connectionState.value = MidiConnectionState.Error("Failed to open GP-200 device")
                return@openDevice
            }
            activeDevice = device

            // Find input and output ports
            val inputPortInfo = info.ports.firstOrNull { it.type == MidiDeviceInfo.PortInfo.TYPE_INPUT }
            val outputPortInfo = info.ports.firstOrNull { it.type == MidiDeviceInfo.PortInfo.TYPE_OUTPUT }

            if (inputPortInfo != null) {
                inputPort = device.openInputPort(inputPortInfo.portNumber)
            }
            if (outputPortInfo != null) {
                outputPort = device.openOutputPort(outputPortInfo.portNumber)
                outputPort?.connect(object : MidiReceiver() {
                    override fun onSend(msg: ByteArray?, offset: Int, count: Int, timestamp: Long) {
                        if (msg != null && count > 0) {
                            handleIncomingMidi(msg.copyOfRange(offset, offset + count))
                        }
                    }
                })
            }

            val deviceName = info.properties.getString(MidiDeviceInfo.PROPERTY_NAME) ?: "Valeton GP-200"
            _connectionState.value = MidiConnectionState.Handshaking

            // Initiate handshake and load active preset
            coroutineScope.launch {
                delay(150)
                // 1. Enter Editor Mode so device streams notifications
                sendBytesDirect(SysExCodec.buildEnterEditorMode())
                delay(100)
                // 2. Request State Dump to determine active preset slot
                sendBytesDirect(SysExCodec.buildStateDumpRequest())

                // 3. Fallback watchdog: if state dump didn't trigger preset read within 800ms
                delay(800)
                if (_connectionState.value is MidiConnectionState.Handshaking) {
                    requestReadPreset(currentActiveSlot)
                }
                delay(1200)
                if (_connectionState.value is MidiConnectionState.Handshaking) {
                    markConnected(deviceName, currentActiveSlot)
                }
            }
        }, handler)
    }

    internal val bufferedSysExSize: Int get() = synchronized(incomingSysExStream) { incomingSysExStream.size() }

    internal fun handleIncomingMidi(bytes: ByteArray) {
        DebugLog.d("ValetonMidi") { "RX [${bytes.size}]: ${bytes.joinToString(" ") { "%02X".format(it) }}" }
        synchronized(incomingSysExStream) {
            for (b in bytes) {
                if (b == 0xF0.toByte()) {
                    incomingSysExStream.reset()
                    incomingSysExStream.write(b.toInt())
                } else if (incomingSysExStream.size() > 0) {
                    if (incomingSysExStream.size() >= MAX_SYSEX_SIZE) {
                        // A message without F7 would grow forever; drop it and wait for the next F0.
                        incomingSysExStream.reset()
                        continue
                    }
                    incomingSysExStream.write(b.toInt())
                    if (b == 0xF7.toByte()) {
                        val completeMsg = incomingSysExStream.toByteArray()
                        incomingSysExStream.reset()
                        processCompleteSysEx(completeMsg)
                    }
                }
            }
        }
    }

    private fun processCompleteSysEx(msg: ByteArray) {
        // 0. User IR / SnapTone name (0x12, 28-byte record 0x1009/0x1012)
        SysExCodec.parseUserModelNameResponse(msg)?.let { response ->
            val entry = response.kind.effectId(response.index) to response.name
            val buffer = userModelReadBuffer
            if (buffer != null) buffer[entry.first] = entry.second else _userModelNames.update { it + entry }
            if (awaitedUserModel == (response.kind to response.index)) userModelNameDeferred?.complete(Unit)
            return
        }
        // 1. Preset Read Chunk (0x12 0x18)
        if (SysExCodec.isSysEx(msg, 0x12, 0x18)) {
            val offset = if (msg.size >= 13) (msg[11].toInt() and 0xFF) or ((msg[12].toInt() and 0xFF) shl 8) else -1

            // If not in a full preset read, this is a name-only chunk (sub=0x20 read or background scan)
            if (requestedPresetSlot == null) {
                if (saveReadDeferred != null) return
                val slot = awaitingSlotName ?: currentActiveSlot
                if (offset == 0) {
                    val parsedName = SysExCodec.parsePresetName(msg)
                    if (parsedName.isNotBlank() && slot in 0..255) {
                        confirmedSlotIndices.add(slot)
                        val currentList = _presetNamesState.value.toMutableList()
                        while (currentList.size <= slot) {
                            currentList.add("Patch %02d".format(currentList.size))
                        }
                        currentList[slot] = parsedName
                        _presetNamesState.value = currentList
                        _pedalEvents.tryEmit(PedalEvent.PresetNameLoaded(slot, parsedName))
                    }
                }
                return
            }

            // Full preset read chunk
            val targetSlot = requestedPresetSlot ?: currentActiveSlot

            // If this is chunk 0 with a valid name, update preset names list and UI immediately!
            if (offset == 0) {
                val parsedName = SysExCodec.parsePresetName(msg)
                if (saveReadDeferred == null && parsedName.isNotBlank() && targetSlot in 0..255) {
                    confirmedSlotIndices.add(targetSlot)
                    val currentList = _presetNamesState.value.toMutableList()
                    while (currentList.size <= targetSlot) {
                        currentList.add("Patch %02d".format(currentList.size))
                    }
                    currentList[targetSlot] = parsedName
                    _presetNamesState.value = currentList
                    _pedalEvents.tryEmit(PedalEvent.PresetNameLoaded(targetSlot, parsedName))
                }
            }

            val chunkCount: Int
            synchronized(pendingChunks) {
                pendingChunks.removeAll { existing ->
                    if (existing.size >= 13) {
                        val existOff = (existing[11].toInt() and 0xFF) or ((existing[12].toInt() and 0xFF) shl 8)
                        existOff == offset
                    } else false
                }
                pendingChunks.add(msg.clone())
                chunkCount = pendingChunks.size
            }

            chunkCollectJob?.cancel()
            chunkCollectJob = coroutineScope.launch {
                // Wait up to 150ms for more chunks unless all 7 arrived
                if (chunkCount < 7) {
                    delay(150)
                }
                val chunksToProcess = synchronized(pendingChunks) {
                    pendingChunks.toList()
                }
                if (chunksToProcess.isNotEmpty()) {
                    try {
                        val preset = SysExCodec.parseReadChunks(chunksToProcess)
                        val isFullSync = (currentSyncSlotDeferred != null)
                        val wasRequested = (targetSlot == requestedPresetSlot)
                        if (wasRequested) confirmedPresets[targetSlot] = preset

                        if (saveReadDeferred == null && preset.patchName.isNotBlank() && targetSlot in 0..255) {
                            confirmedSlotIndices.add(targetSlot)
                            val currentList = _presetNamesState.value.toMutableList()
                            while (currentList.size <= targetSlot) {
                                currentList.add("Patch %02d".format(currentList.size))
                            }
                            currentList[targetSlot] = preset.patchName
                            _presetNamesState.value = currentList
                            _pedalEvents.tryEmit(PedalEvent.PresetNameLoaded(targetSlot, preset.patchName))
                        }

                        if (isFullSync) {
                            if (wasRequested) {
                                synchronized(pendingChunks) { pendingChunks.clear() }
                                requestedPresetSlot = null
                                currentSyncSlotDeferred?.complete(preset)
                            }
                        } else if (saveReadSlot == targetSlot && saveReadDeferred != null) {
                            synchronized(pendingChunks) { pendingChunks.clear() }
                            requestedPresetSlot = null
                            saveReadDeferred?.complete(preset)
                        } else {
                            if (targetSlot == requestedPresetSlot) {
                                synchronized(pendingChunks) { pendingChunks.clear() }
                                requestedPresetSlot = null
                                currentActiveSlot = targetSlot
                                _pedalEvents.emit(PedalEvent.PresetLoaded(preset, targetSlot))

                                if (_connectionState.value is MidiConnectionState.Handshaking) {
                                    val name = activeDevice?.info?.properties?.getString(MidiDeviceInfo.PROPERTY_NAME) ?: "Valeton GP-200"
                                    markConnected(name, targetSlot)
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // Keep requestedPresetSlot active so remaining incoming chunks can be processed
                    }
                }
            }
            return
        }

        // Mapping records are distinct from physical knob-value / stomp notifications.
        // Full patch reads supply their state; do not interpret assignment echoes as toggles.
        if (SysExCodec.isControlAssignmentMessage(msg)) return

        val isSuppressed = saveReadDeferred != null || System.currentTimeMillis() < suppressEchoUntilMs

        // 2. State Dump (0x12 0x4E)
        if (SysExCodec.isSysEx(msg, 0x12, 0x4E)) {
            // Only chunk 0 (offset 0) carries the current slot record at decoded[8:10]
            val isChunkZero = msg.size >= 13 && msg[11] == 0.toByte() && msg[12] == 0.toByte()
            if (isChunkZero && !isSuppressed) {
                val slot = SysExCodec.parseStateDumpSlot(listOf(msg))
                if (slot != currentActiveSlot) {
                    currentActiveSlot = slot
                    _pedalEvents.tryEmit(PedalEvent.SlotChanged(slot))
                    pausePresetNamesScan()
                    coroutineScope.launch {
                        delay(150)
                        requestReadPreset(slot)
                    }
                }
            }
            return
        }

        // 3. Slot Change Notification (0x12 0x08)
        val slotChange = SysExCodec.parseSlotChangeNotification(msg)
        if (slotChange != null) {
            if (!isSuppressed && slotChange != currentActiveSlot) {
                currentActiveSlot = slotChange
                _pedalEvents.tryEmit(PedalEvent.SlotChanged(slotChange))
                pausePresetNamesScan()
                coroutineScope.launch {
                    delay(200)
                    requestReadPreset(slotChange)
                }
            }
            return
        }

        // 4. Knob Notification (0x12 0x10)
        val knob = SysExCodec.parseKnobNotification(msg)
        if (knob != null) {
            if (!isSuppressed) {
                _pedalEvents.tryEmit(PedalEvent.ParamChanged(knob.first, knob.second, knob.third))
            }
            return
        }

        // 5. Toggle Notification (0x12 0x10 or 0x12 0x08)
        val toggle = SysExCodec.parseToggleNotification(msg)
        if (toggle != null) {
            if (!isSuppressed) {
                _pedalEvents.tryEmit(PedalEvent.ToggleChanged(toggle.first, toggle.second))
            }
            return
        }

        // 6. Effect Change Notification (0x12 0x0C)
        val effect = SysExCodec.parseEffectChangeNotification(msg)
        if (effect != null) {
            if (!isSuppressed) {
                _pedalEvents.tryEmit(PedalEvent.EffectChanged(effect.first, effect.second))
            }
            return
        }

        // 7. Drum Machine Notifications (0x12 0x1008 / 0x1011 / 0x1001)
        val drumNotif = SysExCodec.parseDrumNotification(msg)
        if (drumNotif != null) {
            DebugLog.d("ValetonMidi") { "Received drum notification: $drumNotif" }
            when (drumNotif) {
                is SysExCodec.DrumNotification.Sync -> _pedalEvents.tryEmit(PedalEvent.DrumSyncChanged(drumNotif.enabled))
                is SysExCodec.DrumNotification.Pattern -> _pedalEvents.tryEmit(PedalEvent.DrumPatternChanged(drumNotif.patternId))
                is SysExCodec.DrumNotification.Volume -> _pedalEvents.tryEmit(PedalEvent.DrumVolumeChanged(drumNotif.volume))
                is SysExCodec.DrumNotification.Bpm -> _pedalEvents.tryEmit(PedalEvent.DrumBpmChanged(drumNotif.bpm))
                is SysExCodec.DrumNotification.PlayStop -> _pedalEvents.tryEmit(PedalEvent.DrumPlayChanged(drumNotif.playing))
            }
            return
        }
    }

    private fun sendBytesDirect(bytes: ByteArray) {
        DebugLog.d("ValetonMidi") { "TX [${bytes.size}]: ${bytes.joinToString(" ") { "%02X".format(it) }}" }
        try {
            inputPort?.send(bytes, 0, bytes.size)
        } catch (e: Exception) {
            _connectionState.value = MidiConnectionState.Error("Send failed: ${e.message}")
        }
    }

    override fun sendParamChange(blockIndex: Int, paramIndex: Int, effectId: Int, value: Float) {
        pendingEffectBlocks.add(blockIndex)
        suppressEchoUntilMs = System.currentTimeMillis() + 100L
        pausePresetNamesScan()
        framer?.sendParam(blockIndex, paramIndex, effectId, value)
    }

    override fun sendToggle(blockIndex: Int, enabled: Boolean) {
        pendingEffectBlocks.add(blockIndex)
        suppressEchoUntilMs = System.currentTimeMillis() + 200L
        pausePresetNamesScan()
        val msg = SysExCodec.buildToggle(blockIndex, enabled)
        coroutineScope.launch {
            framer?.sendImmediate(msg)
        }
    }

    override fun sendEffectChange(blockIndex: Int, effectId: Int) {
        pendingEffectBlocks.add(blockIndex)
        suppressEchoUntilMs = System.currentTimeMillis() + 300L
        pausePresetNamesScan()
        val msg = SysExCodec.buildEffectChange(blockIndex, effectId)
        coroutineScope.launch {
            framer?.sendImmediate(msg)
        }
    }

    override fun sendReorder(order: List<Int>, send: Int, ret: Int) {
        pendingPresetSettings.set(true)
        suppressEchoUntilMs = System.currentTimeMillis() + 300L
        pausePresetNamesScan()
        val msg = SysExCodec.buildReorder(order, send, ret)
        coroutineScope.launch {
            framer?.sendImmediate(msg)
        }
    }

    override fun sendQuickKnobAssignment(index: Int, assignment: QuickKnobAssignment) {
        pendingPresetSettings.set(true)
        pausePresetNamesScan()
        suppressEchoUntilMs = System.currentTimeMillis() + 300L
        coroutineScope.launch { framer?.sendImmediate(SysExCodec.buildQuickKnobAssignment(index, assignment)) }
    }

    override fun sendCtrlAssignment(index: Int, assignment: CtrlAssignment) {
        pendingPresetSettings.set(true)
        pausePresetNamesScan()
        suppressEchoUntilMs = System.currentTimeMillis() + 300L
        coroutineScope.launch { framer?.sendImmediate(SysExCodec.buildCtrlAssignment(index, assignment)) }
    }

    override suspend fun savePreset(preset: GP200Preset, slot: Int, forceReplay: Boolean): GP200Preset? {
        userModelNamesJob?.join()
        if (saveReadDeferred != null || fullSyncJob?.isActive == true ||
            (rawSender == null && _connectionState.value !is MidiConnectionState.Connected)) return null
        val targetSlot = slot.coerceIn(0, 255)
        val deferred = CompletableDeferred<GP200Preset>()
        saveReadDeferred = deferred
        val touchedBlocks = pendingEffectBlocks.toSet()
        pendingEffectBlocks.removeAll(touchedBlocks)
        val touchedSettings = pendingPresetSettings.getAndSet(false)
        var verified = false
        try {
            pausePresetNamesScan()
            readTimeoutJob?.cancel()
            chunkCollectJob?.cancel()
            requestedPresetSlot = null
            synchronized(pendingChunks) { pendingChunks.clear() }
            // Let already queued knob updates enter the framer before committing.
            delay(40)
            framer?.flush()
            val baseline = if (forceReplay) null else confirmedPresets[targetSlot]?.let {
                it.copy(effects = it.effects.filterNot { effect -> effect.slotIndex in touchedBlocks })
            }
            val commands = PresetWritePlan.commands(preset, baseline, forceSettings = touchedSettings)
            DebugLog.d("ValetonSave") { "slot=$targetSlot forceReplay=$forceReplay replay=${commands.size} confirmedBaseline=${confirmedPresets.containsKey(targetSlot)}" }
            for (command in commands) {
                framer?.sendImmediate(command)
                delay(40)
            }
            framer?.sendImmediate(SysExCodec.buildSaveCommit(preset.patchName, targetSlot))
            delay(600)
            if (rawSender == null && _connectionState.value !is MidiConnectionState.Connected) return null
            saveReadSlot = targetSlot
            requestReadPreset(targetSlot)
            val result = withTimeoutOrNull(2500) { deferred.await() }
            verified = result != null && PresetSaveVerifier.differences(preset, result).isEmpty()
            return result
        } finally {
            if (!verified) {
                pendingEffectBlocks.addAll(touchedBlocks)
                if (touchedSettings) pendingPresetSettings.set(true)
            }
            readTimeoutJob?.cancel()
            saveReadSlot = null
            saveReadDeferred = null
            chunkCollectJob?.cancel()
            requestedPresetSlot = null
            synchronized(pendingChunks) { pendingChunks.clear() }
        }
    }

    override suspend fun readPresetForVerification(slot: Int): GP200Preset? {
        if (saveReadDeferred != null || fullSyncJob?.isActive == true ||
            (rawSender == null && _connectionState.value !is MidiConnectionState.Connected)) return null
        val targetSlot = slot.coerceIn(0, 255)
        val deferred = CompletableDeferred<GP200Preset>()
        saveReadDeferred = deferred
        try {
            pausePresetNamesScan()
            readTimeoutJob?.cancel()
            chunkCollectJob?.cancel()
            requestedPresetSlot = null
            synchronized(pendingChunks) { pendingChunks.clear() }
            delay(250)
            saveReadSlot = targetSlot
            requestReadPreset(targetSlot)
            return withTimeoutOrNull(2500) { deferred.await() }
        } finally {
            readTimeoutJob?.cancel()
            chunkCollectJob?.cancel()
            saveReadSlot = null
            saveReadDeferred = null
            requestedPresetSlot = null
            synchronized(pendingChunks) { pendingChunks.clear() }
        }
    }

    override fun sendSaveCommit(presetName: String, slot: Int) {
        val clampedSlot = slot.coerceIn(0, 255)
        currentActiveSlot = clampedSlot
        suppressEchoUntilMs = System.currentTimeMillis() + 1000L
        pausePresetNamesScan()
        chunkCollectJob?.cancel()
        synchronized(pendingChunks) {
            pendingChunks.clear()
        }
        val currentList = _presetNamesState.value.toMutableList()
        while (currentList.size <= clampedSlot) {
            currentList.add("Patch %02d".format(currentList.size))
        }
        currentList[clampedSlot] = presetName
        _presetNamesState.value = currentList
        val msg = SysExCodec.buildSaveCommit(presetName, clampedSlot)
        coroutineScope.launch {
            framer?.sendImmediate(msg)
        }
    }

    override fun restoreSlotName(slot: Int, name: String) {
        if (slot !in 0..255) return
        _presetNamesState.update { names -> names.mapIndexed { index, current -> if (index == slot) name else current } }
    }

    override fun sendPatchName(name: String) {
        pendingPresetSettings.set(true)
        suppressEchoUntilMs = System.currentTimeMillis() + 200L
        pausePresetNamesScan()
        val currentList = _presetNamesState.value.toMutableList()
        currentList[currentActiveSlot] = name
        _presetNamesState.value = currentList
        val msg = SysExCodec.buildPatchName(name)
        coroutineScope.launch {
            framer?.sendImmediate(msg)
        }
    }

    override fun sendAuthorName(author: String) {
        pendingPresetSettings.set(true)
        suppressEchoUntilMs = System.currentTimeMillis() + 200L
        pausePresetNamesScan()
        val msg = SysExCodec.buildAuthorName(author)
        coroutineScope.launch {
            framer?.sendImmediate(msg)
        }
    }

    override fun sendPresetChange(slot: Int) {
        if (saveReadDeferred != null) return
        currentActiveSlot = slot
        pausePresetNamesScan()
        chunkCollectJob?.cancel()
        synchronized(pendingChunks) {
            pendingChunks.clear()
        }
        val pcMessages = SysExCodec.buildProgramChange(slot)
        val sysexMsg = SysExCodec.buildPresetChange(slot)
        coroutineScope.launch {
            // 1. Send Bank Select (CC 0) and Program Change (PC) to physically switch patch on GP-200
            for (m in pcMessages) {
                if (rawSender != null) {
                    rawSender.invoke(m)
                } else {
                    sendBytesDirect(m)
                }
            }
            // 2. Send SysEx preset change message
            if (rawSender != null) {
                rawSender.invoke(sysexMsg)
            } else {
                framer?.sendImmediate(sysexMsg)
            }
            // 3. Allow DSP 200ms settling time before reading full preset parameters
            delay(200)
            requestReadPreset(slot)
        }
    }

    override fun sendControlChange(cc: Int, value: Int, channel: Int) {
        val msg = SysExCodec.buildControlChange(cc, value, channel)
        coroutineScope.launch {
            framer?.sendImmediate(msg)
        }
    }

    override fun sendPatchVolume(volume: Int) {
        pendingPresetSettings.set(true)
        val clamped = volume.coerceIn(0, 100)
        val ccMsg = SysExCodec.buildControlChange(cc = 7, value = clamped)
        val sysexMsg = SysExCodec.buildPatchSetting(target = 0x00, value = clamped)
        coroutineScope.launch {
            framer?.sendImmediate(ccMsg)
            framer?.sendImmediate(sysexMsg)
        }
    }

    override fun sendPatchPan(pan: Int) {
        pendingPresetSettings.set(true)
        val clamped = pan.coerceIn(-50, 50)
        val value = if (clamped < 0) 256 + clamped else clamped
        val sysexMsg = SysExCodec.buildPatchSetting(target = 0x06, value = value)
        coroutineScope.launch {
            framer?.sendImmediate(sysexMsg)
        }
    }

    override fun sendPatchTempo(bpm: Int) {
        pendingPresetSettings.set(true)
        val clamped = bpm.coerceIn(40, 250)
        val ccMsgs = SysExCodec.buildTempoControlChange(clamped)
        val sysexMsg = SysExCodec.buildPatchSetting(target = 0x01, value = clamped)
        coroutineScope.launch {
            ccMsgs.forEach { ccMsg ->
                framer?.sendImmediate(ccMsg)
            }
            framer?.sendImmediate(sysexMsg)
        }
    }

    override fun sendTapTempo() {
        val tapMsg = SysExCodec.buildTapTempoControlChange()
        coroutineScope.launch {
            framer?.sendImmediate(tapMsg)
        }
    }

    override fun triggerCtrl(ctrlNumber: Int, active: Boolean) {
        if (ctrlNumber !in 1..4) return
        val ccNumber = 68 + ctrlNumber
        val value = if (active) 127 else 0
        sendControlChange(ccNumber, value)
    }

    override fun sendDrumMenu(enabled: Boolean) {
        sendControlChange(cc = 92, value = if (enabled) 127 else 0, channel = 0)
    }

    override fun sendDrumPlay(playing: Boolean) {
        sendControlChange(cc = 93, value = if (playing) 127 else 0, channel = 0)
        val sysexPlay = SysExCodec.buildDrumPlayStop(playing)
        coroutineScope.launch {
            framer?.sendImmediate(sysexPlay)
        }
    }

    override fun sendDrumPattern(patternId: Int) {
        val clamped = patternId.coerceIn(0, 99)
        sendControlChange(cc = 94, value = clamped, channel = 0)
        val sysexPattern = SysExCodec.buildDrumPattern(clamped)
        coroutineScope.launch {
            framer?.sendImmediate(sysexPattern)
        }
    }

    override fun sendDrumVolume(volume: Int) {
        val clamped = volume.coerceIn(0, 100)
        sendControlChange(cc = 95, value = clamped, channel = 0)
        val sysexVolume = SysExCodec.buildDrumVolume(clamped)
        coroutineScope.launch {
            framer?.sendImmediate(sysexVolume)
        }
    }

    override fun sendDrumSync(enabled: Boolean, bpm: Int) {
        pendingPresetSettings.set(true)
        val clampedBpm = bpm.coerceIn(40, 250)
        val valByte = if (enabled) 1 else 0
        DebugLog.d("ValetonMidi") { "sendDrumSync: enabled=$enabled, bpm=$clampedBpm" }

        // 1. Build Patch Setting Param SysEx (0x1008 LE, target = 0x19) - primary command from GP-200.exe
        val patchSyncParam = SysExCodec.buildPatchSettingParam(target = 0x19, value = valByte)
        // 2. Build Drum Subsystem Sync SysEx (0x1011 LE, target = 2)
        val drumSubsystemSync = SysExCodec.buildDrumSync(enabled)
        // 3. Build Drum Machine Parameter SysEx (0x1001 LE, target = 3)
        val drumParamSync = SysExCodec.buildDrumParam(target = 3, value = valByte)

        coroutineScope.launch {
            framer?.sendImmediate(patchSyncParam)
            delay(35)
            framer?.sendImmediate(drumSubsystemSync)
            delay(35)
            framer?.sendImmediate(drumParamSync)

            // When Sync is enabled, align drum tempo to patch tempo
            if (enabled) {
                delay(35)
                sendDrumBpm(clampedBpm, isSyncWithPatch = true)
            }
        }
    }

    override fun sendDrumBpm(bpm: Int, isSyncWithPatch: Boolean) {
        val clampedBpm = bpm.coerceIn(40, 250)

        // 1. Send CC 73 / 74 (Tempo MSB/LSB)
        val ccMsgs = SysExCodec.buildTempoControlChange(clampedBpm)
        coroutineScope.launch {
            ccMsgs.forEach { ccMsg ->
                framer?.sendImmediate(ccMsg)
            }
        }

        // 2. Send Patch Tempo SysEx (target = 0x01)
        val sysexPatchTempo = SysExCodec.buildPatchSetting(target = 0x01, value = clampedBpm)
        // 3. Send Drum BPM SysEx (target = 2)
        val sysexDrumBpm = SysExCodec.buildDrumBpm(clampedBpm)

        coroutineScope.launch {
            framer?.sendImmediate(sysexPatchTempo)
            framer?.sendImmediate(sysexDrumBpm)
        }

        // 4. Send Quick Access Knob 1 (CC 16)
        val scaledKnob = ((clampedBpm - 40) * 100 / 210).coerceIn(0, 100)
        sendControlChange(cc = 16, value = scaledKnob, channel = 0)
    }

    /** Starts the name read before publishing Connected, so a full sync triggered by the state waits for it. */
    internal fun markConnected(deviceName: String, slot: Int) {
        startUserModelNamesRead()
        _connectionState.value = MidiConnectionState.Connected(deviceName = deviceName, activeSlot = slot)
    }

    /** Reads user IR/SnapTone names, then starts the preset-name scan. Full sync and saves wait for it. */
    internal fun startUserModelNamesRead(): Job {
        userModelNamesJob?.cancel()
        return coroutineScope.launch(ioDispatcher) {
            readUserModelNames()
            startPresetNamesScan()
        }.also { userModelNamesJob = it }
    }

    internal suspend fun readUserModelNames() {
        val buffer = java.util.concurrent.ConcurrentHashMap<Int, String>()
        userModelReadBuffer = buffer
        var consecutiveTimeouts = 0
        try {
            for (kind in UserModelKind.entries) {
                for (index in 0 until kind.slotCount) {
                    // A pedal that ignores these records would otherwise hold up sync and saves for 9 s.
                    if (consecutiveTimeouts >= 3) return
                    val deferred = CompletableDeferred<Unit>()
                    awaitedUserModel = kind to index
                    userModelNameDeferred = deferred
                    val req = SysExCodec.buildUserModelNameRequest(kind, index, userModelNameRequestTail)
                    if (rawSender != null) rawSender.invoke(req) else sendBytesDirect(req)
                    val answered = withTimeoutOrNull(300L) { deferred.await() } != null
                    consecutiveTimeouts = if (answered) 0 else consecutiveTimeouts + 1
                    DebugLog.d("ValetonUserModels") {
                        "$kind[$index] = ${if (answered) buffer[kind.effectId(index)] else "<no response>"}"
                    }
                }
            }
        } finally {
            if (userModelReadBuffer === buffer) userModelReadBuffer = null
            if (buffer.isNotEmpty()) _userModelNames.update { it + buffer }
            awaitedUserModel = null
            userModelNameDeferred = null
        }
    }

    override fun startPresetNamesScan() {
        if (confirmedSlotIndices.size == 256) {
            _namesSyncProgress.value = 256
            return
        }
        if (currentSyncSlotDeferred != null || (fullSyncJob != null && fullSyncJob!!.isActive)) {
            return
        }
        scannerJob?.cancel()
        scannerJob = coroutineScope.launch(Dispatchers.IO) {
            delay(800) // let initial preset settle

            // Pass 1: Scan all slots 0..255 with adaptive confirmation wait
            for (s in 0..255) {
                if (!isActive || _connectionState.value !is MidiConnectionState.Connected) break
                while (saveReadDeferred != null || System.currentTimeMillis() < scannerPausedUntilMs || requestedPresetSlot != null || (fullSyncJob != null && fullSyncJob!!.isActive)) {
                    awaitingSlotName = null
                    delay(150)
                }
                if (requestedPresetSlot == null) {
                    if (s !in confirmedSlotIndices) {
                        awaitingSlotName = s
                        requestPresetName(s)
                        var waited = 0
                        while (s !in confirmedSlotIndices && waited < 90 && isActive) {
                            delay(15)
                            waited += 15
                        }
                    }
                    _namesSyncProgress.value = s + 1
                    delay(15)
                }
            }
            awaitingSlotName = null

            // Pass 2: Retry pass on any remaining unconfirmed slots
            val unconfirmed = (0..255).filter { it !in confirmedSlotIndices }
            if (unconfirmed.isNotEmpty() && isActive && _connectionState.value is MidiConnectionState.Connected) {
                delay(200)
                for (s in unconfirmed) {
                    if (!isActive || _connectionState.value !is MidiConnectionState.Connected) break
                    while (saveReadDeferred != null || System.currentTimeMillis() < scannerPausedUntilMs || requestedPresetSlot != null) {
                        awaitingSlotName = null
                        delay(150)
                    }
                    if (requestedPresetSlot == null && s !in confirmedSlotIndices) {
                        awaitingSlotName = s
                        requestPresetName(s)
                        var waited = 0
                        while (s !in confirmedSlotIndices && waited < 120 && isActive) {
                            delay(20)
                            waited += 20
                        }
                    }
                }
                awaitingSlotName = null
            }
            _namesSyncProgress.value = 256
        }
    }

    override fun syncCurrentPatch(slot: Int) {
        val targetSlot = if (slot in 0..255) slot else currentActiveSlot
        currentActiveSlot = targetSlot
        pausePresetNamesScan()
        requestReadPreset(targetSlot)
    }

    override fun preloadCachedNames(names: Map<Int, String>) {
        if (names.isEmpty()) return
        val currentNames = _presetNamesState.value.toMutableList()
        for ((slot, name) in names) {
            if (slot in 0..255) {
                currentNames[slot] = name
                confirmedSlotIndices.add(slot)
            }
        }
        _presetNamesState.value = currentNames
        if (confirmedSlotIndices.size == 256) {
            _namesSyncProgress.value = 256
        }
    }

    override fun pausePresetNamesScan() {
        scannerPausedUntilMs = System.currentTimeMillis() + 1500L
        awaitingSlotName = null
    }

    override fun requestReadPreset(slot: Int) {
        if (currentSyncSlotDeferred != null) return
        if (saveReadDeferred != null && saveReadSlot != slot) return
        readTimeoutJob?.cancel()
        awaitingSlotName = null
        currentActiveSlot = slot
        requestedPresetSlot = slot
        pausePresetNamesScan()
        chunkCollectJob?.cancel()
        synchronized(pendingChunks) {
            pendingChunks.clear()
        }
        val msg = SysExCodec.buildReadRequest(slot)
        readTimeoutJob = coroutineScope.launch {
            framer?.sendImmediate(msg)
            delay(1500)
            if (requestedPresetSlot == slot) {
                chunkCollectJob?.cancel()
                synchronized(pendingChunks) { pendingChunks.clear() }
                requestedPresetSlot = null
            }
        }
    }

    override fun requestPresetName(slot: Int) {
        val msg = SysExCodec.buildNameReadRequest(slot)
        coroutineScope.launch {
            framer?.sendImmediate(msg)
        }
    }

    override fun startFullLibrarySync() {
        if (saveReadDeferred != null) return
        if (rawSender == null && _connectionState.value !is MidiConnectionState.Connected) {
            return
        }
        scannerJob?.cancel()
        scannerJob = null
        readTimeoutJob?.cancel()
        fullSyncJob?.cancel()
        fullSyncJob = null
        currentSyncSlotDeferred?.cancel()
        currentSyncSlotDeferred = null
        confirmedSlotIndices.clear()
        _namesSyncProgress.value = 0
        pausePresetNamesScan()
        fullSyncJob = coroutineScope.launch(ioDispatcher) {
            userModelNamesJob?.join()
            val syncedPresets = mutableMapOf<Int, GP200Preset>()
            try {
                for (slot in 0..255) {
                    if (!isActive || (rawSender == null && _connectionState.value !is MidiConnectionState.Connected)) break
                    val deferred = CompletableDeferred<GP200Preset>()
                    currentSyncSlotDeferred = deferred
                    chunkCollectJob?.cancel()
                    requestedPresetSlot = slot
                    synchronized(pendingChunks) { pendingChunks.clear() }
                    val req = SysExCodec.buildReadRequest(slot)
                    if (rawSender != null) {
                        rawSender.invoke(req)
                    } else {
                        sendBytesDirect(req)
                    }
                    var preset = withTimeoutOrNull(700L) { deferred.await() }
                    if (preset == null && isActive && (rawSender != null || _connectionState.value is MidiConnectionState.Connected)) {
                        // Retry once on timeout
                        val retryDeferred = CompletableDeferred<GP200Preset>()
                        currentSyncSlotDeferred = retryDeferred
                        chunkCollectJob?.cancel()
                        requestedPresetSlot = slot
                        synchronized(pendingChunks) { pendingChunks.clear() }
                        if (rawSender != null) {
                            rawSender.invoke(req)
                        } else {
                            sendBytesDirect(req)
                        }
                        preset = withTimeoutOrNull(700L) { retryDeferred.await() }
                    }
                    if (preset != null) syncedPresets[slot] = preset
                    else confirmedSlotIndices.remove(slot)
                    val patchName = preset?.patchName ?: "Falha no slot ${SysExCodec.slotToLabel(slot)}"
                    _fullSyncProgress.value = FullSyncProgress(
                        currentSlot = slot + 1,
                        totalSlots = 256,
                        currentPatchName = patchName,
                        progressFraction = (slot + 1) / 256f,
                        isCompleted = false
                    )
                    delay(25L)
                }
                if (isActive && (rawSender != null || _connectionState.value is MidiConnectionState.Connected)) {
                    _fullSyncProgress.value = FullSyncProgress(
                        currentSlot = 256,
                        totalSlots = 256,
                        currentPatchName = if (syncedPresets.size == 256) "Concluído" else "${256 - syncedPresets.size} slots pendentes",
                        progressFraction = 1f,
                        isCompleted = syncedPresets.size == 256
                    )
                    confirmedSlotIndices.clear()
                    confirmedSlotIndices.addAll(syncedPresets.keys)
                    _namesSyncProgress.value = syncedPresets.size
                    _pedalEvents.emit(PedalEvent.FullLibrarySynced(syncedPresets))
                }
            } finally {
                withContext(NonCancellable) {
                    if (!isActive || (rawSender == null && _connectionState.value !is MidiConnectionState.Connected)) {
                        _fullSyncProgress.value = null
                    }
                    requestedPresetSlot = null
                    currentSyncSlotDeferred = null
                }
            }
        }
    }

    override fun cancelFullLibrarySync() {
        fullSyncJob?.cancel()
        fullSyncJob = null
        currentSyncSlotDeferred?.cancel()
        currentSyncSlotDeferred = null
        requestedPresetSlot = null
        _fullSyncProgress.value = null
    }

    override fun disconnect() {
        userModelNamesJob?.cancel()
        userModelNamesJob = null
        confirmedPresets.clear()
        pendingEffectBlocks.clear()
        pendingPresetSettings.set(false)
        saveReadDeferred?.completeExceptionally(IllegalStateException("Device disconnected during save"))
        readTimeoutJob?.cancel()
        chunkCollectJob?.cancel()
        scannerJob?.cancel()
        scannerJob = null
        cancelFullLibrarySync()
        try {
            inputPort?.close()
            outputPort?.close()
            activeDevice?.close()
        } catch (_: Exception) {}
        inputPort = null
        outputPort = null
        activeDevice = null
        _connectionState.value = MidiConnectionState.Disconnected
    }
}
