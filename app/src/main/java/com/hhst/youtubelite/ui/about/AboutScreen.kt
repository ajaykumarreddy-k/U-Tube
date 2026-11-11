package com.hhst.youtubelite.ui.about

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hhst.youtubelite.R
import com.hhst.youtubelite.ui.components.AppTopAppBar as TopAppBar
import com.hhst.youtubelite.ui.components.ConstrainedPage
import com.hhst.youtubelite.ui.design.AppIcon
import com.hhst.youtubelite.ui.design.DesignTokens
import com.hhst.youtubelite.ui.glass.LiquidGlassCard
import com.hhst.youtubelite.ui.glass.LiquidGlassIconButton
import com.hhst.youtubelite.ui.glass.LiquidGlassTier
import com.hhst.youtubelite.ui.glass.UTubeWordmark
import com.hhst.youtubelite.ui.theme.SettingsTokens
import com.hhst.youtubelite.ui.theme.settingsAppBarHeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(label: String, version: String, onClose: () -> Unit, busy: Boolean, status: String,
    onSource: () -> Unit, onUpdate: () -> Unit, onRelease: () -> Unit, onClear: () -> Unit, onExport: () -> Unit,
    onSave: () -> Unit = {}) {
    BackHandler { onClose() }
    val glass = DesignTokens.Glass
    Scaffold(containerColor = DesignTokens.Background, topBar = {
        TopAppBar(title = { Text(stringResource(R.string.about), style = MaterialTheme.typography.titleLarge) },
            expandedHeight = settingsAppBarHeight(),
            navigationIcon = {
                LiquidGlassIconButton(
                    icon = AppIcon.Back,
                    description = stringResource(R.string.navigate_back),
                    onClick = onClose,
                    diameter = 38.dp,
                    tier = LiquidGlassTier.Translucent,
                    modifier = Modifier.padding(start = 8.dp),
                )
            })
    }) { padding ->
        ConstrainedPage(Modifier.padding(padding), maxWidth = 720.dp) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(SettingsTokens.GroupGap)) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    tier = LiquidGlassTier.Translucent,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                ) {
                    UTubeWordmark(large = true)
                    Text(
                        text = if (label.equals("U Tube", ignoreCase = true)) version else "$label · $version",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = glass.contentSecondary,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    tier = LiquidGlassTier.Translucent,
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    AboutRow(stringResource(R.string.about_source), onClick = onSource)
                    HorizontalDivider(color = glass.hairlineBorder)
                    AboutRow(stringResource(R.string.about_check_update), !busy, onUpdate)
                    HorizontalDivider(color = glass.hairlineBorder)
                    AboutRow(stringResource(R.string.about_releases), onClick = onRelease)
                }
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    tier = LiquidGlassTier.Translucent,
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    AboutRow(stringResource(R.string.about_clear_cache), !busy, onClear)
                    HorizontalDivider(color = glass.hairlineBorder)
                    AboutRow(stringResource(R.string.about_export_logs), !busy, onExport)
                    HorizontalDivider(color = glass.hairlineBorder)
                    AboutRow(stringResource(R.string.about_save_logs), !busy, onSave)
                }
                if (busy) Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
                    com.hhst.youtubelite.home.presentation.LoadingSpinner()
                }
                if (status.isNotEmpty()) Text(status, style = MaterialTheme.typography.bodySmall,
                    color = glass.contentSecondary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun AboutRow(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 50.dp).clickable(enabled = enabled, onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium,
            color = DesignTokens.Glass.contentPrimary.copy(alpha = if (enabled) 1f else .38f))
    }
}
