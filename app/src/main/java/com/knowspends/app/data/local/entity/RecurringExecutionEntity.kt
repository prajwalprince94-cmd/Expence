package com.knowspends.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_executions",
    indices = [
        Index(value = ["ruleId", "dueDate"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = RecurringRuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["ruleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecurringExecutionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleId: Long,
    val dueDate: Long, // epoch day or midnight millis
    val transactionId: Long? = null,
    val executedAt: Long = System.currentTimeMillis()
)
