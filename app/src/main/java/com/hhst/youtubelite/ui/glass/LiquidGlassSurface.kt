package com.hhst.youtubelite.ui.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hhst.youtubelite.ui.design.appStateLayer
import com.hhst.youtubelite.ui.design.rememberAppInteractions
import com.hhst.youtubelite.ui.design.rememberHapticAction

/**
 * Reusable Liquid Glass container separating the API-tiered frosted backdrop from sharp foreground content.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LiquidGlassDefaults.CornerMedium),
    cornerRadius: Dp = LiquidGlassDefaults.CornerMedium,
    tier: LiquidGlassTier = LiquidGlassTier.Optical,
    fillColor: Color? = null,
    borderColor: Color? = null,
    contentColor: Color? = null,
    blurRadius: Dp = LiquidGlassDefaults.BlurRadius,
    borderWidth: Dp = LiquidGlassDefaults.BorderWidth,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentAlignment: Alignment = Alignment.TopStart,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = LocalLiquidGlassPalette.current
    val interactions = if (onClick != null) rememberAppInteractions() else null
    val haptic = if (onClick != null) rememberHapticAction() else null
    val resolvedFill = fillColor ?: when (tier) {
        LiquidGlassTier.Flat -> palette.recessedFill
        LiquidGlassTier.Translucent -> palette.frostFill
        LiquidGlassTier.Optical -> palette.elevatedFill
    }
    val resolvedContent = contentColor ?: palette.contentPrimary
    val interactiveModifier = if (onClick != null && interactions != null) {
        Modifier
            .liquidGlassPressScale(interactions)
            .appStateLayer(interactions, cornerRadius)
            .hoverable(interactions, enabled = enabled)
            .clickable(
                interactionSource = interactions,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = {
                    haptic?.invoke()
                    onClick()
                },
            )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(interactiveModifier),
        contentAlignment = contentAlignment,
    ) {
        Box(
            Modifier
                .matchParentSize()
                .liquidGlassBackdrop(
                    shape = shape,
                    cornerRadius = cornerRadius,
                    tier = tier,
                    fillColor = resolvedFill,
                    sheenStart = palette.sheenStart,
                    sheenEnd = palette.sheenEnd,
                    specularTop = palette.specularTop,
                    specularBottom = palette.specularBottom,
                    hairlineBorder = borderColor ?: palette.hairlineBorder,
                    dark = palette.dark,
                    blurRadius = blurRadius,
                    borderWidth = borderWidth,
                ),
        )
        Box(
            modifier = Modifier.padding(contentPadding),
            contentAlignment = contentAlignment,
        ) {
            CompositionLocalProvider(LocalContentColor provides resolvedContent) {
                content()
            }
        }
    }
}

/**
 * Lightweight Liquid Glass card defaulting to [LiquidGlassTier.Translucent] so scrollable
 * feeds and library shelves preserve smooth frame pacing without excessive shader passes.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = LiquidGlassDefaults.CornerMedium,
    tier: LiquidGlassTier = LiquidGlassTier.Translucent,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    LiquidGlassSurface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        cornerRadius = cornerRadius,
        tier = tier,
        contentPadding = contentPadding,
        enabled = enabled,
        onClick = onClick,
    ) {
        Column(content = content)
    }
}

