package com.knowspends.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_transactions",
    indices = [
        Index("status"),
        Index("date")
    ]
)
data class PendingTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amountMinor: Long,
    val type: String,
    val suggestedPayee: String,
    val accountTail: String? = null,
    val date: Long = System.currentTimeMillis(),
    val rawMessage: String = "",
    val status: String = "PENDING" // PENDING, APPROVED, DISMISSED
)
