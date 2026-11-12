package com.hhst.youtubelite.player

import com.hhst.youtubelite.core.JsonCache
import com.hhst.youtubelite.extension.ExtensionManager
import com.hhst.youtubelite.extension.PreferenceKeys

/**
 * Shared read/write access to preferred playback quality and speed persisted for
 * `PlaybackEngine`, using keys [KEY_QUALITY] and [KEY_SPEED].
 */
class PlaybackDefaults(
    private val prefs: ExtensionManager,
    private val cache: JsonCache,
) {
    fun defaultQuality(): String? =
        if (prefs.isEnabled(PreferenceKeys.REMEMBER_QUALITY)) {
            cache.get(KEY_QUALITY, String::class.java)
        } else {
            null
        }

    fun setDefaultQuality(label: String?) {
        if (label == null) {
            cache.invalidate(KEY_QUALITY)
        } else {
            prefs.setEnabled(PreferenceKeys.REMEMBER_QUALITY, true)
            cache.put(KEY_QUALITY, label, PREF_TTL_MS)
        }
    }

    fun defaultSpeed(): Float =
        if (prefs.isEnabled(PreferenceKeys.REMEMBER_PLAYBACK_SPEED)) {
            cache.get(KEY_SPEED, String::class.java)?.toFloatOrNull() ?: 1f
        } else {
            1f
        }

    fun setDefaultSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.25f, 3f)
        if (kotlin.math.abs(clamped - 1f) < 0.01f) {
            cache.invalidate(KEY_SPEED)
        } else {
            prefs.setEnabled(PreferenceKeys.REMEMBER_PLAYBACK_SPEED, true)
            cache.put(KEY_SPEED, clamped.toString(), PREF_TTL_MS)
        }
    }

    companion object {
        const val KEY_SPEED = "player:speed"
        const val KEY_QUALITY = "player:quality"
        val QUALITY_OPTIONS: List<String?> = listOf(null, "1080p", "720p", "480p", "360p")
        val SPEED_OPTIONS: List<Float> = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    }
}

