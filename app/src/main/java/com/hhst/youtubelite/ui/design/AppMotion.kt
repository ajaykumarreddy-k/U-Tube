package com.hhst.youtubelite.ui.design

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import kotlin.math.roundToInt
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/** Spatial changes share Apple-inspired fluid spring curves for silky 120Hz deceleration. */
object AppMotion {
    const val WindowMs = 300
    const val ContentMs = 190
    const val ExpandMs = 240
    const val CollapseMs = 190
    const val ChromeInMs = 170
    const val ChromeOutMs = 140
    val Spatial = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    fun <T> appleSpring(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.84f, stiffness = 360f)
    // Retain velocity when scroll direction changes, with soft Apple deceleration.
    fun scrollResponse(scale: Float): FiniteAnimationSpec<Float> = if (scale <= 0f) tween(0)
        else spring(dampingRatio = 0.88f, stiffness = 420f, visibilityThreshold = 0.01f)
}

/** A panel owns its exit, including Back during entry. The host removes it only after it lands. */
@Composable
fun PanelTransition(
    fromBottom: Boolean,
    onDismiss: () -> Unit,
    backEnabled: Boolean = true,
    scrim: @Composable (Modifier, () -> Unit) -> Unit = { _, _ -> },
    content: @Composable (Modifier, () -> Unit) -> Unit,
) {
    val scale = LocalMotionScale.current
    val progress = remember { Animatable(if (scale <= 0f) 1f else 0f) }
    var closing by remember { mutableStateOf(false) }
    val dismiss by rememberUpdatedState(onDismiss)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val close = { closing = true }
    LaunchedEffect(closing, scale) {
        if (scale <= 0f) {
            progress.snapTo(if (closing) 0f else 1f)
            if (closing) dismiss()
            return@LaunchedEffect
        }
        if (closing) {
            val duration = (190 * progress.value).roundToInt().coerceAtLeast(1)
            progress.animateTo(0f, tween(motionDuration(duration, scale), easing = FastOutLinearInEasing))
            dismiss()
        } else {
            progress.animateTo(1f, spring(dampingRatio = 0.88f, stiffness = 420f))
        }
    }
    BackHandler(enabled = backEnabled || closing, onBack = close)
    scrim(Modifier.graphicsLayer { alpha = progress.value }, close)
    content(Modifier.graphicsLayer {
        if (fromBottom) translationY = (1f - progress.value) * size.height
        else translationX = (1f - progress.value) * size.width * if (rtl) -1f else 1f
    }, close)
}
