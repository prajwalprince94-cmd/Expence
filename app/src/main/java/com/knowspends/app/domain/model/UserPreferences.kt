package com.knowspends.app.domain.model

data class UserPreferences(
    // Profile Identity
    val profileName: String = "My Personal Ledger",
    val profileTagline: String = "Personal Expense Vault",
    val profileAvatarUri: String = "",
    val profileCoverUri: String = "",
    val currencySymbol: String = "₹",

    // Appearance & Dynamic Scale
    val themeMode: String = "system", // "system", "light", "dark", "amoled"
    val dynamicColor: Boolean = false,
    val fontId: String = "plus_jakarta_sans",
    val displayScale: Float = 1.0f, // 0.85f, 1.0f, 1.15f, 1.30f

    // Notifications
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderTime: String = "20:00",
    val budgetAlertEnabled: Boolean = true,

    // Updates
    val autoCheckUpdates: Boolean = true,
    val lastCheckedTimestamp: Long = 0L,
    val updateAvailable: Boolean = false,
    val latestVersionName: String = "",
    val latestReleaseUrl: String = "",
    val latestReleaseNotes: String = ""
)
