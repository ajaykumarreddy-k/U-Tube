package com.hhst.youtubelite.ui.glass

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hhst.youtubelite.ui.design.AppIcon
import com.hhst.youtubelite.ui.design.AppText
import com.hhst.youtubelite.ui.design.DesignTokens
import com.hhst.youtubelite.ui.design.LocalMotionScale
import com.hhst.youtubelite.ui.design.rememberHapticAction

/**
 * Floating Liquid Glass search capsule supporting either an interactive text query input
 * (`onQueryChange != null`) or a read-only trigger pill (`onBarClick != null`).
 */
@Composable
fun LiquidGlassSearchBar(
    query: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    onQueryChange: ((String) -> Unit)? = null,
    onSubmit: ((String) -> Unit)? = null,
    onLeadingClick: (() -> Unit)? = null,
    leadingIcon: AppIcon = AppIcon.Search,
    leadingDescription: String = placeholder,
    onClearClick: (() -> Unit)? = null,
    clearDescription: String = "",
    onVoiceClick: (() -> Unit)? = null,
    voiceDescription: String = "",
    onBarClick: (() -> Unit)? = null,
) {
    val palette = LocalLiquidGlassPalette.current
    LiquidGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(LiquidGlassDefaults.SearchBarHeight),
        shape = RoundedCornerShape(LiquidGlassDefaults.SearchBarHeight / 2),
        cornerRadius = LiquidGlassDefaults.SearchBarHeight / 2,
        tier = LiquidGlassTier.Optical,
        contentAlignment = Alignment.CenterStart,
        onClick = onBarClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (onLeadingClick != null) {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onLeadingClick)
                        .semantics { contentDescription = leadingDescription },
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(leadingIcon, color = palette.contentSecondary)
                }
            } else {
                AppIcon(leadingIcon, modifier = Modifier.padding(start = 6.dp), color = palette.contentSecondary)
            }

            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (onQueryChange != null) {
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = DesignTokens.text(size = 15, line = 20, color = palette.contentPrimary),
                        cursorBrush = SolidColor(palette.accent),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSubmit?.invoke(query) }),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (query.isEmpty()) {
                                AppText(placeholder, size = 15, line = 20, color = palette.contentSecondary, maxLines = 1)
                            }
                            innerTextField()
                        },
                    )
                } else {
                    AppText(
                        text = if (query.isNotEmpty()) query else placeholder,
                        size = 15,
                        line = 20,
                        color = if (query.isNotEmpty()) palette.contentPrimary else palette.contentSecondary,
                        maxLines = 1,
                    )
                }
            }

            if (query.isNotEmpty() && onClearClick != null) {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onClearClick)
                        .semantics { contentDescription = clearDescription },
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(AppIcon.Close, color = palette.contentSecondary)
                }
            }

            if (onVoiceClick != null) {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onVoiceClick)
                        .semantics { contentDescription = voiceDescription },
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(AppIcon.Voice, color = palette.contentPrimary)
                }
            }
        }
    }
}

/** Frosted capsule slider preserving Material Slider accessibility, RTL handling, and tactile thumb feedback. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidGlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors? = null,
) {
    val palette = LocalLiquidGlassPalette.current
    val resolvedColors = colors ?: SliderDefaults.colors(
        thumbColor = palette.sliderThumb,
        activeTrackColor = palette.sliderActive,
        inactiveTrackColor = palette.sliderInactive,
    )
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val motionScale = LocalMotionScale.current
    val opacity = if (enabled) 1f else 0.38f
    val haptic = rememberHapticAction()
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    var changed by remember { mutableStateOf(false) }

    val thumbWidth by animateDpAsState(
        targetValue = if (pressed) 22.dp else 18.dp,
        animationSpec = if (motionScale > 0f) spring(dampingRatio = 0.7f, stiffness = 550f) else snap(),
        label = "glass-slider-thumb",
    )

    Slider(
        value = value,
        onValueChange = {
            changed = true
            onValueChange(it)
        },
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        interactionSource = interactions,
        onValueChangeFinished = {
            if (changed) haptic()
            changed = false
            onValueChangeFinished?.invoke()
        },
        colors = resolvedColors,
        thumb = {
            Canvas(Modifier.size(width = thumbWidth, height = 18.dp)) {
                drawRoundRect(
                    color = resolvedColors.thumbColor.copy(alpha = opacity),
                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                )
                drawRoundRect(
                    color = palette.hairlineBorder,
                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
                )
            }
        },
        track = { state ->
            Canvas(Modifier.fillMaxWidth().height(6.dp)) {
                val span = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.001f)
                val fraction = ((state.value - valueRange.start) / span).coerceIn(0f, 1f)
                val radius = CornerRadius(size.height / 2f, size.height / 2f)
                drawRoundRect(
                    color = resolvedColors.inactiveTrackColor.copy(alpha = opacity),
                    cornerRadius = radius,
                )
                if (fraction > 0f) {
                    val activeWidth = size.width * fraction
                    val startX = if (rtl) size.width - activeWidth else 0f
                    drawRoundRect(
                        color = resolvedColors.activeTrackColor.copy(alpha = opacity),
                        topLeft = Offset(startX, 0f),
                        size = Size(activeWidth, size.height),
                        cornerRadius = radius,
                    )
                }
            }
        },
    )
}
