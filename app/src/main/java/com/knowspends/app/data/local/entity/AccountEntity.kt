package com.knowspends.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // CASH, BANK, WALLET, CARD, LOAN
    val openingBalanceMinor: Long = 0L, // in paise
    val archived: Boolean = false,
    val sortOrder: Int = 0
)
