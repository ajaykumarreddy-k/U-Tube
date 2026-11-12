package com.hhst.youtubelite.onboarding

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.hhst.youtubelite.extension.ExtensionManager
import com.hhst.youtubelite.home.HomeActivity
import com.hhst.youtubelite.player.PlaybackDefaults
import com.hhst.youtubelite.ui.theme.AppAccent
import com.hhst.youtubelite.ui.theme.AppAppearance
import com.hhst.youtubelite.ui.theme.LiquidGlassStyle
import com.hhst.youtubelite.ui.theme.ThemeMode
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.getKoin

@RunWith(AndroidJUnit4::class)
class OnboardingAndroidTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private lateinit var onboardingPrefs: OnboardingPreferences
    private lateinit var extensionManager: ExtensionManager
    private lateinit var playbackDefaults: PlaybackDefaults

    @Before
    fun setup() {
        val koin = getKoin()
        onboardingPrefs = koin.get()
        extensionManager = koin.get()
        playbackDefaults = koin.get()
        onboardingPrefs.resetForReplay()
        extensionManager.resetToDefault()
        extensionManager.setThemeMode(ThemeMode.System)
        extensionManager.setAccentColor(AppAccent.Crimson)
        extensionManager.setGlassStyle(LiquidGlassStyle.Standard)
        playbackDefaults.setDefaultQuality(null)
        playbackDefaults.setDefaultSpeed(1f)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            AppAppearance.update(ThemeMode.System, AppAccent.Crimson, false, LiquidGlassStyle.Standard)
        }
    }

    @After
    fun tearDown() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(Stage.RESUMED)
                .filterIsInstance<OnboardingActivity>()
                .forEach { it.finish() }
            AppAppearance.update(ThemeMode.System, AppAccent.Crimson, false, LiquidGlassStyle.Standard)
        }
        onboardingPrefs.markCompleted()
        extensionManager.setThemeMode(ThemeMode.System)
        extensionManager.setAccentColor(AppAccent.Crimson)
        extensionManager.setGlassStyle(LiquidGlassStyle.Standard)
    }

    @Test
    fun fullGuidedFlowGuestModeCustomizesAndPersists() {
        val intent = OnboardingActivity.intent(ApplicationProvider.getApplicationContext(), fromSettings = true)
        ActivityScenario.launch<OnboardingActivity>(intent).use {
            compose.onNodeWithTag("onboarding-step-welcome").assertIsDisplayed()
            compose.onNodeWithTag("onboarding-get-started").performClick()

            compose.onNodeWithTag("onboarding-step-account").assertIsDisplayed()
            compose.onNodeWithTag("onboarding-account-guest").performClick()

            compose.onNodeWithTag("onboarding-step-playback").assertIsDisplayed()
            compose.onNodeWithTag("quality-720p").performScrollTo().performClick()
            compose.onNodeWithTag("speed-1.5x").performScrollTo().performClick()
            compose.onNodeWithTag("onboarding-playback-continue").assertIsDisplayed().performClick()

            compose.onNodeWithTag("onboarding-step-appearance").assertIsDisplayed()
            compose.onNodeWithTag("theme-option-Dark").performScrollTo().performClick()
            compose.onNodeWithTag("accent-swatch-cobalt").performScrollTo().performClick()
            compose.onNodeWithTag("glass-option-Frosted").performScrollTo().performClick()
            compose.onNodeWithTag("onboarding-appearance-continue").assertIsDisplayed().performClick()

            compose.onNodeWithTag("onboarding-step-preferences").assertIsDisplayed()
            compose.onNodeWithTag("onboarding-skip-step").performClick()

            compose.onNodeWithTag("onboarding-step-review").assertIsDisplayed()
            compose.onNodeWithTag("review-edit-appearance").performScrollTo().performClick()
            compose.onNodeWithTag("onboarding-step-appearance").assertIsDisplayed()
            compose.onNodeWithTag("onboarding-appearance-continue").assertIsDisplayed().performClick()
            compose.onNodeWithTag("onboarding-step-review").assertIsDisplayed()

            compose.onNodeWithTag("onboarding-start-watching").assertIsDisplayed().performClick()
            compose.waitForIdle()

            assertTrue(onboardingPrefs.isCompleted())
            assertEquals("720p", playbackDefaults.defaultQuality())
            assertEquals(1.5f, playbackDefaults.defaultSpeed(), 0.01f)
            assertEquals(ThemeMode.Dark, extensionManager.themeMode())
            assertEquals(AppAccent.Cobalt, extensionManager.accentColor())
            assertEquals(LiquidGlassStyle.Frosted, extensionManager.glassStyle())
        }
    }

    @Test
    fun skipSetupFromWelcomeMarksCompletedImmediately() {
        val intent = OnboardingActivity.intent(ApplicationProvider.getApplicationContext(), fromSettings = true)
        ActivityScenario.launch<OnboardingActivity>(intent).use {
            compose.onNodeWithTag("onboarding-skip-setup-welcome").performClick()
            compose.waitForIdle()
            assertTrue(onboardingPrefs.isCompleted())
        }
    }

    @Test
    fun homeActivityRedirectsToOnboardingWhenForced() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), HomeActivity::class.java)
            .putExtra(HomeActivity.EXTRA_FORCE_ONBOARDING, true)
        ActivityScenario.launch<HomeActivity>(intent).use {
            compose.onNodeWithTag("onboarding-step-welcome").assertIsDisplayed()
        }
    }
}

