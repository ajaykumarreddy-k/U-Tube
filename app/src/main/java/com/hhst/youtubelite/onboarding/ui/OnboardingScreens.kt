package com.hhst.youtubelite.onboarding.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import com.hhst.youtubelite.R
import com.hhst.youtubelite.core.AppLocale
import com.hhst.youtubelite.onboarding.OnboardingAccountMode
import com.hhst.youtubelite.onboarding.OnboardingStep
import com.hhst.youtubelite.onboarding.OnboardingUiState
import com.hhst.youtubelite.onboarding.OnboardingViewModel
import com.hhst.youtubelite.player.PlaybackDefaults
import com.hhst.youtubelite.ui.design.AppIcon
import com.hhst.youtubelite.ui.design.AppText
import com.hhst.youtubelite.ui.design.Artwork
import com.hhst.youtubelite.ui.design.DarkAppPalette
import com.hhst.youtubelite.ui.design.LightAppPalette
import com.hhst.youtubelite.ui.glass.LiquidGlassSurface
import com.hhst.youtubelite.ui.glass.LiquidGlassTier
import com.hhst.youtubelite.ui.glass.LocalLiquidGlassBackdrop
import com.hhst.youtubelite.ui.glass.LocalLiquidGlassPalette
import com.hhst.youtubelite.ui.glass.rememberUTubeBackdrop
import com.hhst.youtubelite.ui.glass.utubeBackdropSource
import com.hhst.youtubelite.ui.theme.AppAccent
import com.hhst.youtubelite.ui.theme.LiquidGlassStyle
import com.hhst.youtubelite.ui.theme.ThemeMode

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkipStep: () -> Unit,
    onSkipSetup: () -> Unit,
    onEditFromReview: (OnboardingStep) -> Unit,
    onSignInClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onContinueGuest: () -> Unit,
    onOpenAccountSwitch: () -> Unit,
    onSelectQuality: (String?) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onToggleBackgroundPlay: (Boolean) -> Unit,
    onToggleAutoplay: (Boolean) -> Unit,
    onTogglePip: (Boolean) -> Unit,
    onToggleMiniPlayer: (Boolean) -> Unit,
    onToggleDataSaver: (Boolean) -> Unit,
    onToggleRememberPosition: (Boolean) -> Unit,
    onToggleRememberResize: (Boolean) -> Unit,
    onToggleGestures: (Boolean) -> Unit,
    onSelectTheme: (ThemeMode) -> Unit,
    onSelectAccent: (AppAccent) -> Unit,
    onToggleDynamicColors: (Boolean) -> Unit,
    onSelectGlassStyle: (LiquidGlassStyle) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onToggleSponsorBlock: (Boolean) -> Unit,
    onToggleDeArrow: (Boolean) -> Unit,
    onToggleHideShorts: (Boolean) -> Unit,
    onToggleShowDislikes: (Boolean) -> Unit,
    onToggleDownloadWifiOnly: (Boolean) -> Unit,
    onSelectDownloadQuality: (String) -> Unit,
    onSelectHapticStrength: (Int) -> Unit,
    onStartWatching: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingSystemBarEffect()
    val backdrop = rememberUTubeBackdrop(OnboardingEditorialColors.ObsidianBg)
    val baseAccent = LocalLiquidGlassPalette.current.accent
    val displayAccent = if (!state.dynamicColors && state.accentColor == AppAccent.Crimson) {
        OnboardingEditorialColors.SignatureCoral
    } else {
        baseAccent
    }

    CompositionLocalProvider(LocalLiquidGlassBackdrop provides backdrop) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(OnboardingEditorialColors.ObsidianBg)
                .testTag("onboarding-screen"),
        ) {
            OnboardingAmbientCanvas(Modifier.utubeBackdropSource(backdrop))

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .fillMaxWidth(),
                ) {
                    OnboardingTopChrome(
                        step = state.step,
                        canReturnToReview = state.returnToReviewAfterEdit,
                        onBack = onBack,
                        onSkipStep = onSkipStep,
                        onSkipSetup = onSkipSetup,
                    )

                    EditorialStepHeader(
                        state = state,
                        accentColor = displayAccent,
                    )
                }

                OnboardingPearlSheet(
                    accentColor = displayAccent,
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 640.dp),
                ) {
                    OnboardingStepRail(
                        currentStep = state.step,
                        accentColor = displayAccent,
                    )

                    OnboardingStepContainer(
                        step = state.step,
                        forward = state.navigatingForward,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) { currentStep ->
                        when (currentStep) {
                            OnboardingStep.Welcome -> WelcomeStep(
                                accentColor = displayAccent,
                                onGetStarted = onNext,
                                onSkipSetup = onSkipSetup,
                            )
                            OnboardingStep.Account -> AccountSetupStep(
                                state = state,
                                accentColor = displayAccent,
                                onSignInClick = onSignInClick,
                                onCreateAccountClick = onCreateAccountClick,
                                onContinueGuest = onContinueGuest,
                                onOpenAccountSwitch = onOpenAccountSwitch,
                                onContinueSignedIn = onNext,
                            )
                            OnboardingStep.Playback -> PlaybackPreferencesStep(
                                state = state,
                                accentColor = displayAccent,
                                onSelectQuality = onSelectQuality,
                                onSelectSpeed = onSelectSpeed,
                                onToggleBackgroundPlay = onToggleBackgroundPlay,
                                onToggleAutoplay = onToggleAutoplay,
                                onTogglePip = onTogglePip,
                                onToggleMiniPlayer = onToggleMiniPlayer,
                                onToggleDataSaver = onToggleDataSaver,
                                onToggleRememberPosition = onToggleRememberPosition,
                                onToggleRememberResize = onToggleRememberResize,
                                onToggleGestures = onToggleGestures,
                                onContinue = onNext,
                            )
                            OnboardingStep.Appearance -> AppearanceStep(
                                state = state,
                                accentColor = displayAccent,
                                onSelectTheme = onSelectTheme,
                                onSelectAccent = onSelectAccent,
                                onToggleDynamicColors = onToggleDynamicColors,
                                onSelectGlassStyle = onSelectGlassStyle,
                                onContinue = onNext,
                            )
                            OnboardingStep.Preferences -> AdditionalPreferencesStep(
                                state = state,
                                accentColor = displayAccent,
                                onSelectLanguage = onSelectLanguage,
                                onToggleSponsorBlock = onToggleSponsorBlock,
                                onToggleDeArrow = onToggleDeArrow,
                                onToggleHideShorts = onToggleHideShorts,
                                onToggleShowDislikes = onToggleShowDislikes,
                                onToggleDownloadWifiOnly = onToggleDownloadWifiOnly,
                                onSelectDownloadQuality = onSelectDownloadQuality,
                                onSelectHapticStrength = onSelectHapticStrength,
                                onContinue = onNext,
                            )
                            OnboardingStep.Review -> ReviewSetupStep(
                                state = state,
                                accentColor = displayAccent,
                                onEdit = onEditFromReview,
                                onStartWatching = onStartWatching,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingSystemBarEffect() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = view.context.findActivity()?.window ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)
        val prevLightStatus = controller.isAppearanceLightStatusBars
        val prevLightNav = controller.isAppearanceLightNavigationBars
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = true
        onDispose {
            controller.isAppearanceLightStatusBars = prevLightStatus
            controller.isAppearanceLightNavigationBars = prevLightNav
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun EditorialStepHeader(
    state: OnboardingUiState,
    accentColor: Color,
) {
    AnimatedContent(
        targetState = state.step,
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
        label = "editorial-hero",
    ) { step ->
        val stepCounter = stringResource(R.string.onboarding_step_of, step.index + 1, OnboardingStep.TOTAL_STEPS)
        val qualityLabel = state.defaultQuality ?: stringResource(R.string.settings_quality_auto)
        val langLabel = AppLocale.OPTIONS.firstOrNull { it.first == state.languageTag }?.second
            ?: state.languageTag.ifBlank { stringResource(R.string.settings_theme_system) }

        when (step) {
            OnboardingStep.Welcome -> OnboardingObsidianHero(
                keyword = stringResource(R.string.app_name),
                stepMetaTop = stepCounter,
                stepMetaBottom = "U-Tube Setup",
                dotColor = accentColor,
                narrative = editorialNarrative(
                    highlight = stringResource(R.string.onboarding_welcome_headline),
                    body = stringResource(R.string.onboarding_welcome_subtitle),
                    leadingIconId = "play",
                ),
                inlineIcons = mapOf(inlineEditorialIcon("play", AppIcon.Play)),
                statusContent = {
                    EditorialStat(AppIcon.Explore, stringResource(R.string.onboarding_welcome_pill_glass), accentColor)
                    EditorialStat(AppIcon.Block, stringResource(R.string.onboarding_welcome_pill_sponsor), OnboardingEditorialColors.ObsidianMuted)
                },
            )
            OnboardingStep.Account -> OnboardingObsidianHero(
                keyword = "Account",
                stepMetaTop = stepCounter,
                stepMetaBottom = "Identity",
                dotColor = accentColor,
                narrative = editorialNarrative(
                    highlight = stringResource(R.string.onboarding_account_title),
                    body = stringResource(R.string.onboarding_account_subtitle),
                    leadingIconId = "you",
                ),
                inlineIcons = mapOf(inlineEditorialIcon("you", AppIcon.You)),
                statusContent = {
                    val accountLabel = when (state.accountMode) {
                        OnboardingAccountMode.SignedIn -> state.signedInName ?: stringResource(R.string.onboarding_review_signed_in)
                        OnboardingAccountMode.Guest, OnboardingAccountMode.Unset -> stringResource(R.string.onboarding_review_guest)
                    }
                    EditorialStat(AppIcon.Device, accountLabel, accentColor)
                },
            )
            OnboardingStep.Playback -> OnboardingObsidianHero(
                keyword = "Playback",
                stepMetaTop = stepCounter,
                stepMetaBottom = "Media3 Engine",
                dotColor = accentColor,
                narrative = editorialNarrative(
                    highlight = stringResource(R.string.onboarding_playback_title),
                    body = stringResource(R.string.onboarding_playback_subtitle),
                    leadingIconId = "tune",
                ),
                inlineIcons = mapOf(inlineEditorialIcon("tune", AppIcon.Play)),
                statusContent = {
                    EditorialStat(AppIcon.Play, "$qualityLabel · ${formatSpeed(state.defaultSpeed)}", accentColor)
                    if (state.backgroundPlay) EditorialStat(AppIcon.Queue, "Background", OnboardingEditorialColors.ObsidianMuted)
                },
            )
            OnboardingStep.Appearance -> OnboardingObsidianHero(
                keyword = "Style",
                stepMetaTop = stepCounter,
                stepMetaBottom = "Theme & Glass",
                dotColor = accentColor,
                narrative = editorialNarrative(
                    highlight = stringResource(R.string.onboarding_appearance_title),
                    body = stringResource(R.string.onboarding_appearance_subtitle),
                    leadingIconId = "theme",
                ),
                inlineIcons = mapOf(inlineEditorialIcon("theme", AppIcon.Refresh)),
                statusContent = {
                    val accentName = if (state.dynamicColors) "Dynamic" else state.accentColor.label
                    EditorialStat(AppIcon.Explore, "${state.themeMode.label} · $accentName", accentColor)
                    EditorialStat(AppIcon.ChannelGrid, state.glassStyle.label, OnboardingEditorialColors.ObsidianMuted)
                },
            )
            OnboardingStep.Preferences -> OnboardingObsidianHero(
                keyword = "Extras",
                stepMetaTop = stepCounter,
                stepMetaBottom = "Optional Controls",
                dotColor = accentColor,
                narrative = editorialNarrative(
                    highlight = stringResource(R.string.onboarding_prefs_title),
                    body = stringResource(R.string.onboarding_prefs_subtitle),
                    leadingIconId = "globe",
                ),
                inlineIcons = mapOf(inlineEditorialIcon("globe", AppIcon.Globe)),
                statusContent = {
                    EditorialStat(AppIcon.Globe, langLabel, accentColor)
                    EditorialStat(AppIcon.Download, "DL ${state.downloadQuality}", OnboardingEditorialColors.ObsidianMuted)
                },
            )
            OnboardingStep.Review -> OnboardingObsidianHero(
                keyword = "Ready",
                stepMetaTop = stepCounter,
                stepMetaBottom = "Final Summary",
                dotColor = accentColor,
                narrative = editorialNarrative(
                    highlight = stringResource(R.string.onboarding_review_title),
                    body = stringResource(R.string.onboarding_review_subtitle),
                    leadingIconId = "check",
                ),
                inlineIcons = mapOf(inlineEditorialIcon("check", AppIcon.Check, accentColor)),
                statusContent = {
                    EditorialStat(AppIcon.Play, "$qualityLabel · ${formatSpeed(state.defaultSpeed)}", accentColor)
                    EditorialStat(AppIcon.Refresh, state.themeMode.label, OnboardingEditorialColors.ObsidianMuted)
                },
            )
        }
    }
}

@Composable
private fun EditorialStat(icon: AppIcon, label: String, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        AppIcon(icon = icon, color = tint, modifier = Modifier.size(13.dp))
        AppText(
            text = label,
            size = 12,
            line = 16,
            weight = FontWeight.Medium,
            color = OnboardingEditorialColors.ObsidianMuted,
        )
    }
}

@Composable
private fun WelcomeStep(
    accentColor: Color,
    onGetStarted: () -> Unit,
    onSkipSetup: () -> Unit,
) {
    OnboardingFrostedStepLayout(
        modifier = Modifier.testTag("onboarding-step-welcome"),
        footer = {
            OnboardingPrimaryPillButton(
                text = stringResource(R.string.onboarding_welcome_cta),
                onClick = onGetStarted,
                accentDot = accentColor,
                modifier = Modifier.testTag("onboarding-get-started"),
            )
            Box(
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(role = Role.Button, onClick = onSkipSetup)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("onboarding-skip-setup-welcome"),
            ) {
                AppText(
                    text = stringResource(R.string.onboarding_skip_setup),
                    size = 13,
                    line = 18,
                    weight = FontWeight.Medium,
                    color = OnboardingEditorialColors.PearlMuted,
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OnboardingGlassCard(contentPadding = PaddingValues(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorResource(R.color.ic_launcher_background)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = stringResource(R.string.app_name),
                            modifier = Modifier.size(46.dp),
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        AppText(
                            text = stringResource(R.string.app_name),
                            size = 17,
                            line = 22,
                            weight = FontWeight.Bold,
                            color = OnboardingEditorialColors.PearlInk,
                        )
                        AppText(
                            text = stringResource(R.string.onboarding_welcome_headline),
                            size = 13,
                            line = 18,
                            color = OnboardingEditorialColors.PearlMuted,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(OnboardingEditorialColors.PearlCard)
                    .border(0.75.dp, OnboardingEditorialColors.PearlBorder, RoundedCornerShape(20.dp)),
            ) {
                EditorialHighlightRow(
                    icon = AppIcon.Explore,
                    iconTint = accentColor,
                    title = stringResource(R.string.onboarding_welcome_pill_glass),
                    trailing = "01",
                )
                PearlDivider()
                EditorialHighlightRow(
                    icon = AppIcon.Block,
                    iconTint = Color(0xFFD97706),
                    title = stringResource(R.string.onboarding_welcome_pill_sponsor),
                    trailing = "02",
                )
                PearlDivider()
                EditorialHighlightRow(
                    icon = AppIcon.Play,
                    iconTint = OnboardingEditorialColors.PearlInk,
                    title = stringResource(R.string.onboarding_welcome_pill_background),
                    trailing = "03",
                )
            }

            AppText(
                text = stringResource(R.string.onboarding_welcome_note),
                size = 12,
                line = 18,
                color = OnboardingEditorialColors.PearlMuted,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun EditorialHighlightRow(
    icon: AppIcon,
    iconTint: Color,
    title: String,
    trailing: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppIcon(icon = icon, color = iconTint, modifier = Modifier.size(18.dp))
        AppText(
            text = title,
            size = 15,
            line = 20,
            weight = FontWeight.Medium,
            color = OnboardingEditorialColors.PearlInk,
            modifier = Modifier.weight(1f),
        )
        AppText(
            text = trailing,
            size = 12,
            line = 16,
            weight = FontWeight.Medium,
            color = OnboardingEditorialColors.PearlDim,
        )
    }
}

@Composable
private fun PearlDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(0.75.dp)
            .background(OnboardingEditorialColors.PearlDivider),
    )
}

@Composable
private fun AccountSetupStep(
    state: OnboardingUiState,
    accentColor: Color,
    onSignInClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onContinueGuest: () -> Unit,
    onOpenAccountSwitch: () -> Unit,
    onContinueSignedIn: () -> Unit,
) {
    OnboardingFrostedStepLayout(
        modifier = Modifier.testTag("onboarding-step-account"),
        footer = if (state.accountMode == OnboardingAccountMode.SignedIn) {
            {
                OnboardingPrimaryPillButton(
                    text = stringResource(
                        R.string.onboarding_account_continue_as,
                        state.signedInName ?: stringResource(R.string.onboarding_review_signed_in),
                    ),
                    onClick = onContinueSignedIn,
                    accentDot = accentColor,
                    modifier = Modifier.testTag("onboarding-account-continue-signedin"),
                )
            }
        } else {
            null
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.accountMode == OnboardingAccountMode.SignedIn) {
                OnboardingGlassCard(selected = true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(Modifier.size(44.dp).clip(CircleShape)) {
                            Artwork(state.signedInAvatar.orEmpty(), modifier = Modifier.fillMaxSize())
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            AppText(
                                text = stringResource(R.string.onboarding_account_connected),
                                size = 11,
                                line = 14,
                                weight = FontWeight.SemiBold,
                                color = accentColor,
                            )
                            AppText(
                                text = state.signedInName ?: stringResource(R.string.onboarding_review_signed_in),
                                size = 16,
                                line = 20,
                                weight = FontWeight.SemiBold,
                                color = OnboardingEditorialColors.PearlInk,
                            )
                            if (!state.signedInEmail.isNullOrBlank()) {
                                AppText(state.signedInEmail, size = 12, line = 16, color = OnboardingEditorialColors.PearlMuted)
                            }
                        }
                        if (state.accounts.size > 1) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(OnboardingEditorialColors.PearlSubtle)
                                    .border(0.75.dp, OnboardingEditorialColors.PearlBorder, CircleShape)
                                    .clickable(role = Role.Button, onClick = onOpenAccountSwitch)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                AppText(
                                    stringResource(R.string.onboarding_account_switch),
                                    size = 12,
                                    line = 16,
                                    weight = FontWeight.Medium,
                                    color = OnboardingEditorialColors.PearlInk,
                                )
                            }
                        }
                    }
                }
            }

            AccountChoiceCard(
                icon = AppIcon.You,
                accentColor = accentColor,
                title = if (state.accountMode == OnboardingAccountMode.SignedIn) {
                    stringResource(R.string.onboarding_account_add_another)
                } else {
                    stringResource(R.string.onboarding_account_sign_in)
                },
                summary = stringResource(R.string.onboarding_account_sign_in_summary),
                selected = false,
                onClick = onSignInClick,
                tag = "onboarding-account-signin",
            )

            AccountChoiceCard(
                icon = AppIcon.Add,
                accentColor = accentColor,
                title = stringResource(R.string.onboarding_account_create),
                summary = stringResource(R.string.onboarding_account_create_summary),
                selected = false,
                onClick = onCreateAccountClick,
                tag = "onboarding-account-create",
            )

            AccountChoiceCard(
                icon = AppIcon.Device,
                accentColor = accentColor,
                title = stringResource(R.string.onboarding_account_guest),
                summary = stringResource(R.string.onboarding_account_guest_summary),
                selected = state.accountMode == OnboardingAccountMode.Guest,
                onClick = onContinueGuest,
                tag = "onboarding-account-guest",
            )
        }
    }
}

@Composable
private fun AccountChoiceCard(
    icon: AppIcon,
    accentColor: Color,
    title: String,
    summary: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String,
) {
    OnboardingGlassCard(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.testTag(tag),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (selected) accentColor else OnboardingEditorialColors.PearlSubtle),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    icon = icon,
                    color = if (selected) Color.White else OnboardingEditorialColors.PearlInk,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                AppText(title, size = 15, line = 20, weight = FontWeight.SemiBold, color = OnboardingEditorialColors.PearlInk)
                AppText(summary, size = 12, line = 17, color = OnboardingEditorialColors.PearlMuted)
            }
            if (selected) {
                AppIcon(AppIcon.Check, color = accentColor, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlaybackPreferencesStep(
    state: OnboardingUiState,
    accentColor: Color,
    onSelectQuality: (String?) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onToggleBackgroundPlay: (Boolean) -> Unit,
    onToggleAutoplay: (Boolean) -> Unit,
    onTogglePip: (Boolean) -> Unit,
    onToggleMiniPlayer: (Boolean) -> Unit,
    onToggleDataSaver: (Boolean) -> Unit,
    onToggleRememberPosition: (Boolean) -> Unit,
    onToggleRememberResize: (Boolean) -> Unit,
    onToggleGestures: (Boolean) -> Unit,
    onContinue: () -> Unit,
) {
    OnboardingFrostedStepLayout(
        modifier = Modifier.testTag("onboarding-step-playback"),
        footer = {
            OnboardingPrimaryPillButton(
                text = stringResource(if (state.returnToReviewAfterEdit) R.string.onboarding_done else R.string.onboarding_continue),
                onClick = onContinue,
                accentDot = accentColor,
                modifier = Modifier.testTag("onboarding-playback-continue"),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OnboardingGlassCard {
                AppText(
                    text = stringResource(R.string.onboarding_playback_preview_label),
                    size = 11,
                    line = 14,
                    weight = FontWeight.SemiBold,
                    color = accentColor,
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(106.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF0E0E12), Color(0xFF1D1F28))))
                        .padding(14.dp),
                ) {
                    Row(
                        Modifier.align(Alignment.TopStart),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        PreviewBadge(state.defaultQuality ?: stringResource(R.string.settings_quality_auto), accentColor)
                        PreviewBadge(formatSpeed(state.defaultSpeed), Color.White.copy(alpha = 0.16f))
                        if (state.dataSaver) PreviewBadge("Data Saver", Color.White.copy(alpha = 0.16f))
                    }
                    Row(
                        Modifier.align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            AppIcon(AppIcon.Play, color = Color.White, modifier = Modifier.size(15.dp))
                        }
                        AppText(
                            text = stringResource(R.string.onboarding_playback_preview_title),
                            size = 13,
                            line = 18,
                            weight = FontWeight.Medium,
                            color = Color.White,
                        )
                    }
                    Row(
                        Modifier.align(Alignment.BottomStart),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (state.backgroundPlay) PreviewFooterTag("Background")
                        if (state.autoplay) PreviewFooterTag("Autoplay")
                        if (state.pipSupported && state.pipEnabled) PreviewFooterTag("PiP")
                        if (state.gesturesEnabled) PreviewFooterTag("Gestures")
                    }
                }
            }

            OnboardingGlassCard {
                AppText(stringResource(R.string.onboarding_playback_quality), size = 15, line = 20, weight = FontWeight.SemiBold)
                AppText(stringResource(R.string.onboarding_playback_quality_summary), size = 12, line = 16, color = OnboardingEditorialColors.PearlMuted)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PlaybackDefaults.QUALITY_OPTIONS.forEach { quality ->
                        val label = quality ?: stringResource(R.string.settings_quality_auto)
                        SelectableChip(
                            label = label,
                            selected = state.defaultQuality == quality,
                            accentColor = accentColor,
                            onClick = { onSelectQuality(quality) },
                            tag = "quality-$label",
                        )
                    }
                }
            }

            OnboardingGlassCard {
                AppText(stringResource(R.string.onboarding_playback_speed), size = 15, line = 20, weight = FontWeight.SemiBold)
                AppText(stringResource(R.string.onboarding_playback_speed_summary), size = 12, line = 16, color = OnboardingEditorialColors.PearlMuted)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PlaybackDefaults.SPEED_OPTIONS.forEach { speed ->
                        val label = formatSpeed(speed)
                        SelectableChip(
                            label = label,
                            selected = kotlin.math.abs(state.defaultSpeed - speed) < 0.01f,
                            accentColor = accentColor,
                            onClick = { onSelectSpeed(speed) },
                            tag = "speed-$label",
                        )
                    }
                }
            }

            OnboardingToggleRow(
                title = stringResource(R.string.onboarding_playback_background),
                summary = stringResource(R.string.onboarding_playback_background_summary),
                checked = state.backgroundPlay,
                onCheckedChange = onToggleBackgroundPlay,
                icon = AppIcon.Play,
            )
            OnboardingToggleRow(
                title = stringResource(R.string.onboarding_playback_autoplay),
                summary = stringResource(R.string.onboarding_playback_autoplay_summary),
                checked = state.autoplay,
                onCheckedChange = onToggleAutoplay,
                icon = AppIcon.Queue,
            )
            if (state.pipSupported) {
                OnboardingToggleRow(
                    title = stringResource(R.string.onboarding_playback_pip),
                    summary = stringResource(R.string.onboarding_playback_pip_summary),
                    checked = state.pipEnabled,
                    onCheckedChange = onTogglePip,
                    icon = AppIcon.OpenWith,
                )
            }
            OnboardingToggleRow(
                title = stringResource(R.string.onboarding_playback_mini_player),
                summary = stringResource(R.string.onboarding_playback_mini_player_summary),
                checked = state.inAppMiniPlayer,
                onCheckedChange = onToggleMiniPlayer,
                icon = AppIcon.VideoOutline,
            )
            OnboardingToggleRow(
                title = stringResource(R.string.onboarding_playback_data_saver),
                summary = stringResource(R.string.onboarding_playback_data_saver_summary),
                checked = state.dataSaver,
                onCheckedChange = onToggleDataSaver,
                icon = AppIcon.Globe,
            )
            OnboardingToggleRow(
                title = stringResource(R.string.onboarding_playback_remember_position),
                summary = stringResource(R.string.onboarding_playback_remember_position_summary),
                checked = state.rememberLastPosition,
                onCheckedChange = onToggleRememberPosition,
                icon = AppIcon.Clock,
            )
            OnboardingToggleRow(
                title = stringResource(R.string.onboarding_playback_remember_resize),
                summary = stringResource(R.string.onboarding_playback_remember_resize_summary),
                checked = state.rememberResizeMode,
                onCheckedChange = onToggleRememberResize,
                icon = AppIcon.ChannelGrid,
            )
            OnboardingToggleRow(
                title = stringResource(R.string.onboarding_playback_gestures),
                summary = stringResource(R.string.onboarding_playback_gestures_summary),
                checked = state.gesturesEnabled,
                onCheckedChange = onToggleGestures,
                icon = AppIcon.Filter,
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun AppearanceStep(
    state: OnboardingUiState,
    accentColor: Color,
    onSelectTheme: (ThemeMode) -> Unit,
    onSelectAccent: (AppAccent) -> Unit,
    onToggleDynamicColors: (Boolean) -> Unit,
    onSelectGlassStyle: (LiquidGlassStyle) -> Unit,
    onContinue: () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val previewDark = when (state.themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> systemDark
    }
    val previewPalette = if (previewDark) DarkAppPalette else LightAppPalette
    val previewAccent = if (state.dynamicColors) {
        accentColor
    } else {
        if (previewDark) state.accentColor.darkColor else state.accentColor.lightColor
    }
    val glassAlpha = when (state.glassStyle) {
        LiquidGlassStyle.Clear -> if (previewDark) 0.42f else 0.56f
        LiquidGlassStyle.Standard -> if (previewDark) 0.62f else 0.78f
        LiquidGlassStyle.Frosted -> if (previewDark) 0.84f else 0.92f
    }

    val animBg by animateColorAsState(previewPalette.background, tween(220), label = "preview-bg")
    val animSurface by animateColorAsState(previewPalette.surface, tween(220), label = "preview-surface")
    val animInk by animateColorAsState(previewPalette.ink, tween(220), label = "preview-ink")
    val animAccent by animateColorAsState(previewAccent, tween(220), label = "preview-accent")

    OnboardingFrostedStepLayout(
        modifier = Modifier.testTag("onboarding-step-appearance"),
        footer = {
            OnboardingPrimaryPillButton(
                text = stringResource(if (state.returnToReviewAfterEdit) R.string.onboarding_done else R.string.onboarding_continue),
                onClick = onContinue,
                accentDot = accentColor,
                modifier = Modifier.testTag("onboarding-appearance-continue"),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OnboardingGlassCard {
                AppText(
                    text = stringResource(R.string.onboarding_appearance_preview_caption),
                    size = 11,
                    line = 14,
                    weight = FontWeight.SemiBold,
                    color = accentColor,
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(animBg)
                        .border(0.75.dp, previewPalette.border, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(animAccent))
                                AppText("U-Tube", size = 14, line = 18, weight = FontWeight.Bold, color = animInk)
                            }
                            Box(
                                Modifier
                                    .clip(CircleShape)
                                    .background(animSurface.copy(alpha = glassAlpha))
                                    .border(0.5.dp, previewPalette.border, CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                AppText("Search", size = 11, line = 14, color = previewPalette.secondary)
                            }
                        }

                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(animSurface),
                        ) {
                            Box(
                                Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 12.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(animAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                AppIcon(AppIcon.Play, color = animAccent, modifier = Modifier.size(15.dp))
                            }
                            Box(
                                Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth(0.58f)
                                    .height(3.dp)
                                    .background(animAccent),
                            )
                        }

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .background(animSurface.copy(alpha = glassAlpha))
                                .border(0.75.dp, previewPalette.border, CircleShape)
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AppIcon(AppIcon.Home, color = animAccent, modifier = Modifier.size(15.dp))
                            AppIcon(AppIcon.Shorts, color = previewPalette.secondary, modifier = Modifier.size(15.dp))
                            AppIcon(AppIcon.Subscriptions, color = previewPalette.secondary, modifier = Modifier.size(15.dp))
                            AppIcon(AppIcon.You, color = previewPalette.secondary, modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }

            OnboardingGlassCard {
                AppText(stringResource(R.string.onboarding_appearance_theme), size = 15, line = 20, weight = FontWeight.SemiBold)
                OnboardingSegmentedControl(
                    options = listOf(
                        ThemeMode.System to stringResource(R.string.settings_theme_system),
                        ThemeMode.Light to stringResource(R.string.settings_theme_light),
                        ThemeMode.Dark to stringResource(R.string.settings_theme_dark),
                    ),
                    selected = state.themeMode,
                    onSelect = onSelectTheme,
                    tagPrefix = "theme-option",
                )
            }

            OnboardingToggleRow(
                title = stringResource(R.string.onboarding_appearance_dynamic),
                summary = if (state.dynamicColorsSupported) {
                    stringResource(R.string.onboarding_appearance_dynamic_summary)
                } else {
                    stringResource(R.string.onboarding_appearance_dynamic_unsupported)
                },
                checked = state.dynamicColors,
                enabled = state.dynamicColorsSupported,
                onCheckedChange = onToggleDynamicColors,
                icon = AppIcon.Refresh,
            )

            OnboardingGlassCard {
                AppText(stringResource(R.string.onboarding_appearance_accent), size = 15, line = 20, weight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppAccent.entries.forEach { accent ->
                        val selected = !state.dynamicColors && state.accentColor == accent
                        val swatchScale by animateFloatAsState(
                            targetValue = if (selected) 1.12f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "swatch-scale-${accent.storageKey}",
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clickable(role = Role.RadioButton) { onSelectAccent(accent) }
                                .testTag("accent-swatch-${accent.storageKey}")
                                .padding(vertical = 4.dp),
                        ) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .graphicsLayer {
                                        scaleX = swatchScale
                                        scaleY = swatchScale
                                    }
                                    .clip(CircleShape)
                                    .background(accent.lightColor)
                                    .border(
                                        width = if (selected) 2.5.dp else 1.dp,
                                        color = if (selected) OnboardingEditorialColors.PearlInk else OnboardingEditorialColors.PearlBorder,
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (selected) {
                                    AppIcon(AppIcon.Check, color = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                            AppText(
                                text = accent.label,
                                size = 11,
                                line = 14,
                                weight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) OnboardingEditorialColors.PearlInk else OnboardingEditorialColors.PearlMuted,
                            )
                        }
                    }
                }
            }

            OnboardingGlassCard {
                AppText(stringResource(R.string.onboarding_appearance_glass), size = 15, line = 20, weight = FontWeight.SemiBold)
                OnboardingSegmentedControl(
                    options = LiquidGlassStyle.entries.map { it to it.label },
                    selected = state.glassStyle,
                    onSelect = onSelectGlassStyle,
                    tagPrefix = "glass-option",
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun AdditionalPreferencesStep(
    state: OnboardingUiState,
    accentColor: Color,
    onSelectLanguage: (String) -> Unit,
    onToggleSponsorBlock: (Boolean) -> Unit,
    onToggleDeArrow: (Boolean) -> Unit,
    onToggleHideShorts: (Boolean) -> Unit,
    onToggleShowDislikes: (Boolean) -> Unit,
    onToggleDownloadWifiOnly: (Boolean) -> Unit,
    onSelectDownloadQuality: (String) -> Unit,
    onSelectHapticStrength: (Int) -> Unit,
    onContinue: () -> Unit,
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    val languageName = AppLocale.OPTIONS.firstOrNull { it.first == state.languageTag }?.second
        ?: state.languageTag.ifBlank { stringResource(R.string.settings_theme_system) }

    OnboardingFrostedStepLayout(
        modifier = Modifier.testTag("onboarding-step-preferences"),
        footer = {
            OnboardingPrimaryPillButton(
                text = stringResource(if (state.returnToReviewAfterEdit) R.string.onboarding_done else R.string.onboarding_continue),
                onClick = onContinue,
                accentDot = accentColor,
                modifier = Modifier.testTag("onboarding-prefs-continue"),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OnboardingGlassCard(
                onClick = { showLanguageDialog = true },
                modifier = Modifier.testTag("onboarding-prefs-language"),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            Modifier.size(34.dp).clip(CircleShape).background(OnboardingEditorialColors.PearlSubtle),
                            contentAlignment = Alignment.Center,
                        ) {
                            AppIcon(AppIcon.Globe, color = OnboardingEditorialColors.PearlInk, modifier = Modifier.size(16.dp))
                        }
                        Column {
                            AppText(stringResource(R.string.onboarding_prefs_language), size = 15, line = 20, weight = FontWeight.Medium)
                            AppText(languageName, size = 12, line = 16, color = accentColor)
                        }
                    }
                    AppIcon(AppIcon.ChevronDown, color = OnboardingEditorialColors.PearlMuted, modifier = Modifier.size(18.dp))
                }
            }

            OnboardingToggleRow(
                title = stringResource(R.string.onboarding_prefs_sponsorblock),
                summary = stringResource(R.string.onboarding_prefs_sponsorblock_summary),
                checked = state.sponsorBlockEnabled,
                onCheckedChange = onToggleSponsorBlock,
                icon = AppIcon.Block,
            )
            OnboardingToggleRow(
                title = stringResource(R.string.onboarding_prefs_dearrow),
                summary = stringResource(R.string.onboarding_prefs_dearrow_summary),
                checked = state.deArrowTitles && state.deArrowThumbnails,
                onCheckedChange = onToggleDeArrow,
                icon = AppIcon.Info,
            )
            OnboardingToggleRow(
                title = stringResource(R.string.onboarding_prefs_hide_shorts),
                summary = stringResource(R.string.onboarding_prefs_hide_shorts_summary),
                checked = state.hideShorts,
                onCheckedChange = onToggleHideShorts,
                icon = AppIcon.Shorts,
            )
            OnboardingToggleRow(
                title = stringResource(R.string.onboarding_prefs_dislikes),
                summary = stringResource(R.string.onboarding_prefs_dislikes_summary),
                checked = state.showDislikes,
                onCheckedChange = onToggleShowDislikes,
                icon = AppIcon.Dislike,
            )
            OnboardingToggleRow(
                title = stringResource(R.string.onboarding_prefs_download_wifi),
                summary = stringResource(R.string.onboarding_prefs_download_wifi_summary),
                checked = state.downloadWifiOnly,
                onCheckedChange = onToggleDownloadWifiOnly,
                icon = AppIcon.Download,
            )

            OnboardingGlassCard {
                AppText(stringResource(R.string.onboarding_prefs_download_quality), size = 15, line = 20, weight = FontWeight.SemiBold)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OnboardingViewModel.DOWNLOAD_QUALITY_OPTIONS.forEach { quality ->
                        SelectableChip(
                            label = quality,
                            selected = state.downloadQuality == quality,
                            accentColor = accentColor,
                            onClick = { onSelectDownloadQuality(quality) },
                            tag = "dl-quality-$quality",
                        )
                    }
                }
            }

            OnboardingGlassCard {
                AppText(stringResource(R.string.onboarding_prefs_haptics), size = 15, line = 20, weight = FontWeight.SemiBold)
                val currentPreset = when {
                    state.hapticStrength <= 0 -> 0
                    state.hapticStrength <= 45 -> 30
                    else -> 75
                }
                OnboardingSegmentedControl(
                    options = listOf(
                        0 to stringResource(R.string.onboarding_prefs_haptics_off),
                        30 to stringResource(R.string.onboarding_prefs_haptics_subtle),
                        75 to stringResource(R.string.onboarding_prefs_haptics_crisp),
                    ),
                    selected = currentPreset,
                    onSelect = onSelectHapticStrength,
                    tagPrefix = "haptic-option",
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            selectedTag = state.languageTag,
            accentColor = accentColor,
            onSelect = { tag ->
                onSelectLanguage(tag)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false },
        )
    }
}

@Composable
private fun LanguagePickerDialog(
    selectedTag: String,
    accentColor: Color,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 460.dp)
                .testTag("onboarding-language-dialog"),
            shape = RoundedCornerShape(24.dp),
            cornerRadius = 24.dp,
            tier = LiquidGlassTier.Translucent,
            fillColor = OnboardingEditorialColors.PearlCard,
            contentPadding = PaddingValues(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppText(
                    text = stringResource(R.string.onboarding_prefs_language),
                    size = 18,
                    line = 22,
                    weight = FontWeight.SemiBold,
                    color = OnboardingEditorialColors.PearlInk,
                )
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    AppLocale.OPTIONS.forEach { (tag, label) ->
                        val active = tag == selectedTag
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (active) accentColor.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable(role = Role.RadioButton) { onSelect(tag) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            AppText(
                                text = label,
                                size = 14,
                                line = 18,
                                weight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (active) accentColor else OnboardingEditorialColors.PearlInk,
                            )
                            if (active) {
                                AppIcon(AppIcon.Check, color = accentColor, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewSetupStep(
    state: OnboardingUiState,
    accentColor: Color,
    onEdit: (OnboardingStep) -> Unit,
    onStartWatching: () -> Unit,
) {
    val languageLabel = AppLocale.OPTIONS.firstOrNull { it.first == state.languageTag }?.second
        ?: state.languageTag.ifBlank { stringResource(R.string.settings_theme_system) }
    val qualityLabel = state.defaultQuality ?: stringResource(R.string.settings_quality_auto)

    OnboardingFrostedStepLayout(
        modifier = Modifier.testTag("onboarding-step-review"),
        footer = {
            OnboardingPrimaryPillButton(
                text = stringResource(R.string.onboarding_start_watching),
                onClick = onStartWatching,
                leadingIcon = AppIcon.Play,
                accentDot = accentColor,
                modifier = Modifier.testTag("onboarding-start-watching"),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ReviewSummaryCard(
                title = stringResource(R.string.onboarding_review_account),
                summary = if (state.accountMode == OnboardingAccountMode.SignedIn) {
                    "${stringResource(R.string.onboarding_review_signed_in)} · ${state.signedInName ?: ""}".trimEnd(' ', '·')
                } else {
                    stringResource(R.string.onboarding_review_guest)
                },
                accentColor = accentColor,
                onEdit = { onEdit(OnboardingStep.Account) },
                tag = "review-edit-account",
            )

            ReviewSummaryCard(
                title = stringResource(R.string.onboarding_review_playback),
                summary = buildList {
                    add("$qualityLabel · ${formatSpeed(state.defaultSpeed)}")
                    if (state.backgroundPlay) add("Background")
                    if (state.autoplay) add("Autoplay")
                    if (state.pipSupported && state.pipEnabled) add("PiP")
                    if (state.dataSaver) add("Data Saver")
                }.joinToString(" • "),
                accentColor = accentColor,
                onEdit = { onEdit(OnboardingStep.Playback) },
                tag = "review-edit-playback",
            )

            ReviewSummaryCard(
                title = stringResource(R.string.onboarding_review_appearance),
                summary = buildList {
                    add(state.themeMode.label)
                    add(if (state.dynamicColors) "Dynamic" else state.accentColor.label)
                    add(state.glassStyle.label)
                }.joinToString(" • "),
                accentColor = accentColor,
                onEdit = { onEdit(OnboardingStep.Appearance) },
                tag = "review-edit-appearance",
            )

            ReviewSummaryCard(
                title = stringResource(R.string.onboarding_review_extras),
                summary = buildList {
                    add(languageLabel)
                    if (state.sponsorBlockEnabled) add("SponsorBlock")
                    if (state.deArrowTitles || state.deArrowThumbnails) add("DeArrow")
                    add("DL ${state.downloadQuality}${if (state.downloadWifiOnly) " (Wi-Fi)" else ""}")
                }.joinToString(" • "),
                accentColor = accentColor,
                onEdit = { onEdit(OnboardingStep.Preferences) },
                tag = "review-edit-preferences",
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ReviewSummaryCard(
    title: String,
    summary: String,
    accentColor: Color,
    onEdit: () -> Unit,
    tag: String,
) {
    OnboardingGlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                AppText(title, size = 15, line = 20, weight = FontWeight.SemiBold, color = OnboardingEditorialColors.PearlInk)
                AppText(summary, size = 13, line = 18, color = OnboardingEditorialColors.PearlMuted)
            }
            Box(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .clip(CircleShape)
                    .background(OnboardingEditorialColors.PearlSubtle)
                    .border(0.75.dp, OnboardingEditorialColors.PearlBorder, CircleShape)
                    .clickable(role = Role.Button, onClick = onEdit)
                    .testTag(tag)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                AppText(stringResource(R.string.onboarding_edit), size = 12, line = 16, weight = FontWeight.SemiBold, color = accentColor)
            }
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    tag: String,
) {
    val fill = if (selected) OnboardingEditorialColors.PearlInk else OnboardingEditorialColors.PearlSubtle
    val textColor = if (selected) Color.White else OnboardingEditorialColors.PearlInk
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(fill)
            .border(0.75.dp, if (selected) accentColor else OnboardingEditorialColors.PearlBorder, CircleShape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .testTag(tag)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppText(label, size = 13, line = 16, weight = if (selected) FontWeight.SemiBold else FontWeight.Medium, color = textColor)
    }
}

@Composable
private fun PreviewBadge(text: String, bg: Color) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 3.dp),
    ) {
        AppText(text, size = 11, line = 14, weight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
private fun PreviewFooterTag(text: String) {
    AppText(
        text = "• $text",
        size = 11,
        line = 14,
        weight = FontWeight.Medium,
        color = Color.White.copy(alpha = 0.78f),
    )
}

private fun formatSpeed(speed: Float): String =
    if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x"
