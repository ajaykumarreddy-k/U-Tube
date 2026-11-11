package com.hhst.youtubelite.ui.glass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hhst.youtubelite.core.HapticsController
import com.hhst.youtubelite.ui.design.AppMotion
import com.hhst.youtubelite.ui.design.AppText
import com.hhst.youtubelite.ui.design.LocalMotionScale
import com.hhst.youtubelite.ui.design.motionDuration
import com.hhst.youtubelite.ui.design.rememberHapticAction

data class LiquidGlassNavItem(
    val id: String,
    val label: String,
    val selected: Boolean,
    val testTag: String? = null,
    val icon: @Composable () -> Unit,
)

/**
 * Floating Liquid Glass navigation dock with a spring-animated active lens indicator.
 * Preserves accessibility roles, selection semantics, and `testTag` hooks for existing tests.
 */
@Composable
fun LiquidGlassNavigationBar(
    items: List<LiquidGlassNavItem>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    val palette = LocalLiquidGlassPalette.current
    val motionScale = LocalMotionScale.current
    val haptic = rememberHapticAction(HapticsController.Event.TAB)
    val selectedIndex = items.indexOfFirst { it.selected }.coerceAtLeast(0)

    LiquidGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(LiquidGlassDefaults.PillHeight),
        shape = RoundedCornerShape(LiquidGlassDefaults.PillHeight / 2),
        cornerRadius = LiquidGlassDefaults.PillHeight / 2,
        tier = LiquidGlassTier.Optical,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().fillMaxHeight()) {
            val slotWidth = maxWidth / items.size
            val indicatorOffset by animateDpAsState(
                targetValue = slotWidth * selectedIndex,
                animationSpec = if (motionScale > 0f) {
                    spring(dampingRatio = 0.78f, stiffness = 480f)
                } else {
                    snap()
                },
                label = "glass-nav-pill",
            )
            Box(
                Modifier
                    .offset(x = indicatorOffset)
                    .width(slotWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(LiquidGlassDefaults.PillHeight / 2))
                    .background(palette.pillSelection),
            )

            Row(Modifier.fillMaxWidth().fillMaxHeight()) {
                items.forEachIndexed { index, item ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(LiquidGlassDefaults.PillHeight / 2))
                            .then(if (item.testTag != null) Modifier.testTag(item.testTag) else Modifier)
                            .selectable(selected = item.selected, role = Role.Tab) {
                                haptic()
                                onSelect(index)
                            }
                            .semantics { selected = item.selected },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        item.icon()
                        AppText(
                            text = item.label,
                            size = 11,
                            line = 14,
                            weight = if (item.selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (item.selected) palette.contentPrimary else palette.contentSecondary,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

/** Horizontal Liquid Glass pill tab bar for category chips, channel sections, or search filters. */
@Composable
fun LiquidGlassTabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    val palette = LocalLiquidGlassPalette.current
    val haptic = rememberHapticAction()
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingContent != null) {
            item(key = "leading-control") { leadingContent() }
        }
        itemsIndexed(tabs) { index, title ->
            val selected = index == selectedIndex
            LiquidGlassSurface(
                modifier = Modifier.selectable(selected = selected, role = Role.Tab) {
                    haptic()
                    onSelect(index)
                },
                shape = RoundedCornerShape(18.dp),
                cornerRadius = 18.dp,
                tier = if (selected) LiquidGlassTier.Translucent else LiquidGlassTier.Flat,
                fillColor = if (selected) palette.contentPrimary else palette.recessedFill,
                contentColor = if (selected) {
                    if (palette.dark) palette.onAccent else palette.elevatedFill
                } else {
                    palette.contentPrimary
                },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
                onClick = { onSelect(index) },
            ) {
                AppText(
                    text = title,
                    size = 13,
                    line = 18,
                    weight = FontWeight.Medium,
                    color = if (selected) {
                        if (palette.dark) palette.onAccent else palette.elevatedFill
                    } else {
                        palette.contentPrimary
                    },
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * Floating Liquid Glass overlay cluster tailored for Media3 video playback controls.
 * Designed to float cleanly above hardware video surfaces without CPU frame readbacks.
 */
@Composable
fun LiquidGlassPlayerControls(
    visible: Boolean,
    modifier: Modifier = Modifier,
    horizontalPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val motionScale = LocalMotionScale.current
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(motionDuration(AppMotion.ChromeInMs, motionScale))) +
            scaleIn(
                initialScale = if (motionScale > 0f) 0.96f else 1f,
                animationSpec = tween(motionDuration(AppMotion.ChromeInMs, motionScale)),
            ),
        exit = fadeOut(tween(motionDuration(AppMotion.ChromeOutMs, motionScale))) +
            scaleOut(
                targetScale = if (motionScale > 0f) 0.97f else 1f,
                animationSpec = tween(motionDuration(AppMotion.ChromeOutMs, motionScale)),
            ),
    ) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(24.dp),
            cornerRadius = 24.dp,
            tier = LiquidGlassTier.Optical,
            contentPadding = horizontalPadding,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = content,
            )
        }
    }
}
