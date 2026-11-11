package com.hhst.youtubelite.core

import android.webkit.WebResourceResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.ByteArrayInputStream
import java.net.URI
import java.util.Locale

/**
 * Brave-style network, player-API, HLS-SSAI, and WebView ad/tracker shield.
 * Blocks known ad networks, YouTube ad telemetry/break endpoints, SSAI HLS cue-out
 * ad blocks, and strips player ad payload keys without blocking video streams,
 * authentication, comments, PoTokens, SponsorBlock, DeArrow, or thumbnails.
 */
object AdShield {

    private val blockedExactOrSuffixHosts = setOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "google-analytics.com",
        "googletagmanager.com",
        "googletagservices.com",
        "2mdn.net",
        "admob.com",
        "app-measurement.com",
        "fundingchoicesmessages.google.com",
        "dai.google.com",
        "ima3vpaid.appspot.com",
    )

    private val blockedPathPrefixes = listOf(
        "/pagead/",
        "/ptracking",
        "/api/stats/ads",
        "/api/stats/atr",
        "/get_midroll_info",
        "/youtubei/v1/player/ad_break",
        "/youtubei/v1/log_event",
        "/error_204",
    )

    private val playerAdJsonKeys = setOf(
        "adPlacements",
        "playerAds",
        "adSlots",
        "adBreakHeartbeatParams",
        "adBreakParams",
    )

    private val json = Json { ignoreUnknownKeys = true }

    /** True when [url] targets a third-party ad/tracker domain or YouTube ad endpoint. */
    fun isBlockedRequest(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val uri = runCatching { URI(url) }.getOrNull() ?: return false
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return false
        if (scheme != "https" && scheme != "http") return false
        val host = uri.host?.lowercase(Locale.ROOT)?.trimEnd('.') ?: return false

        if (host.startsWith("adservice.google.")) return true
        if (blockedExactOrSuffixHosts.any { host == it || host.endsWith(".$it") }) return true

        val path = uri.rawPath.orEmpty().lowercase(Locale.ROOT)
        val query = uri.rawQuery.orEmpty().lowercase(Locale.ROOT)
        if (host == "youtube.com" || host.endsWith(".youtube.com") || host == "youtubei.googleapis.com") {
            if (blockedPathPrefixes.any { path == it.trimEnd('/') || path.startsWith(it) }) return true
        }
        if (host == "googlevideo.com" || host.endsWith(".googlevideo.com")) {
            if (path.startsWith("/videogoodput") || query.contains("adformat=") || query.contains("ptk=youtube_ad")) {
                return true
            }
        }
        return false
    }

    /** True when [url] is an Innertube player/watch payload that may carry adPlacements/playerAds. */
    fun isPlayerPayloadUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val uri = runCatching { URI(url) }.getOrNull() ?: return false
        val host = uri.host?.lowercase(Locale.ROOT) ?: return false
        if (host != "youtubei.googleapis.com" && !host.endsWith(".youtube.com") && host != "youtube.com") {
            return false
        }
        val path = uri.rawPath.orEmpty().lowercase(Locale.ROOT)
        return path.contains("/youtubei/v1/player") || path.contains("/youtubei/v1/get_watch")
    }

    /** Removes ad payload top-level keys from Innertube `/player` JSON responses. */
    fun stripPlayerAdJson(url: String?, body: String): String {
        if (!isPlayerPayloadUrl(url) || body.isBlank()) return body
        if (playerAdJsonKeys.none { body.contains("\"$it\"") }) return body
        val parsed = runCatching { json.parseToJsonElement(body) }.getOrNull() as? JsonObject ?: return body
        val filtered = parsed.filterKeys { it !in playerAdJsonKeys }
        if (filtered.size == parsed.size) return body
        return JsonObject(filtered).toString()
    }

    /** Strips SSAI SCTE-35 / CUE-OUT / ad segments from an HLS playlist document. */
    fun stripHlsAdSegments(playlistText: String, baseUri: String? = null): String {
        if (playlistText.isBlank() || !playlistText.contains("#EXTM3U")) return playlistText
        val hasMarkers = playlistText.contains("#EXT-X-CUE-OUT") ||
            playlistText.contains("#EXT-X-OATCLS-SCTE35") ||
            playlistText.contains("SCTE35-OUT") ||
            playlistText.contains("com.apple.hls.interstitial") ||
            playlistText.contains("dai.google.com") ||
            playlistText.contains("adformat=") ||
            playlistText.contains("ptk=youtube_ad")
        if (!hasMarkers) return playlistText

        val lines = playlistText.split('\n')
        val out = ArrayList<String>(lines.size)
        var inAdBreak = false
        var skipNextSegmentUri = false
        var pendingExtInf: String? = null

        for (rawLine in lines) {
            val line = rawLine.trimEnd('\r')
            val trimmed = line.trim()

            if (inAdBreak) {
                if (trimmed.startsWith("#EXT-X-CUE-IN")) {
                    inAdBreak = false
                }
                pendingExtInf = null
                continue
            }

            when {
                trimmed.startsWith("#EXT-X-CUE-OUT") || trimmed.startsWith("#EXT-X-OATCLS-SCTE35") -> {
                    pendingExtInf = null
                    inAdBreak = true
                }
                trimmed.startsWith("#EXT-X-DATERANGE") && isAdDateRange(trimmed) -> {
                    pendingExtInf = null
                }
                trimmed.startsWith("#EXTINF:") -> {
                    pendingExtInf?.let { out += it }
                    pendingExtInf = line
                }
                trimmed.startsWith("#EXT-X-BYTERANGE:") && pendingExtInf != null -> {
                    pendingExtInf = "$pendingExtInf\n$line"
                }
                trimmed.isEmpty() || trimmed.startsWith("#") -> {
                    pendingExtInf?.let {
                        out += it
                        pendingExtInf = null
                    }
                    out += line
                }
                else -> {
                    val resolved = resolveUri(baseUri, trimmed)
                    val blocked = skipNextSegmentUri || isBlockedRequest(resolved)
                    skipNextSegmentUri = false
                    if (blocked) {
                        pendingExtInf = null
                    } else {
                        pendingExtInf?.let {
                            out += it
                            pendingExtInf = null
                        }
                        out += line
                    }
                }
            }
        }
        pendingExtInf?.let { out += it }
        return out.joinToString("\n")
    }

    private fun isAdDateRange(line: String): Boolean {
        val upper = line.uppercase(Locale.ROOT)
        return upper.contains("SCTE35-OUT") ||
            upper.contains("COM.APPLE.HLS.INTERSTITIAL") ||
            upper.contains("X-ASSET-LIST") ||
            upper.contains("ID=\"AD")
    }

    private fun resolveUri(baseUri: String?, segment: String): String {
        if (baseUri.isNullOrBlank() || segment.contains("://")) return segment
        return runCatching { URI(baseUri).resolve(segment).toString() }.getOrDefault(segment)
    }

    fun emptyWebResourceResponse(): WebResourceResponse = WebResourceResponse(
        "text/plain",
        "utf-8",
        204,
        "No Content",
        emptyMap(),
        ByteArrayInputStream(ByteArray(0)),
    )

    class AdShieldInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val url = request.url.toString()
            if (isBlockedRequest(url)) {
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(204)
                    .message("Blocked by AdShield")
                    .body(ByteArray(0).toResponseBody("text/plain".toMediaType()))
                    .build()
            }
            val response = chain.proceed(request)
            if (response.isSuccessful && isPlayerPayloadUrl(url)) {
                val contentType = response.header("Content-Type").orEmpty()
                if (contentType.contains("json", ignoreCase = true) || contentType.isEmpty()) {
                    val body = response.body ?: return response
                    val mediaType = body.contentType() ?: "application/json; charset=utf-8".toMediaType()
                    val raw = body.string()
                    val sanitized = stripPlayerAdJson(url, raw)
                    return response.newBuilder()
                        .body(sanitized.toResponseBody(mediaType))
                        .build()
                }
            }
            return response
        }
    }
}
