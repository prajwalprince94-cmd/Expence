package com.knowspends.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetCalculationTest {

    @Test
    fun categoryBudget_safeStatus() {
        val budget = CategoryBudget(
            budgetId = 1,
            categoryId = 1,
            categoryName = "Food & Dining",
            categoryIcon = "restaurant",
            categoryColour = 0xFFF57C00,
            period = "2026-10",
            budgetAmountMinor = 1000000L, // ₹10,000.00
            spentAmountMinor = 500000L    // ₹5,000.00 (50%)
        )

        assertEquals(500000L, budget.remainingAmountMinor)
        assertEquals(0.5f, budget.progressRatio, 0.001f)
        assertEquals(BudgetStatus.SAFE, budget.status)
    }

    @Test
    fun categoryBudget_warningStatus() {
        val budget = CategoryBudget(
            budgetId = 2,
            categoryId = 2,
            categoryName = "Shopping",
            categoryIcon = "shopping_bag",
            categoryColour = 0xFF7B1FA2,
            period = "2026-10",
            budgetAmountMinor = 1000000L, // ₹10,000.00
            spentAmountMinor = 850000L    // ₹8,500.00 (85%)
        )

        assertEquals(150000L, budget.remainingAmountMinor)
        assertEquals(0.85f, budget.progressRatio, 0.001f)
        assertEquals(BudgetStatus.WARNING, budget.status)
    }

    @Test
    fun categoryBudget_overspentStatus() {
        val budget = CategoryBudget(
            budgetId = 3,
            categoryId = 3,
            categoryName = "Entertainment",
            categoryIcon = "movie",
            categoryColour = 0xFFC2185B,
            period = "2026-10",
            budgetAmountMinor = 500000L,  // ₹5,000.00
            spentAmountMinor = 650000L    // ₹6,500.00 (130%)
        )

        assertEquals(-150000L, budget.remainingAmountMinor) // Negative remaining
        assertEquals(1.3f, budget.progressRatio, 0.001f)
        assertEquals(BudgetStatus.OVERSPENT, budget.status)
    }
}
