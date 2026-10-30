package com.knowspends.app.data.repository

import com.knowspends.app.data.local.dao.TransactionDao
import com.knowspends.app.data.local.dao.TransactionDao.TransactionWithDetailsEntity
import com.knowspends.app.data.local.entity.TransactionEntity
import com.knowspends.app.di.IoDispatcher
import com.knowspends.app.domain.model.MonthlySummary
import com.knowspends.app.domain.model.Transaction
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.model.TransactionWithDetails
import com.knowspends.app.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TransactionRepository {

    override fun getAllTransactionsWithDetails(): Flow<List<TransactionWithDetails>> {
        return transactionDao.getAllTransactionsWithDetailsFlow().map { list ->
            list.map { it.toDomain() }
        }.flowOn(ioDispatcher)
    }

    override fun getTransactionsBetween(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<TransactionWithDetails>> {
        return transactionDao.getTransactionsBetweenWithDetailsFlow(startMillis, endMillis).map { list ->
            list.map { it.toDomain() }
        }.flowOn(ioDispatcher)
    }

    override fun getTransactionWithDetails(id: Long): Flow<TransactionWithDetails?> {
        return transactionDao.getTransactionByIdWithDetailsFlow(id).map { it?.toDomain() }.flowOn(ioDispatcher)
    }

    override fun getMonthlySummary(
        startMillis: Long,
        endMillis: Long
    ): Flow<MonthlySummary> {
        val expenseFlow = transactionDao.getExpenseSumBetweenFlow(startMillis, endMillis)
        val incomeFlow = transactionDao.getIncomeSumBetweenFlow(startMillis, endMillis)

        return combine(expenseFlow, incomeFlow) { expense, income ->
            MonthlySummary(
                totalExpenseMinor = expense,
                totalIncomeMinor = income,
                periodStartMillis = startMillis,
                periodEndMillis = endMillis
            )
        }.flowOn(ioDispatcher)
    }

    override fun searchTransactions(query: String): Flow<List<TransactionWithDetails>> {
        return transactionDao.searchTransactionsWithDetailsFlow(query).map { list ->
            list.map { it.toDomain() }
        }.flowOn(ioDispatcher)
    }

    override fun getCategoryBreakdown(
        startDate: Long,
        endDate: Long,
        type: TransactionType
    ): Flow<List<com.knowspends.app.domain.model.CategoryReportItem>> {
        return transactionDao.getCategoryAggregatesBetweenFlow(startDate, endDate, type.name).map { list ->
            val total = list.sumOf { it.totalAmountMinor }
            list.map { entity ->
                com.knowspends.app.domain.model.CategoryReportItem(
                    categoryId = entity.categoryId,
                    categoryName = entity.categoryName,
                    categoryIcon = entity.categoryIcon,
                    categoryColour = entity.categoryColour,
                    totalAmountMinor = entity.totalAmountMinor,
                    percentage = if (total > 0L) (entity.totalAmountMinor.toFloat() / total.toFloat()) * 100f else 0f,
                    transactionCount = entity.transactionCount
                )
            }
        }.flowOn(ioDispatcher)
    }

    override fun getTopPayees(
        startDate: Long,
        endDate: Long,
        limit: Int
    ): Flow<List<com.knowspends.app.domain.model.PayeeReportItem>> {
        return transactionDao.getTopPayeesBetweenFlow(startDate, endDate, limit).map { list ->
            val total = list.sumOf { it.totalAmountMinor }
            list.map { entity ->
                com.knowspends.app.domain.model.PayeeReportItem(
                    payeeName = entity.payeeName,
                    totalAmountMinor = entity.totalAmountMinor,
                    percentage = if (total > 0L) (entity.totalAmountMinor.toFloat() / total.toFloat()) * 100f else 0f,
                    transactionCount = entity.transactionCount
                )
            }
        }.flowOn(ioDispatcher)
    }

    override fun getMonthlyTrends(
        startDate: Long,
        endDate: Long
    ): Flow<List<com.knowspends.app.domain.model.MonthlyTrendItem>> {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("MMM ''yy", java.util.Locale.getDefault())

        return transactionDao.getMonthlyAggregatesBetweenFlow(startDate, endDate).map { list ->
            list.map { entity ->
                val displayLabel = runCatching {
                    val parsedDate = inputFormat.parse(entity.yearMonth)
                    if (parsedDate != null) outputFormat.format(parsedDate) else entity.yearMonth
                }.getOrDefault(entity.yearMonth)

                com.knowspends.app.domain.model.MonthlyTrendItem(
                    yearMonth = entity.yearMonth,
                    monthDisplayLabel = displayLabel,
                    expenseMinor = entity.totalExpenseMinor,
                    incomeMinor = entity.totalIncomeMinor
                )
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun saveTransaction(transaction: Transaction): Long = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        val entity = TransactionEntity(
            id = transaction.id,
            accountId = transaction.accountId,
            categoryId = transaction.categoryId,
            amountMinor = transaction.amountMinor,
            type = transaction.type.name,
            transferAccountId = transaction.transferAccountId,
            date = transaction.date,
            note = transaction.note,
            payee = transaction.payee,
            createdAt = if (transaction.id == 0L) now else transaction.createdAt,
            updatedAt = now,
            deletedAt = transaction.deletedAt
        )
        if (transaction.id == 0L) {
            transactionDao.insertTransaction(entity)
        } else {
            transactionDao.updateTransaction(entity)
            transaction.id
        }
    }

    override suspend fun softDeleteTransaction(id: Long) = withContext(ioDispatcher) {
        transactionDao.softDeleteTransaction(id)
    }

    override suspend fun restoreTransaction(id: Long) = withContext(ioDispatcher) {
        transactionDao.restoreTransaction(id)
    }

    private fun TransactionWithDetailsEntity.toDomain() = TransactionWithDetails(
        transaction = Transaction(
            id = transaction.id,
            accountId = transaction.accountId,
            categoryId = transaction.categoryId,
            amountMinor = transaction.amountMinor,
            type = runCatching { TransactionType.valueOf(transaction.type) }.getOrDefault(TransactionType.EXPENSE),
            transferAccountId = transaction.transferAccountId,
            date = transaction.date,
            note = transaction.note,
            payee = transaction.payee,
            createdAt = transaction.createdAt,
            updatedAt = transaction.updatedAt,
            deletedAt = transaction.deletedAt
        ),
        accountName = accountName,
        categoryName = categoryName,
        categoryIcon = categoryIcon,
        categoryColour = categoryColour,
        transferAccountName = transferAccountName
    )
}
