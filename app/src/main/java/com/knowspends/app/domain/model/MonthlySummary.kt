package com.knowspends.app.domain.model

data class MonthlySummary(
    val totalExpenseMinor: Long = 0L,
    val totalIncomeMinor: Long = 0L,
    val periodStartMillis: Long = 0L,
    val periodEndMillis: Long = 0L
) {
    val netSavingsMinor: Long
        get() = totalIncomeMinor - totalExpenseMinor
}
