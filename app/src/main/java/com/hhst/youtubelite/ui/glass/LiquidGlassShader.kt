package com.hhst.youtubelite.ui.glass

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hhst.youtubelite.ui.design.LocalMotionScale

/**
 * AGSL shader adapted from SDF rounded-rectangle meniscus refraction physics:
 * concentrates subtle refractive shimmer and top-left directional specular highlights
 * along the perimeter meniscus without distorting sharp center typography.
 */
private const val LIQUID_GLASS_EDGE_AGSL = """
uniform shader content;
uniform float2 uResolution;
uniform float uCornerRadius;
uniform float uEdgeWidth;
uniform float uIsDark;

float sdRoundedRect(float2 p, float2 halfSize, float radius) {
    float r = clamp(radius, 0.0, min(halfSize.x, halfSize.y));
    float2 q = abs(p) - halfSize + float2(r);
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

half4 main(float2 fragCoord) {
    half4 base = content.eval(fragCoord);
    if (uResolution.x <= 1.0 || uResolution.y <= 1.0) {
        return base;
    }

    float2 center = uResolution * 0.5;
    float2 p = fragCoord - center;
    float2 halfSize = center;
    float d = sdRoundedRect(p, halfSize, uCornerRadius);
    if (d > 0.0) {
        return half4(0.0);
    }

    float inward = clamp(-d / max(uEdgeWidth, 1.0), 0.0, 1.0);
    float band = 1.0 - smoothstep(0.0, 1.0, inward);
    float2 norm = p / max(halfSize, float2(1.0));
    float lightDir = clamp((-norm.x * 0.55 - norm.y * 0.85) * 0.5 + 0.5, 0.0, 1.0);

    float specStrength = mix(0.18, 0.10, uIsDark);
    float spec = band * lightDir * specStrength;
    float3 tinted = clamp(base.rgb + float3(spec), 0.0, 1.0);
    return half4(tinted, base.a);
}
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun buildOpticalRenderEffect(
    widthPx: Float,
    heightPx: Float,
    cornerPx: Float,
    edgeWidthPx: Float,
    blurRadiusPx: Float,
    dark: Boolean,
): AndroidRenderEffect {
    val shader = RuntimeShader(LIQUID_GLASS_EDGE_AGSL).apply {
        setFloatUniform("uResolution", widthPx, heightPx)
        setFloatUniform("uCornerRadius", cornerPx)
        setFloatUniform("uEdgeWidth", edgeWidthPx)
        setFloatUniform("uIsDark", if (dark) 1f else 0f)
    }
    val optical = AndroidRenderEffect.createRuntimeShaderEffect(shader, "content")
    if (blurRadiusPx <= 0.5f) return optical
    val blur = AndroidRenderEffect.createBlurEffect(
        blurRadiusPx,
        blurRadiusPx,
        Shader.TileMode.CLAMP,
    )
    return AndroidRenderEffect.createChainEffect(optical, blur)
}

/**
 * Draws the translucent frost body, directional sheen, specular hairline border, and
 * optional API-tiered optical effect for a glass backdrop layer.
 *
 * Apply this ONLY to the backdrop layer behind text/icons so foreground content remains crisp.
 */
internal fun Modifier.liquidGlassBackdrop(
    shape: Shape,
    cornerRadius: Dp,
    tier: LiquidGlassTier,
    fillColor: Color,
    sheenStart: Color,
    sheenEnd: Color,
    specularTop: Color,
    specularBottom: Color,
    hairlineBorder: Color,
    dark: Boolean,
    blurRadius: Dp = LiquidGlassDefaults.BlurRadius,
    borderWidth: Dp = LiquidGlassDefaults.BorderWidth,
): Modifier {
    val mode = LiquidGlassOptics.resolveRenderMode(tier = tier)
    val effectModifier = when (mode) {
        LiquidGlassRenderMode.AgslRefraction -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Modifier.graphicsLayer {
                    val w = size.width
                    val h = size.height
                    if (w > 1f && h > 1f) {
                        val cornerPx = cornerRadius.toPx().coerceAtMost(minOf(w, h) * 0.5f)
                        val edgePx = LiquidGlassDefaults.EdgeBandWidth.toPx()
                        val blurPx = blurRadius.toPx().coerceIn(0f, 24f) * 0.25f
                        renderEffect = buildOpticalRenderEffect(w, h, cornerPx, edgePx, blurPx, dark)
                            .asComposeRenderEffect()
                    }
                    clip = true
                    this.shape = shape
                }
            } else {
                Modifier.clip(shape)
            }
        }
        LiquidGlassRenderMode.FrostedBlur -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Modifier.graphicsLayer {
                    val blurPx = blurRadius.toPx().coerceIn(0f, 18f) * 0.2f
                    if (blurPx > 0.5f) {
                        renderEffect = AndroidRenderEffect
                            .createBlurEffect(blurPx, blurPx, Shader.TileMode.CLAMP)
                            .asComposeRenderEffect()
                    }
                    clip = true
                    this.shape = shape
                }
            } else {
                Modifier.clip(shape)
            }
        }
        LiquidGlassRenderMode.SolidFallback -> Modifier.clip(shape)
    }

    return this
        .then(effectModifier)
        .background(fillColor, shape)
        .drawWithCache {
            val radiusPx = cornerRadius.toPx().coerceAtMost(size.minDimension * 0.5f)
            val strokePx = borderWidth.toPx().coerceAtLeast(1f)
            val sheenBrush = Brush.verticalGradient(
                colors = listOf(sheenStart, sheenEnd),
                startY = 0f,
                endY = size.height,
            )
            val rimBrush = Brush.linearGradient(
                colors = listOf(specularTop, hairlineBorder, specularBottom),
                start = Offset.Zero,
                end = Offset(size.width * 0.45f, size.height),
            )
            onDrawBehind {
                drawRoundRect(
                    brush = sheenBrush,
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                )
                drawRoundRect(
                    brush = rimBrush,
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    style = Stroke(width = strokePx),
                )
            }
        }
}

/**
 * Tactile spring compression feedback inspired by Apple Liquid Glass surfaces.
 * Automatically degrades to instant/no-scale when system motion scale is 0.
 */
@Composable
fun Modifier.liquidGlassPressScale(
    interactions: MutableInteractionSource,
    pressedScale: Float = 0.97f,
): Modifier {
    val pressed by interactions.collectIsPressedAsState()
    val motionScale = LocalMotionScale.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && motionScale > 0f) pressedScale else 1f,
        animationSpec = if (motionScale > 0f) spring(dampingRatio = 0.72f, stiffness = 580f) else snap(),
        label = "glass-press-scale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
