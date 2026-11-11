package com.hhst.youtubelite.ui.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hhst.youtubelite.R
import com.hhst.youtubelite.ui.design.DesignTokens
import com.hhst.youtubelite.ui.design.PanelTransition
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource

/**
 * Floating Liquid Glass modal sheet using the shared [PanelTransition] motion timing
 * and vertical drag-to-dismiss interaction.
 */
@Composable
fun LiquidGlassBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetMaxWidth: Dp = 440.dp,
    dismissEnabled: Boolean = true,
    content: @Composable ColumnScope.(close: () -> Unit) -> Unit,
) {
    val palette = LocalLiquidGlassPalette.current
    val scope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }

    PanelTransition(
        fromBottom = true,
        onDismiss = onDismiss,
        backEnabled = dismissEnabled,
        scrim = { scrimModifier, close ->
            Box(
                scrimModifier
                    .fillMaxSize()
                    .background(palette.scrim)
                    .then(if (dismissEnabled) Modifier.clickable(onClick = close) else Modifier),
            )
        },
    ) { panelModifier, close ->
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            val targetWidth = minOf(sheetMaxWidth, maxWidth - 16.dp).coerceAtLeast(280.dp)
            LiquidGlassSurface(
                modifier = panelModifier
                    .widthIn(max = targetWidth)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp)
                    .graphicsLayer { translationY = dragOffset.value }
                    .pointerInput(dismissEnabled) {
                        if (!dismissEnabled) return@pointerInput
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (dragOffset.value > 110f) {
                                    close()
                                } else {
                                    scope.launch { dragOffset.animateTo(0f) }
                                }
                            },
                            onDragCancel = { scope.launch { dragOffset.animateTo(0f) } },
                            onVerticalDrag = { _, delta ->
                                scope.launch {
                                    dragOffset.snapTo((dragOffset.value + delta).coerceAtLeast(0f))
                                }
                            },
                        )
                    }
                    .then(modifier),
                shape = RoundedCornerShape(24.dp),
                cornerRadius = 24.dp,
                tier = LiquidGlassTier.Optical,
                contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp),
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            Modifier
                                .width(38.dp)
                                .size(width = 38.dp, height = 4.dp)
                                .background(
                                    palette.contentSecondary.copy(alpha = 0.32f),
                                    RoundedCornerShape(2.dp),
                                ),
                        )
                    }
                    content(close)
                }
            }
        }
    }
}

/** Frosted Liquid Glass modal dialog matching U Tube's neutral typography and restrained actions. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiquidGlassDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
) {
    val palette = LocalLiquidGlassPalette.current
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        BoxWithConstraints {
            LiquidGlassSurface(
                modifier = modifier
                    .widthIn(min = 280.dp, max = 520.dp)
                    .fillMaxWidth()
                    .heightIn(max = maxHeight),
                shape = RoundedCornerShape(24.dp),
                cornerRadius = 24.dp,
                tier = LiquidGlassTier.Optical,
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 18.dp),
            ) {
                Column {
                    CompositionLocalProvider(LocalContentColor provides palette.contentPrimary) {
                        icon?.let {
                            Box(
                                Modifier
                                    .padding(bottom = 10.dp)
                                    .align(Alignment.CenterHorizontally),
                            ) { it() }
                        }
                        title?.let {
                            Box(Modifier.padding(bottom = if (text != null) 8.dp else 4.dp)) {
                                ProvideTextStyle(MaterialTheme.typography.titleMedium, it)
                            }
                        }
                    }
                    text?.let {
                        Box(Modifier.weight(1f, fill = false)) {
                            CompositionLocalProvider(LocalContentColor provides palette.contentSecondary) {
                                ProvideTextStyle(MaterialTheme.typography.bodyMedium, it)
                            }
                        }
                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    ) {
                        dismissButton?.invoke()
                        confirmButton()
                    }
                }
            }
        }
    }
}

/**
 * Clean, text-only U Tube wordmark placeholder.
 * Intentionally avoids any custom symbol or generated icon so developer branding drops in cleanly later.
 */
@Composable
fun UTubeWordmark(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    large: Boolean = false,
) {
    val palette = LocalLiquidGlassPalette.current
    val appName = stringResource(R.string.app_name)
    val markSize = when {
        large -> 34.dp
        compact -> 26.dp
        else -> 24.dp
    }
    val fontSize = when {
        large -> 26
        compact -> 15
        else -> 20
    }
    val lineHeight = when {
        large -> 32
        compact -> 20
        else -> 24
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (large) 10.dp else 7.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_utube_mark),
            contentDescription = if (compact) appName else null,
            modifier = Modifier.size(markSize),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(palette.contentPrimary),
        )
        if (!compact) {
            BasicText(
                text = appName,
                style = DesignTokens.text(
                    size = fontSize,
                    line = lineHeight,
                    weight = FontWeight.Bold,
                    color = palette.contentPrimary,
                ).copy(letterSpacing = (-0.4).sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
