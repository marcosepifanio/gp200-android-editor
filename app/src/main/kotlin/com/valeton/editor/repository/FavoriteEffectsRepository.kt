package com.valeton.editor.repository

import android.content.Context

/** Stores effect favorites locally, independently of presets and device synchronization. */
class FavoriteEffectsRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun getFavoriteEffectIds(): Set<Int> = preferences
        .getStringSet(KEY_FAVORITE_EFFECT_IDS, emptySet())
        .orEmpty()
        .mapNotNull { it.toIntOrNull() }
        .toSet()

    fun toggleFavorite(effectId: Int): Set<Int> {
        val updated = getFavoriteEffectIds().let { current ->
            if (effectId in current) current - effectId else current + effectId
        }
        preferences.edit()
            .putStringSet(KEY_FAVORITE_EFFECT_IDS, updated.map { it.toString() }.toSet())
            .apply()
        return updated
    }

    private companion object {
        const val PREFERENCES_NAME = "effect_favorites"
        const val KEY_FAVORITE_EFFECT_IDS = "favorite_effect_ids"
    }
}
