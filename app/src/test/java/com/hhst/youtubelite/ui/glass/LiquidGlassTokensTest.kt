package com.hhst.youtubelite.ui.glass

import android.os.Build
import com.hhst.youtubelite.ui.design.DarkAppPalette
import com.hhst.youtubelite.ui.design.LightAppPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LiquidGlassTokensTest {
    @Test
    fun resolveRenderModeHonorsTierAndSdkLevel() {
        assertEquals(
            LiquidGlassRenderMode.SolidFallback,
            LiquidGlassOptics.resolveRenderMode(Build.VERSION_CODES.TIRAMISU, LiquidGlassTier.Flat),
        )
        assertEquals(
            LiquidGlassRenderMode.SolidFallback,
            LiquidGlassOptics.resolveRenderMode(Build.VERSION_CODES.TIRAMISU, LiquidGlassTier.Translucent),
        )
        assertEquals(
            LiquidGlassRenderMode.AgslRefraction,
            LiquidGlassOptics.resolveRenderMode(Build.VERSION_CODES.TIRAMISU, LiquidGlassTier.Optical),
        )
        assertEquals(
            LiquidGlassRenderMode.FrostedBlur,
            LiquidGlassOptics.resolveRenderMode(Build.VERSION_CODES.S, LiquidGlassTier.Optical),
        )
        assertEquals(
            LiquidGlassRenderMode.SolidFallback,
            LiquidGlassOptics.resolveRenderMode(Build.VERSION_CODES.R, LiquidGlassTier.Optical),
        )
    }

    @Test
    fun meniscusHeightAndEdgeHighlightBehaveSmoothlyAcrossPerimeter() {
        assertEquals(0f, LiquidGlassOptics.meniscusHeight(0f, 10f), 0.0001f)
        assertEquals(1f, LiquidGlassOptics.meniscusHeight(10f, 10f), 0.0001f)
        val mid = LiquidGlassOptics.meniscusHeight(5f, 10f)
        assertTrue("Expected normalized arc height > 0.85 at midpoint, got $mid", mid > 0.85f)

        assertEquals(1f, LiquidGlassOptics.edgeHighlightWeight(0f, 10f), 0.0001f)
        assertEquals(0f, LiquidGlassOptics.edgeHighlightWeight(10f, 10f), 0.0001f)
    }

    @Test
    fun sdRoundedRectDistinguishesInteriorBoundaryAndExterior() {
        val center = LiquidGlassOptics.sdRoundedRect(0f, 0f, 50f, 25f, 12f)
        val boundary = LiquidGlassOptics.sdRoundedRect(50f, 0f, 50f, 25f, 12f)
        val outside = LiquidGlassOptics.sdRoundedRect(60f, 0f, 50f, 25f, 12f)
        assertTrue(center < 0f)
        assertEquals(0f, boundary, 0.0001f)
        assertTrue(outside > 0f)
    }

    @Test
    fun themePalettesStayAlignedBetweenCoreAndGlassTokens() {
        assertTrue(DarkLiquidGlassPalette.dark)
        assertTrue(!LightLiquidGlassPalette.dark)
        assertEquals(DarkAppPalette.ink, DarkLiquidGlassPalette.contentPrimary)
        assertEquals(LightAppPalette.ink, LightLiquidGlassPalette.contentPrimary)
    }
}

