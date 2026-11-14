package com.knowspends.app.data.repository

import androidx.room.withTransaction
import com.knowspends.app.data.local.db.KnowSpendsDatabase
import com.knowspends.app.data.local.entity.AccountEntity
import com.knowspends.app.data.local.entity.BudgetEntity
import com.knowspends.app.data.local.entity.CategoryEntity
import com.knowspends.app.data.local.entity.RecurringRuleEntity
import com.knowspends.app.data.local.entity.TransactionEntity
import com.knowspends.app.di.IoDispatcher
import com.knowspends.app.data.local.entity.PendingTransactionEntity
import com.knowspends.app.data.local.entity.RecurringExecutionEntity
import com.knowspends.app.domain.model.PendingTransactionBackupDto
import com.knowspends.app.domain.model.RecurringExecutionBackupDto
import com.knowspends.app.domain.model.MerchantRule
import com.knowspends.app.domain.model.MerchantRuleBackupDto
import com.knowspends.app.domain.repository.MerchantRuleRepository
import com.knowspends.app.domain.model.AccountBackupDto
import com.knowspends.app.domain.model.BackupPayload
import com.knowspends.app.domain.model.BudgetBackupDto
import com.knowspends.app.domain.model.CategoryBackupDto
import com.knowspends.app.domain.model.RecurringRuleBackupDto
import com.knowspends.app.domain.model.RestoreResult
import com.knowspends.app.domain.model.TransactionBackupDto
import com.knowspends.app.domain.repository.BackupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val database: KnowSpendsDatabase,
    private val merchantRuleRepository: MerchantRuleRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BackupRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun createBackupPayload(): BackupPayload = withContext(ioDispatcher) {
        val accounts = database.accountDao().getAllAccountsFlow().firstOrNull() ?: emptyList()
        val categories = database.categoryDao().getAllCategoriesFlow().firstOrNull() ?: emptyList()
        val transactions = database.transactionDao().getAllTransactionsForBackup()
        val budgets = database.budgetDao().getAllBudgetsForBackup()
        val recurringRules = database.recurringRuleDao().getAllRulesForBackup()
        val recurringExecutions = database.recurringRuleDao().getAllExecutionsForBackup()
        val pendingTransactions = database.pendingTransactionDao().getAllForBackup()
        val merchantRules = merchantRuleRepository.getAllRules()

        BackupPayload(
            schemaVersion = BackupPayload.CURRENT_SCHEMA_VERSION,
            exportedAt = System.currentTimeMillis(),
            appVersion = "1.0.0",
            accounts = accounts.map {
                AccountBackupDto(
                    id = it.id,
                    name = it.name,
                    type = it.type,
                    openingBalanceMinor = it.openingBalanceMinor,
                    archived = it.archived,
                    sortOrder = it.sortOrder
                )
            },
            categories = categories.map {
                CategoryBackupDto(
                    id = it.id,
                    name = it.name,
                    kind = it.kind,
                    iconKey = it.iconKey,
                    colour = it.colour,
                    parentId = it.parentId,
                    archived = it.archived
                )
            },
            transactions = transactions.map {
                TransactionBackupDto(
                    id = it.id,
                    accountId = it.accountId,
                    categoryId = it.categoryId,
                    amountMinor = it.amountMinor,
                    type = it.type,
                    transferAccountId = it.transferAccountId,
                    date = it.date,
                    note = it.note,
                    payee = it.payee,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    deletedAt = it.deletedAt,
                    importBatchId = it.importBatchId,
                    sourceHash = it.sourceHash
                )
            },
            budgets = budgets.map {
                BudgetBackupDto(
                    id = it.id,
                    categoryId = it.categoryId,
                    period = it.period,
                    amountMinor = it.amountMinor,
                    rollover = it.rollover
                )
            },
            recurringRules = recurringRules.map {
                RecurringRuleBackupDto(
                    id = it.id,
                    accountId = it.accountId,
                    categoryId = it.categoryId,
                    amountMinor = it.amountMinor,
                    type = it.type,
                    transferAccountId = it.transferAccountId,
                    note = it.note,
                    payee = it.payee,
                    frequency = it.frequency,
                    interval = it.interval,
                    dayOfMonth = it.dayOfMonth,
                    nextDueDate = it.nextDueDate,
                    endDate = it.endDate,
                    autoPost = it.autoPost,
                    isActive = it.isActive
                )
            },
            merchantRules = merchantRules.map {
                MerchantRuleBackupDto(
                    id = it.id,
                    merchantPattern = it.merchantPattern,
                    categoryId = it.categoryId,
                    categoryName = it.categoryName,
                    isExactMatch = it.isExactMatch,
                    createdAt = it.createdAt
                )
            },
            pendingTransactions = pendingTransactions.map {
                PendingTransactionBackupDto(
                    id = it.id,
                    amountMinor = it.amountMinor,
                    type = it.type,
                    suggestedPayee = it.suggestedPayee,
                    accountTail = it.accountTail,
                    date = it.date,
                    rawMessage = it.rawMessage,
                    status = it.status
                )
            },
            recurringExecutions = recurringExecutions.map {
                RecurringExecutionBackupDto(
                    id = it.id,
                    ruleId = it.ruleId,
                    dueDate = it.dueDate,
                    transactionId = it.transactionId,
                    executedAt = it.executedAt
                )
            }
        )
    }

    override suspend fun exportBackupJson(outputStream: OutputStream) = withContext(ioDispatcher) {
        val payload = createBackupPayload()
        val jsonString = json.encodeToString(payload)
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(jsonString)
            writer.flush()
        }
    }

    override suspend fun restoreBackupJson(inputStream: InputStream): RestoreResult = withContext(ioDispatcher) {
        try {
            val jsonString = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val payload = json.decodeFromString<BackupPayload>(jsonString)

            if (payload.schemaVersion > BackupPayload.CURRENT_SCHEMA_VERSION) {
                return@withContext RestoreResult.Error("Unsupported backup schema version: ${payload.schemaVersion}. Please update KnowSpends.")
            }

            // Capture snapshot of current database state before clearing for atomic rollback if restore fails
            val preRestoreSnapshot = createBackupPayload()

            try {
                database.clearAllTables()
                restorePayload(payload)

                RestoreResult.Success(
                    accountsRestored = payload.accounts.size,
                    categoriesRestored = payload.categories.size,
                    transactionsRestored = payload.transactions.size,
                    budgetsRestored = payload.budgets.size,
                    recurringRulesRestored = payload.recurringRules.size,
                    merchantRulesRestored = payload.merchantRules.size,
                    pendingTransactionsRestored = payload.pendingTransactions.size,
                    recurringExecutionsRestored = payload.recurringExecutions.size
                )
            } catch (restoreException: Exception) {
                // Restore failed mid-way: Roll back to previous snapshot so user data is never lost
                try {
                    database.clearAllTables()
                    restorePayload(preRestoreSnapshot)
                } catch (_: Exception) {
                    // Best-effort rollback
                }
                RestoreResult.Error(restoreException.localizedMessage ?: "Failed to restore backup")
            }
        } catch (e: Exception) {
            RestoreResult.Error(e.localizedMessage ?: "Failed to parse backup")
        }
    }

    private suspend fun restorePayload(payload: BackupPayload) {
        database.withTransaction {
            // Restore accounts
            if (payload.accounts.isNotEmpty()) {
                val accountEntities = payload.accounts.map {
                    AccountEntity(
                        id = it.id,
                        name = it.name,
                        type = it.type,
                        openingBalanceMinor = it.openingBalanceMinor,
                        archived = it.archived,
                        sortOrder = it.sortOrder
                    )
                }
                database.accountDao().insertAccounts(accountEntities)
            }

            // Restore categories
            if (payload.categories.isNotEmpty()) {
                val categoryEntities = payload.categories.map {
                    CategoryEntity(
                        id = it.id,
                        name = it.name,
                        kind = it.kind,
                        iconKey = it.iconKey,
                        colour = it.colour,
                        parentId = it.parentId,
                        archived = it.archived
                    )
                }
                database.categoryDao().insertCategories(categoryEntities)
            }

            // Restore transactions
            if (payload.transactions.isNotEmpty()) {
                val transactionEntities = payload.transactions.map {
                    TransactionEntity(
                        id = it.id,
                        accountId = it.accountId,
                        categoryId = it.categoryId,
                        amountMinor = it.amountMinor,
                        type = it.type,
                        transferAccountId = it.transferAccountId,
                        date = it.date,
                        note = it.note,
                        payee = it.payee,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        deletedAt = it.deletedAt,
                        importBatchId = it.importBatchId,
                        sourceHash = it.sourceHash
                    )
                }
                database.transactionDao().insertTransactions(transactionEntities)
            }

            // Restore budgets
            if (payload.budgets.isNotEmpty()) {
                val budgetEntities = payload.budgets.map {
                    BudgetEntity(
                        id = it.id,
                        categoryId = it.categoryId,
                        period = it.period,
                        amountMinor = it.amountMinor,
                        rollover = it.rollover
                    )
                }
                database.budgetDao().insertBudgets(budgetEntities)
            }

            // Restore recurring rules
            if (payload.recurringRules.isNotEmpty()) {
                val ruleEntities = payload.recurringRules.map {
                    RecurringRuleEntity(
                        id = it.id,
                        accountId = it.accountId,
                        categoryId = it.categoryId,
                        amountMinor = it.amountMinor,
                        type = it.type,
                        transferAccountId = it.transferAccountId,
                        note = it.note,
                        payee = it.payee,
                        frequency = it.frequency,
                        interval = it.interval,
                        dayOfMonth = it.dayOfMonth,
                        nextDueDate = it.nextDueDate,
                        endDate = it.endDate,
                        autoPost = it.autoPost,
                        isActive = it.isActive
                    )
                }
                database.recurringRuleDao().insertRules(ruleEntities)
            }

            // Restore recurring executions
            if (payload.recurringExecutions.isNotEmpty()) {
                val executionEntities = payload.recurringExecutions.map {
                    RecurringExecutionEntity(
                        id = it.id,
                        ruleId = it.ruleId,
                        dueDate = it.dueDate,
                        transactionId = it.transactionId,
                        executedAt = it.executedAt
                    )
                }
                database.recurringRuleDao().insertExecutions(executionEntities)
            }

            // Restore pending transactions
            if (payload.pendingTransactions.isNotEmpty()) {
                val pendingEntities = payload.pendingTransactions.map {
                    PendingTransactionEntity(
                        id = it.id,
                        amountMinor = it.amountMinor,
                        type = it.type,
                        suggestedPayee = it.suggestedPayee,
                        accountTail = it.accountTail,
                        date = it.date,
                        rawMessage = it.rawMessage,
                        status = it.status
                    )
                }
                database.pendingTransactionDao().insertPendingTransactions(pendingEntities)
            }
        }

        // Restore merchant categorization rules
        if (payload.merchantRules.isNotEmpty()) {
            val rules = payload.merchantRules.map {
                MerchantRule(
                    id = it.id,
                    merchantPattern = it.merchantPattern,
                    categoryId = it.categoryId,
                    categoryName = it.categoryName,
                    isExactMatch = it.isExactMatch,
                    createdAt = it.createdAt
                )
            }
            merchantRuleRepository.restoreRules(rules)
        }
    }
}
