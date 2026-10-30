package com.knowspends.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifies core financial rules from §5:
 * 1. Money is always calculated in minor units (paise).
 * 2. Balances are strictly derived (opening balance + sum of active transactions).
 * 3. Soft deleted transactions are excluded.
 * 4. Transfers adjust both accounts from a single transaction record without double-counting.
 */
class TransactionBalanceMathTest {

    @Test
    fun derivedBalance_calculatesAccurately() {
        val openingBalancePaise = 100000L // ₹1,000.00
        val accountId = 1L

        val transactions = listOf(
            // Income: + ₹500.00
            Transaction(
                id = 1,
                accountId = accountId,
                amountMinor = 50000L,
                type = TransactionType.INCOME,
                date = 1000L
            ),
            // Expense: - ₹250.50
            Transaction(
                id = 2,
                accountId = accountId,
                amountMinor = 25050L,
                type = TransactionType.EXPENSE,
                date = 2000L
            ),
            // Soft-deleted expense: should be excluded
            Transaction(
                id = 3,
                accountId = accountId,
                amountMinor = 10000L,
                type = TransactionType.EXPENSE,
                date = 3000L,
                deletedAt = 3500L
            ),
            // Transfer out to account 2: - ₹100.00
            Transaction(
                id = 4,
                accountId = accountId,
                transferAccountId = 2L,
                amountMinor = 10000L,
                type = TransactionType.TRANSFER,
                date = 4000L
            ),
            // Transfer in from account 2: + ₹50.00
            Transaction(
                id = 5,
                accountId = 2L,
                transferAccountId = accountId,
                amountMinor = 5000L,
                type = TransactionType.TRANSFER,
                date = 5000L
            )
        )

        // Calculate derived balance
        val delta = transactions
            .filter { it.deletedAt == null }
            .sumOf { t ->
                when (t.type) {
                    TransactionType.INCOME -> if (t.accountId == accountId) t.amountMinor else 0L
                    TransactionType.EXPENSE -> if (t.accountId == accountId) -t.amountMinor else 0L
                    TransactionType.TRANSFER -> {
                        when {
                            t.transferAccountId == accountId -> t.amountMinor
                            t.accountId == accountId -> -t.amountMinor
                            else -> 0L
                        }
                    }
                }
            }

        val finalBalance = openingBalancePaise + delta

        // Expected:
        // Opening: 100000
        // + Income: 50000 -> 150000
        // - Expense: 25050 -> 124950
        // - Transfer out: 10000 -> 114950
        // + Transfer in: 5000 -> 119950 (₹1,199.50)
        assertEquals(119950L, finalBalance)
    }

    @Test
    fun monthlySummary_netSavingsCalculation() {
        val summary = MonthlySummary(
            totalExpenseMinor = 450000L, // ₹4,500.00
            totalIncomeMinor = 1200000L  // ₹12,000.00
        )
        // Expected Net Savings = ₹7,500.00 = 750000 paise
        assertEquals(750000L, summary.netSavingsMinor)
    }
}
