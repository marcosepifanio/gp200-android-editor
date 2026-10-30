package com.valeton.editor.core.model

/**
 * User-loaded models whose names live on the pedal: 20 CAB IRs and 10 NAM SnapTones
 * (indices 0..4 AMP, 5..9 DST). See docs/protocol/user-model-names.md.
 */
enum class UserModelKind(val recordId: Int, val slotCount: Int, val firstEffectId: Int) {
    IR(0x1009, 20, 0x0A100000),
    SNAPTONE(0x1012, 10, 0x0F000000);

    fun effectId(index: Int): Int = firstEffectId + index
}

data class UserModelName(val kind: UserModelKind, val index: Int, val name: String)
