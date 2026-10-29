package com.valeton.editor.usb

import android.util.Log

/** Logs de diagnóstico emitidos apenas em builds debug; a mensagem só é montada quando registrada. */
internal object DebugLog {
    fun d(tag: String, message: () -> String) {
        if (BuildConfig.DEBUG) Log.d(tag, message())
    }
}
