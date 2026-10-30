package com.knowspends.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.knowspends.app.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE archived = 0 ORDER BY sortOrder ASC, id ASC")
    fun getActiveAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY archived ASC, sortOrder ASC")
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getAccountById(id: Long): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountByIdOnce(id: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET archived = :archived WHERE id = :id")
    suspend fun setAccountArchived(id: Long, archived: Boolean)

    data class AccountWithBalanceEntity(
        val id: Long,
        val name: String,
        val type: String,
        val openingBalanceMinor: Long,
        val archived: Boolean,
        val sortOrder: Int,
        val currentBalanceMinor: Long
    )

    @Query("""
        SELECT a.id, a.name, a.type, a.openingBalanceMinor, a.archived, a.sortOrder,
               (a.openingBalanceMinor + COALESCE(SUM(
                   CASE 
                       WHEN t.type = 'INCOME' AND t.accountId = a.id THEN t.amountMinor
                       WHEN t.type = 'EXPENSE' AND t.accountId = a.id THEN -t.amountMinor
                       WHEN t.type = 'TRANSFER' AND t.transferAccountId = a.id THEN t.amountMinor
                       WHEN t.type = 'TRANSFER' AND t.accountId = a.id THEN -t.amountMinor
                       ELSE 0
                   END
               ), 0)) AS currentBalanceMinor
        FROM accounts a
        LEFT JOIN transactions t ON (t.accountId = a.id OR t.transferAccountId = a.id) AND t.deletedAt IS NULL
        WHERE a.archived = 0
        GROUP BY a.id
        ORDER BY a.sortOrder ASC, a.id ASC
    """)
    fun getActiveAccountsWithBalanceFlow(): Flow<List<AccountWithBalanceEntity>>

    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN type = 'INCOME' AND accountId = :accountId THEN amountMinor
                WHEN type = 'EXPENSE' AND accountId = :accountId THEN -amountMinor
                WHEN type = 'TRANSFER' AND transferAccountId = :accountId THEN amountMinor
                WHEN type = 'TRANSFER' AND accountId = :accountId THEN -amountMinor
                ELSE 0
            END
        ), 0)
        FROM transactions 
        WHERE (accountId = :accountId OR transferAccountId = :accountId)
          AND deletedAt IS NULL
    """)
    fun getDerivedTransactionDeltaFlow(accountId: Long): Flow<Long>
}
