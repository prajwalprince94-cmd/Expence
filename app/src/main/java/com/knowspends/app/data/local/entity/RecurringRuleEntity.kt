package com.knowspends.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_rules",
    indices = [
        Index("accountId"),
        Index("categoryId"),
        Index("nextDueDate")
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
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val categoryId: Long? = null,
    val amountMinor: Long,
    val type: String, // EXPENSE, INCOME, TRANSFER
    val transferAccountId: Long? = null,
    val note: String = "",
    val payee: String = "",
    val frequency: String, // DAILY, WEEKLY, MONTHLY, YEARLY
    val interval: Int = 1,
    val dayOfMonth: Int = 1,
    val nextDueDate: Long, // epoch millis UTC
    val endDate: Long? = null,
    val autoPost: Boolean = true,
    val isActive: Boolean = true
)
