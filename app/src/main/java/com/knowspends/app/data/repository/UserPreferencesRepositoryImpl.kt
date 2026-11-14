package com.knowspends.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

        // Regional & Formatting
        val NUMBER_FORMAT = stringPreferencesKey("number_format")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val FIRST_DAY_OF_WEEK = stringPreferencesKey("first_day_of_week")
        val MONTH_START_DAY = intPreferencesKey("month_start_day")

        // Budget Preferences
        val BUDGET_PERIOD = stringPreferencesKey("budget_period")
        val BUDGET_ROLLOVER_ENABLED = booleanPreferencesKey("budget_rollover_enabled")
        val BUDGET_ALERT_THRESHOLD = intPreferencesKey("budget_alert_threshold")

        // Appearance
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val FONT_ID = stringPreferencesKey("font_id")

        // Notifications
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val DAILY_REMINDER_TIME = stringPreferencesKey("daily_reminder_time")
        val BUDGET_ALERT_ENABLED = booleanPreferencesKey("budget_alert_enabled")
        val BILL_REMINDERS_ENABLED = booleanPreferencesKey("bill_reminders_enabled")
        val SUMMARY_REPORTS_ENABLED = booleanPreferencesKey("summary_reports_enabled")

        // Security & Privacy
        val HIDE_BALANCES_ON_HOME = booleanPreferencesKey("hide_balances_on_home")
        val BLOCK_SCREENSHOTS = booleanPreferencesKey("block_screenshots")

        // Updates
        val AUTO_CHECK_UPDATES = booleanPreferencesKey("auto_check_updates")
        val LAST_CHECKED_TIMESTAMP = longPreferencesKey("last_checked_timestamp")
        val UPDATE_AVAILABLE = booleanPreferencesKey("update_available")
        val LATEST_VERSION_NAME = stringPreferencesKey("latest_version_name")
        val LATEST_RELEASE_URL = stringPreferencesKey("latest_release_url")
        val LATEST_RELEASE_NOTES = stringPreferencesKey("latest_release_notes")

        // Tap to Add (Dynamic Island & Back Tap)
        val BACK_TAP_ENABLED = booleanPreferencesKey("back_tap_enabled")
        val BACK_TAP_SENSITIVITY = stringPreferencesKey("back_tap_sensitivity")

        // Smart SMS Auto-Tracking
        val SMS_AUTO_CAPTURE_ENABLED = booleanPreferencesKey("sms_auto_capture_enabled")
        val SMS_DIRECT_AUTO_ADD = booleanPreferencesKey("sms_direct_auto_add")
        val SMS_LAST_SCAN_TIMESTAMP = longPreferencesKey("sms_last_scan_timestamp")

        // Data & Sync
        val AUTO_BACKUP_FREQUENCY = stringPreferencesKey("auto_backup_frequency")

        // Feedback & Haptics
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
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

            numberFormat = prefs[Keys.NUMBER_FORMAT] ?: "indian",
            dateFormat = prefs[Keys.DATE_FORMAT] ?: "dd MMM yyyy",
            firstDayOfWeek = prefs[Keys.FIRST_DAY_OF_WEEK] ?: "monday",
            monthStartDay = prefs[Keys.MONTH_START_DAY] ?: 1,

            budgetPeriod = prefs[Keys.BUDGET_PERIOD] ?: "monthly",
            budgetRolloverEnabled = prefs[Keys.BUDGET_ROLLOVER_ENABLED] ?: false,
            budgetAlertThreshold = prefs[Keys.BUDGET_ALERT_THRESHOLD] ?: 80,

            themeMode = prefs[Keys.THEME_MODE] ?: "system",
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
            fontId = prefs[Keys.FONT_ID] ?: "plus_jakarta_sans",

            dailyReminderEnabled = prefs[Keys.DAILY_REMINDER_ENABLED] ?: false,
            dailyReminderTime = prefs[Keys.DAILY_REMINDER_TIME] ?: "20:00",
            budgetAlertEnabled = prefs[Keys.BUDGET_ALERT_ENABLED] ?: true,
            billRemindersEnabled = prefs[Keys.BILL_REMINDERS_ENABLED] ?: true,
            summaryReportsEnabled = prefs[Keys.SUMMARY_REPORTS_ENABLED] ?: true,

            hideBalancesOnHome = prefs[Keys.HIDE_BALANCES_ON_HOME] ?: false,
            blockScreenshots = prefs[Keys.BLOCK_SCREENSHOTS] ?: false,

            autoCheckUpdates = prefs[Keys.AUTO_CHECK_UPDATES] ?: true,
            lastCheckedTimestamp = prefs[Keys.LAST_CHECKED_TIMESTAMP] ?: 0L,
            updateAvailable = prefs[Keys.UPDATE_AVAILABLE] ?: false,
            latestVersionName = prefs[Keys.LATEST_VERSION_NAME] ?: "",
            latestReleaseUrl = prefs[Keys.LATEST_RELEASE_URL] ?: "",
            latestReleaseNotes = prefs[Keys.LATEST_RELEASE_NOTES] ?: "",

            backTapEnabled = prefs[Keys.BACK_TAP_ENABLED] ?: true,
            backTapSensitivity = prefs[Keys.BACK_TAP_SENSITIVITY] ?: "normal",

            smsAutoCaptureEnabled = prefs[Keys.SMS_AUTO_CAPTURE_ENABLED] ?: true,
            smsDirectAutoAdd = prefs[Keys.SMS_DIRECT_AUTO_ADD] ?: false,
            smsLastScanTimestamp = prefs[Keys.SMS_LAST_SCAN_TIMESTAMP] ?: 0L,

            autoBackupFrequency = prefs[Keys.AUTO_BACKUP_FREQUENCY] ?: "weekly",

            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true
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

    override suspend fun setRegionalPreferences(
        currency: String,
        numberFormat: String,
        dateFormat: String,
        firstDayOfWeek: String,
        monthStartDay: Int
    ) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.CURRENCY_SYMBOL] = currency
            prefs[Keys.NUMBER_FORMAT] = numberFormat
            prefs[Keys.DATE_FORMAT] = dateFormat
            prefs[Keys.FIRST_DAY_OF_WEEK] = firstDayOfWeek
            prefs[Keys.MONTH_START_DAY] = monthStartDay
        }
        Unit
    }

    override suspend fun setBudgetPreferences(
        budgetPeriod: String,
        rollover: Boolean,
        alertThreshold: Int,
        savingsGoal: String
    ) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.BUDGET_PERIOD] = budgetPeriod
            prefs[Keys.BUDGET_ROLLOVER_ENABLED] = rollover
            prefs[Keys.BUDGET_ALERT_THRESHOLD] = alertThreshold
            prefs[Keys.MONTHLY_SAVINGS_GOAL] = savingsGoal
        }
        Unit
    }

    override suspend fun setNotificationsPreferences(
        dailyReminder: Boolean,
        reminderTime: String,
        budgetAlerts: Boolean,
        billReminders: Boolean,
        summaryReports: Boolean
    ) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.DAILY_REMINDER_ENABLED] = dailyReminder
            prefs[Keys.DAILY_REMINDER_TIME] = reminderTime
            prefs[Keys.BUDGET_ALERT_ENABLED] = budgetAlerts
            prefs[Keys.BILL_REMINDERS_ENABLED] = billReminders
            prefs[Keys.SUMMARY_REPORTS_ENABLED] = summaryReports
        }
        Unit
    }

    override suspend fun setSecurityPrivacyPreferences(
        hideBalances: Boolean,
        blockScreenshots: Boolean
    ) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.HIDE_BALANCES_ON_HOME] = hideBalances
            prefs[Keys.BLOCK_SCREENSHOTS] = blockScreenshots
        }
        Unit
    }

    override suspend fun setAutoBackupFrequency(
        autoBackupFrequency: String
    ) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.AUTO_BACKUP_FREQUENCY] = autoBackupFrequency
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

    override suspend fun setSmsSettings(enabled: Boolean, directAutoAdd: Boolean) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.SMS_AUTO_CAPTURE_ENABLED] = enabled
            prefs[Keys.SMS_DIRECT_AUTO_ADD] = directAutoAdd
        }
        Unit
    }

    override suspend fun setSmsLastScanTimestamp(timestamp: Long) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.SMS_LAST_SCAN_TIMESTAMP] = timestamp
        }
        Unit
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
        Unit
    }

    override suspend fun setCurrency(currency: String) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.CURRENCY_SYMBOL] = currency
        }
        Unit
    }
}
