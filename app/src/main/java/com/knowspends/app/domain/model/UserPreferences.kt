package com.knowspends.app.domain.model

data class UserPreferences(
    // Profile Identity & Customization
    val profileName: String = "My Personal Ledger",
    val profileTagline: String = "Personal Expense Vault",
    val profileAvatarUri: String = "",
    val profileCoverUri: String = "",
    val currencySymbol: String = "₹",
    val monthlySavingsGoal: String = "20,000",
    val financialYearStart: String = "April",

    // Appearance
    val themeMode: String = "system", // "system", "light", "dark", "amoled"
    val dynamicColor: Boolean = false,
    val fontId: String = "plus_jakarta_sans",

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
    val latestReleaseNotes: String = "",

    // Tap to Add (Dynamic Island & Back Tap)
    val backTapEnabled: Boolean = true,
    val backTapSensitivity: String = "normal", // "high", "normal", "low"

    // Smart SMS Auto-Tracking
    val smsAutoCaptureEnabled: Boolean = true,
    val smsDirectAutoAdd: Boolean = false, // false = Review Inbox first, true = Direct to Ledger
    val smsLastScanTimestamp: Long = 0L,

    // Feedback & Haptics
    val hapticsEnabled: Boolean = true
)
