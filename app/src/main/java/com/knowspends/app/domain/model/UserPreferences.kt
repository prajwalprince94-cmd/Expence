package com.knowspends.app.domain.model

data class UserPreferences(
    // 1. Profile Identity & Customization
    val profileName: String = "My Personal Ledger",
    val profileTagline: String = "Personal Expense Vault",
    val profileAvatarUri: String = "",
    val profileCoverUri: String = "",
    val currencySymbol: String = "₹",
    val monthlySavingsGoal: String = "20,000",
    val financialYearStart: String = "April",

    // 2. Regional & Formatting
    val numberFormat: String = "indian", // "indian" (Lakhs/Crores) vs "international" (Millions)
    val dateFormat: String = "dd MMM yyyy", // "dd MMM yyyy", "MM/dd/yyyy", "yyyy-MM-dd"
    val firstDayOfWeek: String = "monday", // "monday", "sunday"
    val monthStartDay: Int = 1, // 1..31

    // 3. Budget Preferences
    val budgetPeriod: String = "monthly", // "monthly", "weekly", "custom"
    val budgetRolloverEnabled: Boolean = false,
    val budgetAlertThreshold: Int = 80, // percentage (e.g. 80, 100)

    // 4 & 5. Automation & Sensors
    val backTapEnabled: Boolean = true,
    val backTapSensitivity: String = "normal", // "high", "normal", "low"
    val smsAutoCaptureEnabled: Boolean = true,
    val smsDirectAutoAdd: Boolean = false, // false = Review Inbox first, true = Direct to Ledger
    val smsLastScanTimestamp: Long = 0L,

    // 6. Notifications
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderTime: String = "20:00",
    val budgetAlertEnabled: Boolean = true,
    val billRemindersEnabled: Boolean = true,
    val summaryReportsEnabled: Boolean = true,

    // 7. Security & Privacy
    val hideBalancesOnHome: Boolean = false,
    val blockScreenshots: Boolean = false,

    // 8. Appearance
    val themeMode: String = "system", // "system", "light", "dark", "amoled"
    val dynamicColor: Boolean = false,
    val fontId: String = "plus_jakarta_sans",
    val hapticsEnabled: Boolean = true,

    // 9 & 10. Data & Sync
    val autoBackupFrequency: String = "weekly", // "off", "daily", "weekly"

    // 11. Updates & Metadata
    val autoCheckUpdates: Boolean = true,
    val lastCheckedTimestamp: Long = 0L,
    val updateAvailable: Boolean = false,
    val latestVersionName: String = "",
    val latestReleaseUrl: String = "",
    val latestReleaseNotes: String = ""
)
