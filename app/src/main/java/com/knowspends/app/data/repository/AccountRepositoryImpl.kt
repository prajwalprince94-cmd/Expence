package com.knowspends.app.data.repository

import com.knowspends.app.data.local.dao.AccountDao
import com.knowspends.app.data.local.entity.AccountEntity
import com.knowspends.app.di.IoDispatcher
import com.knowspends.app.domain.model.Account
import com.knowspends.app.domain.model.AccountType
import com.knowspends.app.domain.repository.AccountRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AccountRepository {

    override fun getActiveAccounts(): Flow<List<Account>> {
        return accountDao.getActiveAccountsFlow().map { list ->
            list.map { it.toDomain() }
        }.flowOn(ioDispatcher)
    }

    override fun getAllAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccountsFlow().map { list ->
            list.map { it.toDomain() }
        }.flowOn(ioDispatcher)
    }

    override fun getAccount(id: Long): Flow<Account?> {
        return accountDao.getAccountById(id).map { it?.toDomain() }.flowOn(ioDispatcher)
    }

    override fun getActiveAccountsWithBalances(): Flow<List<Account>> {
        return accountDao.getActiveAccountsWithBalanceFlow().map { list ->
            list.map { item ->
                Account(
                    id = item.id,
                    name = item.name,
                    type = runCatching { AccountType.valueOf(item.type) }.getOrDefault(AccountType.BANK),
                    openingBalanceMinor = item.openingBalanceMinor,
                    currentBalanceMinor = item.currentBalanceMinor,
                    archived = item.archived,
                    sortOrder = item.sortOrder
                )
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun saveAccount(account: Account): Long = withContext(ioDispatcher) {
        val entity = AccountEntity(
            id = account.id,
            name = account.name,
            type = account.type.name,
            openingBalanceMinor = account.openingBalanceMinor,
            archived = account.archived,
            sortOrder = account.sortOrder
        )
        if (account.id == 0L) {
            accountDao.insertAccount(entity)
        } else {
            accountDao.updateAccount(entity)
            account.id
        }
    }

    override suspend fun archiveAccount(id: Long, archived: Boolean) = withContext(ioDispatcher) {
        accountDao.setAccountArchived(id, archived)
    }

    private fun AccountEntity.toDomain() = Account(
        id = id,
        name = name,
        type = runCatching { AccountType.valueOf(type) }.getOrDefault(AccountType.BANK),
        openingBalanceMinor = openingBalanceMinor,
        currentBalanceMinor = openingBalanceMinor,
        archived = archived,
        sortOrder = sortOrder
    )
}
