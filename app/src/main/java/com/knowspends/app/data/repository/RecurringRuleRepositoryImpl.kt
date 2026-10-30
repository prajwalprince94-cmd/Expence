package com.knowspends.app.data.repository

import com.knowspends.app.data.local.dao.AccountDao
import com.knowspends.app.data.local.dao.CategoryDao
import com.knowspends.app.data.local.dao.RecurringRuleDao
import com.knowspends.app.data.local.entity.RecurringRuleEntity
import com.knowspends.app.di.IoDispatcher
import com.knowspends.app.domain.model.RecurrenceFrequency
import com.knowspends.app.domain.model.RecurringRule
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.repository.RecurringRuleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringRuleRepositoryImpl @Inject constructor(
    private val recurringRuleDao: RecurringRuleDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RecurringRuleRepository {

    override fun getActiveRules(): Flow<List<RecurringRule>> {
        return recurringRuleDao.getActiveRulesFlow().map { list ->
            list.map { it.toDomain() }
        }.flowOn(ioDispatcher)
    }

    override fun getAllRules(): Flow<List<RecurringRule>> {
        return recurringRuleDao.getAllRulesFlow().map { list ->
            list.map { it.toDomain() }
        }.flowOn(ioDispatcher)
    }

    override suspend fun getRuleById(id: Long): RecurringRule? = withContext(ioDispatcher) {
        recurringRuleDao.getRuleById(id)?.toDomain()
    }

    override suspend fun saveRule(rule: RecurringRule): Long = withContext(ioDispatcher) {
        val entity = RecurringRuleEntity(
            id = rule.id,
            accountId = rule.accountId,
            categoryId = rule.categoryId,
            amountMinor = rule.amountMinor,
            type = rule.type.name,
            transferAccountId = rule.transferAccountId,
            note = rule.note,
            payee = rule.payee,
            frequency = rule.frequency.name,
            interval = rule.interval,
            dayOfMonth = rule.dayOfMonth,
            nextDueDate = rule.nextDueDate,
            endDate = rule.endDate,
            autoPost = rule.autoPost,
            isActive = rule.isActive
        )
        if (rule.id == 0L) {
            recurringRuleDao.insertRule(entity)
        } else {
            recurringRuleDao.updateRule(entity)
            rule.id
        }
    }

    override suspend fun deleteRule(id: Long) = withContext(ioDispatcher) {
        recurringRuleDao.deleteRule(id)
    }

    override suspend fun setRuleActive(id: Long, isActive: Boolean) = withContext(ioDispatcher) {
        recurringRuleDao.setRuleActive(id, isActive)
    }

    private suspend fun RecurringRuleEntity.toDomain(): RecurringRule {
        val account = accountDao.getAccountByIdOnce(accountId)
        val category = categoryId?.let { categoryDao.getCategoryByIdOnce(it) }
        val transferAccount = transferAccountId?.let { accountDao.getAccountByIdOnce(it) }

        return RecurringRule(
            id = id,
            accountId = accountId,
            accountName = account?.name ?: "Account",
            categoryId = categoryId,
            categoryName = category?.name,
            categoryColour = category?.colour,
            amountMinor = amountMinor,
            type = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.EXPENSE),
            transferAccountId = transferAccountId,
            transferAccountName = transferAccount?.name,
            note = note,
            payee = payee,
            frequency = runCatching { RecurrenceFrequency.valueOf(frequency) }.getOrDefault(RecurrenceFrequency.MONTHLY),
            interval = interval,
            dayOfMonth = dayOfMonth,
            nextDueDate = nextDueDate,
            endDate = endDate,
            autoPost = autoPost,
            isActive = isActive
        )
    }
}
