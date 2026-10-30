package com.knowspends.app.domain.repository

import com.knowspends.app.domain.model.MonthlySummary
import com.knowspends.app.domain.model.Transaction
import com.knowspends.app.domain.model.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactionsWithDetails(): Flow<List<TransactionWithDetails>>
    fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionWithDetails>>
    fun getTransactionWithDetails(id: Long): Flow<TransactionWithDetails?>
    fun getMonthlySummary(startMillis: Long, endMillis: Long): Flow<MonthlySummary>
    fun searchTransactions(query: String): Flow<List<TransactionWithDetails>>
    fun getCategoryBreakdown(startDate: Long, endDate: Long, type: com.knowspends.app.domain.model.TransactionType): Flow<List<com.knowspends.app.domain.model.CategoryReportItem>>
    fun getTopPayees(startDate: Long, endDate: Long, limit: Int = 10): Flow<List<com.knowspends.app.domain.model.PayeeReportItem>>
    fun getMonthlyTrends(startDate: Long, endDate: Long): Flow<List<com.knowspends.app.domain.model.MonthlyTrendItem>>
    suspend fun saveTransaction(transaction: Transaction): Long
    suspend fun softDeleteTransaction(id: Long)
    suspend fun restoreTransaction(id: Long)
}
