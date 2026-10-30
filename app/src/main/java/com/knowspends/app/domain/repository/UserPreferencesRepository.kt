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
        currency: String
    )
    suspend fun setThemeMode(themeMode: String)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setFontId(fontId: String)
    suspend fun setDisplayScale(scale: Float)
    suspend fun setDailyReminder(enabled: Boolean, time: String)
    suspend fun setBudgetAlert(enabled: Boolean)
    suspend fun setAutoCheckUpdates(enabled: Boolean)
    suspend fun setUpdateInfo(
        hasUpdate: Boolean,
        version: String,
        releaseUrl: String,
        notes: String
    )
}
