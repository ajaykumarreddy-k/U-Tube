package com.hhst.youtubelite.onboarding

import com.hhst.youtubelite.account.domain.AccountItem
import com.hhst.youtubelite.ui.theme.AppAccent
import com.hhst.youtubelite.ui.theme.LiquidGlassStyle
import com.hhst.youtubelite.ui.theme.ThemeMode

enum class OnboardingStep(val index: Int) {
    Welcome(0),
    Account(1),
    Playback(2),
    Appearance(3),
    Preferences(4),
    Review(5);

    val canSkipStep: Boolean
        get() = this != Welcome && this != Review

    val previous: OnboardingStep?
        get() = entries.getOrNull(index - 1)

    val next: OnboardingStep?
        get() = entries.getOrNull(index + 1)

    companion object {
        val TOTAL_STEPS: Int = entries.size
    }
}

enum class OnboardingAccountMode {
    Unset,
    SignedIn,
    Guest,
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Welcome,
    val navigatingForward: Boolean = true,
    val returnToReviewAfterEdit: Boolean = false,
    // Account
    val accountMode: OnboardingAccountMode = OnboardingAccountMode.Unset,
    val signedInName: String? = null,
    val signedInEmail: String? = null,
    val signedInAvatar: String? = null,
    val accounts: List<AccountItem> = emptyList(),
    // Playback
    val defaultQuality: String? = null,
    val defaultSpeed: Float = 1.0f,
    val rememberQuality: Boolean = true,
    val rememberSpeed: Boolean = false,
    val backgroundPlay: Boolean = true,
    val autoplay: Boolean = true,
    val pipEnabled: Boolean = true,
    val pipSupported: Boolean = true,
    val inAppMiniPlayer: Boolean = true,
    val rememberLastPosition: Boolean = true,
    val rememberResizeMode: Boolean = false,
    val dataSaver: Boolean = false,
    val gesturesEnabled: Boolean = true,
    // Appearance
    val themeMode: ThemeMode = ThemeMode.System,
    val accentColor: AppAccent = AppAccent.Crimson,
    val dynamicColors: Boolean = false,
    val dynamicColorsSupported: Boolean = false,
    val glassStyle: LiquidGlassStyle = LiquidGlassStyle.Standard,
    // Additional Preferences
    val languageTag: String = "",
    val sponsorBlockEnabled: Boolean = true,
    val deArrowTitles: Boolean = false,
    val deArrowThumbnails: Boolean = false,
    val hideShorts: Boolean = false,
    val showDislikes: Boolean = true,
    val downloadWifiOnly: Boolean = false,
    val downloadQuality: String = "1080p",
    val hapticStrength: Int = 30,
)

