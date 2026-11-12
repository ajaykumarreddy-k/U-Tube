package com.hhst.youtubelite.onboarding

import androidx.lifecycle.ViewModel
import com.hhst.youtubelite.account.domain.AccountItem
import com.hhst.youtubelite.data.media.MediaSession
import com.hhst.youtubelite.downloader.core.DownloadPrefs
import com.hhst.youtubelite.extension.ExtensionManager
import com.hhst.youtubelite.extension.PreferenceKeys
import com.hhst.youtubelite.player.PlaybackDefaults
import com.hhst.youtubelite.ui.theme.AppAccent
import com.hhst.youtubelite.ui.theme.AppAppearance
import com.hhst.youtubelite.ui.theme.LiquidGlassStyle
import com.hhst.youtubelite.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Coordinates the 6-step onboarding journey, immediately persisting changes into the
 * existing [ExtensionManager], [PlaybackDefaults], [DownloadPrefs], and [AppAppearance].
 */
class OnboardingViewModel(
    private val onboardingPrefs: OnboardingPreferences,
    private val extensionManager: ExtensionManager,
    private val playbackDefaults: PlaybackDefaults,
    private val downloadPrefs: DownloadPrefs,
    private val pipSupported: Boolean = true,
    private val dynamicColorsSupported: Boolean = false,
    private val sessionSnapshotProvider: () -> AccountSnapshot = { currentSessionAccountSnapshot() },
    private val applyLanguage: (String) -> Unit = {},
) : ViewModel() {

    data class AccountSnapshot(
        val signedIn: Boolean,
        val activeName: String? = null,
        val activeEmail: String? = null,
        val activeAvatar: String? = null,
        val accounts: List<AccountItem> = emptyList(),
    )

    private val _uiState = MutableStateFlow(loadInitialState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private fun loadInitialState(): OnboardingUiState {
        val account = sessionSnapshotProvider()
        val gesturesOn = PreferenceKeys.GESTURE_KEYS.any { extensionManager.isEnabled(it) }
        val sponsorBlockOn = SPONSOR_KEYS.any { extensionManager.isEnabled(it) }
        return OnboardingUiState(
            accountMode = if (account.signedIn) OnboardingAccountMode.SignedIn else OnboardingAccountMode.Unset,
            signedInName = account.activeName,
            signedInEmail = account.activeEmail,
            signedInAvatar = account.activeAvatar,
            accounts = account.accounts,
            defaultQuality = playbackDefaults.defaultQuality(),
            defaultSpeed = playbackDefaults.defaultSpeed(),
            rememberQuality = extensionManager.isEnabled(PreferenceKeys.REMEMBER_QUALITY),
            rememberSpeed = extensionManager.isEnabled(PreferenceKeys.REMEMBER_PLAYBACK_SPEED),
            backgroundPlay = extensionManager.isEnabled(PreferenceKeys.ENABLE_BACKGROUND_PLAY),
            autoplay = extensionManager.isEnabled(PreferenceKeys.ENABLE_AUTOPLAY),
            pipEnabled = extensionManager.isEnabled(PreferenceKeys.ENABLE_PIP),
            pipSupported = pipSupported,
            inAppMiniPlayer = extensionManager.isEnabled(PreferenceKeys.ENABLE_IN_APP_MINI_PLAYER),
            rememberLastPosition = extensionManager.isEnabled(PreferenceKeys.REMEMBER_LAST_POSITION),
            rememberResizeMode = extensionManager.isEnabled(PreferenceKeys.REMEMBER_RESIZE_MODE),
            dataSaver = extensionManager.isEnabled(PreferenceKeys.DATA_SAVER),
            gesturesEnabled = gesturesOn,
            themeMode = extensionManager.themeMode(),
            accentColor = extensionManager.accentColor(),
            dynamicColors = dynamicColorsSupported && extensionManager.dynamicColors(),
            dynamicColorsSupported = dynamicColorsSupported,
            glassStyle = extensionManager.glassStyle(),
            languageTag = extensionManager.appLanguage(),
            sponsorBlockEnabled = sponsorBlockOn,
            deArrowTitles = extensionManager.isEnabled(PreferenceKeys.DEARROW_TITLES),
            deArrowThumbnails = extensionManager.isEnabled(PreferenceKeys.DEARROW_THUMBNAILS),
            hideShorts = extensionManager.isEnabled(PreferenceKeys.ENABLE_HIDE_SHORTS),
            showDislikes = extensionManager.isEnabled(PreferenceKeys.ENABLE_DISPLAY_DISLIKES),
            downloadWifiOnly = downloadPrefs.wifiOnly(),
            downloadQuality = downloadPrefs.defaultQuality(),
            hapticStrength = extensionManager.hapticStrength(),
        )
    }

    fun refreshAccountState() {
        val account = sessionSnapshotProvider()
        _uiState.update { state ->
            val mode = when {
                account.signedIn -> OnboardingAccountMode.SignedIn
                state.accountMode == OnboardingAccountMode.Guest -> OnboardingAccountMode.Guest
                else -> OnboardingAccountMode.Unset
            }
            state.copy(
                accountMode = mode,
                signedInName = account.activeName,
                signedInEmail = account.activeEmail,
                signedInAvatar = account.activeAvatar,
                accounts = account.accounts,
            )
        }
    }

    fun nextStep() {
        _uiState.update { state ->
            val target = if (state.returnToReviewAfterEdit && state.step != OnboardingStep.Review) {
                OnboardingStep.Review
            } else {
                state.step.next ?: state.step
            }
            state.copy(
                step = target,
                navigatingForward = true,
                returnToReviewAfterEdit = if (target == OnboardingStep.Review) false else state.returnToReviewAfterEdit,
            )
        }
    }

    fun skipCurrentStep() {
        val current = _uiState.value.step
        if (current == OnboardingStep.Account && _uiState.value.accountMode == OnboardingAccountMode.Unset) {
            _uiState.update { it.copy(accountMode = OnboardingAccountMode.Guest) }
        }
        nextStep()
    }

    fun previousStep(): Boolean {
        val state = _uiState.value
        if (state.returnToReviewAfterEdit) {
            _uiState.update {
                it.copy(
                    step = OnboardingStep.Review,
                    navigatingForward = false,
                    returnToReviewAfterEdit = false,
                )
            }
            return true
        }
        val prev = state.step.previous ?: return false
        _uiState.update { it.copy(step = prev, navigatingForward = false) }
        return true
    }

    fun editFromReview(target: OnboardingStep) {
        _uiState.update {
            it.copy(
                step = target,
                navigatingForward = false,
                returnToReviewAfterEdit = true,
            )
        }
    }

    fun continueAsGuest(advance: Boolean = true) {
        if (MediaSession.signedIn()) {
            MediaSession.setAnonymousMode(true)
        }
        _uiState.update { it.copy(accountMode = OnboardingAccountMode.Guest) }
        if (advance) nextStep()
    }

    fun selectAccount(accountId: String) {
        if (MediaSession.selectAccount(accountId)) {
            refreshAccountState()
        }
    }

    fun onSignInCompleted() {
        refreshAccountState()
        if (_uiState.value.accountMode == OnboardingAccountMode.SignedIn) {
            nextStep()
        }
    }

    // Playback setters
    fun setDefaultQuality(quality: String?) {
        playbackDefaults.setDefaultQuality(quality)
        _uiState.update {
            it.copy(
                defaultQuality = quality,
                rememberQuality = extensionManager.isEnabled(PreferenceKeys.REMEMBER_QUALITY),
            )
        }
    }

    fun setDefaultSpeed(speed: Float) {
        playbackDefaults.setDefaultSpeed(speed)
        _uiState.update {
            it.copy(
                defaultSpeed = speed,
                rememberSpeed = extensionManager.isEnabled(PreferenceKeys.REMEMBER_PLAYBACK_SPEED),
            )
        }
    }

    fun setRememberQuality(enabled: Boolean) {
        extensionManager.setEnabled(PreferenceKeys.REMEMBER_QUALITY, enabled)
        _uiState.update {
            it.copy(
                rememberQuality = enabled,
                defaultQuality = playbackDefaults.defaultQuality(),
            )
        }
    }

    fun setBackgroundPlay(enabled: Boolean) {
        extensionManager.setEnabled(PreferenceKeys.ENABLE_BACKGROUND_PLAY, enabled)
        _uiState.update { it.copy(backgroundPlay = enabled) }
    }

    fun setAutoplay(enabled: Boolean) {
        extensionManager.setEnabled(PreferenceKeys.ENABLE_AUTOPLAY, enabled)
        _uiState.update { it.copy(autoplay = enabled) }
    }

    fun setPipEnabled(enabled: Boolean) {
        if (!pipSupported) return
        extensionManager.setEnabled(PreferenceKeys.ENABLE_PIP, enabled)
        _uiState.update { it.copy(pipEnabled = enabled) }
    }

    fun setInAppMiniPlayer(enabled: Boolean) {
        extensionManager.setEnabled(PreferenceKeys.ENABLE_IN_APP_MINI_PLAYER, enabled)
        _uiState.update { it.copy(inAppMiniPlayer = enabled) }
    }

    fun setRememberLastPosition(enabled: Boolean) {
        extensionManager.setEnabled(PreferenceKeys.REMEMBER_LAST_POSITION, enabled)
        _uiState.update { it.copy(rememberLastPosition = enabled) }
    }

    fun setRememberResizeMode(enabled: Boolean) {
        extensionManager.setEnabled(PreferenceKeys.REMEMBER_RESIZE_MODE, enabled)
        _uiState.update { it.copy(rememberResizeMode = enabled) }
    }

    fun setDataSaver(enabled: Boolean) {
        extensionManager.setEnabled(PreferenceKeys.DATA_SAVER, enabled)
        _uiState.update { it.copy(dataSaver = enabled) }
    }

    fun setGesturesEnabled(enabled: Boolean) {
        PreferenceKeys.GESTURE_KEYS.forEach { key ->
            extensionManager.setEnabled(key, enabled)
        }
        _uiState.update { it.copy(gesturesEnabled = enabled) }
    }

    // Appearance setters
    fun setThemeMode(mode: ThemeMode) {
        extensionManager.setThemeMode(mode)
        AppAppearance.update(mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setAccentColor(accent: AppAccent) {
        extensionManager.setAccentColor(accent)
        if (_uiState.value.dynamicColors) {
            extensionManager.setDynamicColors(false)
            AppAppearance.updateDynamicColor(false)
        }
        AppAppearance.updateAccent(accent)
        _uiState.update { it.copy(accentColor = accent, dynamicColors = false) }
    }

    fun setDynamicColors(enabled: Boolean) {
        if (!dynamicColorsSupported) return
        extensionManager.setDynamicColors(enabled)
        AppAppearance.updateDynamicColor(enabled)
        _uiState.update { it.copy(dynamicColors = enabled) }
    }

    fun setGlassStyle(style: LiquidGlassStyle) {
        extensionManager.setGlassStyle(style)
        AppAppearance.updateGlassStyle(style)
        _uiState.update { it.copy(glassStyle = style) }
    }

    // Additional preferences
    fun setLanguage(tag: String) {
        extensionManager.setAppLanguage(tag)
        applyLanguage(tag)
        _uiState.update { it.copy(languageTag = tag) }
    }

    fun setSponsorBlockEnabled(enabled: Boolean) {
        SPONSOR_KEYS.forEach { key ->
            extensionManager.setEnabled(key, enabled)
        }
        _uiState.update { it.copy(sponsorBlockEnabled = enabled) }
    }

    fun setDeArrowEnabled(enabled: Boolean) {
        extensionManager.setEnabled(PreferenceKeys.DEARROW_TITLES, enabled)
        extensionManager.setEnabled(PreferenceKeys.DEARROW_THUMBNAILS, enabled)
        _uiState.update {
            it.copy(
                deArrowTitles = enabled,
                deArrowThumbnails = enabled,
            )
        }
    }

    fun setHideShorts(enabled: Boolean) {
        extensionManager.setEnabled(PreferenceKeys.ENABLE_HIDE_SHORTS, enabled)
        _uiState.update { it.copy(hideShorts = enabled) }
    }

    fun setShowDislikes(enabled: Boolean) {
        extensionManager.setEnabled(PreferenceKeys.ENABLE_DISPLAY_DISLIKES, enabled)
        _uiState.update { it.copy(showDislikes = enabled) }
    }

    fun setDownloadWifiOnly(enabled: Boolean) {
        downloadPrefs.setWifiOnly(enabled)
        _uiState.update { it.copy(downloadWifiOnly = enabled) }
    }

    fun setDownloadQuality(quality: String) {
        downloadPrefs.setDefaultQuality(quality)
        _uiState.update { it.copy(downloadQuality = quality) }
    }

    fun setHapticStrength(strength: Int) {
        extensionManager.setHapticStrength(strength)
        _uiState.update { it.copy(hapticStrength = extensionManager.hapticStrength()) }
    }

    fun completeOnboarding() {
        onboardingPrefs.markCompleted()
    }

    fun skipSetup() {
        onboardingPrefs.markCompleted()
    }

    companion object {
        private val SPONSOR_KEYS = listOf(
            PreferenceKeys.SKIP_SPONSORS,
            PreferenceKeys.SKIP_SELF_PROMO,
            PreferenceKeys.SKIP_INTERACTION,
            PreferenceKeys.SKIP_INTRO,
            PreferenceKeys.SKIP_OUTRO,
            PreferenceKeys.SKIP_PREVIEW,
        )

        val DOWNLOAD_QUALITY_OPTIONS: List<String> = listOf("1080p", "720p", "480p", "360p")

        fun currentSessionAccountSnapshot(): AccountSnapshot {
            val snap = MediaSession.snapshot()
            val selected = MediaSession.selectedAccount()
            val accounts = MediaSession.accounts().map { account ->
                AccountItem(
                    id = account.id,
                    name = account.name ?: account.email ?: account.id,
                    email = account.email.orEmpty(),
                    photo = account.photo.orEmpty(),
                    selected = account.selected,
                )
            }
            return AccountSnapshot(
                signedIn = snap.signedIn,
                activeName = selected?.name ?: selected?.email,
                activeEmail = selected?.email,
                activeAvatar = selected?.photo,
                accounts = accounts,
            )
        }
    }
}
