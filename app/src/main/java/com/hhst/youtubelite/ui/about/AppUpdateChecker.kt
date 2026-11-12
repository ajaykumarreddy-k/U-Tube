package com.hhst.youtubelite.ui.about

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.hhst.youtubelite.R
import com.hhst.youtubelite.core.PreferenceStore
import com.hhst.youtubelite.diagnostics.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request

data class ReleaseInfo(
    val tagName: String,
    val name: String,
    val htmlUrl: String,
)

/**
 * Checks [REPO_URL] GitHub Releases for newer stable versions and posts an update notification
 * when a newer release is published.
 */
object AppUpdateChecker {
    const val REPO_URL = "https://github.com/ajaykumarreddy-k/U-Tube"
    const val LATEST_RELEASE_API_URL = "https://api.github.com/repos/ajaykumarreddy-k/U-Tube/releases/latest"
    const val LATEST_RELEASE_PAGE_URL = "$REPO_URL/releases/latest"

    internal const val UPDATE_CHANNEL_ID = "app_updates"
    internal const val UPDATE_NOTIFICATION_ID = 4001
    internal const val KEY_LAST_CHECK_MS = "update:last_check_ms"
    internal const val KEY_LAST_NOTIFIED_TAG = "update:last_notified_tag"
    private const val CHECK_INTERVAL_MS = 6L * 60L * 60L * 1000L

    fun fetchLatestStableRelease(
        client: OkHttpClient,
        apiUrl: String = LATEST_RELEASE_API_URL,
    ): ReleaseInfo {
        val request = Request.Builder()
            .url(apiUrl)
            .header("Accept", "application/vnd.github+json")
            .build()
        return client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "HTTP ${response.code}" }
            val body = response.body?.string() ?: error("Empty release response")
            parseLatestStableRelease(body)
        }
    }

    internal fun parseLatestStableRelease(body: String): ReleaseInfo {
        val json = Json.parseToJsonElement(body) as JsonObject
        fun flag(key: String): Boolean = (json[key] as? JsonPrimitive)
            ?.takeUnless { it is JsonNull }?.content?.toBoolean() == true
        fun text(key: String): String = (json[key] as? JsonPrimitive)
            ?.takeUnless { it is JsonNull }?.content
            ?: error("update_check_payload")
        fun optionalText(key: String): String = (json[key] as? JsonPrimitive)
            ?.takeUnless { it is JsonNull }?.content.orEmpty()

        check(!flag("prerelease") && !flag("draft")) { "Pre-release or draft ignored" }
        val tag = text("tag_name")
        val url = optionalText("html_url").ifBlank { LATEST_RELEASE_PAGE_URL }
        val title = optionalText("name").ifBlank { tag }
        return ReleaseInfo(tagName = tag, name = title, htmlUrl = url)
    }

    internal fun shouldNotifyForRelease(
        currentVersion: String,
        latestTag: String,
        lastNotifiedTag: String?,
    ): Boolean {
        if (latestTag.isBlank()) return false
        if (latestTag == lastNotifiedTag) return false
        return StableVersion.isNewer(currentVersion, latestTag)
    }

    internal fun isCheckDue(lastCheckMs: Long, nowMs: Long): Boolean =
        lastCheckMs <= 0L || nowMs - lastCheckMs >= CHECK_INTERVAL_MS

    suspend fun checkAndNotifyIfNeeded(
        context: Context,
        client: OkHttpClient,
        store: PreferenceStore,
        nowMs: Long = System.currentTimeMillis(),
    ): ReleaseInfo? {
        val lastCheck = store.getLong(KEY_LAST_CHECK_MS, 0L)
        if (!isCheckDue(lastCheck, nowMs)) return null

        val currentVersion = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        }.getOrDefault("")
        if (currentVersion.isBlank()) return null

        val release = withContext(Dispatchers.IO) {
            runCatching { fetchLatestStableRelease(client) }
        }.getOrElse { failure ->
            AppLog.event(AppLog.Category.APP, "update_check_skipped", failure = failure)
            return null
        }

        store.putLong(KEY_LAST_CHECK_MS, nowMs)
        val lastNotified = store.getString(KEY_LAST_NOTIFIED_TAG, "")
        if (!shouldNotifyForRelease(currentVersion, release.tagName, lastNotified)) {
            return null
        }

        store.putString(KEY_LAST_NOTIFIED_TAG, release.tagName)
        postUpdateNotification(context, release)
        return release
    }

    private fun postUpdateNotification(context: Context, release: ReleaseInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) return
        ensureUpdateChannel(context)

        val openIntent = Intent(Intent.ACTION_VIEW, Uri.parse(release.htmlUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            UPDATE_NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_play)
            .setContentTitle(context.getString(R.string.update_notification_title))
            .setContentText(context.getString(R.string.update_notification_body, release.tagName))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching { nm.notify(UPDATE_NOTIFICATION_ID, notification) }
    }

    private fun ensureUpdateChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (mgr.getNotificationChannel(UPDATE_CHANNEL_ID) == null) {
            mgr.createNotificationChannel(
                NotificationChannel(
                    UPDATE_CHANNEL_ID,
                    context.getString(R.string.update_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = context.getString(R.string.update_channel_desc)
                },
            )
        }
    }
}

