package com.arelore.android.base.hotupdate

import android.content.Context
import android.util.Log
import com.arelore.android.base.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Lightweight content hot-update:
 * - Shell APK only provides WebView container + network bootstrap
 * - Business content is remote H5; server-side changes take effect without reinstall
 * - Optional remote JSON can switch [contentUrl] / force cache refresh
 */
class HotUpdateManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun resolveContentUrl(): HotUpdateConfig = withContext(Dispatchers.IO) {
        val remote = fetchRemoteConfig()
        if (remote != null) {
            persist(remote)
            return@withContext remote
        }

        val cachedUrl = prefs.getString(KEY_CONTENT_URL, null)
        if (!cachedUrl.isNullOrBlank()) {
            return@withContext HotUpdateConfig(
                contentUrl = cachedUrl,
                version = prefs.getString(KEY_VERSION, null),
                forceRefresh = false
            )
        }

        HotUpdateConfig(contentUrl = BuildConfig.DEFAULT_CONTENT_URL)
    }

    private fun fetchRemoteConfig(): HotUpdateConfig? {
        return try {
            val connection = (URL(BuildConfig.HOT_UPDATE_CONFIG_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 4_000
                readTimeout = 4_000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }

            connection.use { conn ->
                if (conn.responseCode !in 200..299) {
                    Log.w(TAG, "Hot-update config HTTP ${conn.responseCode}")
                    return null
                }
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                parseConfig(body)
            }
        } catch (e: Exception) {
            // Config endpoint may not exist yet; fall back to default / cache.
            Log.i(TAG, "Hot-update config unavailable: ${e.message}")
            null
        }
    }

    private fun parseConfig(body: String): HotUpdateConfig? {
        val json = JSONObject(body)
        val contentUrl = json.optString("contentUrl").ifBlank {
            json.optString("url")
        }
        if (contentUrl.isBlank()) return null
        return HotUpdateConfig(
            contentUrl = contentUrl,
            version = json.optString("version").ifBlank { null },
            forceRefresh = json.optBoolean("forceRefresh", false)
        )
    }

    private fun persist(config: HotUpdateConfig) {
        prefs.edit()
            .putString(KEY_CONTENT_URL, config.contentUrl)
            .putString(KEY_VERSION, config.version)
            .apply()
    }

    private inline fun <T> HttpURLConnection.use(block: (HttpURLConnection) -> T): T {
        try {
            return block(this)
        } finally {
            disconnect()
        }
    }

    companion object {
        private const val TAG = "HotUpdateManager"
        private const val PREFS_NAME = "arelore_hot_update"
        private const val KEY_CONTENT_URL = "content_url"
        private const val KEY_VERSION = "version"
    }
}
