package com.hhst.youtubelite.onboarding

import com.hhst.youtubelite.extension.ExtensionManager
import com.hhst.youtubelite.extension.MemoryPrefStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingPreferencesTest {

    @Test
    fun freshInstallIsNotCompletedEvenAfterResolvingUpgradeState() {
        val store = MemoryPrefStore()
        val prefs = OnboardingPreferences(store = store, hasExistingAuth = { false })

        prefs.resolveUpgradeStateBeforeSeeding()
        ExtensionManager(store)

        assertFalse(prefs.isCompleted())
    }

    @Test
    fun existingInstallWithSeededPreferencesIsMarkedCompletedOnUpgrade() {
        val store = MemoryPrefStore()
        ExtensionManager(store) // simulates prior app launch before onboarding existed

        val prefs = OnboardingPreferences(store = store, hasExistingAuth = { false })
        prefs.resolveUpgradeStateBeforeSeeding()

        assertTrue(prefs.isCompleted())
    }

    @Test
    fun existingInstallWithSignedInAccountIsMarkedCompletedOnUpgrade() {
        val store = MemoryPrefStore()
        val prefs = OnboardingPreferences(store = store, hasExistingAuth = { true })

        prefs.resolveUpgradeStateBeforeSeeding()

        assertTrue(prefs.isCompleted())
    }

    @Test
    fun explicitReplayResetClearsCompletionWithoutBeingOverwrittenByUpgradeCheck() {
        val store = MemoryPrefStore()
        ExtensionManager(store)
        val prefs = OnboardingPreferences(store = store, hasExistingAuth = { true })
        prefs.resolveUpgradeStateBeforeSeeding()
        assertTrue(prefs.isCompleted())

        prefs.resetForReplay()
        assertFalse(prefs.isCompleted())

        prefs.resolveUpgradeStateBeforeSeeding()
        assertFalse(prefs.isCompleted())
    }
}

