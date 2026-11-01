package com.knowspends.app.domain.repository

import com.knowspends.app.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userPreferencesFlow: Flow<UserPreferences>
    suspend fun updateProfile(
        name: String,
        tagline: String,
        avatarUri: String,
        coverUri: String,
        currency: String,
        savingsGoal: String,
        financialYearStart: String
    )
    suspend fun setThemeMode(themeMode: String)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setFontId(fontId: String)
    suspend fun setDailyReminder(enabled: Boolean, time: String)
    suspend fun setBudgetAlert(enabled: Boolean)
    suspend fun setAutoCheckUpdates(enabled: Boolean)
    suspend fun setUpdateInfo(
        hasUpdate: Boolean,
        version: String,
        releaseUrl: String,
        notes: String
    )
    suspend fun setBackTapSettings(enabled: Boolean, sensitivity: String)
    suspend fun setSmsSettings(enabled: Boolean, directAutoAdd: Boolean)
    suspend fun setSmsLastScanTimestamp(timestamp: Long)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setCurrency(currency: String)
}
