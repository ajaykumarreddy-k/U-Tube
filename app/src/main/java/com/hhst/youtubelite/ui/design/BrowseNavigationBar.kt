package com.hhst.youtubelite.ui.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hhst.youtubelite.ui.glass.LiquidGlassNavItem
import com.hhst.youtubelite.ui.glass.LiquidGlassNavigationBar

enum class BrowseDestination(val label: String) { Home("Home"), Shorts("Shorts"), Subscriptions("Subscriptions"), You("You") }

@Composable
fun BrowseNavigationBar(tag: String, signedIn: Boolean = false, onSelect: (BrowseDestination) -> Unit) {
    val destinations = com.hhst.youtubelite.ui.components.MainTabs
        .tabs(signedIn, LocalHideShorts.current)
        .map { BrowseDestination.valueOf(it.name) }
    val glassPalette = DesignTokens.Glass
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("$tag-navigation"),
    ) {
        LiquidGlassNavigationBar(
            items = destinations.map { destination ->
                LiquidGlassNavItem(
                    id = destination.name,
                    label = stringResource(destination.labelResource),
                    selected = false,
                    testTag = "$tag-nav-${destination.name}",
                ) {
                    AppIcon(
                        icon = when (destination) {
                            BrowseDestination.Home -> AppIcon.HomeOutline
                            BrowseDestination.Shorts -> AppIcon.Shorts
                            BrowseDestination.Subscriptions -> AppIcon.Subscriptions
                            BrowseDestination.You -> AppIcon.You
                        },
                        color = glassPalette.contentSecondary,
                    )
                }
            },
            onSelect = { index -> onSelect(destinations[index]) },
        )
    }
}
