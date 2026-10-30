package com.knowspends.app.domain.repository

import com.knowspends.app.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getActiveAccounts(): Flow<List<Account>>
    fun getAllAccounts(): Flow<List<Account>>
    fun getAccount(id: Long): Flow<Account?>
    fun getActiveAccountsWithBalances(): Flow<List<Account>>
    suspend fun saveAccount(account: Account): Long
    suspend fun archiveAccount(id: Long, archived: Boolean)
}
