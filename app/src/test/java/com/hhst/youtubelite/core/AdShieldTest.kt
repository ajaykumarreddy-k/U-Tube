package com.hhst.youtubelite.core

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdShieldTest {

    @Test
    fun blocksAdAndTrackerUrlsWithoutBreakingCorePlaybackOrAuth() {
        assertTrue(AdShield.isBlockedRequest("https://googleads.g.doubleclick.net/pagead/id"))
        assertTrue(AdShield.isBlockedRequest("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"))
        assertTrue(AdShield.isBlockedRequest("https://www.youtube.com/api/stats/ads?ns=yt"))
        assertTrue(AdShield.isBlockedRequest("https://www.youtube.com/pagead/interaction/"))
        assertTrue(AdShield.isBlockedRequest("https://www.youtube.com/ptracking?html5=1"))
        assertTrue(AdShield.isBlockedRequest("https://youtubei.googleapis.com/youtubei/v1/player/ad_break"))
        assertTrue(AdShield.isBlockedRequest("https://rr1---sn.googlevideo.com/videoplayback?adformat=2_1"))

        assertFalse(AdShield.isBlockedRequest("https://rr1---sn.googlevideo.com/videoplayback?itag=251&clen=12345"))
        assertFalse(AdShield.isBlockedRequest("https://youtubei.googleapis.com/youtubei/v1/player"))
        assertFalse(AdShield.isBlockedRequest("https://youtubei.googleapis.com/youtubei/v1/next"))
        assertFalse(AdShield.isBlockedRequest("https://i.ytimg.com/vi/abc123/hqdefault.jpg"))
        assertFalse(AdShield.isBlockedRequest("https://accounts.google.com/ServiceLogin"))
        assertFalse(AdShield.isBlockedRequest("https://sponsor.ajay.app/api/skipSegments/abcd"))
    }

    @Test
    fun stripsAdKeysFromPlayerJsonResponse() {
        val raw = """
            {
              "playabilityStatus": {"status": "OK"},
              "streamingData": {"formats": []},
              "adPlacements": [{"adPlacementRenderer": {}}],
              "playerAds": [{"playerLegacyDesktopWatchAdsRenderer": {}}],
              "adSlots": [{"adSlotRenderer": {}}],
              "adBreakHeartbeatParams": "abc"
            }
        """.trimIndent()
        val stripped = AdShield.stripPlayerAdJson("https://youtubei.googleapis.com/youtubei/v1/player", raw)
        assertTrue(stripped.contains("playabilityStatus"))
        assertTrue(stripped.contains("streamingData"))
        assertFalse(stripped.contains("adPlacements"))
        assertFalse(stripped.contains("playerAds"))
        assertFalse(stripped.contains("adSlots"))
        assertFalse(stripped.contains("adBreakHeartbeatParams"))
    }

    @Test
    fun stripsScteAndCueOutAdSegmentsFromHlsPlaylist() {
        val playlist = """
            #EXTM3U
            #EXT-X-TARGETDURATION:6
            #EXTINF:5.0,
            https://rr1.googlevideo.com/seg-1.ts
            #EXT-X-CUE-OUT:DURATION=15.0
            #EXTINF:5.0,
            https://dai.google.com/ad-seg-1.ts
            #EXT-X-CUE-IN
            #EXTINF:5.0,
            https://rr1.googlevideo.com/seg-2.ts
            #EXT-X-ENDLIST
        """.trimIndent()

        val cleaned = AdShield.stripHlsAdSegments(playlist, "https://manifest.googlevideo.com/api/manifest/hls_playlist/master.m3u8")
        assertTrue(cleaned.contains("seg-1.ts"))
        assertTrue(cleaned.contains("seg-2.ts"))
        assertFalse(cleaned.contains("ad-seg-1.ts"))
        assertFalse(cleaned.contains("EXT-X-CUE-OUT"))
    }

    @Test
    fun interceptorShortCircuitsBlockedAdRequestsLocally() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(200).setBody("should-not-hit"))
        server.start()
        try {
            val client = OkHttpClient.Builder()
                .addInterceptor(AdShield.AdShieldInterceptor())
                .build()
            val blocked = server.url("/api/stats/ads").newBuilder()
                .host("youtube.com")
                .build()
            // Redirect test uses URL with blocked path on loopback? Keep host allowed only for youtube.com:
            // verify non-blocked hits server and blocked URL short-circuits before DNS.
            val req = Request.Builder().url("https://doubleclick.net/ad").build()
            client.newCall(req).execute().use { resp ->
                assertEquals(204, resp.code)
            }
            assertEquals(0, server.requestCount)
        } finally {
            server.shutdown()
        }
    }
}

