package com.knowspends.app.data.remote

import com.knowspends.app.BuildConfig
import com.knowspends.app.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
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

    suspend fun checkForUpdates(): UpdateCheckResult = withContext(ioDispatcher) {
        UpdateCheckResult(
            hasUpdate = false,
            latestVersion = BuildConfig.VERSION_NAME,
            downloadUrl = "",
            releaseNotes = "KnowSpends operates 100% offline with zero network connectivity. Your private ledger is completely air-gapped.",
            isSuccess = true
        )
    }
}
