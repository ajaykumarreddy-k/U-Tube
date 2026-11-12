package com.hhst.youtubelite.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hhst.youtubelite.R
import com.hhst.youtubelite.core.AppLocale
import com.hhst.youtubelite.extension.PreferenceKeys
import com.hhst.youtubelite.settings.domain.SettingsPage
import com.hhst.youtubelite.ui.design.AppIcon
import com.hhst.youtubelite.ui.design.AppText
import com.hhst.youtubelite.ui.design.DesignTokens
import com.hhst.youtubelite.ui.glass.LiquidGlassIconButton
import com.hhst.youtubelite.ui.glass.LiquidGlassTier
import com.hhst.youtubelite.ui.theme.AppAppearance
import android.os.Build
import com.hhst.youtubelite.player.PlaybackDefaults
import com.hhst.youtubelite.ui.theme.AppAccent
import com.hhst.youtubelite.ui.theme.LiquidGlassStyle
import com.hhst.youtubelite.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    page: SettingsPage,
    preferences: Map<String, Boolean>,
    notificationsEnabled: Boolean,
    onNavigate: (SettingsPage) -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onAbout: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    defaultQuality: String? = null,
    defaultSpeed: Float = 1f,
    onAdvanced: (() -> Unit)? = null,
    onSelectTheme: (ThemeMode) -> Unit = {},
    onSelectAccent: (AppAccent) -> Unit = {},
    onSelectGlassStyle: (LiquidGlassStyle) -> Unit = {},
    onSelectDefaultQuality: (String?) -> Unit = {},
    onSelectDefaultSpeed: (Float) -> Unit = {},
    onReplayOnboarding: (() -> Unit)? = null,
    onSelectLanguage: (String) -> Unit = {},
) {
    Column(modifier.fillMaxSize().background(DesignTokens.Background).testTag("settings-screen")) {
        SettingsTopBar(stringResource(when (page) {
            SettingsPage.Home -> R.string.settings_title
            SettingsPage.General -> R.string.settings_general
            SettingsPage.Playback -> R.string.settings_playback
            SettingsPage.Notifications -> R.string.settings_notifications
        }), onBack)
        Box(Modifier.fillMaxWidth().weight(1f)) {
            Column(Modifier.align(Alignment.TopCenter).widthIn(max = 720.dp).fillMaxWidth().fillMaxHeight()
                .verticalScroll(rememberScrollState()).testTag("settings-page-${page.argument}")) {
                when (page) {
                    SettingsPage.Home -> SettingsHome(onNavigate, onAbout, onAdvanced, onReplayOnboarding)
                    SettingsPage.General -> GeneralSettings(preferences, onToggle, onSelectTheme, onSelectAccent, onSelectGlassStyle, onSelectLanguage)
                    SettingsPage.Playback -> PlaybackSettings(preferences, defaultQuality, defaultSpeed, onSelectDefaultQuality, onSelectDefaultSpeed, onToggle)
                    SettingsPage.Notifications -> NotificationSettings(notificationsEnabled, onOpenNotificationSettings)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private val SettingsPage.title: String
    @Composable get() = when (this) {
        SettingsPage.Home -> stringResource(R.string.settings_title)
        SettingsPage.General -> stringResource(R.string.settings_general)
        SettingsPage.Playback -> stringResource(R.string.settings_playback)
        SettingsPage.Notifications -> stringResource(R.string.settings_notifications)
    }

@Composable
private fun SettingsTopBar(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(DesignTokens.TopBarHeight + 8.dp).padding(horizontal = 12.dp).testTag("settings-topbar"),
        verticalAlignment = Alignment.CenterVertically) {
        LiquidGlassIconButton(AppIcon.Back, stringResource(R.string.a11y_back), onClick = onBack,
            diameter = 38.dp, tier = LiquidGlassTier.Translucent)
        Spacer(Modifier.width(12.dp))
        AppText(title, Modifier.padding(end = 16.dp), size = 20, line = 28, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun SettingsHome(
    onNavigate: (SettingsPage) -> Unit,
    onAbout: () -> Unit,
    onAdvanced: (() -> Unit)?,
    onReplayOnboarding: (() -> Unit)? = null,
) {
    SettingsGroup(stringResource(R.string.settings_group_preferences))
    SettingsRow(stringResource(R.string.settings_general), stringResource(R.string.settings_general_summary), tag = "settings-row-general") {
        onNavigate(SettingsPage.General)
    }
    SettingsRow(stringResource(R.string.settings_playback), stringResource(R.string.settings_playback_summary), tag = "settings-row-playback") {
        onNavigate(SettingsPage.Playback)
    }
    SettingsRow(stringResource(R.string.settings_notifications), stringResource(R.string.settings_notifications_summary), tag = "settings-row-notifications") {
        onNavigate(SettingsPage.Notifications)
    }
    if (onAdvanced != null) SettingsRow(stringResource(R.string.settings_advanced),
        stringResource(R.string.settings_advanced_summary), tag = "settings-row-advanced", onClick = onAdvanced)
    if (onReplayOnboarding != null) SettingsRow(
        stringResource(R.string.settings_replay_onboarding),
        stringResource(R.string.settings_replay_onboarding_summary),
        tag = "settings-row-onboarding",
        onClick = onReplayOnboarding,
    )
    SettingsGroup(stringResource(R.string.settings_group_about))
    SettingsRow(stringResource(R.string.settings_about_title), stringResource(R.string.settings_about_summary),
        tag = "settings-row-about", onClick = onAbout)
}

@Composable
private fun GeneralSettings(
    preferences: Map<String, Boolean>,
    onToggle: (String, Boolean) -> Unit,
    onSelectTheme: (ThemeMode) -> Unit,
    onSelectAccent: (AppAccent) -> Unit,
    onSelectGlassStyle: (LiquidGlassStyle) -> Unit,
    onSelectLanguage: (String) -> Unit,
) {
    SettingsGroup(stringResource(R.string.settings_group_appearance))
    var themeDialog by remember { mutableStateOf(false) }
    var accentDialog by remember { mutableStateOf(false) }
    var glassDialog by remember { mutableStateOf(false) }
    var languageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    SettingsRow(stringResource(R.string.settings_theme),
        value = stringResource(when (AppAppearance.mode) {
            ThemeMode.System -> R.string.settings_theme_system
            ThemeMode.Light -> R.string.settings_theme_light
            ThemeMode.Dark -> R.string.settings_theme_dark
        }), tag = "settings-row-theme", onClick = { themeDialog = true })
    SettingsRow(stringResource(R.string.settings_accent_color),
        value = AppAppearance.accent.label,
        tag = "settings-row-accent", onClick = { accentDialog = true })
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        SettingsToggle(
            stringResource(R.string.settings_dynamic_colors),
            stringResource(R.string.settings_dynamic_colors_summary),
            PreferenceKeys.DYNAMIC_COLORS, preferences, onToggle,
        )
    }
    SettingsRow(stringResource(R.string.settings_glass_style),
        value = AppAppearance.glassStyle.label,
        tag = "settings-row-glass", onClick = { glassDialog = true })
    SettingsRow(stringResource(R.string.settings_language),
        value = AppLocale.OPTIONS.firstOrNull { it.first == AppLocale.tag(context) }?.second
            ?: java.util.Locale.forLanguageTag(AppLocale.tag(context)).displayName,
        tag = "settings-row-language", onClick = { languageDialog = true })
    SettingsGroup(stringResource(R.string.settings_group_feed))
    SettingsToggle(stringResource(R.string.settings_hide_shorts), stringResource(R.string.settings_hide_shorts_summary),
        PreferenceKeys.ENABLE_HIDE_SHORTS, preferences, onToggle)
    SettingsGroup(stringResource(R.string.settings_group_privacy))
    SettingsToggle(stringResource(R.string.settings_private_browsing), stringResource(R.string.settings_private_browsing_summary),
        PreferenceKeys.PRIVATE_BROWSING, preferences, onToggle)
    SettingsToggle(stringResource(R.string.settings_data_saver), stringResource(R.string.settings_data_saver_summary),
        PreferenceKeys.DATA_SAVER, preferences, onToggle)
    if (themeDialog) RadioDialog(
        title = stringResource(R.string.settings_theme),
        options = ThemeMode.entries.map { mode ->
            stringResource(when (mode) {
                ThemeMode.System -> R.string.settings_theme_system
                ThemeMode.Light -> R.string.settings_theme_light
                ThemeMode.Dark -> R.string.settings_theme_dark
            }) to (mode == AppAppearance.mode)
        },
        tag = "theme-dialog",
        onSelect = { index -> onSelectTheme(ThemeMode.entries[index]); themeDialog = false },
        onDismiss = { themeDialog = false })
    if (accentDialog) RadioDialog(
        title = stringResource(R.string.settings_accent_color),
        options = AppAccent.entries.map { it.label to (it == AppAppearance.accent) },
        tag = "accent-dialog",
        onSelect = { index -> onSelectAccent(AppAccent.entries[index]); accentDialog = false },
        onDismiss = { accentDialog = false })
    if (glassDialog) RadioDialog(
        title = stringResource(R.string.settings_glass_style),
        options = LiquidGlassStyle.entries.map { it.label to (it == AppAppearance.glassStyle) },
        tag = "glass-dialog",
        onSelect = { index -> onSelectGlassStyle(LiquidGlassStyle.entries[index]); glassDialog = false },
        onDismiss = { glassDialog = false })
    if (languageDialog) RadioDialog(
        title = stringResource(R.string.settings_language),
        options = AppLocale.OPTIONS.map { (tag, label) ->
            label to (tag == AppLocale.tag(context))
        },
        tag = "language-dialog",
        onSelect = { index -> onSelectLanguage(AppLocale.OPTIONS[index].first); languageDialog = false },
        onDismiss = { languageDialog = false })
}

@Composable
private fun PlaybackSettings(
    preferences: Map<String, Boolean>,
    defaultQuality: String?,
    defaultSpeed: Float,
    onSelectDefaultQuality: (String?) -> Unit,
    onSelectDefaultSpeed: (Float) -> Unit,
    onToggle: (String, Boolean) -> Unit,
) {
    SettingsGroup(stringResource(R.string.settings_playback))
    var qualityDialog by remember { mutableStateOf(false) }
    var speedDialog by remember { mutableStateOf(false) }
    SettingsRow(stringResource(R.string.settings_default_quality),
        value = defaultQuality ?: stringResource(R.string.settings_quality_auto),
        tag = "settings-row-default-quality", onClick = { qualityDialog = true })
    SettingsRow(stringResource(R.string.settings_default_speed),
        value = "${if (defaultSpeed % 1f == 0f) defaultSpeed.toInt().toString() else defaultSpeed.toString()}x",
        tag = "settings-row-default-speed", onClick = { speedDialog = true })
    SettingsToggle(stringResource(R.string.settings_background_play), stringResource(R.string.settings_background_play_summary),
        PreferenceKeys.ENABLE_BACKGROUND_PLAY, preferences, onToggle)
    SettingsToggle(stringResource(R.string.settings_pip), stringResource(R.string.settings_pip_summary),
        PreferenceKeys.ENABLE_PIP, preferences, onToggle)
    SettingsToggle(stringResource(R.string.settings_remember_position), stringResource(R.string.settings_remember_position_summary),
        PreferenceKeys.REMEMBER_LAST_POSITION, preferences, onToggle)
    if (qualityDialog) RadioDialog(
        title = stringResource(R.string.settings_default_quality),
        options = PlaybackDefaults.QUALITY_OPTIONS.map { opt ->
            (opt ?: stringResource(R.string.settings_quality_auto)) to (opt == defaultQuality)
        },
        tag = "quality-dialog",
        onSelect = { index -> onSelectDefaultQuality(PlaybackDefaults.QUALITY_OPTIONS[index]); qualityDialog = false },
        onDismiss = { qualityDialog = false })
    if (speedDialog) RadioDialog(
        title = stringResource(R.string.settings_default_speed),
        options = PlaybackDefaults.SPEED_OPTIONS.map { speed ->
            "${if (speed % 1f == 0f) speed.toInt().toString() else speed.toString()}x" to (kotlin.math.abs(speed - defaultSpeed) < 0.01f)
        },
        tag = "speed-dialog",
        onSelect = { index -> onSelectDefaultSpeed(PlaybackDefaults.SPEED_OPTIONS[index]); speedDialog = false },
        onDismiss = { speedDialog = false })
}

@Composable
private fun NotificationSettings(notificationsEnabled: Boolean, onOpen: () -> Unit) {
    SettingsGroup(stringResource(R.string.settings_group_system))
    SettingsRow(stringResource(R.string.settings_notifications),
        value = stringResource(if (notificationsEnabled) R.string.settings_notifications_state_on
        else R.string.settings_notifications_state_off),
        tag = "settings-notifications-state")
    SettingsRow(stringResource(R.string.settings_open_system_settings), stringResource(R.string.settings_open_system_settings_summary),
        tag = "settings-open-notification-settings", onClick = onOpen)
}

@Composable
internal fun RadioDialog(title: String, options: List<Pair<String, Boolean>>, tag: String,
                        onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    val haptic = com.hhst.youtubelite.ui.design.rememberHapticAction()
    com.hhst.youtubelite.ui.components.AppAlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(title, size = 16, line = 22, weight = FontWeight.Medium) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).testTag(tag)) {
                options.forEachIndexed { index, (label, selected) ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 44.dp)
                        .selectable(selected = selected, role = Role.RadioButton, onClick = { if (!selected) haptic(); onSelect(index) })
                        .testTag("$tag-option-$index").padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        AppText(label, Modifier.weight(1f), size = 14, line = 20)
                        if (selected) AppIcon(AppIcon.Check, color = com.hhst.youtubelite.ui.theme.AppStyle.Action,
                            modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            com.hhst.youtubelite.ui.components.AppTextButton(onClick = onDismiss) {
                AppText(stringResource(R.string.cancel))
            }
        })
}
