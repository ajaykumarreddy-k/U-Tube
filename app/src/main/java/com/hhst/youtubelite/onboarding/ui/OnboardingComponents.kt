package com.hhst.youtubelite.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hhst.youtubelite.R
import com.hhst.youtubelite.onboarding.OnboardingStep
import com.hhst.youtubelite.ui.design.AppIcon
import com.hhst.youtubelite.ui.design.AppPalette
import com.hhst.youtubelite.ui.design.AppText
import com.hhst.youtubelite.ui.design.DesignTokens
import com.hhst.youtubelite.ui.design.LightAppPalette
import com.hhst.youtubelite.ui.design.LocalAppColors
import com.hhst.youtubelite.ui.design.LocalMotionScale
import com.hhst.youtubelite.ui.glass.LiquidGlassPalette
import com.hhst.youtubelite.ui.glass.LightLiquidGlassPalette
import com.hhst.youtubelite.ui.glass.LocalLiquidGlassPalette
import kotlin.math.roundToInt

/**
 * Editorial monochrome + frosted sheet design tokens shared across all onboarding steps:
 * deep matte obsidian top header paired with a luminous pearl-white lower interactive sheet.
 */
internal object OnboardingEditorialColors {
    val ObsidianBg = Color(0xFF09090C)
    val ObsidianSurface = Color(0xFF151519)
    val ObsidianBorder = Color(0xFF28282F)
    val ObsidianInk = Color(0xFFF6F6F9)
    val ObsidianMuted = Color(0xFF8B8B94)
    val ObsidianDim = Color(0xFF6B6B73)

    val PearlBg = Color(0xFFFAFAFC)
    val PearlCard = Color(0xFFFFFFFF)
    val PearlSubtle = Color(0xFFF2F2F7)
    val PearlDivider = Color(0xFFE9E9F0)
    val PearlBorder = Color(0xFFE4E4EC)
    val PearlInk = Color(0xFF111115)
    val PearlMuted = Color(0xFF71717A)
    val PearlDim = Color(0xFFA8A8B3)
    val SignatureCoral = Color(0xFFEA4C3C)
}

/**
 * Matte obsidian ambient canvas behind the editorial top header.
 */
@Composable
fun OnboardingAmbientCanvas(modifier: Modifier = Modifier) {
    val accent = LocalLiquidGlassPalette.current.accent
    Box(
        modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        OnboardingEditorialColors.ObsidianBg,
                        Color(0xFF0E0E12),
                        OnboardingEditorialColors.ObsidianBg,
                    ),
                ),
            ),
    ) {
        Box(
            Modifier
                .size(260.dp)
                .offset(x = (-90).dp, y = (-70).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            accent.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(220.dp)
                .offset(x = 90.dp, y = (-20).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color.White.copy(alpha = 0.04f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

/**
 * Minimal obsidian top utility bar: back pill (on sub-steps), subtle skip pill on right.
 */
@Composable
fun OnboardingTopChrome(
    step: OnboardingStep,
    canReturnToReview: Boolean,
    onBack: () -> Unit,
    onSkipStep: () -> Unit,
    onSkipSetup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        if (step != OnboardingStep.Welcome) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(OnboardingEditorialColors.ObsidianSurface)
                    .border(0.75.dp, OnboardingEditorialColors.ObsidianBorder, CircleShape)
                    .clickable(role = Role.Button, onClick = onBack)
                    .semantics { contentDescription = "Back" }
                    .testTag("onboarding-back"),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    icon = AppIcon.Back,
                    color = OnboardingEditorialColors.ObsidianInk,
                    modifier = Modifier.size(16.dp),
                )
            }
        } else {
            Spacer(Modifier.size(36.dp))
        }

        when {
            step.canSkipStep && !canReturnToReview -> {
                ObsidianActionPill(
                    label = stringResource(R.string.onboarding_skip),
                    onClick = onSkipStep,
                    tag = "onboarding-skip-step",
                )
            }
            step == OnboardingStep.Welcome -> {
                ObsidianActionPill(
                    label = stringResource(R.string.onboarding_skip_setup),
                    onClick = onSkipSetup,
                    tag = "onboarding-skip-setup-top",
                )
            }
            else -> Spacer(Modifier.size(36.dp))
        }
    }
}

@Composable
private fun ObsidianActionPill(
    label: String,
    onClick: () -> Unit,
    tag: String,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(OnboardingEditorialColors.ObsidianSurface)
            .border(0.75.dp, OnboardingEditorialColors.ObsidianBorder, CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(tag)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppText(
            text = label,
            size = 12,
            line = 16,
            weight = FontWeight.Medium,
            color = OnboardingEditorialColors.ObsidianMuted,
        )
    }
}

/**
 * Editorial upper section rendered on the obsidian canvas:
 * Bold white title + accent dot, right-aligned metadata, mixed-contrast narrative text,
 * and a compact muted status row at the bottom.
 */
@Composable
fun OnboardingObsidianHero(
    keyword: String,
    stepMetaTop: String,
    stepMetaBottom: String,
    dotColor: Color,
    narrative: AnnotatedString,
    inlineIcons: Map<String, InlineTextContent> = emptyMap(),
    statusContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 2.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppText(
                    text = keyword,
                    size = 32,
                    line = 36,
                    weight = FontWeight.Bold,
                    color = OnboardingEditorialColors.ObsidianInk,
                )
                Box(
                    Modifier
                        .offset(y = 2.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(dotColor),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                AppText(
                    text = stepMetaTop,
                    size = 12,
                    line = 16,
                    weight = FontWeight.Medium,
                    color = OnboardingEditorialColors.ObsidianMuted,
                    align = TextAlign.End,
                )
                AppText(
                    text = stepMetaBottom,
                    size = 12,
                    line = 16,
                    weight = FontWeight.Normal,
                    color = OnboardingEditorialColors.ObsidianDim,
                    align = TextAlign.End,
                )
            }
        }

        BasicText(
            text = narrative,
            style = DesignTokens.text(
                size = 18,
                line = 25,
                weight = FontWeight.Normal,
                color = OnboardingEditorialColors.ObsidianMuted,
            ),
            inlineContent = inlineIcons,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            statusContent()
        }
    }
}

/** Builds a mixed muted/white editorial paragraph matching the monochrome brief layout. */
internal fun editorialNarrative(
    highlight: String,
    body: String,
    leadingIconId: String? = null,
): AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(color = OnboardingEditorialColors.ObsidianInk, fontWeight = FontWeight.Medium)) {
        append(highlight)
    }
    if (body.isNotBlank()) {
        append(" ")
        if (leadingIconId != null) {
            appendInlineContent(leadingIconId, "[icon]")
            append(" ")
        }
        append(body)
    }
}

internal fun inlineEditorialIcon(
    id: String,
    icon: AppIcon,
    tint: Color = OnboardingEditorialColors.ObsidianInk,
): Pair<String, InlineTextContent> = id to InlineTextContent(
    Placeholder(
        width = 18.sp,
        height = 18.sp,
        placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
    ),
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AppIcon(icon = icon, color = tint, modifier = Modifier.size(15.dp))
    }
}

/**
 * Rounded luminous pearl-white lower sheet housing the step rail, interactive controls,
 * frosted scroll fade, and primary CTA dock.
 */
@Composable
fun OnboardingPearlSheet(
    accentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetPalette: AppPalette = remember(accentColor) {
        LightAppPalette.copy(
            background = OnboardingEditorialColors.PearlBg,
            ink = OnboardingEditorialColors.PearlInk,
            secondary = OnboardingEditorialColors.PearlMuted,
            surface = OnboardingEditorialColors.PearlSubtle,
            ghost = Color(0xFFE7E7F0),
            border = OnboardingEditorialColors.PearlBorder,
            navigationDivider = OnboardingEditorialColors.PearlDivider,
            red = accentColor,
        )
    }
    val sheetGlass: LiquidGlassPalette = remember(accentColor) {
        LightLiquidGlassPalette.copy(
            dark = false,
            frostFill = OnboardingEditorialColors.PearlCard,
            elevatedFill = OnboardingEditorialColors.PearlCard,
            recessedFill = OnboardingEditorialColors.PearlSubtle,
            hairlineBorder = OnboardingEditorialColors.PearlBorder,
            specularTop = Color.White,
            specularBottom = OnboardingEditorialColors.PearlBorder,
            accent = accentColor,
            onAccent = Color.White,
            contentPrimary = OnboardingEditorialColors.PearlInk,
            contentSecondary = OnboardingEditorialColors.PearlMuted,
            pillSelection = OnboardingEditorialColors.PearlInk.copy(alpha = 0.06f),
            sliderActive = accentColor,
        )
    }

    val shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    CompositionLocalProvider(
        LocalAppColors provides sheetPalette,
        LocalLiquidGlassPalette provides sheetGlass,
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = shape,
                    ambientColor = Color.White.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.4f),
                )
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            OnboardingEditorialColors.PearlCard,
                            OnboardingEditorialColors.PearlBg,
                        ),
                    ),
                )
                .border(1.dp, Color.White.copy(alpha = 0.9f), shape),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                content = content,
            )
        }
    }
}

/**
 * Calendar-inspired 6-step horizontal rail at the top of the pearl sheet (`01 INTRO .. 06 DONE`).
 */
@Composable
fun OnboardingStepRail(
    currentStep: OnboardingStep,
    accentColor: Color,
    onSelectStep: ((OnboardingStep) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val steps = OnboardingStep.entries
    val shortLabels = listOf("INTRO", "ACCT", "PLAY", "LOOK", "PREF", "DONE")
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("onboarding-progress"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            steps.forEachIndexed { index, step ->
                val active = step == currentStep
                val completed = index < currentStep.index
                val canTap = onSelectStep != null && completed
                val bgColor by animateColorAsState(
                    targetValue = if (active) OnboardingEditorialColors.PearlSubtle else Color.Transparent,
                    animationSpec = tween(180),
                    label = "rail-bg-$index",
                )
                val borderColor by animateColorAsState(
                    targetValue = if (active) OnboardingEditorialColors.PearlBorder else Color.Transparent,
                    animationSpec = tween(180),
                    label = "rail-border-$index",
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bgColor)
                        .border(0.75.dp, borderColor, RoundedCornerShape(14.dp))
                        .then(
                            if (canTap) Modifier.clickable(role = Role.Tab) { onSelectStep?.invoke(step) }
                            else Modifier,
                        )
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    AppText(
                        text = "${index + 1}",
                        size = 16,
                        line = 18,
                        weight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = when {
                            active -> OnboardingEditorialColors.PearlInk
                            completed -> OnboardingEditorialColors.PearlInk.copy(alpha = 0.72f)
                            else -> OnboardingEditorialColors.PearlDim
                        },
                    )
                    AppText(
                        text = shortLabels[index],
                        size = 9,
                        line = 12,
                        weight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = when {
                            active -> accentColor
                            completed -> OnboardingEditorialColors.PearlMuted
                            else -> OnboardingEditorialColors.PearlDim
                        },
                    )
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(0.75.dp)
                .background(OnboardingEditorialColors.PearlDivider),
        )
    }
}

@Composable
fun OnboardingProgressBar(
    currentIndex: Int,
    totalSteps: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val motionScale = LocalMotionScale.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0 until totalSteps) {
            val completed = i <= currentIndex
            val active = i == currentIndex
            val targetWeight = if (active) 1.65f else 1f
            val animatedWeight by animateFloatAsState(
                targetValue = targetWeight,
                animationSpec = if (motionScale == 0f) tween(0) else spring(stiffness = Spring.StiffnessMediumLow),
                label = "step-weight-$i",
            )
            val fillColor by animateColorAsState(
                targetValue = if (completed) accentColor else OnboardingEditorialColors.ObsidianBorder,
                animationSpec = tween(if (motionScale == 0f) 0 else (240 * motionScale).roundToInt()),
                label = "step-color-$i",
            )
            Box(
                Modifier
                    .weight(animatedWeight)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(fillColor),
            )
        }
    }
}

@Composable
fun OnboardingStepContainer(
    step: OnboardingStep,
    forward: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (OnboardingStep) -> Unit,
) {
    val motionScale = LocalMotionScale.current
    AnimatedContent(
        targetState = step,
        modifier = modifier,
        transitionSpec = {
            if (motionScale == 0f) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                val direction = if (forward) 1 else -1
                val duration = (250 * motionScale).roundToInt().coerceAtLeast(80)
                (slideInHorizontally(animationSpec = tween(duration)) { it / 6 * direction } +
                    fadeIn(tween(duration)) +
                    scaleIn(initialScale = 0.985f, animationSpec = tween(duration))) togetherWith
                    (slideOutHorizontally(animationSpec = tween(duration)) { -it / 7 * direction } +
                        fadeOut(tween((duration * 0.72f).roundToInt())))
            }
        },
        label = "onboarding-step-transition",
    ) { target ->
        content(target)
    }
}

/**
 * Clean high-contrast card inside the pearl-white sheet with delicate hairline perimeter.
 */
@Composable
fun OnboardingGlassCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val accent = LocalLiquidGlassPalette.current.accent
    val motionScale = LocalMotionScale.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (onClick != null && pressed) 0.985f else 1f,
        animationSpec = if (motionScale == 0f) tween(0) else spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card-press-scale",
    )
    val fill by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.06f) else OnboardingEditorialColors.PearlCard,
        animationSpec = tween(if (motionScale == 0f) 0 else 180),
        label = "card-fill",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.65f) else OnboardingEditorialColors.PearlBorder,
        animationSpec = tween(if (motionScale == 0f) 0 else 180),
        label = "card-border",
    )
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(fill)
            .border(if (selected) 1.25.dp else 0.75.dp, borderColor, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
fun OnboardingToggleRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: AppIcon? = null,
    enabled: Boolean = true,
) {
    val accent = LocalLiquidGlassPalette.current.accent
    val motionScale = LocalMotionScale.current
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
        animationSpec = if (motionScale == 0f) tween(0) else spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "switch-thumb",
    )
    val trackColor by animateColorAsState(
        targetValue = when {
            !enabled -> OnboardingEditorialColors.PearlSubtle
            checked -> accent
            else -> Color(0xFFE2E2EA)
        },
        animationSpec = tween(if (motionScale == 0f) 0 else 160),
        label = "switch-track",
    )
    val shape = RoundedCornerShape(18.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OnboardingEditorialColors.PearlCard)
            .border(0.75.dp, OnboardingEditorialColors.PearlBorder, shape)
            .then(
                if (enabled) Modifier.clickable(role = Role.Switch) { onCheckedChange(!checked) }
                else Modifier,
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (checked) accent.copy(alpha = 0.11f) else OnboardingEditorialColors.PearlSubtle),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    icon = icon,
                    color = if (checked) accent else OnboardingEditorialColors.PearlInk,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AppText(
                text = title,
                size = 15,
                line = 20,
                weight = FontWeight.Medium,
                color = if (enabled) OnboardingEditorialColors.PearlInk else OnboardingEditorialColors.PearlMuted,
            )
            AppText(
                text = summary,
                size = 12,
                line = 16,
                color = OnboardingEditorialColors.PearlMuted,
            )
        }
        Box(
            Modifier
                .size(width = 46.dp, height = 28.dp)
                .clip(CircleShape)
                .background(trackColor)
                .semantics { contentDescription = title }
                .padding(vertical = 2.dp),
        ) {
            Box(
                Modifier
                    .offset(x = thumbOffset)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White),
            )
        }
    }
}

@Composable
fun <T> OnboardingSegmentedControl(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    tagPrefix: String = "segment",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(OnboardingEditorialColors.PearlSubtle)
            .border(0.75.dp, OnboardingEditorialColors.PearlBorder, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (value, label) ->
            val active = value == selected
            val bg by animateColorAsState(
                targetValue = if (active) OnboardingEditorialColors.PearlCard else Color.Transparent,
                animationSpec = tween(160),
                label = "seg-bg-$label",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg)
                    .then(
                        if (active) Modifier.border(0.75.dp, OnboardingEditorialColors.PearlBorder, RoundedCornerShape(10.dp))
                        else Modifier,
                    )
                    .clickable(role = Role.RadioButton) { onSelect(value) }
                    .testTag("$tagPrefix-$label")
                    .padding(vertical = 9.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                AppText(
                    text = label,
                    size = 13,
                    line = 16,
                    weight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (active) OnboardingEditorialColors.PearlInk else OnboardingEditorialColors.PearlMuted,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * Scroll viewport + subtle bottom frosted fade scrim + docked bottom CTA bar inside the pearl sheet.
 */
@Composable
fun OnboardingFrostedStepLayout(
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    modifier: Modifier = Modifier,
    body: @Composable BoxScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            body()
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(26.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                OnboardingEditorialColors.PearlBg.copy(alpha = 0.92f),
                            ),
                        ),
                    ),
            )
        }
        if (footer != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OnboardingEditorialColors.PearlBg)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 6.dp, bottom = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = footer,
            )
        } else {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .background(OnboardingEditorialColors.PearlBg)
                    .navigationBarsPadding()
                    .height(6.dp),
            )
        }
    }
}

/** High-contrast primary button styled cleanly for the pearl-white editorial sheet. */
@Composable
fun OnboardingPrimaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentDot: Color? = null,
    leadingIcon: AppIcon? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(OnboardingEditorialColors.PearlInk)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            AppIcon(icon = leadingIcon, color = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        AppText(
            text = text,
            size = 15,
            line = 20,
            weight = FontWeight.SemiBold,
            color = Color.White,
        )
        if (accentDot != null) {
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(accentDot),
            )
        }
    }
}
