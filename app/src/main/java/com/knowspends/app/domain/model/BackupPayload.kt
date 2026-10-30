package com.knowspends.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupPayload(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val exportedAt: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0.0",
    val accounts: List<AccountBackupDto> = emptyList(),
    val categories: List<CategoryBackupDto> = emptyList(),
    val transactions: List<TransactionBackupDto> = emptyList(),
    val budgets: List<BudgetBackupDto> = emptyList(),
    val recurringRules: List<RecurringRuleBackupDto> = emptyList()
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

@Serializable
data class AccountBackupDto(
    val id: Long,
    val name: String,
    val type: String,
    val openingBalanceMinor: Long,
    val archived: Boolean = false,
    val sortOrder: Int = 0
)

@Serializable
data class CategoryBackupDto(
    val id: Long,
    val name: String,
    val kind: String,
    val iconKey: String,
    val colour: Long,
    val parentId: Long? = null,
    val archived: Boolean = false
)

@Serializable
data class TransactionBackupDto(
    val id: Long,
    val accountId: Long,
    val categoryId: Long? = null,
    val amountMinor: Long,
    val type: String,
    val transferAccountId: Long? = null,
    val date: Long,
    val note: String = "",
    val payee: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val importBatchId: String? = null,
    val sourceHash: String? = null
)

@Serializable
data class BudgetBackupDto(
    val id: Long,
    val categoryId: Long,
    val period: String,
    val amountMinor: Long,
    val rollover: Boolean = false
)

@Serializable
data class RecurringRuleBackupDto(
    val id: Long,
    val accountId: Long,
    val categoryId: Long? = null,
    val amountMinor: Long,
    val type: String,
    val transferAccountId: Long? = null,
    val note: String = "",
    val payee: String = "",
    val frequency: String,
    val interval: Int = 1,
    val dayOfMonth: Int = 1,
    val nextDueDate: Long,
    val endDate: Long? = null,
    val autoPost: Boolean = true,
    val isActive: Boolean = true
)

sealed class RestoreResult {
    data class Success(
        val accountsRestored: Int,
        val categoriesRestored: Int,
        val transactionsRestored: Int,
        val budgetsRestored: Int,
        val recurringRulesRestored: Int
    ) : RestoreResult()

    data class Error(val message: String) : RestoreResult()
}
