package com.valeton.editor

import android.content.Intent
import android.content.ClipData
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import com.valeton.editor.repository.UserModelNamesRepository
import com.valeton.editor.ui.components.LocalUserModelNames
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.valeton.editor.core.catalog.DrumCatalog
import com.valeton.editor.core.codec.PRSTDecoder
import com.valeton.editor.core.codec.PRSTEncoder
import com.valeton.editor.core.codec.SysExCodec
import com.valeton.editor.core.model.GP200Preset
import com.valeton.editor.repository.PresetLibraryRepository
import com.valeton.editor.repository.PresetNamesRepository
import com.valeton.editor.ui.chain.StudioChainScreen
import com.valeton.editor.ui.about.StudioAboutScreen
import com.valeton.editor.ui.components.SaveInProgressOverlay
import com.valeton.editor.ui.components.StudioHeader
import com.valeton.editor.ui.components.StudioTabBar
import com.valeton.editor.ui.components.StudioTab
import com.valeton.editor.ui.connect.WelcomeConnectScreen
import com.valeton.editor.ui.controls.StudioControlsScreen
import com.valeton.editor.ui.drums.StudioDrumMiniPlayer
import com.valeton.editor.ui.drums.StudioDrumScreen
import com.valeton.editor.ui.editor.StudioPedalEditorScreen
import com.valeton.editor.ui.manage.ImportPatchDialog
import com.valeton.editor.ui.manage.PatchMetadataDialog
import com.valeton.editor.ui.manage.StudioPatchManagerScreen
import com.valeton.editor.ui.sync.StudioFullSyncScreen
import com.valeton.editor.ui.theme.GP200Theme
import com.valeton.editor.ui.theme.StudioPalette
import com.valeton.editor.usb.AndroidMidiDriver
import com.valeton.editor.usb.MidiConnectionState
import com.valeton.editor.viewmodel.ConnectionViewModel
import com.valeton.editor.viewmodel.PresetViewModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var midiDriver: AndroidMidiDriver
    private lateinit var presetViewModel: PresetViewModel
    private lateinit var connectionViewModel: ConnectionViewModel
    private var onUriReceived: ((Uri) -> Unit)? = null

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { uri ->
            onUriReceived?.invoke(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        val namesRepository = PresetNamesRepository(applicationContext.filesDir)
        val libraryRepository = PresetLibraryRepository(applicationContext.filesDir)
        val userModelNamesRepository = UserModelNamesRepository(applicationContext.filesDir)
        midiDriver = AndroidMidiDriver(applicationContext, lifecycleScope)
        presetViewModel = PresetViewModel(midiDriver, namesRepository, libraryRepository, userModelNamesRepository)
        connectionViewModel = ConnectionViewModel(midiDriver)

        // Seed with initial default preset only if no cached preset was loaded
        if (presetViewModel.uiState.value.preset == null) {
            presetViewModel.loadPreset(createDefaultPreset())
        }

        setContent {
            val userModelNames = presetViewModel.uiState.collectAsStateWithLifecycle().value.userModelNames
            CompositionLocalProvider(LocalUserModelNames provides userModelNames) {
            GP200Theme {
                val connectionState by connectionViewModel.connectionState.collectAsStateWithLifecycle()
                val uiState by presetViewModel.uiState.collectAsStateWithLifecycle()

                var showWelcomeScreen by remember { mutableStateOf(true) }
                var currentTab by remember { mutableStateOf(StudioTab.CHAIN) }
                var editingSlotIndex by remember { mutableStateOf<Int?>(null) }
                var showMetadataDialog by remember { mutableStateOf(false) }
                var showImportDialog by remember { mutableStateOf(false) }
                var pendingImportPreset by remember { mutableStateOf<GP200Preset?>(null) }
                var pendingExportBytes by rememberSaveable { mutableStateOf<ByteArray?>(null) }

                val handlePrstUri: (Uri) -> Unit = remember {
                    { uri ->
                        lifecycleScope.launch {
                            // Bounded read off the main thread: the URI may come from any app.
                            val result = withContext(Dispatchers.IO) {
                                runCatching {
                                    contentResolver.openInputStream(uri)?.use { PRSTDecoder(PRSTDecoder.readFile(it)).decode() }
                                }
                            }
                            val preset = result.getOrNull()
                            if (preset != null) {
                                pendingImportPreset = preset
                                showImportDialog = true
                                showWelcomeScreen = false
                            } else {
                                val message = if (result.exceptionOrNull() is IllegalArgumentException) {
                                    "Arquivo .prst inválido."
                                } else {
                                    "Não foi possível abrir o arquivo."
                                }
                                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                DisposableEffect(Unit) {
                    onUriReceived = handlePrstUri
                    intent?.data?.let { uri ->
                        handlePrstUri(uri)
                    }
                    onDispose {
                        onUriReceived = null
                    }
                }

                val openDocumentLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri: Uri? ->
                    uri?.let { handlePrstUri(it) }
                }

                val createExportDocumentLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("application/octet-stream")
                ) { uri: Uri? ->
                    val bytes = pendingExportBytes
                    pendingExportBytes = null
                    if (uri != null && bytes != null) {
                        try {
                            contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                                ?: error("Não foi possível abrir o arquivo escolhido")
                            Toast.makeText(this@MainActivity, "Preset exportado com sucesso", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "Falha ao salvar preset: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                fun presetForExport(slotIndex: Int): Pair<String, ByteArray>? {
                    val preset = uiState.cachedLibrary[slotIndex]
                    if (preset == null || preset.effects.isEmpty()) {
                        Toast.makeText(this@MainActivity, "Este preset ainda não está sincronizado. Sincronize a biblioteca e tente novamente.", Toast.LENGTH_LONG).show()
                        return null
                    }
                    val label = SysExCodec.slotToLabel(slotIndex)
                    return label to PRSTEncoder().encode(preset)
                }

                var observedSaveCount by remember { mutableIntStateOf(uiState.saveSuccessCount) }
                LaunchedEffect(uiState.saveSuccessCount) {
                    if (uiState.saveSuccessCount > observedSaveCount) {
                        Toast.makeText(this@MainActivity, "Salvamento verificado na pedaleira!", Toast.LENGTH_SHORT).show()
                    }
                    observedSaveCount = uiState.saveSuccessCount
                }
                LaunchedEffect(uiState.errorMessage) {
                    uiState.errorMessage?.let { message ->
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    }
                }

                // Automatically navigate away from welcome screen when device connects
                LaunchedEffect(connectionState) {
                    if (connectionState is MidiConnectionState.Connected) {
                        showWelcomeScreen = false
                    }
                }

                LaunchedEffect(uiState.isPerformingFullSync) {
                    if (uiState.isPerformingFullSync) {
                        showMetadataDialog = false
                        // Keep the external import pending; visibility resumes after synchronization.
                        editingSlotIndex = null
                    }
                }

                LaunchedEffect(uiState.isSaving) {
                    if (uiState.isSaving) {
                        showMetadataDialog = false
                        showImportDialog = false
                    }
                }

                // System Back button handling for sub-navigation
                BackHandler(enabled = editingSlotIndex != null) {
                    editingSlotIndex = null
                }
                BackHandler(enabled = editingSlotIndex == null && currentTab != StudioTab.CHAIN && !showWelcomeScreen) {
                    currentTab = StudioTab.CHAIN
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (showWelcomeScreen) {
                        WelcomeConnectScreen(
                            connectionState = connectionState,
                            onConnectClick = {
                                connectionViewModel.scanDevices()
                            },
                            onProceedOfflineClick = {
                                showWelcomeScreen = false
                            }
                        )
                    } else {
                        val isConnected = connectionState is MidiConnectionState.Connected
                        val currentPreset = uiState.preset

                        Scaffold(
                        topBar = {
                            if (currentTab != StudioTab.ABOUT) {
                                StudioHeader(
                                    connected = isConnected,
                                    patchName = run {
                                        val isConfirmed = uiState.confirmedSlots.contains(uiState.currentSlotNumber)
                                        val rawName = uiState.presetNames.getOrNull(uiState.currentSlotNumber)?.takeIf { it.isNotBlank() }
                                        val validCachedName = if (isConfirmed || (rawName != null && !rawName.matches(Regex("""^Patch \d+$""")))) rawName else null
                                        validCachedName
                                            ?: currentPreset?.patchName?.takeIf { it.isNotBlank() && (isConfirmed || !it.matches(Regex("""^Patch \d+$"""))) }
                                            ?: "Carregando..."
                                    },
                                    currentSlot = uiState.currentSlotNumber,
                                    hasUnsavedChanges = uiState.hasUnsavedChanges,
                                    saving = uiState.isSaving || uiState.isImporting,
                                    syncProgress = uiState.namesSyncProgress,
                                    onPatchClick = {
                                        showMetadataDialog = true
                                    },
                                    onSaveClick = {
                                        presetViewModel.saveToDevice()
                                    },
                                    onSaveLongClick = {
                                        showMetadataDialog = true
                                    },
                                    onImportClick = {
                                        openDocumentLauncher.launch(arrayOf("*/*"))
                                    }
                                )
                            }
                        },
                        bottomBar = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(StudioPalette.ChromeBg)
                                    .navigationBarsPadding()
                            ) {
                                AnimatedVisibility(
                                    visible = uiState.drum.isPlaying && currentTab != StudioTab.DRUMS,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    val activePattern = remember(uiState.drum.selectedPatternId) {
                                        DrumCatalog.getPatternById(uiState.drum.selectedPatternId)
                                            ?: DrumCatalog.getAllPatterns().first()
                                    }
                                    val activeDrumBpm = if (uiState.drum.isSyncWithPatch) {
                                        currentPreset?.patchTempo ?: uiState.drum.bpm
                                    } else {
                                        uiState.drum.bpm
                                    }
                                    StudioDrumMiniPlayer(
                                        pattern = activePattern,
                                        bpm = activeDrumBpm,
                                        isPlaying = uiState.drum.isPlaying,
                                        onTogglePlay = { presetViewModel.toggleDrumPlay() },
                                        onClick = {
                                            presetViewModel.openDrumsMenu()
                                            currentTab = StudioTab.DRUMS
                                            editingSlotIndex = null
                                        }
                                    )
                                }
                                StudioTabBar(
                                    activeTab = currentTab,
                                    onTabSelected = { tab ->
                                        if (tab == StudioTab.DRUMS && currentTab != StudioTab.DRUMS) {
                                            presetViewModel.openDrumsMenu()
                                        } else if (tab != StudioTab.DRUMS && currentTab == StudioTab.DRUMS) {
                                            presetViewModel.closeDrumsMenu()
                                        }
                                        currentTab = tab
                                        if (tab != StudioTab.CHAIN) {
                                            editingSlotIndex = null
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(StudioPalette.Case),
                            contentAlignment = Alignment.TopStart
                        ) {
                            when (currentTab) {
                                StudioTab.CHAIN -> {
                                    if (currentPreset != null) {
                                        val activeEditingSlot = editingSlotIndex?.let { slotIdx ->
                                            currentPreset.effects.find { it.slotIndex == slotIdx }
                                        }

                                        if (activeEditingSlot != null) {
                                            StudioPedalEditorScreen(
                                                slot = activeEditingSlot,
                                                index = currentPreset.effects.indexOf(activeEditingSlot),
                                                chainLength = currentPreset.effects.size,
                                                tempo = currentPreset.patchTempo,
                                                onTempoChange = { bpm ->
                                                    presetViewModel.updatePatchTempo(bpm)
                                                },
                                                onTapTempo = {
                                                    presetViewModel.tapTempo()
                                                },
                                                onBack = { editingSlotIndex = null },
                                                onToggle = {
                                                    presetViewModel.toggleSlot(activeEditingSlot.slotIndex)
                                                },
                                                onChangeEffect = { newEffectId ->
                                                    presetViewModel.changeEffect(activeEditingSlot.slotIndex, newEffectId)
                                                },
                                                onParamChange = { paramIndex, value ->
                                                    presetViewModel.updateParam(
                                                        activeEditingSlot.slotIndex,
                                                        paramIndex,
                                                        value
                                                    )
                                                }
                                            )
                                        } else {
                                            StudioChainScreen(
                                                preset = currentPreset,
                                                onToggleSlot = { slotIndex ->
                                                    presetViewModel.toggleSlot(slotIndex)
                                                },
                                                onOpenSlot = { slotIndex ->
                                                    editingSlotIndex = slotIndex
                                                },
                                                onMoveSlot = { fromIndex, toIndex ->
                                                    presetViewModel.reorderSlots(fromIndex, toIndex)
                                                },
                                                onMoveFxLoopSend = { position ->
                                                    presetViewModel.moveFxLoopSend(position)
                                                },
                                                onMoveFxLoopReturn = { position ->
                                                    presetViewModel.moveFxLoopReturn(position)
                                                },
                                                reorderEnabled = !uiState.isSaving && !uiState.isImporting && !uiState.isPerformingFullSync,
                                                onVolumeChange = { volume ->
                                                    presetViewModel.updatePatchVolume(volume)
                                                },
                                                onPanChange = { pan ->
                                                    presetViewModel.updatePatchPan(pan)
                                                },
                                                onTempoChange = { bpm ->
                                                    presetViewModel.updatePatchTempo(bpm)
                                                },
                                                onTapTempo = {
                                                    presetViewModel.tapTempo()
                                                }
                                            )
                                        }
                                    }
                                }
                                StudioTab.PATCHES -> {
                                    StudioPatchManagerScreen(
                                        currentSlot = uiState.currentSlotNumber,
                                        presetNames = uiState.presetNames,
                                        syncProgress = uiState.namesSyncProgress,
                                        onSelectSlot = { slotIndex ->
                                            presetViewModel.selectPresetSlot(slotIndex)
                                            currentTab = StudioTab.CHAIN
                                            editingSlotIndex = null
                                        },
                                        onRenameSlot = { slotIndex, newName ->
                                            presetViewModel.renameSlot(slotIndex, newName)
                                        },
                                        onSaveExportSlot = { slotIndex ->
                                            presetForExport(slotIndex)?.let { (label, bytes) ->
                                                pendingExportBytes = bytes
                                                val presetName = uiState.presetNames.getOrNull(slotIndex).orEmpty()
                                                createExportDocumentLauncher.launch(exportPresetFileName(label, presetName))
                                            }
                                        },
                                        onShareExportSlot = { slotIndex ->
                                            presetForExport(slotIndex)?.let { (label, bytes) ->
                                                try {
                                                    val presetName = uiState.presetNames.getOrNull(slotIndex).orEmpty()
                                                    val fileName = exportPresetFileName(label, presetName)
                                                    val exportDir = File(cacheDir, "exports").apply { mkdirs() }
                                                    val exportFile = File(exportDir, fileName).apply { writeBytes(bytes) }
                                                    val uri = FileProvider.getUriForFile(
                                                        this@MainActivity,
                                                        "${applicationContext.packageName}.fileprovider",
                                                        exportFile
                                                    )
                                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "application/octet-stream"
                                                        putExtra(Intent.EXTRA_STREAM, uri)
                                                        clipData = ClipData.newUri(contentResolver, fileName, uri)
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    startActivity(Intent.createChooser(sendIntent, "Compartilhar preset"))
                                                } catch (e: Exception) {
                                                    Toast.makeText(this@MainActivity, "Falha ao compartilhar preset: ${e.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        onImportClick = {
                                            openDocumentLauncher.launch(arrayOf("*/*"))
                                        },
                                        onResyncAllClick = {
                                            presetViewModel.forceFullLibraryResync()
                                        },
                                        resyncEnabled = isConnected,
                                        isPerformingFullSync = uiState.isPerformingFullSync
                                    )
                                }
                                StudioTab.ABOUT -> StudioAboutScreen()
                                StudioTab.CONTROLS -> {
                                    StudioControlsScreen(
                                        preset = currentPreset,
                                        currentSlot = uiState.currentSlotNumber,
                                        connected = isConnected,
                                        busy = uiState.isSaving || uiState.isImporting || uiState.isPerformingFullSync || uiState.isSyncingCurrentPreset,
                                        hasUnsavedChanges = uiState.hasUnsavedChanges,
                                        onQuickKnobChange = { index, block, param -> presetViewModel.updateQuickKnob(index, block, param) },
                                        onCtrlTargetChange = { ctrlIndex, blockIndex, enabled ->
                                            presetViewModel.setCtrlTarget(ctrlIndex, blockIndex, enabled)
                                        },
                                        onReload = { presetViewModel.syncCurrentPatch() }
                                    )
                                }
                                StudioTab.DRUMS -> {
                                    StudioDrumScreen(
                                        drumState = uiState.drum,
                                        tempo = uiState.drum.bpm,
                                        onTogglePlay = { presetViewModel.toggleDrumPlay() },
                                        onSelectPattern = { patternId -> presetViewModel.selectDrumPattern(patternId) },
                                        onVolumeChange = { volume -> presetViewModel.updateDrumVolume(volume) },
                                        onTempoChange = { bpm -> presetViewModel.updateDrumBpm(bpm) },
                                        onGenreChange = { genre -> presetViewModel.setDrumGenre(genre) },
                                        onSearchQueryChange = { query -> presetViewModel.setDrumSearchQuery(query) }
                                    )
                                }
                            }
                        }
                    }

                    PatchMetadataDialog(
                        isOpen = showMetadataDialog && !uiState.isPerformingFullSync && !uiState.isSaving,
                        connected = isConnected,
                        currentName = currentPreset?.patchName ?: "",
                        currentAuthor = currentPreset?.author ?: "Valeton",
                        currentVolume = currentPreset?.patchVolume ?: 50,
                        currentSlot = uiState.currentSlotNumber,
                        presetNames = uiState.presetNames,
                        onDismiss = { showMetadataDialog = false },
                        onApplyLocal = { newName, newAuthor, newVolume ->
                            presetViewModel.renamePatch(newName)
                            presetViewModel.updatePatchAuthor(newAuthor)
                            presetViewModel.updatePatchVolume(newVolume)
                            showMetadataDialog = false
                            Toast.makeText(
                                this@MainActivity,
                                "Configurações atualizadas localmente",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onSaveToDevice = { targetSlot, newName, newAuthor, newVolume ->
                            presetViewModel.saveAs(targetSlot, newName, newAuthor, newVolume)
                            showMetadataDialog = false
                        }
                    )

                    ImportPatchDialog(
                        isOpen = showImportDialog && !uiState.isPerformingFullSync && !uiState.isImporting && !uiState.isSaving,
                        connected = isConnected,
                        preset = pendingImportPreset,
                        currentSlot = uiState.currentSlotNumber,
                        presetNames = uiState.presetNames,
                        onDismiss = {
                            intent?.data = null
                            showImportDialog = false
                            pendingImportPreset = null
                        },
                        onConfirmImport = { targetSlot, saveToPedal ->
                            intent?.data = null
                            pendingImportPreset?.let { prst ->
                                presetViewModel.importPreset(prst, targetSlot, saveToPedal)
                                if (!saveToPedal) {
                                    val slotLabel = SysExCodec.slotToLabel(targetSlot)
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Patch '${prst.patchName}' carregado (live) no slot $slotLabel!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            showImportDialog = false
                            pendingImportPreset = null
                        }
                    )
                }

                AnimatedVisibility(
                    visible = uiState.isPerformingFullSync,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    StudioFullSyncScreen(progress = uiState.fullSyncProgress)
                }

                if (!showWelcomeScreen) {
                    SaveInProgressOverlay(visible = uiState.isSaving)
                }
            }
            }
        }
    }
}

    override fun onDestroy() {
        super.onDestroy()
        midiDriver.disconnect()
    }
}

private fun exportPresetFileName(slotLabel: String, patchName: String): String {
    val sanitized = patchName
        .replace(Regex("[\\\\/:*?\"<>|\\p{Cc}]"), "_")
        .trim()
        .trim('.', ' ')
        .take((80 - slotLabel.length - 8).coerceAtLeast(1))
        .trimEnd('.', ' ')
        .ifBlank { "Patch" }
    return "$slotLabel - $sanitized.prst"
}
