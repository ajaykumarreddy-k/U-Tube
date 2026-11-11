package com.hhst.youtubelite.ui.design

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppPalette(
    val background: Color,
    val ink: Color,
    val secondary: Color,
    /** Secondary container: chips, pressed pills, quiet surfaces. */
    val surface: Color,
    /** Stronger state container: selected rows, skeletons, banners. */
    val ghost: Color,
    val border: Color,
    val navigationDivider: Color,
    val scrim: Color,
    val menuScrim: Color,
    /** Immediate press response under icons and rows. */
    val stateLayer: Color,
    val red: Color,
    val link: Color,
)

val LightAppPalette = AppPalette(
    background = Color(0xFFF3F5F8),
    ink = Color(0xFF101216),
    secondary = Color(0xFF5C6370),
    surface = Color(0xFFE9ECF2),
    ghost = Color(0xFFDDE2EC),
    border = Color.Black.copy(alpha = 0.08f),
    navigationDivider = Color.Black.copy(alpha = 0.12f),
    scrim = Color.Black.copy(alpha = 0.28f),
    menuScrim = Color.Black.copy(alpha = 0.45f),
    stateLayer = Color.Black.copy(alpha = 0.07f),
    red = Color(0xFFD9304F),
    link = Color(0xFF2864D8),
)

val DarkAppPalette = AppPalette(
    background = Color(0xFF050608),
    ink = Color(0xFFF5F7FA),
    secondary = Color(0xFF9AA1AE),
    surface = Color(0xFF12151C),
    ghost = Color(0xFF1A1E28),
    border = Color.White.copy(alpha = 0.12f),
    navigationDivider = Color.White.copy(alpha = 0.16f),
    scrim = Color.Black.copy(alpha = 0.48f),
    menuScrim = Color.Black.copy(alpha = 0.68f),
    stateLayer = Color.White.copy(alpha = 0.09f),
    red = Color(0xFFD9304F),
    link = Color(0xFF7FB2FF),
)

val LocalAppColors = staticCompositionLocalOf { LightAppPalette }
