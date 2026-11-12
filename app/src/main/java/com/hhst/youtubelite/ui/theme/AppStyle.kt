package com.hhst.youtubelite.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class ThemeMode(val label: String) { System("System"), Light("Light"), Dark("Dark") }

enum class AppAccent(
    val label: String,
    val storageKey: String,
    val lightColor: Color,
    val darkColor: Color,
) {
    Crimson("Crimson", "crimson", Color(0xFFD9304F), Color(0xFFD9304F)),
    Cobalt("Cobalt", "cobalt", Color(0xFF0A84FF), Color(0xFF3A9BFF)),
    Indigo("Indigo", "indigo", Color(0xFF5856D6), Color(0xFF7D7AFF)),
    Teal("Teal", "teal", Color(0xFF00897B), Color(0xFF2DD4BF)),
    Amber("Amber", "amber", Color(0xFFD97706), Color(0xFFFFB340)),
    Emerald("Emerald", "emerald", Color(0xFF059669), Color(0xFF34D399));

    companion object {
        fun fromStorage(raw: String?): AppAccent =
            entries.firstOrNull { it.storageKey == raw } ?: Crimson
    }
}

enum class LiquidGlassStyle(val label: String, val storageKey: String) {
    Standard("Standard", "standard"),
    Clear("Clear", "clear"),
    Frosted("Frosted", "frosted");

    companion object {
        fun fromStorage(raw: String?): LiquidGlassStyle =
            entries.firstOrNull { it.storageKey == raw } ?: Standard
    }
}

object AppAppearance {
    /** Resolved darkness for the Compose theme; null means "follow the system". */
    var dark by mutableStateOf<Boolean?>(null)
        private set
    var mode by mutableStateOf(ThemeMode.System)
        private set
    var accent by mutableStateOf(AppAccent.Crimson)
        private set
    var dynamicColor by mutableStateOf(false)
        private set
    var glassStyle by mutableStateOf(LiquidGlassStyle.Standard)
        private set
    private val listeners = mutableListOf<() -> Unit>()

    fun update(value: ThemeMode) {
        mode = value
        dark = when (value) {
            ThemeMode.Dark -> true
            ThemeMode.Light -> false
            ThemeMode.System -> null
        }
        notifyListeners()
    }

    fun update(
        value: ThemeMode,
        accent: AppAccent = this.accent,
        dynamicColor: Boolean = this.dynamicColor,
        glassStyle: LiquidGlassStyle = this.glassStyle,
    ) {
        this.accent = accent
        this.dynamicColor = dynamicColor
        this.glassStyle = glassStyle
        update(value)
    }

    fun updateAccent(value: AppAccent) {
        accent = value
        notifyListeners()
    }

    fun updateDynamicColor(enabled: Boolean) {
        dynamicColor = enabled
        notifyListeners()
    }

    fun updateGlassStyle(style: LiquidGlassStyle) {
        glassStyle = style
        notifyListeners()
    }

    private fun notifyListeners() {
        synchronized(listeners) { listeners.toList() }.forEach { it() }
    }

    /** Registers a non-Compose observer (system bars); returns the unsubscribe handle. */
    fun onChange(listener: () -> Unit): () -> Unit {
        synchronized(listeners) { listeners += listener }
        return { synchronized(listeners) { listeners -= listener } }
    }
}

object AppStyle {
    val Shapes = Shapes(
        extraSmall = RoundedCornerShape(6.dp), small = RoundedCornerShape(10.dp),
        medium = RoundedCornerShape(14.dp), large = RoundedCornerShape(18.dp),
        extraLarge = RoundedCornerShape(24.dp),
    )
    val SheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val MenuShape = RoundedCornerShape(18.dp)
    val GlassPillShape = RoundedCornerShape(26.dp)
    /** Sheets, dialogs and popup menus share the page's neutral black/white surface.
     * Grey containers belong to controls and item states inside these panels. */
    val Panel: Color
        @Composable get() = MaterialTheme.colorScheme.surface
    val Action: Color
        @Composable get() = LocalActionColors.current?.foreground ?: MaterialTheme.colorScheme.onSurface
    val OnAction: Color
        @Composable get() = LocalActionColors.current?.background ?: MaterialTheme.colorScheme.surface
}

private data class ActionColors(val foreground: Color, val background: Color)
private val LocalActionColors = staticCompositionLocalOf<ActionColors?> { null }

@Composable
fun NeutralControls(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalActionColors provides ActionColors(
        MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.surface,
    ), content = content)
}
