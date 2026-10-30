package com.knowspends.app.domain.model

enum class BudgetStatus {
    SAFE,       // < 80% spent
    WARNING,    // 80% - 100% spent
    OVERSPENT   // > 100% spent
}

data class CategoryBudget(
    val budgetId: Long = 0,
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColour: Long,
    val period: String,
    val budgetAmountMinor: Long,
    val spentAmountMinor: Long,
    val rollover: Boolean = false
) {
    val remainingAmountMinor: Long
        get() = budgetAmountMinor - spentAmountMinor

    val progressRatio: Float
        get() = if (budgetAmountMinor <= 0) 1f else (spentAmountMinor.toFloat() / budgetAmountMinor.toFloat()).coerceAtLeast(0f)

    val status: BudgetStatus
        get() = when {
            spentAmountMinor > budgetAmountMinor -> BudgetStatus.OVERSPENT
            progressRatio >= 0.80f -> BudgetStatus.WARNING
            else -> BudgetStatus.SAFE
        }
}
