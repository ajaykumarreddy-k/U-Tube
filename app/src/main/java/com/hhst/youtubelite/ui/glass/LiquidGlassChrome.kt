package com.hhst.youtubelite.ui.glass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hhst.youtubelite.R
import com.hhst.youtubelite.core.HapticsController
import com.hhst.youtubelite.ui.design.AppIcon
import com.hhst.youtubelite.ui.design.AppMotion
import com.hhst.youtubelite.ui.design.AppText
import com.hhst.youtubelite.ui.design.LocalMotionScale
import com.hhst.youtubelite.ui.design.motionDuration
import com.hhst.youtubelite.ui.design.rememberHapticAction
import com.hhst.youtubelite.ui.glass.backdrop.backdrops.layerBackdrop
import com.hhst.youtubelite.ui.glass.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

data class LiquidGlassNavItem(
    val id: String,
    val label: String,
    val selected: Boolean,
    val testTag: String? = null,
    val icon: @Composable () -> Unit,
)

private val NAV_BAR_HEIGHT = 62.dp

/**
 * Floating Liquid Glass navigation dock matching Wavelet's iOS 26 layout:
 * a 3-tab glass pill (`Home | Shorts | You`) with an interactive hold-and-drag
 * lifted refractive lens, paired with an optional detached circular Search button.
 */
@Composable
fun LiquidGlassNavigationBar(
    items: List<LiquidGlassNavItem>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onSearch: (() -> Unit)? = null,
    searchTag: String = "nav-search",
) {
    if (items.isEmpty()) return
    val palette = LocalLiquidGlassPalette.current
    val motionScale = LocalMotionScale.current
    val haptic = rememberHapticAction(HapticsController.Event.TAB)
    val density = LocalDensity.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val selectedIndex = items.indexOfFirst { it.selected }.coerceAtLeast(0)
    val currentSelectedIndex by rememberUpdatedState(selectedIndex)
    val currentOnSelect by rememberUpdatedState(onSelect)

    val coroutineScope = rememberCoroutineScope()
    val pill = remember { GlassPillMotion(coroutineScope, selectedIndex) }
    pill.reduceMotion = motionScale <= 0f

    LaunchedEffect(selectedIndex) {
        if (motionScale <= 0f) pill.snapTo(selectedIndex) else pill.animateTo(selectedIndex)
    }

    val liveBackdrop = LocalLiquidGlassBackdrop.current
    val opticalLensSupported = liveBackdrop != null && isLiveOpticalBackdropSupported()
    val barGlass = if (opticalLensSupported) rememberLayerBackdrop() else null
    val tabRow = if (opticalLensSupported) rememberLayerBackdrop() else null

    val pillShape = remember { RoundedCornerShape(percent = 50) }
    val horizontalPad = 5.dp
    val verticalPad = 5.dp
    val padLeftPx = with(density) { horizontalPad.toPx() }
    val padTopPx = with(density) { verticalPad.toPx() }
    val padVerticalPx = with(density) { (verticalPad * 2).toPx() }
    var rowSize by remember { mutableStateOf(IntSize.Zero) }
    val tabWidthPx = if (rowSize.width > 0) rowSize.width.toFloat() / items.size else 0f
    val tabStepPx = tabWidthPx
    pill.stepDp = tabStepPx / density.density

    val rowHeightPx = rowSize.height.toFloat()
    val restSize = Size(tabWidthPx, rowHeightPx)
    val liftedHeightPx = rowHeightPx + padVerticalPx + with(density) { PILL_GROW_HEIGHT.toPx() }
    val liftedSize = if (rowHeightPx > 0f) {
        Size(liftedHeightPx * tabWidthPx / rowHeightPx, liftedHeightPx)
    } else {
        restSize
    }
    val pillCenterInRow: (Float) -> Offset = { pos ->
        val x = pos * tabStepPx + tabWidthPx / 2f
        Offset(if (isRtl) rowSize.width - x else x, rowHeightPx / 2f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Outer unclipped container lets the lifted lens grow beyond the pill's top/bottom edges.
        Box(modifier = Modifier.weight(1f)) {
            LiquidGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NAV_BAR_HEIGHT)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false).consume()
                            while (true) {
                                val event = awaitPointerEvent()
                                event.changes.forEach { it.consume() }
                                if (event.changes.none { it.pressed }) break
                            }
                        }
                    },
                shape = pillShape,
                cornerRadius = NAV_BAR_HEIGHT / 2,
                tier = LiquidGlassTier.Optical,
                contentPadding = PaddingValues(horizontal = horizontalPad, vertical = verticalPad),
                exportedBackdrop = barGlass,
            ) {
                Box(Modifier.fillMaxWidth().fillMaxHeight()) {
                    if (tabWidthPx > 0f && (!opticalLensSupported || pill.isFlat)) {
                        FlatSelectionPill(
                            color = palette.pillSelection,
                            borderColor = palette.hairlineBorder,
                            modifier = Modifier
                                .matchParentSize()
                                .pillPlacement(
                                    center = { pillCenterInRow(pill.position) },
                                    size = { pill.liveSize(restSize, liftedSize) },
                                ),
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .onSizeChanged { rowSize = it }
                            .then(if (tabRow != null) Modifier.layerBackdrop(tabRow) else Modifier)
                            .pointerInput(items.size, isRtl, tabStepPx) {
                                if (items.isEmpty() || tabStepPx <= 0f) return@pointerInput
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                    val touchSlop = viewConfiguration.touchSlop
                                    var active = false
                                    var lastHapticTab = currentSelectedIndex
                                    val touchedTab = ((if (isRtl) rowSize.width - down.position.x else down.position.x) / tabStepPx)
                                        .toInt().coerceIn(0, items.lastIndex).toFloat()
                                    var currentTabFloat = touchedTab

                                    val holdJob = coroutineScope.launch {
                                        delay(130L)
                                        if (!active) {
                                            active = true
                                            pill.startDrag()
                                            pill.dragTo(currentTabFloat)
                                            haptic()
                                            lastHapticTab = currentTabFloat.roundToInt()
                                        }
                                    }
                                    try {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                            if (!change.pressed) {
                                                if (active) change.consume()
                                                break
                                            }
                                            val dx = change.position.x - down.position.x
                                            val dy = change.position.y - down.position.y
                                            if (!active && abs(dx) > touchSlop && abs(dx) >= abs(dy)) {
                                                active = true
                                                holdJob.cancel()
                                                pill.startDrag()
                                                currentTabFloat = touchedTab
                                                pill.dragTo(currentTabFloat)
                                                haptic()
                                                lastHapticTab = currentTabFloat.roundToInt()
                                            } else if (!active && abs(dy) > touchSlop) {
                                                holdJob.cancel()
                                                break
                                            }
                                            if (active) {
                                                change.consume()
                                                val effectiveDx = if (isRtl) -dx else dx
                                                currentTabFloat = (touchedTab + effectiveDx / tabStepPx)
                                                    .coerceIn(0f, items.lastIndex.toFloat())
                                                pill.dragTo(currentTabFloat)
                                                val approxTab = currentTabFloat.roundToInt()
                                                if (approxTab != lastHapticTab) {
                                                    haptic()
                                                    lastHapticTab = approxTab
                                                }
                                            }
                                        }
                                    } finally {
                                        holdJob.cancel()
                                    }
                                    if (active) {
                                        val target = currentTabFloat.roundToInt().coerceIn(0, items.lastIndex)
                                        pill.release(target)
                                        if (target != currentSelectedIndex) {
                                            currentOnSelect(target)
                                        }
                                    }
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        items.forEachIndexed { index, item ->
                            val iconScale by animateFloatAsState(
                                targetValue = if (item.selected && motionScale > 0f) 1.07f else 1f,
                                animationSpec = if (motionScale > 0f) {
                                    spring(dampingRatio = 0.72f, stiffness = 320f)
                                } else {
                                    snap()
                                },
                                label = "nav-tab-scale",
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(pillShape)
                                    .then(if (item.testTag != null) Modifier.testTag(item.testTag) else Modifier)
                                    .selectable(selected = item.selected, role = Role.Tab) {
                                        haptic()
                                        onSelect(index)
                                    }
                                    .semantics { selected = item.selected },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Box(
                                    Modifier.graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    },
                                ) {
                                    item.icon()
                                }
                                Spacer(Modifier.height(2.dp))
                                AppText(
                                    text = item.label,
                                    size = 11,
                                    line = 14,
                                    weight = if (item.selected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (item.selected) palette.contentPrimary else palette.contentSecondary,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }

            if (opticalLensSupported && barGlass != null && tabRow != null && tabWidthPx > 0f && !pill.isFlat) {
                GlassSelectionPill(
                    motion = pill,
                    fillColor = palette.pillSelection,
                    pageBackdrop = liveBackdrop,
                    barGlass = barGlass,
                    tabRow = tabRow,
                    modifier = Modifier
                        .matchParentSize()
                        .pillPlacement(
                            center = { pillCenterInRow(pill.position) + Offset(padLeftPx, padTopPx) },
                            size = { pill.liveSize(restSize, liftedSize) },
                        ),
                )
            }
        }

        if (onSearch != null) {
            LiquidGlassSurface(
                modifier = Modifier
                    .size(NAV_BAR_HEIGHT)
                    .testTag(searchTag),
                shape = CircleShape,
                cornerRadius = NAV_BAR_HEIGHT / 2,
                tier = LiquidGlassTier.Optical,
                contentAlignment = Alignment.Center,
                onClick = onSearch,
            ) {
                AppIcon(
                    icon = AppIcon.Search,
                    color = palette.contentPrimary,
                )
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
