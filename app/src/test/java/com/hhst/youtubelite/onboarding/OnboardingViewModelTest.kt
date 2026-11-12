package com.hhst.youtubelite.onboarding

import com.hhst.youtubelite.downloader.core.DownloadConfig
import com.hhst.youtubelite.downloader.core.DownloadPrefs
import com.hhst.youtubelite.extension.ExtensionManager
import com.hhst.youtubelite.extension.MemoryPrefStore
import com.hhst.youtubelite.extension.PreferenceKeys
import com.hhst.youtubelite.player.MemJsonCache
import com.hhst.youtubelite.player.PlaybackDefaults
import com.hhst.youtubelite.ui.theme.AppAccent
import com.hhst.youtubelite.ui.theme.AppAppearance
import com.hhst.youtubelite.ui.theme.LiquidGlassStyle
import com.hhst.youtubelite.ui.theme.ThemeMode
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingViewModelTest {

    private class FakeDownloadPrefs : DownloadPrefs {
        private var wifiOnly = false
        private var maxConn = 4
        private var quality = "1080p"
        private var last = DownloadConfig(videoQuality = quality)

        override fun wifiOnly(): Boolean = wifiOnly
        override fun maxConnections(): Int = maxConn
        override fun defaultQuality(): String = quality
        override fun lastConfig(): DownloadConfig = last
        override fun setWifiOnly(value: Boolean) { wifiOnly = value }
        override fun setMaxConnections(value: Int) { maxConn = value }
        override fun setDefaultQuality(value: String) { quality = value }
        override fun setLastConfig(config: DownloadConfig) { last = config }
    }

    @After
    fun resetAppearance() {
        AppAppearance.update(
            value = ThemeMode.System,
            accent = AppAccent.Crimson,
            dynamicColor = false,
            glassStyle = LiquidGlassStyle.Standard,
        )
    }

    @Test
    fun skippingEveryOptionalStepReachesReviewAndPreservesDefaults() {
        val store = MemoryPrefStore()
        val onboardingPrefs = OnboardingPreferences(store) { false }
        val ext = ExtensionManager(store)
        val cache = MemJsonCache()
        val playbackDefaults = PlaybackDefaults(ext, cache)
        val downloadPrefs = FakeDownloadPrefs()
        val vm = OnboardingViewModel(
            onboardingPrefs = onboardingPrefs,
            extensionManager = ext,
            playbackDefaults = playbackDefaults,
            downloadPrefs = downloadPrefs,
            pipSupported = true,
            dynamicColorsSupported = true,
            sessionSnapshotProvider = { OnboardingViewModel.AccountSnapshot(signedIn = false) },
        )

        assertEquals(OnboardingStep.Welcome, vm.uiState.value.step)
        vm.nextStep()
        assertEquals(OnboardingStep.Account, vm.uiState.value.step)
        vm.skipCurrentStep()
        assertEquals(OnboardingAccountMode.Guest, vm.uiState.value.accountMode)
        assertEquals(OnboardingStep.Playback, vm.uiState.value.step)
        vm.skipCurrentStep()
        assertEquals(OnboardingStep.Appearance, vm.uiState.value.step)
        vm.skipCurrentStep()
        assertEquals(OnboardingStep.Preferences, vm.uiState.value.step)
        vm.skipCurrentStep()
        assertEquals(OnboardingStep.Review, vm.uiState.value.step)

        // Defaults preserved
        assertTrue(ext.isEnabled(PreferenceKeys.ENABLE_BACKGROUND_PLAY))
        assertTrue(ext.isEnabled(PreferenceKeys.ENABLE_AUTOPLAY))
        assertEquals(ThemeMode.System, ext.themeMode())
        assertNull(playbackDefaults.defaultQuality())
        assertEquals(1f, playbackDefaults.defaultSpeed(), 0.01f)

        vm.completeOnboarding()
        assertTrue(onboardingPrefs.isCompleted())
    }

    @Test
    fun skipSetupMarksOnboardingCompletedWithoutChangingDefaults() {
        val store = MemoryPrefStore()
        val onboardingPrefs = OnboardingPreferences(store) { false }
        val ext = ExtensionManager(store)
        val vm = OnboardingViewModel(
            onboardingPrefs = onboardingPrefs,
            extensionManager = ext,
            playbackDefaults = PlaybackDefaults(ext, MemJsonCache()),
            downloadPrefs = FakeDownloadPrefs(),
            sessionSnapshotProvider = { OnboardingViewModel.AccountSnapshot(signedIn = false) },
        )

        vm.skipSetup()

        assertTrue(onboardingPrefs.isCompleted())
        assertEquals(0L, ext.version())
    }

    @Test
    fun playbackAndAppearanceChangesPersistAndReflectInAppState() {
        val store = MemoryPrefStore()
        val onboardingPrefs = OnboardingPreferences(store) { false }
        val ext = ExtensionManager(store)
        val cache = MemJsonCache()
        val playbackDefaults = PlaybackDefaults(ext, cache)
        val downloadPrefs = FakeDownloadPrefs()
        var appliedLanguage = ""
        val vm = OnboardingViewModel(
            onboardingPrefs = onboardingPrefs,
            extensionManager = ext,
            playbackDefaults = playbackDefaults,
            downloadPrefs = downloadPrefs,
            pipSupported = true,
            dynamicColorsSupported = true,
            sessionSnapshotProvider = { OnboardingViewModel.AccountSnapshot(signedIn = false) },
            applyLanguage = { appliedLanguage = it },
        )

        vm.setDefaultQuality("720p")
        vm.setDefaultSpeed(1.5f)
        vm.setBackgroundPlay(false)
        vm.setDataSaver(true)
        vm.setGesturesEnabled(false)

        assertEquals("720p", cache.get(PlaybackDefaults.KEY_QUALITY, String::class.java))
        assertEquals("1.5", cache.get(PlaybackDefaults.KEY_SPEED, String::class.java))
        assertFalse(ext.isEnabled(PreferenceKeys.ENABLE_BACKGROUND_PLAY))
        assertTrue(ext.isEnabled(PreferenceKeys.DATA_SAVER))
        assertFalse(ext.isEnabled(PreferenceKeys.GESTURE_SEEK_WINDOWED))

        vm.setThemeMode(ThemeMode.Dark)
        vm.setAccentColor(AppAccent.Cobalt)
        vm.setGlassStyle(LiquidGlassStyle.Frosted)
        assertEquals(ThemeMode.Dark, ext.themeMode())
        assertEquals(ThemeMode.Dark, AppAppearance.mode)
        assertEquals(AppAccent.Cobalt, ext.accentColor())
        assertEquals(AppAccent.Cobalt, AppAppearance.accent)
        assertEquals(LiquidGlassStyle.Frosted, ext.glassStyle())
        assertEquals(LiquidGlassStyle.Frosted, AppAppearance.glassStyle)

        vm.setDynamicColors(true)
        assertTrue(ext.dynamicColors())
        assertTrue(AppAppearance.dynamicColor)

        vm.setLanguage("ja")
        vm.setSponsorBlockEnabled(false)
        vm.setDeArrowEnabled(true)
        vm.setHideShorts(true)
        vm.setDownloadWifiOnly(true)
        vm.setDownloadQuality("720p")
        vm.setHapticStrength(75)

        assertEquals("ja", appliedLanguage)
        assertEquals("ja", ext.appLanguage())
        assertFalse(ext.isEnabled(PreferenceKeys.SKIP_SPONSORS))
        assertTrue(ext.isEnabled(PreferenceKeys.DEARROW_TITLES))
        assertTrue(ext.isEnabled(PreferenceKeys.ENABLE_HIDE_SHORTS))
        assertTrue(downloadPrefs.wifiOnly())
        assertEquals("720p", downloadPrefs.defaultQuality())
        assertEquals(75, ext.hapticStrength())
    }

    @Test
    fun editFromReviewReturnsDirectlyToReviewStep() {
        val store = MemoryPrefStore()
        val ext = ExtensionManager(store)
        val vm = OnboardingViewModel(
            onboardingPrefs = OnboardingPreferences(store) { false },
            extensionManager = ext,
            playbackDefaults = PlaybackDefaults(ext, MemJsonCache()),
            downloadPrefs = FakeDownloadPrefs(),
            sessionSnapshotProvider = { OnboardingViewModel.AccountSnapshot(signedIn = false) },
        )

        vm.editFromReview(OnboardingStep.Appearance)
        assertEquals(OnboardingStep.Appearance, vm.uiState.value.step)
        assertTrue(vm.uiState.value.returnToReviewAfterEdit)

        vm.nextStep()
        assertEquals(OnboardingStep.Review, vm.uiState.value.step)
        assertFalse(vm.uiState.value.returnToReviewAfterEdit)
    }
}

