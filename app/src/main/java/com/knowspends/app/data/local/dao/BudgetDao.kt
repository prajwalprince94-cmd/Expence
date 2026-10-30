package com.knowspends.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.knowspends.app.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    data class BudgetWithSpentEntity(
        val budgetId: Long,
        val categoryId: Long,
        val period: String,
        val budgetAmountMinor: Long,
        val rollover: Boolean,
        val categoryName: String,
        val categoryIcon: String,
        val categoryColour: Long,
        val spentAmountMinor: Long
    )

    @Query("""
        SELECT 
            b.id AS budgetId,
            b.categoryId,
            b.period,
            b.amountMinor AS budgetAmountMinor,
            b.rollover,
            c.name AS categoryName,
            c.iconKey AS categoryIcon,
            c.colour AS categoryColour,
            COALESCE(SUM(t.amountMinor), 0) AS spentAmountMinor
        FROM budgets b
        INNER JOIN categories c ON b.categoryId = c.id
        LEFT JOIN transactions t ON t.categoryId = b.categoryId 
            AND t.type = 'EXPENSE' 
            AND t.deletedAt IS NULL 
            AND t.date >= :startDate AND t.date <= :endDate
        WHERE b.period = :period
        GROUP BY b.id
        ORDER BY c.name ASC
    """)
    fun getBudgetsWithSpentFlow(
        period: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<BudgetWithSpentEntity>>

    @Query("SELECT * FROM budgets WHERE period = :period AND categoryId = :categoryId")
    suspend fun getBudget(period: String, categoryId: Long): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE id = :id")
    fun getBudgetByIdFlow(id: Long): Flow<BudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Query("SELECT * FROM budgets ORDER BY id ASC")
    suspend fun getAllBudgetsForBackup(): List<BudgetEntity>

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudget(id: Long)
}
