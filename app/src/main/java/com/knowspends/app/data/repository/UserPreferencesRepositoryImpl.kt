package com.knowspends.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.knowspends.app.di.IoDispatcher
import com.knowspends.app.domain.model.UserPreferences
import com.knowspends.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserPreferencesRepository {

    private object Keys {
        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val PROFILE_TAGLINE = stringPreferencesKey("profile_tagline")
        val PROFILE_AVATAR_URI = stringPreferencesKey("profile_avatar_uri")
        val PROFILE_COVER_URI = stringPreferencesKey("profile_cover_uri")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val MONTHLY_SAVINGS_GOAL = stringPreferencesKey("monthly_savings_goal")
        val FINANCIAL_YEAR_START = stringPreferencesKey("financial_year_start")

        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val FONT_ID = stringPreferencesKey("font_id")

        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val DAILY_REMINDER_TIME = stringPreferencesKey("daily_reminder_time")
        val BUDGET_ALERT_ENABLED = booleanPreferencesKey("budget_alert_enabled")

        val AUTO_CHECK_UPDATES = booleanPreferencesKey("auto_check_updates")
        val LAST_CHECKED_TIMESTAMP = longPreferencesKey("last_checked_timestamp")
        val UPDATE_AVAILABLE = booleanPreferencesKey("update_available")
        val LATEST_VERSION_NAME = stringPreferencesKey("latest_version_name")
        val LATEST_RELEASE_URL = stringPreferencesKey("latest_release_url")
        val LATEST_RELEASE_NOTES = stringPreferencesKey("latest_release_notes")

        val BACK_TAP_ENABLED = booleanPreferencesKey("back_tap_enabled")
        val BACK_TAP_SENSITIVITY = stringPreferencesKey("back_tap_sensitivity")
    }

    override val userPreferencesFlow: Flow<UserPreferences> = context.userPreferencesDataStore.data.map { prefs ->
        UserPreferences(
            profileName = prefs[Keys.PROFILE_NAME] ?: "My Personal Ledger",
            profileTagline = prefs[Keys.PROFILE_TAGLINE] ?: "Personal Expense Vault",
            profileAvatarUri = prefs[Keys.PROFILE_AVATAR_URI] ?: "",
            profileCoverUri = prefs[Keys.PROFILE_COVER_URI] ?: "",
            currencySymbol = prefs[Keys.CURRENCY_SYMBOL] ?: "₹",
            monthlySavingsGoal = prefs[Keys.MONTHLY_SAVINGS_GOAL] ?: "20,000",
            financialYearStart = prefs[Keys.FINANCIAL_YEAR_START] ?: "April",

            themeMode = prefs[Keys.THEME_MODE] ?: "system",
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
            fontId = prefs[Keys.FONT_ID] ?: "plus_jakarta_sans",

            dailyReminderEnabled = prefs[Keys.DAILY_REMINDER_ENABLED] ?: false,
            dailyReminderTime = prefs[Keys.DAILY_REMINDER_TIME] ?: "20:00",
            budgetAlertEnabled = prefs[Keys.BUDGET_ALERT_ENABLED] ?: true,

            autoCheckUpdates = prefs[Keys.AUTO_CHECK_UPDATES] ?: true,
            lastCheckedTimestamp = prefs[Keys.LAST_CHECKED_TIMESTAMP] ?: 0L,
            updateAvailable = prefs[Keys.UPDATE_AVAILABLE] ?: false,
            latestVersionName = prefs[Keys.LATEST_VERSION_NAME] ?: "",
            latestReleaseUrl = prefs[Keys.LATEST_RELEASE_URL] ?: "",
            latestReleaseNotes = prefs[Keys.LATEST_RELEASE_NOTES] ?: "",

            backTapEnabled = prefs[Keys.BACK_TAP_ENABLED] ?: true,
            backTapSensitivity = prefs[Keys.BACK_TAP_SENSITIVITY] ?: "normal"
        )
    }.flowOn(ioDispatcher)

    override suspend fun updateProfile(
        name: String,
        tagline: String,
        avatarUri: String,
        coverUri: String,
        currency: String,
        savingsGoal: String,
        financialYearStart: String
    ) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.PROFILE_NAME] = name
            prefs[Keys.PROFILE_TAGLINE] = tagline
            prefs[Keys.PROFILE_AVATAR_URI] = avatarUri
            prefs[Keys.PROFILE_COVER_URI] = coverUri
            prefs[Keys.CURRENCY_SYMBOL] = currency
            prefs[Keys.MONTHLY_SAVINGS_GOAL] = savingsGoal
            prefs[Keys.FINANCIAL_YEAR_START] = financialYearStart
        }
        Unit
    }

    override suspend fun setThemeMode(themeMode: String) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = themeMode
        }
        Unit
    }

    override suspend fun setDynamicColor(enabled: Boolean) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.DYNAMIC_COLOR] = enabled
        }
        Unit
    }

    override suspend fun setFontId(fontId: String) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.FONT_ID] = fontId
        }
        Unit
    }

    override suspend fun setDailyReminder(enabled: Boolean, time: String) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.DAILY_REMINDER_ENABLED] = enabled
            prefs[Keys.DAILY_REMINDER_TIME] = time
        }
        Unit
    }

    override suspend fun setBudgetAlert(enabled: Boolean) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.BUDGET_ALERT_ENABLED] = enabled
        }
        Unit
    }

    override suspend fun setAutoCheckUpdates(enabled: Boolean) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.AUTO_CHECK_UPDATES] = enabled
        }
        Unit
    }

    override suspend fun setUpdateInfo(
        hasUpdate: Boolean,
        version: String,
        releaseUrl: String,
        notes: String
    ) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.LAST_CHECKED_TIMESTAMP] = System.currentTimeMillis()
            prefs[Keys.UPDATE_AVAILABLE] = hasUpdate
            prefs[Keys.LATEST_VERSION_NAME] = version
            prefs[Keys.LATEST_RELEASE_URL] = releaseUrl
            prefs[Keys.LATEST_RELEASE_NOTES] = notes
        }
        Unit
    }

    override suspend fun setBackTapSettings(enabled: Boolean, sensitivity: String) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.BACK_TAP_ENABLED] = enabled
            prefs[Keys.BACK_TAP_SENSITIVITY] = sensitivity
        }
        Unit
    }
}
