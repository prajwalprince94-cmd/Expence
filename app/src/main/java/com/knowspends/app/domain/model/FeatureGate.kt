package com.knowspends.app.domain.model

enum class FeatureGate(
    val title: String,
    val description: String
) {
    UNLIMITED_BUDGETS(
        title = "Unlimited Budgets",
        description = "Set category-level budgets and envelope rollover for every expense category"
    ),
    AUTO_CAPTURE_INBOX(
        title = "Smart Review Inbox",
        description = "Auto-parse bank SMS and UPI notifications with 1-tap ledger approval"
    ),
    ADVANCED_REPORTS_EXPORT(
        title = "Advanced Reports & Export",
        description = "Export deep merchant trends, cash-flow analytics, and formatted CSV statements"
    )
}

data class PremiumState(
    val isPremium: Boolean = false
)
