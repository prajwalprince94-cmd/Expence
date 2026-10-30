package com.knowspends.app.domain.model

enum class RecurrenceFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

data class RecurringRule(
    val id: Long = 0,
    val accountId: Long,
    val accountName: String = "",
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val categoryColour: Long? = null,
    val amountMinor: Long,
    val type: TransactionType,
    val transferAccountId: Long? = null,
    val transferAccountName: String? = null,
    val note: String = "",
    val payee: String = "",
    val frequency: RecurrenceFrequency,
    val interval: Int = 1,
    val dayOfMonth: Int = 1,
    val nextDueDate: Long,
    val endDate: Long? = null,
    val autoPost: Boolean = true,
    val isActive: Boolean = true
)
