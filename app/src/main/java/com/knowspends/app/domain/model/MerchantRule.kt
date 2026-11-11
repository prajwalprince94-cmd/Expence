package com.knowspends.app.domain.model

import java.util.UUID

data class MerchantRule(
    val id: String = UUID.randomUUID().toString(),
    val merchantPattern: String,
    val categoryId: Long,
    val categoryName: String,
    val isExactMatch: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
