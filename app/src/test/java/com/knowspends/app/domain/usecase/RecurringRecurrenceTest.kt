package com.knowspends.app.domain.usecase

import com.knowspends.app.domain.model.RecurrenceFrequency
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class RecurringRecurrenceTest {

    private val useCase = ProcessRecurringTransactionsUseCase(
        recurringRuleDao = mockk(relaxed = true),
        transactionDao = mockk(relaxed = true),
        ioDispatcher = kotlinx.coroutines.Dispatchers.Unconfined
    )

    @Test
    fun calculateNextDueDate_monthlyClampsMonthEnd_janToFeb() {
        // Start date: 31 January 2026 (non-leap year)
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 31)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val nextDue = useCase.calculateNextDueDate(
            calendar = calendar,
            frequency = RecurrenceFrequency.MONTHLY,
            interval = 1,
            preferredDayOfMonth = 31
        )

        val resultCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = nextDue
        }

        // Expected: February 28, 2026 (clamped from 31st)
        assertEquals(2026, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, resultCal.get(Calendar.MONTH))
        assertEquals(28, resultCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun calculateNextDueDate_monthlyClampsMonthEnd_marchToApril() {
        // Start date: 31 March 2026
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.MARCH)
            set(Calendar.DAY_OF_MONTH, 31)
        }

        val nextDue = useCase.calculateNextDueDate(
            calendar = calendar,
            frequency = RecurrenceFrequency.MONTHLY,
            interval = 1,
            preferredDayOfMonth = 31
        )

        val resultCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = nextDue
        }

        // Expected: April 30, 2026 (April has 30 days)
        assertEquals(Calendar.APRIL, resultCal.get(Calendar.MONTH))
        assertEquals(30, resultCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun calculateNextDueDate_dailyAndWeekly() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.OCTOBER)
            set(Calendar.DAY_OF_MONTH, 15)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Add 1 day
        useCase.calculateNextDueDate(calendar, RecurrenceFrequency.DAILY, 1, 15)
        assertEquals(16, calendar.get(Calendar.DAY_OF_MONTH))

        // Add 1 week (+7 days)
        useCase.calculateNextDueDate(calendar, RecurrenceFrequency.WEEKLY, 1, 16)
        assertEquals(23, calendar.get(Calendar.DAY_OF_MONTH))
    }
}
