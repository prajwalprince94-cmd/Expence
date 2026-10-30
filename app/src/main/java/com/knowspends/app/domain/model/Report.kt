package com.knowspends.app.domain.model

data class CategoryReportItem(
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColour: Long,
    val totalAmountMinor: Long,
    val percentage: Float, // 0f to 100f
    val transactionCount: Int
)

data class PayeeReportItem(
    val payeeName: String,
    val totalAmountMinor: Long,
    val percentage: Float, // 0f to 100f
    val transactionCount: Int
)

data class MonthlyTrendItem(
    val yearMonth: String,
    val monthDisplayLabel: String,
    val expenseMinor: Long,
    val incomeMinor: Long
) {
    val netSavingsMinor: Long
        get() = incomeMinor - expenseMinor
}

enum class DateRangeType(val label: String) {
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    LAST_3_MONTHS("Last 3 Months"),
    THIS_YEAR("This Year"),
    ALL_TIME("All Time")
}
