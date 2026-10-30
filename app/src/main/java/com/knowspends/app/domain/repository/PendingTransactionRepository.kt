package com.knowspends.app.domain.repository

import com.knowspends.app.data.local.entity.PendingTransactionEntity
import kotlinx.coroutines.flow.Flow

interface PendingTransactionRepository {
    fun getPendingTransactions(): Flow<List<PendingTransactionEntity>>
    fun getPendingCount(): Flow<Int>
    suspend fun parseAndAddMessage(rawText: String): Boolean
    suspend fun approveTransaction(pendingId: Long, accountId: Long, categoryId: Long?): Long
    suspend fun dismissTransaction(pendingId: Long)
}
