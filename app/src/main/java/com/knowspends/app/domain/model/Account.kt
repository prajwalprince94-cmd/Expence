package com.knowspends.app.domain.model

enum class AccountType {
    CASH,
    BANK,
    WALLET,
    CARD,
    LOAN
}

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val openingBalanceMinor: Long = 0L,
    val currentBalanceMinor: Long = 0L,
    val archived: Boolean = false,
    val sortOrder: Int = 0
)
