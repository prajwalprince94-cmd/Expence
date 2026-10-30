package com.knowspends.app.domain.repository

import com.knowspends.app.domain.model.CategoryBudget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgetsForPeriod(
        period: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<CategoryBudget>>

    suspend fun setBudget(
        categoryId: Long,
        period: String,
        amountMinor: Long,
        rollover: Boolean = false
    ): Long

    suspend fun deleteBudget(budgetId: Long)
}
