package com.knowspends.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {

    @Test
    fun formatPaise_formatsCorrectlyWithPaise() {
        assertEquals("₹12.50", MoneyFormatter.formatPaise(1250L))
        assertEquals("₹0.00", MoneyFormatter.formatPaise(0L))
        assertEquals("₹0.05", MoneyFormatter.formatPaise(5L))
        assertEquals("₹100.00", MoneyFormatter.formatPaise(10000L))
    }

    @Test
    fun formatPaise_formatsIndianDigitGrouping() {
        // 1 lakh = 1,00,000
        assertEquals("₹1,00,000.00", MoneyFormatter.formatPaise(10000000L))
        // 12 lakh 34 thousand 567
        assertEquals("₹12,34,567.89", MoneyFormatter.formatPaise(123456789L))
        // 1 crore = 1,00,00,000
        assertEquals("₹1,00,00,000.00", MoneyFormatter.formatPaise(1000000000L))
    }

    @Test
    fun formatPaise_negativeAmount() {
        assertEquals("- ₹500.00", MoneyFormatter.formatPaise(-50000L))
    }

    @Test
    fun parseToPaise_handlesVariousInputs() {
        assertEquals(1250L, MoneyFormatter.parseToPaise("12.50"))
        assertEquals(1250L, MoneyFormatter.parseToPaise("12.5"))
        assertEquals(1200L, MoneyFormatter.parseToPaise("12"))
        assertEquals(123456700L, MoneyFormatter.parseToPaise("₹ 12,34,567"))
        assertEquals(123456750L, MoneyFormatter.parseToPaise("₹12,34,567.50"))
        assertEquals(0L, MoneyFormatter.parseToPaise(""))
        assertEquals(0L, MoneyFormatter.parseToPaise("abc"))
    }
}
