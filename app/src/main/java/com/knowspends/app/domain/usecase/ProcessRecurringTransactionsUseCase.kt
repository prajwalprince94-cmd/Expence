package com.knowspends.app.domain.usecase

import com.knowspends.app.data.local.dao.RecurringRuleDao
import com.knowspends.app.data.local.dao.TransactionDao
import com.knowspends.app.data.local.entity.RecurringExecutionEntity
import com.knowspends.app.data.local.entity.TransactionEntity
import com.knowspends.app.di.IoDispatcher
import com.knowspends.app.domain.model.RecurrenceFrequency
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessRecurringTransactionsUseCase @Inject constructor(
    private val recurringRuleDao: RecurringRuleDao,
    private val transactionDao: TransactionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Inspects active recurring rules and generates missing occurrences up to [currentTimeMillis].
     * Protected by unique index (ruleId, dueDate) against duplicate executions and missed runs.
     * Accurately handles month-end clamping (e.g., 31st clamps to 28th/30th).
     */
    suspend operator fun invoke(currentTimeMillis: Long = System.currentTimeMillis()): Int = withContext(ioDispatcher) {
        val dueRules = recurringRuleDao.getDueRules(currentTimeMillis)
        var generatedCount = 0

        for (rule in dueRules) {
            var currentDue = rule.nextDueDate
            val calendar = Calendar.getInstance().apply { timeInMillis = currentDue }
            val preferredDayOfMonth = rule.dayOfMonth

            while (currentDue <= currentTimeMillis && (rule.endDate == null || currentDue <= rule.endDate)) {
                val hasExecuted = recurringRuleDao.hasExecuted(rule.id, currentDue) > 0

                if (!hasExecuted) {
                    var transactionId: Long? = null

                    if (rule.autoPost) {
                        val transaction = TransactionEntity(
                            accountId = rule.accountId,
                            categoryId = rule.categoryId,
                            amountMinor = rule.amountMinor,
                            type = rule.type,
                            transferAccountId = rule.transferAccountId,
                            date = currentDue,
                            note = rule.note,
                            payee = rule.payee
                        )
                        transactionId = transactionDao.insertTransaction(transaction)
                        generatedCount++
                    }

                    recurringRuleDao.insertExecution(
                        RecurringExecutionEntity(
                            ruleId = rule.id,
                            dueDate = currentDue,
                            transactionId = transactionId
                        )
                    )
                }

                // Advance to next cycle
                val nextDue = calculateNextDueDate(
                    calendar = calendar,
                    frequency = runCatching { RecurrenceFrequency.valueOf(rule.frequency) }.getOrDefault(RecurrenceFrequency.MONTHLY),
                    interval = rule.interval,
                    preferredDayOfMonth = preferredDayOfMonth
                )

                if (nextDue <= currentDue) {
                    // Prevent infinite loop if calculation fails to advance
                    break
                }
                currentDue = nextDue

                recurringRuleDao.updateNextDueDate(rule.id, currentDue)

                // Deactivate rule if past end date
                if (rule.endDate != null && currentDue > rule.endDate) {
                    recurringRuleDao.setRuleActive(rule.id, false)
                    break
                }
            }
        }

        generatedCount
    }

    /**
     * Calculates next due date with month-end date clamping.
     */
    fun calculateNextDueDate(
        calendar: Calendar,
        frequency: RecurrenceFrequency,
        interval: Int,
        preferredDayOfMonth: Int
    ): Long {
        val safeInterval = maxOf(1, interval)
        when (frequency) {
            RecurrenceFrequency.DAILY -> {
                calendar.add(Calendar.DAY_OF_YEAR, safeInterval)
            }
            RecurrenceFrequency.WEEKLY -> {
                calendar.add(Calendar.WEEK_OF_YEAR, safeInterval)
            }
            RecurrenceFrequency.MONTHLY -> {
                calendar.add(Calendar.MONTH, safeInterval)
                val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                calendar.set(Calendar.DAY_OF_MONTH, Math.min(preferredDayOfMonth, maxDay))
            }
            RecurrenceFrequency.YEARLY -> {
                calendar.add(Calendar.YEAR, safeInterval)
                val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                calendar.set(Calendar.DAY_OF_MONTH, Math.min(preferredDayOfMonth, maxDay))
            }
        }
        return calendar.timeInMillis
    }
}
