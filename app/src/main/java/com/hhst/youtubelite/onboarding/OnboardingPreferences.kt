package com.hhst.youtubelite.onboarding

import android.content.Context
import com.hhst.youtubelite.core.PreferenceStore
import com.hhst.youtubelite.data.media.MediaSession
import com.hhst.youtubelite.extension.PrefStore

/**
 * Persists first-launch onboarding completion and ensures existing installations
 * upgrading from an older version are treated as already onboarded.
 */
class OnboardingPreferences(
    private val store: PrefStore,
    private val hasExistingAuth: () -> Boolean = { MediaSession.signedIn() },
) {
    fun isCompleted(): Boolean = store.getBool(KEY_COMPLETED, false)

    fun markCompleted() {
        store.putBool(KEY_COMPLETED, true)
    }

    fun resetForReplay() {
        store.putBool(KEY_COMPLETED, false)
    }

    /**
     * Runs at application startup BEFORE [com.hhst.youtubelite.extension.ExtensionManager]
     * seeds default preference keys. Pre-existing installs already carry persisted
     * preference keys or credentials, while genuine fresh installs start clean.
     */
    fun resolveUpgradeStateBeforeSeeding() {
        if (store.contains(KEY_COMPLETED)) return
        val existingInstall =
            store.contains(EXISTING_PREF_MARKER) ||
                store.getLong(EXISTING_VERSION_KEY, 0L) > 0L ||
                store.contains(EXISTING_LANGUAGE_KEY) ||
                store.contains(EXISTING_THEME_KEY) ||
                store.contains(EXISTING_DOWNLOAD_WIFI_KEY) ||
                store.contains(EXISTING_DOWNLOAD_QUALITY_KEY) ||
                hasExistingAuth()
        if (existingInstall) {
            store.putBool(KEY_COMPLETED, true)
        }
    }

    companion object {
        const val KEY_COMPLETED = "onboarding:v1:completed"
        private const val EXISTING_PREF_MARKER = "preferences:enable_background_play"
        private const val EXISTING_VERSION_KEY = "preferences:version"
        private const val EXISTING_LANGUAGE_KEY = "preferences:app_language"
        private const val EXISTING_THEME_KEY = "preferences:theme_mode"
        private const val EXISTING_DOWNLOAD_WIFI_KEY = "download.wifi_only"
        private const val EXISTING_DOWNLOAD_QUALITY_KEY = "download.default_quality"

        fun create(context: Context): OnboardingPreferences =
            OnboardingPreferences(
                store = PreferenceStore.app(context),
                hasExistingAuth = { MediaSession.signedIn() },
            )
    }
}
