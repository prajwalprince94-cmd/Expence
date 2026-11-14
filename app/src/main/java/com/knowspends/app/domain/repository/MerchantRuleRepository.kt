package com.knowspends.app.domain.repository

import com.knowspends.app.domain.model.MerchantRule
import kotlinx.coroutines.flow.Flow

interface MerchantRuleRepository {
    fun getRules(): Flow<List<MerchantRule>>
    suspend fun saveRule(pattern: String, categoryId: Long, categoryName: String, isExactMatch: Boolean = false): MerchantRule
    suspend fun deleteRule(id: String)
    suspend fun matchCategory(merchantOrPayee: String): MerchantRule?
    suspend fun getAllRules(): List<MerchantRule>
    suspend fun restoreRules(rules: List<MerchantRule>)
}
