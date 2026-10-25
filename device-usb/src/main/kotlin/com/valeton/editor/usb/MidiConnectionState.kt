package com.valeton.editor.usb

sealed interface MidiConnectionState {
    data object Disconnected : MidiConnectionState
    data object Connecting : MidiConnectionState
    data object Handshaking : MidiConnectionState
    data class Connected(
        val deviceName: String,
        val manufacturer: String = "Valeton",
        val activeSlot: Int = 0
    ) : MidiConnectionState
    data class Error(val message: String) : MidiConnectionState
}
