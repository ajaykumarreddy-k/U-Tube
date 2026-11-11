package com.hhst.youtubelite.ui.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hhst.youtubelite.ui.design.ActionProgress
import com.hhst.youtubelite.ui.design.AppIcon
import com.hhst.youtubelite.ui.design.DesignTokens

enum class LiquidGlassButtonStyle {
    Standard,
    Accent,
    Subtle,
}

/** Tactile pill-shaped Liquid Glass button with optional leading icon and spring press feedback. */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: LiquidGlassButtonStyle = LiquidGlassButtonStyle.Standard,
    tier: LiquidGlassTier = LiquidGlassTier.Translucent,
    cornerRadius: Dp = 22.dp,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val palette = LocalLiquidGlassPalette.current
    val fillColor = when (style) {
        LiquidGlassButtonStyle.Standard -> palette.elevatedFill
        LiquidGlassButtonStyle.Accent -> palette.accent.copy(alpha = if (enabled) 0.92f else 0.4f)
        LiquidGlassButtonStyle.Subtle -> palette.recessedFill
    }
    val contentColor = when (style) {
        LiquidGlassButtonStyle.Accent -> palette.onAccent
        LiquidGlassButtonStyle.Standard, LiquidGlassButtonStyle.Subtle ->
            if (enabled) palette.contentPrimary else palette.contentSecondary.copy(alpha = 0.45f)
    }

    LiquidGlassSurface(
        modifier = modifier.defaultMinSize(minHeight = 40.dp),
        shape = RoundedCornerShape(cornerRadius),
        cornerRadius = cornerRadius,
        tier = tier,
        fillColor = fillColor,
        contentColor = contentColor,
        contentPadding = contentPadding,
        contentAlignment = Alignment.Center,
        enabled = enabled,
        onClick = onClick,
    ) {
        ProvideTextStyle(
            DesignTokens.text(size = 14, line = 18, weight = FontWeight.Medium, color = contentColor),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = content,
            )
        }
    }
}

/** Circular Liquid Glass icon button suitable for toolbars, player actions, and floating clusters. */
@Composable
fun LiquidGlassIconButton(
    icon: AppIcon,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    diameter: Dp = 44.dp,
    tier: LiquidGlassTier = LiquidGlassTier.Translucent,
    selected: Boolean = false,
    enabled: Boolean = true,
    pending: Boolean = false,
    tint: Color? = null,
) {
    val palette = LocalLiquidGlassPalette.current
    val resolvedTint = (tint ?: if (selected) palette.accent else palette.contentPrimary)
        .let { if (enabled) it else it.copy(alpha = 0.4f) }
    val fill = if (selected) palette.accent.copy(alpha = if (palette.dark) 0.22f else 0.14f) else palette.frostFill

    LiquidGlassSurface(
        modifier = modifier
            .size(diameter)
            .semantics { contentDescription = description },
        shape = CircleShape,
        cornerRadius = diameter / 2,
        tier = tier,
        fillColor = fill,
        borderColor = if (selected) palette.accent.copy(alpha = 0.45f) else palette.hairlineBorder,
        contentColor = resolvedTint,
        contentAlignment = Alignment.Center,
        enabled = enabled && !pending,
        onClick = onClick,
    ) {
        if (pending) {
            ActionProgress(color = resolvedTint)
        } else {
            AppIcon(icon = icon, color = resolvedTint)
        }
    }
}

