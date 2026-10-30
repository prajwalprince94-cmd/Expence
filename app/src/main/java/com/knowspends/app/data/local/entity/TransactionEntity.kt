package com.knowspends.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index("accountId", "date"),
        Index("categoryId", "date"),
        Index("deletedAt")
    ],
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val categoryId: Long? = null,
    val amountMinor: Long, // 1250 = ₹12.50
    val type: String, // EXPENSE, INCOME, TRANSFER
    val transferAccountId: Long? = null,
    val date: Long, // epoch millis, UTC
    val note: String = "",
    val payee: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null, // Soft delete
    val importBatchId: String? = null,
    val sourceHash: String? = null
)
