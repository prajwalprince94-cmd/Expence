package com.knowspends.app.domain.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRestoreTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun backupPayload_serializesAndDeserializesAccurately() {
        val originalPayload = BackupPayload(
            schemaVersion = 1,
            exportedAt = 1730000000000L,
            appVersion = "1.0.0",
            accounts = listOf(
                AccountBackupDto(
                    id = 1,
                    name = "Main Savings",
                    type = "BANK",
                    openingBalanceMinor = 5000000L, // ₹50,000.00
                    archived = false,
                    sortOrder = 0
                )
            ),
            categories = listOf(
                CategoryBackupDto(
                    id = 10,
                    name = "Groceries",
                    kind = "EXPENSE",
                    iconKey = "shopping_cart",
                    colour = 0xFF388E3C
                )
            ),
            transactions = listOf(
                TransactionBackupDto(
                    id = 101,
                    accountId = 1,
                    categoryId = 10,
                    amountMinor = 125050L, // ₹1,250.50
                    type = "EXPENSE",
                    date = 1730001000000L,
                    note = "Weekly groceries",
                    payee = "Nature's Basket",
                    createdAt = 1730001000000L,
                    updatedAt = 1730001000000L,
                    sourceHash = "abc123hash"
                )
            ),
            budgets = listOf(
                BudgetBackupDto(
                    id = 5,
                    categoryId = 10,
                    period = "2026-10",
                    amountMinor = 1500000L, // ₹15,000.00
                    rollover = true
                )
            ),
            recurringRules = listOf(
                RecurringRuleBackupDto(
                    id = 2,
                    accountId = 1,
                    amountMinor = 2000000L, // ₹20,000.00
                    type = "EXPENSE",
                    note = "Apartment Rent",
                    payee = "Landlord",
                    frequency = "MONTHLY",
                    interval = 1,
                    dayOfMonth = 1,
                    nextDueDate = 1730419200000L,
                    autoPost = true
                )
            )
        )

        // Serialize to JSON string
        val jsonString = json.encodeToString(originalPayload)
        assertNotNull(jsonString)
        assertTrue(jsonString.contains("Main Savings"))
        assertTrue(jsonString.contains("125050"))
        assertTrue(jsonString.contains("Apartment Rent"))

        // Deserialize back
        val deserialized = json.decodeFromString<BackupPayload>(jsonString)

        assertEquals(originalPayload.schemaVersion, deserialized.schemaVersion)
        assertEquals(originalPayload.accounts.size, deserialized.accounts.size)
        assertEquals("Main Savings", deserialized.accounts.first().name)
        assertEquals(5000000L, deserialized.accounts.first().openingBalanceMinor)

        assertEquals(originalPayload.categories.size, deserialized.categories.size)
        assertEquals("Groceries", deserialized.categories.first().name)

        assertEquals(originalPayload.transactions.size, deserialized.transactions.size)
        assertEquals(125050L, deserialized.transactions.first().amountMinor)
        assertEquals("Nature's Basket", deserialized.transactions.first().payee)

        assertEquals(originalPayload.budgets.size, deserialized.budgets.size)
        assertEquals(1500000L, deserialized.budgets.first().amountMinor)
        assertTrue(deserialized.budgets.first().rollover)

        assertEquals(originalPayload.recurringRules.size, deserialized.recurringRules.size)
        assertEquals("Landlord", deserialized.recurringRules.first().payee)
    }
}
