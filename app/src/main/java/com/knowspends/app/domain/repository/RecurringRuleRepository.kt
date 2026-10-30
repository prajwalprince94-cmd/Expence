package com.knowspends.app.domain.repository

import com.knowspends.app.domain.model.RecurringRule
import kotlinx.coroutines.flow.Flow

interface RecurringRuleRepository {
    fun getActiveRules(): Flow<List<RecurringRule>>
    fun getAllRules(): Flow<List<RecurringRule>>
    suspend fun getRuleById(id: Long): RecurringRule?
    suspend fun saveRule(rule: RecurringRule): Long
    suspend fun deleteRule(id: Long)
    suspend fun setRuleActive(id: Long, isActive: Boolean)
}
