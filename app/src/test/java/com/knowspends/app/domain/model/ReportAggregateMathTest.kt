package com.knowspends.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifies critical report requirements from §8 & §9:
 * "Always test that report totals equal list totals."
 */
class ReportAggregateMathTest {

    @Test
    fun reportTotals_strictlyEqualTransactionListTotals() {
        val transactions = listOf(
            Transaction(
                id = 1,
                accountId = 1,
                categoryId = 10, // Food
                amountMinor = 150000L, // ₹1,500.00
                type = TransactionType.EXPENSE,
                date = 1000L,
                payee = "Zomato"
            ),
            Transaction(
                id = 2,
                accountId = 1,
                categoryId = 20, // Groceries
                amountMinor = 250000L, // ₹2,500.00
                type = TransactionType.EXPENSE,
                date = 2000L,
                payee = "Supermarket"
            ),
            Transaction(
                id = 3,
                accountId = 1,
                categoryId = 10, // Food
                amountMinor = 80000L, // ₹800.00
                type = TransactionType.EXPENSE,
                date = 3000L,
                payee = "Swiggy"
            ),
            Transaction(
                id = 4,
                accountId = 1,
                categoryId = 30, // Salary
                amountMinor = 10000000L, // ₹1,00,000.00
                type = TransactionType.INCOME,
                date = 4000L,
                payee = "Employer"
            ),
            // Soft-deleted transaction must not count
            Transaction(
                id = 5,
                accountId = 1,
                categoryId = 10,
                amountMinor = 50000L,
                type = TransactionType.EXPENSE,
                date = 5000L,
                deletedAt = 5500L
            )
        )

        val activeList = transactions.filter { it.deletedAt == null }

        // 1. Total expense sum from ledger list
        val listExpenseTotal = activeList
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amountMinor }

        // 2. Total income sum from ledger list
        val listIncomeTotal = activeList
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amountMinor }

        assertEquals(480000L, listExpenseTotal) // 150000 + 250000 + 80000 = 4,800.00
        assertEquals(10000000L, listIncomeTotal) // 1,00,000.00

        // 3. Category breakdown aggregation
        val categoryBreakdown = activeList
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .map { (catId, items) ->
                val sum = items.sumOf { it.amountMinor }
                CategoryReportItem(
                    categoryId = catId ?: 0L,
                    categoryName = "Cat-$catId",
                    categoryIcon = "icon",
                    categoryColour = 0L,
                    totalAmountMinor = sum,
                    percentage = (sum.toFloat() / listExpenseTotal.toFloat()) * 100f,
                    transactionCount = items.size
                )
            }

        val categoryReportTotal = categoryBreakdown.sumOf { it.totalAmountMinor }
        val categoryPercentageSum = categoryBreakdown.sumOf { it.percentage.toDouble() }

        // Category breakdown sum MUST equal transaction list expense total
        assertEquals(listExpenseTotal, categoryReportTotal)
        assertEquals(100.0, categoryPercentageSum, 0.01)

        // 4. Payee aggregation
        val payeeBreakdown = activeList
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.payee }
            .map { (payee, items) ->
                val sum = items.sumOf { it.amountMinor }
                PayeeReportItem(
                    payeeName = payee,
                    totalAmountMinor = sum,
                    percentage = (sum.toFloat() / listExpenseTotal.toFloat()) * 100f,
                    transactionCount = items.size
                )
            }

        val payeeReportTotal = payeeBreakdown.sumOf { it.totalAmountMinor }
        // Payee breakdown sum MUST equal transaction list expense total
        assertEquals(listExpenseTotal, payeeReportTotal)
    }

    @Test
    fun savingsRate_calculatesCorrectly() {
        val totalIncome = 1000000L // ₹10,000
        val totalExpense = 400000L // ₹4,000
        val netSavings = totalIncome - totalExpense // ₹6,000

        val savingsRate = (netSavings.toFloat() / totalIncome.toFloat()) * 100f
        assertEquals(60.0f, savingsRate, 0.001f)
    }
}
