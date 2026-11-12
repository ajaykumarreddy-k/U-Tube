package com.hhst.youtubelite.ui.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateCheckerTest {

    @Test
    fun parseLatestStableReleaseExtractsFields() {
        val json = """
            {
              "tag_name": "v4.1.0",
              "name": "U Tube v4.1.0",
              "html_url": "https://github.com/ajaykumarreddy-k/U-Tube/releases/tag/v4.1.0",
              "prerelease": false,
              "draft": false
            }
        """.trimIndent()
        val info = AppUpdateChecker.parseLatestStableRelease(json)
        assertEquals("v4.1.0", info.tagName)
        assertEquals("U Tube v4.1.0", info.name)
        assertEquals("https://github.com/ajaykumarreddy-k/U-Tube/releases/tag/v4.1.0", info.htmlUrl)
    }

    @Test(expected = IllegalStateException::class)
    fun parseLatestStableReleaseRejectsPrerelease() {
        val json = """
            {
              "tag_name": "v4.1.0-rc1",
              "prerelease": true,
              "draft": false
            }
        """.trimIndent()
        AppUpdateChecker.parseLatestStableRelease(json)
    }

    @Test
    fun shouldNotifyDetectsNewerVersionAndSkipsDuplicates() {
        assertTrue(AppUpdateChecker.shouldNotifyForRelease("4.0.0", "v4.0.1", ""))
        assertTrue(AppUpdateChecker.shouldNotifyForRelease("4.0.0-debug", "v4.0.1", ""))
        assertFalse(AppUpdateChecker.shouldNotifyForRelease("4.0.0", "v4.0.0", ""))
        assertFalse(AppUpdateChecker.shouldNotifyForRelease("4.0.0", "v4.0.1", "v4.0.1"))
    }

    @Test
    fun checkIntervalThrottlesRepeatedChecks() {
        val now = 10_000_000L
        assertTrue(AppUpdateChecker.isCheckDue(0L, now))
        assertFalse(AppUpdateChecker.isCheckDue(now - 60_000L, now))
        assertTrue(AppUpdateChecker.isCheckDue(now - 7L * 60L * 60L * 1000L, now))
    }
}

