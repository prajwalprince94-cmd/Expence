package com.knowspends.app.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.graphics.vector.ImageVector

enum class SettingsGroup(val title: String) {
    IDENTITY("Account & Localization"),
    FINANCIAL("Financial Engine"),
    ALERTS_PRIVACY("Alerts & Privacy"),
    VISUAL_DATA("Visuals & Data"),
    INFO("Information")
}

enum class SettingsSection(
    val title: String,
    val subtitle: String,
    val group: SettingsGroup,
    val icon: ImageVector,
    val keywords: List<String>
) {
    ACCOUNT_PROFILE(
        title = "Account & Profile",
        subtitle = "Name, avatar, tagline & local profile switcher",
        group = SettingsGroup.IDENTITY,
        icon = Icons.Default.Person,
        keywords = listOf("name", "profile", "avatar", "photo", "tagline", "vault", "user", "email")
    ),
    CURRENCY_REGIONAL(
        title = "Currency & Regional",
        subtitle = "₹ (INR), Lakhs/Crores, month cycle start date",
        group = SettingsGroup.IDENTITY,
        icon = Icons.Default.CurrencyExchange,
        keywords = listOf("currency", "rupee", "inr", "lakhs", "crores", "date", "week", "month", "salary", "payday")
    ),
    BUDGET_PREFERENCES(
        title = "Budget Preferences",
        subtitle = "Budget period, rollover surplus & alert limits",
        group = SettingsGroup.FINANCIAL,
        icon = Icons.Default.AccountBalanceWallet,
        keywords = listOf("budget", "rollover", "period", "warning", "threshold", "alert", "envelope", "savings")
    ),
    CATEGORIES_ACCOUNTS(
        title = "Categories & Accounts",
        subtitle = "Manage categories, wallets, bank accounts & payees",
        group = SettingsGroup.FINANCIAL,
        icon = Icons.Default.AccountBalance,
        keywords = listOf("category", "categories", "accounts", "wallets", "banks", "upi", "cards", "payees", "rules")
    ),
    RECURRING_AUTOMATION(
        title = "Recurring & Automation",
        subtitle = "Subscriptions, SMS auto-capture & back-tap",
        group = SettingsGroup.FINANCIAL,
        icon = Icons.Default.Repeat,
        keywords = listOf("recurring", "subscriptions", "bills", "emi", "sms", "auto", "capture", "back-tap", "island")
    ),
    NOTIFICATIONS(
        title = "Notifications & Reminders",
        subtitle = "Daily expense reminder & budget alerts",
        group = SettingsGroup.ALERTS_PRIVACY,
        icon = Icons.Default.Notifications,
        keywords = listOf("notifications", "reminders", "daily", "alerts", "digest", "bills", "time")
    ),
    SECURITY_PRIVACY(
        title = "Security & Privacy",
        subtitle = "App lock, biometric, hide balances & privacy mode",
        group = SettingsGroup.ALERTS_PRIVACY,
        icon = Icons.Default.Security,
        keywords = listOf("security", "privacy", "lock", "biometric", "fingerprint", "pin", "hide", "balances", "screenshot")
    ),
    APPEARANCE(
        title = "Appearance & Themes",
        subtitle = "Dark/Light mode, dynamic color & haptics",
        group = SettingsGroup.VISUAL_DATA,
        icon = Icons.Default.Palette,
        keywords = listOf("theme", "appearance", "dark", "light", "amoled", "color", "dynamic", "haptics", "vibration")
    ),
    DATA_MANAGEMENT(
        title = "Data Management",
        subtitle = "Backup, restore, CSV export & clear ledger",
        group = SettingsGroup.VISUAL_DATA,
        icon = Icons.Default.Backup,
        keywords = listOf("data", "backup", "restore", "export", "csv", "excel", "pdf", "import", "clear", "reset", "erase")
    ),
    SYNC_CLOUD(
        title = "Sync & Cloud",
        subtitle = "100% offline local-first notice & Drive backup",
        group = SettingsGroup.VISUAL_DATA,
        icon = Icons.Default.CloudDone,
        keywords = listOf("sync", "cloud", "offline", "local", "privacy", "google drive", "wifi")
    ),
    ABOUT_SUPPORT(
        title = "About & Support",
        subtitle = "KnowSpends v1.4.2, changelog, licenses & help",
        group = SettingsGroup.INFO,
        icon = Icons.Default.Info,
        keywords = listOf("about", "version", "support", "help", "changelog", "licenses", "github", "faq", "terms")
    )
}
