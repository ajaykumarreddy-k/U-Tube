package com.hhst.youtubelite.ui.glass

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hhst.youtubelite.ui.design.DesignTokens
import com.hhst.youtubelite.ui.design.appStateLayer
import com.hhst.youtubelite.ui.design.rememberAppInteractions
import com.hhst.youtubelite.ui.design.rememberHapticAction
import com.hhst.youtubelite.ui.glass.backdrop.Backdrop
import com.hhst.youtubelite.ui.glass.backdrop.backdrops.LayerBackdrop
import com.hhst.youtubelite.ui.glass.backdrop.backdrops.layerBackdrop
import com.hhst.youtubelite.ui.glass.backdrop.backdrops.rememberLayerBackdrop
import com.hhst.youtubelite.ui.glass.backdrop.drawBackdrop
import com.hhst.youtubelite.ui.glass.backdrop.effects.blur
import com.hhst.youtubelite.ui.glass.backdrop.effects.colorControls
import com.hhst.youtubelite.ui.glass.backdrop.effects.lens
import com.hhst.youtubelite.ui.glass.backdrop.highlight.Highlight
import com.hhst.youtubelite.ui.glass.backdrop.isRenderEffectSupported
import com.hhst.youtubelite.ui.glass.backdrop.isRuntimeShaderSupported
import com.hhst.youtubelite.ui.glass.backdrop.shadow.Shadow

private const val WAVELET_BACKDROP_SCALE = 0.33f
private const val WAVELET_BLUR_DP = 8f
private const val WAVELET_LENS_HEIGHT_DP = 22f
private const val WAVELET_LENS_AMOUNT_DP = 24f

/** Backdrop layer sampled by floating optical [LiquidGlassSurface] overlays. */
val LocalLiquidGlassBackdrop = compositionLocalOf<Backdrop?> { null }

internal fun isLiveOpticalBackdropSupported(): Boolean =
    isRenderEffectSupported() && !"robolectric".equals(Build.FINGERPRINT, ignoreCase = true)

/**
 * Records opaque screen content (floor + scrolling hierarchy) into a reusable [LayerBackdrop]
 * so floating chrome can blur, saturate, and optically refract scrolling pixels beneath it.
 */
@Composable
fun rememberUTubeBackdrop(backgroundColor: Color = DesignTokens.Background): LayerBackdrop? {
    if (!isLiveOpticalBackdropSupported()) return null
    val paintBackdrop: ContentDrawScope.() -> Unit = remember(backgroundColor) {
        {
            drawRect(backgroundColor)
            drawContent()
        }
    }
    return rememberLayerBackdrop(onDraw = paintBackdrop)
}

/** Attaches a [LayerBackdrop] recorder when optical backdrop capture is active. */
fun Modifier.utubeBackdropSource(backdrop: LayerBackdrop?): Modifier =
    if (backdrop != null) layerBackdrop(backdrop) else this

private fun Modifier.specularEdgeSheen(
    cornerRadius: Dp,
    borderWidth: Dp,
    specularTop: Color,
    specularBottom: Color,
    hairlineBorder: Color,
): Modifier = drawWithCache {
    val radiusPx = cornerRadius.toPx().coerceAtMost(size.minDimension * 0.5f)
    val strokePx = borderWidth.toPx().coerceAtLeast(1f)
    val rimBrush = Brush.linearGradient(
        colors = listOf(specularTop.copy(alpha = specularTop.alpha * 0.55f), hairlineBorder, specularBottom),
        start = Offset.Zero,
        end = Offset(size.width * 0.5f, size.height),
    )
    val topHighlightBrush = Brush.horizontalGradient(
        0f to Color.Transparent,
        0.5f to specularTop,
        1f to Color.Transparent,
        startX = 0f,
        endX = size.width.coerceAtLeast(1f),
    )
    val leftHighlightBrush = Brush.verticalGradient(
        0f to specularTop,
        0.5f to Color.Transparent,
        1f to specularBottom,
        startY = 0f,
        endY = size.height.coerceAtLeast(1f),
    )
    val ringPath = Path.combine(
        PathOperation.Difference,
        Path().apply {
            addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(radiusPx, radiusPx)))
        },
        Path().apply {
            val innerRadius = (radiusPx - strokePx).coerceAtLeast(0f)
            addRoundRect(
                RoundRect(
                    left = strokePx,
                    top = strokePx,
                    right = (size.width - strokePx).coerceAtLeast(strokePx),
                    bottom = (size.height - strokePx).coerceAtLeast(strokePx),
                    cornerRadius = CornerRadius(innerRadius, innerRadius),
                ),
            )
        },
    )
    onDrawBehind {
        drawPath(path = ringPath, brush = rimBrush)
        clipRect(bottom = (strokePx + radiusPx * 0.35f).coerceAtMost(size.height * 0.5f)) {
            drawPath(path = ringPath, brush = topHighlightBrush)
        }
        clipRect(right = (strokePx + radiusPx * 0.35f).coerceAtMost(size.width * 0.5f)) {
            drawPath(path = ringPath, brush = leftHighlightBrush)
        }
    }
}

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
    exportedBackdrop: LayerBackdrop? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = LocalLiquidGlassPalette.current
    val liveBackdrop = LocalLiquidGlassBackdrop.current
    val useWaveletOpticalBackdrop = tier == LiquidGlassTier.Optical &&
        liveBackdrop != null &&
        shape is CornerBasedShape &&
        isLiveOpticalBackdropSupported()
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

    val elevationModifier = if (tier == LiquidGlassTier.Optical && !useWaveletOpticalBackdrop) {
        val shadowAlpha = if (palette.dark) 0.28f else 0.12f
        Modifier.shadow(
            elevation = 10.dp,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = shadowAlpha),
            spotColor = Color.Black.copy(alpha = shadowAlpha),
        )
    } else {
        Modifier
    }

    val density = LocalDensity.current
    val opticalGlassModifier = if (useWaveletOpticalBackdrop) {
        val blurPx = with(density) { minOf(blurRadius.value, WAVELET_BLUR_DP).dp.toPx() } * WAVELET_BACKDROP_SCALE
        val lensHeightPx = with(density) { WAVELET_LENS_HEIGHT_DP.dp.toPx() } * WAVELET_BACKDROP_SCALE
        val lensAmountPx = with(density) { WAVELET_LENS_AMOUNT_DP.dp.toPx() } * WAVELET_BACKDROP_SCALE
        val waveletTint = fillColor ?: if (palette.dark) {
            Color(0xFF101218).copy(alpha = 0.62f)
        } else {
            Color(0xFFFAFAFA).copy(alpha = 0.56f)
        }
        Modifier
            .drawBackdrop(
                backdrop = liveBackdrop,
                shape = { shape },
                effects = {
                    colorControls(saturation = 1.5f)
                    blur(blurPx)
                    if (isRuntimeShaderSupported()) {
                        lens(
                            refractionHeight = lensHeightPx,
                            refractionAmount = lensAmountPx,
                            depthEffect = true,
                            chromaticAberration = true,
                        )
                    }
                },
                highlight = { Highlight.Default },
                shadow = { Shadow.Default },
                onDrawSurface = {
                    drawRect(color = waveletTint, size = size)
                },
                exportedBackdrop = exportedBackdrop,
                backdropScale = WAVELET_BACKDROP_SCALE,
            )
            .specularEdgeSheen(
                cornerRadius = cornerRadius,
                borderWidth = borderWidth,
                specularTop = palette.specularTop,
                specularBottom = palette.specularBottom,
                hairlineBorder = borderColor ?: palette.hairlineBorder,
            )
    } else {
        Modifier.liquidGlassBackdrop(
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
        )
    }

    Box(
        modifier = modifier
            .then(elevationModifier)
            .then(interactiveModifier),
        contentAlignment = contentAlignment,
    ) {
        Box(
            Modifier
                .matchParentSize()
                .then(opticalGlassModifier),
        )
        Box(
            modifier = Modifier
                .clip(shape)
                .padding(contentPadding),
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

