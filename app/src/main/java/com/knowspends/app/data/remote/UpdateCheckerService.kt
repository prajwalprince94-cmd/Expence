package com.knowspends.app.data.remote

import com.knowspends.app.BuildConfig
import com.knowspends.app.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateCheckResult(
    val hasUpdate: Boolean,
    val latestVersion: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

@Singleton
class UpdateCheckerService @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    companion object {
        private const val RELEASES_API_URL =
            "https://api.github.com/repos/prajwalprince94-cmd/Expence/releases?per_page=1"
        private const val CONNECT_TIMEOUT_MS = 6000
        private const val READ_TIMEOUT_MS = 6000
    }

    suspend fun checkForUpdates(): UpdateCheckResult = withContext(ioDispatcher) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(RELEASES_API_URL)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "KnowSpends-App")
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorMsg = if (responseCode == 403) {
                    "GitHub API rate limit reached. You can still download the update from GitHub Releases."
                } else {
                    "Server returned code $responseCode"
                }
                return@withContext UpdateCheckResult(
                    hasUpdate = false,
                    latestVersion = "",
                    downloadUrl = "https://github.com/prajwalprince94-cmd/Expence/releases",
                    releaseNotes = "",
                    isSuccess = false,
                    errorMessage = errorMsg
                )
            }

            val responseText = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            val jsonArray = JSONArray(responseText)
            if (jsonArray.length() == 0) {
                return@withContext UpdateCheckResult(
                    hasUpdate = false,
                    latestVersion = "",
                    downloadUrl = "",
                    releaseNotes = "",
                    isSuccess = true
                )
            }

            val releaseObj = jsonArray.getJSONObject(0)
            val tagName = releaseObj.optString("tag_name", "")
            val htmlUrl = releaseObj.optString("html_url", "")
            val body = releaseObj.optString("body", "Bug fixes and performance improvements.")

            // Look for APK download URL in assets
            var apkDownloadUrl = htmlUrl
            val assetsArray = releaseObj.optJSONArray("assets")
            if (assetsArray != null) {
                for (i in 0 until assetsArray.length()) {
                    val asset = assetsArray.getJSONObject(i)
                    val assetName = asset.optString("name", "")
                    if (assetName.endsWith(".apk", ignoreCase = true)) {
                        apkDownloadUrl = asset.optString("browser_download_url", htmlUrl)
                        break
                    }
                }
            }

            val currentVersion = BuildConfig.VERSION_NAME
            val isNewer = isVersionNewer(remoteTag = tagName, currentVersion = currentVersion)

            UpdateCheckResult(
                hasUpdate = isNewer,
                latestVersion = tagName,
                downloadUrl = apkDownloadUrl,
                releaseNotes = body,
                isSuccess = true
            )
        } catch (e: Exception) {
            UpdateCheckResult(
                hasUpdate = false,
                latestVersion = "",
                downloadUrl = "",
                releaseNotes = "",
                isSuccess = false,
                errorMessage = e.message ?: "Failed to check for updates"
            )
        } finally {
            connection?.disconnect()
        }
    }

    private fun isVersionNewer(remoteTag: String, currentVersion: String): Boolean {
        val cleanRemote = remoteTag.removePrefix("v").trim()
        val cleanCurrent = currentVersion.removePrefix("v").removeSuffix("-debug").trim()
        if (cleanRemote.isBlank() || cleanCurrent.isBlank()) return false
        if (cleanRemote.equals(cleanCurrent, ignoreCase = true)) return false

        val remoteNums = cleanRemote.split("-")[0].split(".").mapNotNull { it.toIntOrNull() }
        val currentNums = cleanCurrent.split("-")[0].split(".").mapNotNull { it.toIntOrNull() }

        for (i in 0 until maxOf(remoteNums.size, currentNums.size)) {
            val r = remoteNums.getOrElse(i) { 0 }
            val c = currentNums.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }

        val remoteSuffix = cleanRemote.substringAfter("-", "")
        val currentSuffix = cleanCurrent.substringAfter("-", "")
        if (remoteSuffix.isEmpty() && currentSuffix.isNotEmpty()) return true

        return false
    }
}
