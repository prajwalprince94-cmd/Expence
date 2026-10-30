package com.knowspends.app.domain.model

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

data class Transaction(
    val id: Long = 0,
    val accountId: Long,
    val categoryId: Long? = null,
    val amountMinor: Long, // paise: 1250 = ₹12.50
    val type: TransactionType,
    val transferAccountId: Long? = null,
    val date: Long, // epoch millis UTC
    val note: String = "",
    val payee: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)

data class TransactionWithDetails(
    val transaction: Transaction,
    val accountName: String,
    val categoryName: String? = null,
    val categoryIcon: String? = null,
    val categoryColour: Long? = null,
    val transferAccountName: String? = null
)
