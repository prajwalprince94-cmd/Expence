package com.knowspends.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val kind: String, // EXPENSE, INCOME
    val iconKey: String, // e.g. "shopping", "groceries", "salary"
    val colour: Long, // ARGB Color stored as Long
    val parentId: Long? = null,
    val archived: Boolean = false
)
