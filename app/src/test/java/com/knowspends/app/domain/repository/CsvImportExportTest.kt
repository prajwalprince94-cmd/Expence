package com.knowspends.app.domain.repository

import com.knowspends.app.domain.util.MoneyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.security.MessageDigest

class CsvImportExportTest {

    private fun computeSourceHash(dateMillis: Long, amountMinor: Long, payee: String, accountId: Long): String {
        val raw = "$dateMillis:$amountMinor:${payee.trim().lowercase()}:$accountId"
        val bytes = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun sourceHash_isDeterministicAndDetectsDuplicates() {
        val date = 1730000000000L
        val amount = 45000L // ₹450.00
        val payee = "Uber India"
        val accountId = 1L

        val hash1 = computeSourceHash(date, amount, payee, accountId)
        val hash2 = computeSourceHash(date, amount, "  UBER INDIA  ", accountId) // Case & trim insensitive
        val hashDifferentAmount = computeSourceHash(date, 50000L, payee, accountId)

        assertEquals(hash1, hash2)
        assertNotEquals(hash1, hashDifferentAmount)
    }

    @Test
    fun csvRowParsing_handlesQuotedFieldsWithCommas() {
        val line = "\"2026-10-30 10:00:00\",EXPENSE,125050,1250.50,\"Food, Dining & Snacks\",Bank,\"Uber, Taxi\",Morning trip"

        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    tokens.add(sb.toString())
                    sb.clear()
                }
                else -> sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString())

        assertEquals(8, tokens.size)
        assertEquals("2026-10-30 10:00:00", tokens[0])
        assertEquals("EXPENSE", tokens[1])
        assertEquals("125050", tokens[2])
        assertEquals("1250.50", tokens[3])
        assertEquals("Food, Dining & Snacks", tokens[4]) // Successfully parsed comma inside quotes!
        assertEquals("Bank", tokens[5])
        assertEquals("Uber, Taxi", tokens[6])
        assertEquals("Morning trip", tokens[7])
    }
}
