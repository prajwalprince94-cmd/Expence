package com.knowspends.app.ui.subscriptions

import com.knowspends.app.domain.model.Account
import com.knowspends.app.domain.model.Category
import com.knowspends.app.domain.model.RecurringRule
import com.knowspends.app.domain.util.MoneyFormatter

data class SubscriptionsUiState(
    val rules: List<RecurringRule> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedFilter: String = "All", // "All", "Monthly", "Yearly", "Bills"
    val showAddEditDialog: Boolean = false,
    val editingRule: RecurringRule? = null,
    val isLoading: Boolean = false
) {
    val activeRules: List<RecurringRule>
        get() = rules.filter { it.isActive }

    val totalMonthlyBurnMinor: Long
        get() = activeRules.sumOf { rule ->
            when (rule.frequency) {
                com.knowspends.app.domain.model.RecurrenceFrequency.DAILY -> rule.amountMinor * 30
                com.knowspends.app.domain.model.RecurrenceFrequency.WEEKLY -> rule.amountMinor * 4
                com.knowspends.app.domain.model.RecurrenceFrequency.MONTHLY -> rule.amountMinor
                com.knowspends.app.domain.model.RecurrenceFrequency.YEARLY -> rule.amountMinor / 12
            }
        }

    val totalYearlyProjectionMinor: Long
        get() = totalMonthlyBurnMinor * 12

    val formattedMonthlyBurn: String
        get() = MoneyFormatter.formatPaise(totalMonthlyBurnMinor, includeSymbol = true)

    val formattedYearlyProjection: String
        get() = MoneyFormatter.formatPaise(totalYearlyProjectionMinor, includeSymbol = true)

    val activeSubscriptionsCount: Int
        get() = activeRules.size

    val upcomingRenewals: List<RecurringRule>
        get() = activeRules.sortedBy { it.nextDueDate }

    val nextDueSubscription: RecurringRule?
        get() = activeRules.minByOrNull { it.nextDueDate }

    val daysUntilNextDue: Int?
        get() = nextDueSubscription?.let {
            val diff = it.nextDueDate - System.currentTimeMillis()
            maxOf(0, (diff / (1000 * 60 * 60 * 24)).toInt())
        }

    val filteredRules: List<RecurringRule>
        get() = when (selectedFilter) {
            "Monthly" -> rules.filter { it.frequency == com.knowspends.app.domain.model.RecurrenceFrequency.MONTHLY }
            "Yearly" -> rules.filter { it.frequency == com.knowspends.app.domain.model.RecurrenceFrequency.YEARLY }
            "Active" -> rules.filter { it.isActive }
            "Paused" -> rules.filter { !it.isActive }
            else -> rules
        }.sortedWith(compareByDescending<RecurringRule> { it.isActive }.thenBy { it.nextDueDate })
}
