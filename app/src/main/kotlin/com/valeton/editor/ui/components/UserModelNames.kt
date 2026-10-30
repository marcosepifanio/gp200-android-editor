package com.valeton.editor.ui.components

import androidx.compose.runtime.staticCompositionLocalOf
import com.valeton.editor.core.model.EffectDefinition

/** Names of user IRs and SnapTones read from the pedal, keyed by effectId. */
val LocalUserModelNames = staticCompositionLocalOf<Map<Int, String>> { emptyMap() }

/** Name shown for an effect: the pedal's user name when known, otherwise the catalog name. */
fun effectDisplayName(definition: EffectDefinition, userNames: Map<Int, String>): String =
    userNames[definition.effectId]?.takeIf { it.isNotBlank() } ?: definition.name
