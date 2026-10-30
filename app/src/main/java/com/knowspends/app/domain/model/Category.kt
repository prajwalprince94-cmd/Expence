package com.knowspends.app.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val kind: TransactionType,
    val iconKey: String,
    val colour: Long,
    val parentId: Long? = null,
    val archived: Boolean = false
)
