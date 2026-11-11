package com.hhst.youtubelite.ui.glass

import android.os.Build
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Rendering tier for Liquid Glass surfaces.
 *
 * - [Flat]: Lightweight tinted surface with hairline rim; safe for high-frequency scrolling items.
 * - [Translucent]: Frosted fill with vertical sheen and dual-edge specular rim.
 * - [Optical]: Full API-tiered optical refraction/meniscus shader + frost for floating chrome.
 */
enum class LiquidGlassTier {
    Flat,
    Translucent,
    Optical,
}

/** Resolved runtime rendering strategy for a given Android SDK level and requested [LiquidGlassTier]. */
enum class LiquidGlassRenderMode {
    SolidFallback,
    FrostedBlur,
    AgslRefraction,
}

/** Semantic color and material tokens for U Tube's Liquid Glass surfaces. */
data class LiquidGlassPalette(
    val dark: Boolean,
    val frostFill: Color,
    val elevatedFill: Color,
    val recessedFill: Color,
    val specularTop: Color,
    val specularBottom: Color,
    val hairlineBorder: Color,
    val sheenStart: Color,
    val sheenEnd: Color,
    val accent: Color,
    val onAccent: Color,
    val contentPrimary: Color,
    val contentSecondary: Color,
    val pillSelection: Color,
    val sliderActive: Color,
    val sliderInactive: Color,
    val sliderThumb: Color,
    val scrim: Color,
)

val LightLiquidGlassPalette = LiquidGlassPalette(
    dark = false,
    frostFill = Color(0xD9FFFFFF),
    elevatedFill = Color(0xEBFFFFFF),
    recessedFill = Color(0xB8E6EAF2),
    specularTop = Color.White.copy(alpha = 0.92f),
    specularBottom = Color(0xFF94A3B8).copy(alpha = 0.24f),
    hairlineBorder = Color.Black.copy(alpha = 0.09f),
    sheenStart = Color.White.copy(alpha = 0.42f),
    sheenEnd = Color(0xFFE2E8F0).copy(alpha = 0.14f),
    accent = Color(0xFF2864D8),
    onAccent = Color.White,
    contentPrimary = Color(0xFF101216),
    contentSecondary = Color(0xFF5C6370),
    pillSelection = Color(0xFF101216).copy(alpha = 0.08f),
    sliderActive = Color(0xFF2864D8),
    sliderInactive = Color(0xFF101216).copy(alpha = 0.14f),
    sliderThumb = Color.White,
    scrim = Color.Black.copy(alpha = 0.26f),
)

val DarkLiquidGlassPalette = LiquidGlassPalette(
    dark = true,
    frostFill = Color(0xC213161E),
    elevatedFill = Color(0xD8181C26),
    recessedFill = Color(0x9E0B0D12),
    specularTop = Color.White.copy(alpha = 0.78f),
    specularBottom = Color.White.copy(alpha = 0.22f),
    hairlineBorder = Color.White.copy(alpha = 0.24f),
    sheenStart = Color.White.copy(alpha = 0.12f),
    sheenEnd = Color.White.copy(alpha = 0.02f),
    accent = Color(0xFF7FB2FF),
    onAccent = Color(0xFF061020),
    contentPrimary = Color(0xFFF5F7FA),
    contentSecondary = Color(0xFF9AA1AE),
    pillSelection = Color.White.copy(alpha = 0.13f),
    sliderActive = Color(0xFF7FB2FF),
    sliderInactive = Color.White.copy(alpha = 0.18f),
    sliderThumb = Color(0xFFF5F7FA),
    scrim = Color.Black.copy(alpha = 0.52f),
)

val LocalLiquidGlassPalette = staticCompositionLocalOf { LightLiquidGlassPalette }

/** Shared dimensional defaults so glass components share consistent geometry and optics. */
object LiquidGlassDefaults {
    val CornerSmall = 12.dp
    val CornerMedium = 20.dp
    val CornerLarge = 24.dp
    val PillHeight = 56.dp
    val SearchBarHeight = 46.dp
    val EdgeBandWidth = 10.dp
    val BlurRadius = 18.dp
    val BorderWidth = 1.dp
}

/**
 * Pure Kotlin optical math utilities derived from SDF rounded-rectangle meniscus physics.
 * Kept Android-free where possible so unit tests can verify behavior directly on the JVM.
 */
object LiquidGlassOptics {
    fun resolveRenderMode(
        sdkInt: Int = Build.VERSION.SDK_INT,
        tier: LiquidGlassTier = LiquidGlassTier.Optical,
    ): LiquidGlassRenderMode = when {
        tier != LiquidGlassTier.Optical -> LiquidGlassRenderMode.SolidFallback
        sdkInt >= Build.VERSION_CODES.TIRAMISU -> LiquidGlassRenderMode.AgslRefraction
        sdkInt >= Build.VERSION_CODES.S -> LiquidGlassRenderMode.FrostedBlur
        else -> LiquidGlassRenderMode.SolidFallback
    }

    /** Signed distance from a rounded rectangle centered at (0, 0) with half-extents (hx, hy). */
    fun sdRoundedRect(px: Float, py: Float, halfWidth: Float, halfHeight: Float, cornerRadius: Float): Float {
        val r = cornerRadius.coerceIn(0f, min(halfWidth, halfHeight))
        val qx = abs(px) - halfWidth + r
        val qy = abs(py) - halfHeight + r
        val outX = max(qx, 0f)
        val outY = max(qy, 0f)
        return sqrt(outX * outX + outY * outY) + min(max(qx, qy), 0f) - r
    }

    /** Circular-arc meniscus profile `sqrt(2t - t^2)` normalized across `[0, edgeWidth]`. */
    fun meniscusHeight(inwardDistancePx: Float, edgeWidthPx: Float): Float {
        if (inwardDistancePx <= 0f || edgeWidthPx <= 0f) return 0f
        if (inwardDistancePx >= edgeWidthPx) return 1f
        val t = (inwardDistancePx / edgeWidthPx).coerceIn(0f, 1f)
        return sqrt((2f * t - t * t).coerceAtLeast(0f))
    }

    /** Edge band falloff concentrated near the glass perimeter for restrained rim highlights. */
    fun edgeHighlightWeight(inwardDistancePx: Float, edgeWidthPx: Float): Float {
        if (inwardDistancePx < 0f || edgeWidthPx <= 0f) return 0f
        val t = 1f - (inwardDistancePx / edgeWidthPx).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }
}

