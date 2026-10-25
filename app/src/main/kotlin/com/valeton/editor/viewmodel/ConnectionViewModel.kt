package com.valeton.editor.viewmodel

import androidx.lifecycle.ViewModel
import com.valeton.editor.usb.MidiConnectionManager
import com.valeton.editor.usb.MidiConnectionState
import kotlinx.coroutines.flow.StateFlow

class ConnectionViewModel(
    private val midiManager: MidiConnectionManager
) : ViewModel() {

    val connectionState: StateFlow<MidiConnectionState> = midiManager.connectionState

    fun scanDevices() {
        midiManager.scanDevices()
    }

    fun disconnect() {
        midiManager.disconnect()
    }
}
