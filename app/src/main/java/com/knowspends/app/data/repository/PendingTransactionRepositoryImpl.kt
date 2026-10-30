package com.knowspends.app.data.repository

import com.knowspends.app.data.local.dao.PendingTransactionDao
import com.knowspends.app.data.local.dao.TransactionDao
import com.knowspends.app.data.local.entity.PendingTransactionEntity
import com.knowspends.app.data.local.entity.TransactionEntity
import com.knowspends.app.di.IoDispatcher
import com.knowspends.app.domain.repository.PendingTransactionRepository
import com.knowspends.app.domain.util.BankMessageParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingTransactionRepositoryImpl @Inject constructor(
    private val pendingTransactionDao: PendingTransactionDao,
    private val transactionDao: TransactionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PendingTransactionRepository {

    override fun getPendingTransactions(): Flow<List<PendingTransactionEntity>> {
        return pendingTransactionDao.getPendingTransactionsFlow().flowOn(ioDispatcher)
    }

    override fun getPendingCount(): Flow<Int> {
        return pendingTransactionDao.getPendingCountFlow().flowOn(ioDispatcher)
    }

    override suspend fun parseAndAddMessage(rawText: String): Boolean = withContext(ioDispatcher) {
        val parsed = BankMessageParser.parse(rawText) ?: return@withContext false

        val entity = PendingTransactionEntity(
            amountMinor = parsed.amountMinor,
            type = parsed.type.name,
            suggestedPayee = parsed.merchantOrPayee,
            accountTail = parsed.accountTail,
            date = parsed.dateMillis,
            rawMessage = parsed.rawMessage,
            status = "PENDING"
        )
        pendingTransactionDao.insertPendingTransaction(entity) > 0
    }

    override suspend fun approveTransaction(
        pendingId: Long,
        accountId: Long,
        categoryId: Long?
    ): Long = withContext(ioDispatcher) {
        val pending = pendingTransactionDao.getById(pendingId) ?: return@withContext 0L
        val txId = transactionDao.insertTransaction(
            TransactionEntity(
                accountId = accountId,
                categoryId = categoryId,
                amountMinor = pending.amountMinor,
                type = pending.type,
                payee = pending.suggestedPayee,
                note = "Auto-captured via SMS/Notification",
                date = pending.date
            )
        )
        pendingTransactionDao.updateStatus(pendingId, "APPROVED")
        txId
    }

    override suspend fun dismissTransaction(pendingId: Long) = withContext(ioDispatcher) {
        pendingTransactionDao.updateStatus(pendingId, "DISMISSED")
    }
}
