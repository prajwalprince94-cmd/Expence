package com.knowspends.app.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.knowspends.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    data class TransactionWithDetailsEntity(
        @Embedded val transaction: TransactionEntity,
        val accountName: String,
        val categoryName: String?,
        val categoryIcon: String?,
        val categoryColour: Long?,
        val transferAccountName: String?
    )

    @Query("""
        SELECT 
            t.*,
            a.name AS accountName,
            c.name AS categoryName,
            c.iconKey AS categoryIcon,
            c.colour AS categoryColour,
            ta.name AS transferAccountName
        FROM transactions t
        INNER JOIN accounts a ON t.accountId = a.id
        LEFT JOIN categories c ON t.categoryId = c.id
        LEFT JOIN accounts ta ON t.transferAccountId = ta.id
        WHERE t.deletedAt IS NULL
        ORDER BY t.date DESC, t.id DESC
    """)
    fun getAllTransactionsWithDetailsFlow(): Flow<List<TransactionWithDetailsEntity>>

    @Query("""
        SELECT 
            t.*,
            a.name AS accountName,
            c.name AS categoryName,
            c.iconKey AS categoryIcon,
            c.colour AS categoryColour,
            ta.name AS transferAccountName
        FROM transactions t
        INNER JOIN accounts a ON t.accountId = a.id
        LEFT JOIN categories c ON t.categoryId = c.id
        LEFT JOIN accounts ta ON t.transferAccountId = ta.id
        WHERE t.deletedAt IS NULL AND t.date >= :startDate AND t.date <= :endDate
        ORDER BY t.date DESC, t.id DESC
    """)
    fun getTransactionsBetweenWithDetailsFlow(startDate: Long, endDate: Long): Flow<List<TransactionWithDetailsEntity>>

    @Query("""
        SELECT 
            t.*,
            a.name AS accountName,
            c.name AS categoryName,
            c.iconKey AS categoryIcon,
            c.colour AS categoryColour,
            ta.name AS transferAccountName
        FROM transactions t
        INNER JOIN accounts a ON t.accountId = a.id
        LEFT JOIN categories c ON t.categoryId = c.id
        LEFT JOIN accounts ta ON t.transferAccountId = ta.id
        WHERE t.id = :id
    """)
    fun getTransactionByIdWithDetailsFlow(id: Long): Flow<TransactionWithDetailsEntity?>

    @Query("""
        SELECT 
            t.*,
            a.name AS accountName,
            c.name AS categoryName,
            c.iconKey AS categoryIcon,
            c.colour AS categoryColour,
            ta.name AS transferAccountName
        FROM transactions t
        INNER JOIN accounts a ON t.accountId = a.id
        LEFT JOIN categories c ON t.categoryId = c.id
        LEFT JOIN accounts ta ON t.transferAccountId = ta.id
        WHERE t.deletedAt IS NULL 
          AND (t.note LIKE '%' || :query || '%' OR t.payee LIKE '%' || :query || '%' OR c.name LIKE '%' || :query || '%')
        ORDER BY t.date DESC, t.id DESC
    """)
    fun searchTransactionsWithDetailsFlow(query: String): Flow<List<TransactionWithDetailsEntity>>

    @Query("""
        SELECT COALESCE(SUM(amountMinor), 0) FROM transactions 
        WHERE type = 'EXPENSE' 
          AND deletedAt IS NULL 
          AND date >= :startDate AND date <= :endDate
    """)
    fun getExpenseSumBetweenFlow(startDate: Long, endDate: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(amountMinor), 0) FROM transactions 
        WHERE type = 'INCOME' 
          AND deletedAt IS NULL 
          AND date >= :startDate AND date <= :endDate
    """)
    fun getIncomeSumBetweenFlow(startDate: Long, endDate: Long): Flow<Long>

    data class CategoryAggregateEntity(
        val categoryId: Long,
        val categoryName: String,
        val categoryIcon: String,
        val categoryColour: Long,
        val totalAmountMinor: Long,
        val transactionCount: Int
    )

    data class PayeeAggregateEntity(
        val payeeName: String,
        val totalAmountMinor: Long,
        val transactionCount: Int
    )

    data class MonthlyAggregateEntity(
        val yearMonth: String,
        val totalExpenseMinor: Long,
        val totalIncomeMinor: Long
    )

    @Query("""
        SELECT 
            c.id AS categoryId,
            c.name AS categoryName,
            c.iconKey AS categoryIcon,
            c.colour AS categoryColour,
            SUM(t.amountMinor) AS totalAmountMinor,
            COUNT(t.id) AS transactionCount
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.type = :type AND t.deletedAt IS NULL AND t.date >= :startDate AND t.date <= :endDate
        GROUP BY c.id
        ORDER BY totalAmountMinor DESC
    """)
    fun getCategoryAggregatesBetweenFlow(
        startDate: Long,
        endDate: Long,
        type: String
    ): Flow<List<CategoryAggregateEntity>>

    @Query("""
        SELECT 
            CASE WHEN TRIM(t.payee) = '' THEN 'Unspecified Payee' ELSE t.payee END AS payeeName,
            SUM(t.amountMinor) AS totalAmountMinor,
            COUNT(t.id) AS transactionCount
        FROM transactions t
        WHERE t.type = 'EXPENSE' AND t.deletedAt IS NULL AND t.date >= :startDate AND t.date <= :endDate
        GROUP BY payeeName
        ORDER BY totalAmountMinor DESC
        LIMIT :limit
    """)
    fun getTopPayeesBetweenFlow(
        startDate: Long,
        endDate: Long,
        limit: Int = 10
    ): Flow<List<PayeeAggregateEntity>>

    @Query("""
        SELECT 
            strftime('%Y-%m', date / 1000, 'unixepoch') AS yearMonth,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amountMinor ELSE 0 END), 0) AS totalExpenseMinor,
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountMinor ELSE 0 END), 0) AS totalIncomeMinor
        FROM transactions
        WHERE deletedAt IS NULL AND date >= :startDate AND date <= :endDate
        GROUP BY yearMonth
        ORDER BY yearMonth ASC
    """)
    fun getMonthlyAggregatesBetweenFlow(
        startDate: Long,
        endDate: Long
    ): Flow<List<MonthlyAggregateEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionByIdOnce(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteTransaction(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE transactions SET deletedAt = NULL WHERE id = :id")
    suspend fun restoreTransaction(id: Long)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun hardDeleteTransaction(id: Long)

    @Query("SELECT * FROM transactions ORDER BY id ASC")
    suspend fun getAllTransactionsForBackup(): List<TransactionEntity>

    @Query("SELECT sourceHash FROM transactions WHERE sourceHash IS NOT NULL")
    suspend fun getAllSourceHashes(): List<String>

    @Query("DELETE FROM transactions WHERE importBatchId = :batchId")
    suspend fun deleteBatchTransactions(batchId: String): Int
}
