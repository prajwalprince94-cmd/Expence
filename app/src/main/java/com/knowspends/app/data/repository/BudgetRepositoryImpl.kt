package com.knowspends.app.data.repository

import com.knowspends.app.data.local.dao.BudgetDao
import com.knowspends.app.data.local.entity.BudgetEntity
import com.knowspends.app.di.IoDispatcher
import com.knowspends.app.domain.model.CategoryBudget
import com.knowspends.app.domain.repository.BudgetRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BudgetRepository {

    override fun getBudgetsForPeriod(
        period: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<CategoryBudget>> {
        return budgetDao.getBudgetsWithSpentFlow(period, startDate, endDate).map { list ->
            list.map { item ->
                CategoryBudget(
                    budgetId = item.budgetId,
                    categoryId = item.categoryId,
                    categoryName = item.categoryName,
                    categoryIcon = item.categoryIcon,
                    categoryColour = item.categoryColour,
                    period = item.period,
                    budgetAmountMinor = item.budgetAmountMinor,
                    spentAmountMinor = item.spentAmountMinor,
                    rollover = item.rollover
                )
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun setBudget(
        categoryId: Long,
        period: String,
        amountMinor: Long,
        rollover: Boolean
    ): Long = withContext(ioDispatcher) {
        val existing = budgetDao.getBudget(period, categoryId)
        val entity = BudgetEntity(
            id = existing?.id ?: 0L,
            categoryId = categoryId,
            period = period,
            amountMinor = amountMinor,
            rollover = rollover
        )
        budgetDao.insertOrUpdateBudget(entity)
    }

    override suspend fun deleteBudget(budgetId: Long) = withContext(ioDispatcher) {
        budgetDao.deleteBudget(budgetId)
    }
}
