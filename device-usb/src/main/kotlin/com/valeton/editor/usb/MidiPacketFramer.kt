package com.valeton.editor.usb

import com.valeton.editor.core.codec.SysExCodec
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class ParamUpdate(
    val blockIndex: Int,
    val paramIndex: Int,
    val effectId: Int,
    val value: Float
)

class MidiPacketFramer(
    private val coroutineScope: CoroutineScope,
    private val intervalMs: Long = 40L,
    private val onSendBytes: suspend (ByteArray) -> Unit
) {
    // Map key: (blockIndex shl 8) or paramIndex -> latest value to coalesce
    private val pendingParams = mutableMapOf<Int, ParamUpdate>()
    private val mutex = Mutex()
    private var pacingJob: Job? = null

    fun sendParam(blockIndex: Int, paramIndex: Int, effectId: Int, value: Float) {
        val key = (blockIndex shl 8) or (paramIndex and 0xFF)
        coroutineScope.launch {
            mutex.withLock {
                pendingParams[key] = ParamUpdate(blockIndex, paramIndex, effectId, value)
                if (pacingJob == null || !pacingJob!!.isActive) {
                    pacingJob = launchPacingLoop()
                }
            }
        }
    }

    suspend fun flush() {
        pacingJob?.cancelAndJoin()
        val updates = mutex.withLock {
            pendingParams.values.toList().also { pendingParams.clear() }
        }
        for (update in updates) {
            onSendBytes(SysExCodec.buildParamChange(update.blockIndex, update.paramIndex, update.effectId, update.value))
            delay(intervalMs)
        }
    }

    suspend fun sendImmediate(bytes: ByteArray) {
        onSendBytes(bytes)
    }

    private fun launchPacingLoop(): Job = coroutineScope.launch {
        while (isActive) {
            val nextUpdate: ParamUpdate? = mutex.withLock {
                val key = pendingParams.keys.firstOrNull()
                if (key != null) {
                    pendingParams.remove(key)
                } else {
                    null
                }
            }

            if (nextUpdate == null) break

            val msg = SysExCodec.buildParamChange(
                nextUpdate.blockIndex,
                nextUpdate.paramIndex,
                nextUpdate.effectId,
                nextUpdate.value
            )
            onSendBytes(msg)
            delay(intervalMs)
        }
    }
}
