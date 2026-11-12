package com.hhst.youtubelite.ui.design

import com.hhst.youtubelite.ui.design.appPaneTitle
import androidx.compose.foundation.*
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.hhst.youtubelite.R
import kotlinx.coroutines.launch

@Composable
internal fun SearchPanel(
    query: String,
    suggestions: List<String>,
    onQueryChanged: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onClose: () -> Unit,
    onVoiceSearch: () -> Unit,
    /** Live-mode local history rows (matching entries first); empty in fixture builds. */
    history: List<String> = emptyList(),
    onRemoveHistory: ((String) -> Unit)? = null,
) {
    val focus = remember { FocusRequester() }
    var field by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(query, TextRange(query.length)))
    }
    var observedQuery by remember { mutableStateOf(query) }
    var lastDispatched by remember { mutableStateOf(query) }
    val pendingEchoes = remember { ArrayDeque<String>() }
    if (query != observedQuery) {
        observedQuery = query
        val matched = pendingEchoes.indexOf(query)
        if (matched >= 0) {
            repeat(matched + 1) { pendingEchoes.removeFirst() }
        } else {
            pendingEchoes.clear()
            lastDispatched = query
            if (field.text != query) field = TextFieldValue(query, TextRange(query.length))
        }
    }
    val dispatchQuery: (String, Boolean) -> Unit = { nextText, syncField ->
        if (syncField) field = TextFieldValue(nextText, TextRange(nextText.length))
        if (nextText != lastDispatched) {
            if (pendingEchoes.size >= 16) pendingEchoes.removeFirst()
            pendingEchoes.addLast(nextText)
            lastDispatched = nextText
            onQueryChanged(nextText)
        }
    }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val fieldHeight = maxOf(32.dp, with(LocalDensity.current) { 22.sp.toDp() })
    val toolbarHeight = maxOf(48.dp, fieldHeight)
    val motionScale = LocalMotionScale.current
    val reveal = remember { Animatable(if (motionScale <= 0f) 1f else 0f) }
    val scope = rememberCoroutineScope()
    val currentOnClose by rememberUpdatedState(onClose)
    var closing by remember { mutableStateOf(false) }
    /**
     * Closing only clears focus and hands the event to the host; the IME hides
     * through [onDispose]. An explicit hide here must not be issued while the
     * window may still be animating an earlier IME transition (recreation on
     * older APIs): that synchronous call can wedge the main thread before
     * [onClose] runs, leaving the overlay stuck open.
     */
    val close: () -> Unit = {
        if (!closing) {
            closing = true
            focusManager.clearFocus()
            scope.launch {
                reveal.animateTo(0f, tween(motionDuration(80, motionScale)))
                currentOnClose()
            }
        }
    }
    BackHandler(onBack = close)
    LaunchedEffect(Unit) {
        if (!closing) { focus.requestFocus(); keyboard?.show() }
        reveal.animateTo(1f, tween(motionDuration(100, motionScale), easing = LinearOutSlowInEasing))
    }
    DisposableEffect(Unit) { onDispose { keyboard?.hide() } }
    val glassPalette = DesignTokens.Glass
    Box(Modifier.fillMaxSize().graphicsLayer { alpha = reveal.value }
        .background(DesignTokens.Scrim)
        .clickable(onClick = close).appDescription(R.string.a11y_close_search))
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp).graphicsLayer {
        alpha = reveal.value
        translationY = -4.dp.toPx() * (1f - reveal.value)
    }.testTag("search-overlay").appPaneTitle(R.string.a11y_search_youtube),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth().height(maxOf(48.dp, toolbarHeight)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            com.hhst.youtubelite.ui.glass.LiquidGlassIconButton(
                icon = AppIcon.Back,
                description = stringResource(R.string.a11y_back),
                onClick = close,
                modifier = Modifier.testTag("search-panel-back"),
                diameter = 40.dp,
                tier = com.hhst.youtubelite.ui.glass.LiquidGlassTier.Optical,
            )
            com.hhst.youtubelite.ui.glass.LiquidGlassSurface(
                modifier = Modifier.weight(1f).height(maxOf(42.dp, fieldHeight)),
                shape = CircleShape,
                cornerRadius = 21.dp,
                tier = com.hhst.youtubelite.ui.glass.LiquidGlassTier.Optical,
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    Modifier.fillMaxSize().padding(start = 14.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicTextField(field, { next ->
                        field = next
                        dispatchQuery(next.text, false)
                    },
                        Modifier.weight(1f).focusRequester(focus).testTag("search-input").appDescription(R.string.a11y_search_youtube),
                        textStyle = DesignTokens.text(15, 22).copy(color = glassPalette.contentPrimary),
                        singleLine = true,
                        cursorBrush = SolidColor(glassPalette.accent),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { val submitted = field.text; keyboard?.hide(); onSubmit(submitted) }),
                        decorationBox = { inner -> Box { if (field.text.isEmpty()) AppText(stringResource(R.string.a11y_search_youtube), size = 15, line = 22, color = glassPalette.contentSecondary); inner() } })
                    if (field.text.isNotEmpty()) Box(Modifier.size(32.dp).clip(CircleShape).clickable(role = Role.Button) {
                        dispatchQuery("", true); focus.requestFocus(); keyboard?.show()
                    }.appDescription(R.string.a11y_clear_search), contentAlignment = Alignment.Center) {
                        AppIcon(AppIcon.Close, Modifier.size(16.dp), color = glassPalette.contentSecondary)
                    }
                    Box(Modifier.size(34.dp).clip(CircleShape).clickable(role = Role.Button) { val submitted = field.text; keyboard?.hide(); onSubmit(submitted) }
                        .appDescription(R.string.a11y_search), contentAlignment = Alignment.Center) {
                        AppIcon(AppIcon.Search, Modifier.size(20.dp), color = glassPalette.contentPrimary)
                    }
                }
            }
            if (query.isEmpty()) com.hhst.youtubelite.ui.glass.LiquidGlassIconButton(
                icon = AppIcon.Voice,
                description = stringResource(R.string.a11y_voice_search),
                onClick = { onVoiceSearch() },
                diameter = 40.dp,
                tier = com.hhst.youtubelite.ui.glass.LiquidGlassTier.Optical,
            )
        }
        if (history.isNotEmpty() || suggestions.isNotEmpty()) {
            com.hhst.youtubelite.ui.glass.LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                cornerRadius = 22.dp,
                tier = com.hhst.youtubelite.ui.glass.LiquidGlassTier.Optical,
            ) {
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).testTag("search-suggestions")) {
                    history.forEachIndexed { index, entry ->
                        Row(Modifier.fillMaxWidth().heightIn(min = 42.dp).clickable(role = Role.Button) {
                            keyboard?.hide(); onSubmit(entry)
                        }.padding(horizontal = 8.dp).testTag("history-$index"), verticalAlignment = Alignment.CenterVertically) {
                            AppIcon(AppIcon.Clock, Modifier.padding(start = 6.dp).size(16.dp), color = glassPalette.contentSecondary)
                            BasicText(entry, Modifier.weight(1f).padding(horizontal = 10.dp), style = DesignTokens.text(14, 18).copy(color = glassPalette.contentPrimary), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            if (onRemoveHistory != null) Box(Modifier.size(37.dp).clip(CircleShape).clickable(role = Role.Button) {
                                onRemoveHistory(entry)
                            }.appDescription(R.string.a11y_remove_from_search_history, entry), contentAlignment = Alignment.Center) {
                                AppIcon(AppIcon.Close, Modifier.size(14.dp), color = glassPalette.contentSecondary)
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(0.5.dp).background(glassPalette.hairlineBorder))
                    }
                    suggestions.forEachIndexed { index, suggestion ->
                        Row(Modifier.fillMaxWidth().heightIn(min = 42.dp).clickable(role = Role.Button) {
                            keyboard?.hide(); onSubmit(suggestion)
                        }.padding(start = 8.dp, end = 4.dp).testTag("suggestion-$index"), verticalAlignment = Alignment.CenterVertically) {
                            AppIcon(AppIcon.Search, Modifier.padding(start = 6.dp).size(16.dp), color = glassPalette.contentSecondary)
                            val prefixQuery = query.trim()
                            val text = buildAnnotatedString {
                                val prefix = if (suggestion.startsWith(prefixQuery, ignoreCase = true)) prefixQuery.length else suggestion.length
                                append(suggestion.take(prefix))
                                withStyle(SpanStyle(fontWeight = FontWeight.Medium)) { append(suggestion.drop(prefix)) }
                            }
                            BasicText(text, Modifier.weight(1f).padding(horizontal = 10.dp), style = DesignTokens.text(14, 18).copy(color = glassPalette.contentPrimary), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Box(Modifier.size(37.dp).clip(CircleShape).clickable(role = Role.Button) {
                                dispatchQuery(suggestion, true); focus.requestFocus(); keyboard?.show()
                            }.appDescription(R.string.a11y_use_suggestion, suggestion), contentAlignment = Alignment.Center) {
                                AppIcon(AppIcon.ArrowUpLeft, color = glassPalette.contentSecondary)
                            }
                        }
                        if (index < suggestions.lastIndex) Box(Modifier.fillMaxWidth().height(0.5.dp).background(glassPalette.hairlineBorder))
                    }
                }
            }
        }
    }
}
