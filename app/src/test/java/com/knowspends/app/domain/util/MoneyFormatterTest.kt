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

    @Test
    fun formatPaiseForInput_formatsCleanlyWithoutCommasOrSymbols() {
        assertEquals("0", MoneyFormatter.formatPaiseForInput(0L))
        assertEquals("12.50", MoneyFormatter.formatPaiseForInput(1250L))
        assertEquals("12", MoneyFormatter.formatPaiseForInput(1200L))
        assertEquals("0.05", MoneyFormatter.formatPaiseForInput(5L))
        assertEquals("0.50", MoneyFormatter.formatPaiseForInput(50L))
        assertEquals("1.05", MoneyFormatter.formatPaiseForInput(105L))
        assertEquals("100000", MoneyFormatter.formatPaiseForInput(10000000L))
        assertEquals("10000000", MoneyFormatter.formatPaiseForInput(1000000000L))
        assertEquals("1234567.89", MoneyFormatter.formatPaiseForInput(123456789L))
        assertEquals("-500", MoneyFormatter.formatPaiseForInput(-50000L))
        assertEquals("-500.25", MoneyFormatter.formatPaiseForInput(-50025L))
        assertEquals("-0.05", MoneyFormatter.formatPaiseForInput(-5L))
        assertEquals("-0.50", MoneyFormatter.formatPaiseForInput(-50L))
        assertEquals("92233720368547758.07", MoneyFormatter.formatPaiseForInput(Long.MAX_VALUE))
        assertEquals("-92233720368547758.07", MoneyFormatter.formatPaiseForInput(Long.MIN_VALUE))
    }

    @Test
    fun parseToPaise_handlesOverflowGracefully() {
        // Number larger than Long.MAX_VALUE should clamp safely without wrapping into negative numbers
        val hugeNumber = "999999999999999999999999999.99"
        val parsed = MoneyFormatter.parseToPaise(hugeNumber)
        assertEquals(Long.MAX_VALUE, parsed)

        val hugeNegative = "-999999999999999999999999999.99"
        val parsedNegative = MoneyFormatter.parseToPaise(hugeNegative)
        assertEquals(-Long.MAX_VALUE, parsedNegative)
    }

    @Test
    fun roundtrip_parseToPaise_formatPaiseForInput_equalsOriginal() {
        val testCases = listOf(
            0L,
            1L, 5L, 50L, 99L, 100L, 105L,
            1200L, 1250L, 9999L, 10000L,
            10000000L,
            10000005L,
            123456789L,
            1000000000L,
            -1L, -5L, -50L, -99L, -100L, -105L,
            -50000L, -50025L, -10000000L, -1000000000L
        )

        for (amount in testCases) {
            val formatted = MoneyFormatter.formatPaiseForInput(amount)
            val parsed = MoneyFormatter.parseToPaise(formatted)
            assertEquals("Roundtrip failed for paise: $amount (formatted: $formatted)", amount, parsed)
        }
    }

    @Test
    fun parseToPaise_handlesKeypadIntermediateStatesAndNoise() {
        assertEquals(50L, MoneyFormatter.parseToPaise(".50"))
        assertEquals(50L, MoneyFormatter.parseToPaise(".5"))
        assertEquals(5L, MoneyFormatter.parseToPaise(".05"))
        assertEquals(1200L, MoneyFormatter.parseToPaise("12."))
        assertEquals(0L, MoneyFormatter.parseToPaise("0."))
        assertEquals(0L, MoneyFormatter.parseToPaise("."))
        assertEquals(0L, MoneyFormatter.parseToPaise("-"))
        assertEquals(0L, MoneyFormatter.parseToPaise("-."))
        assertEquals(-50L, MoneyFormatter.parseToPaise("-0.50"))
        assertEquals(-5L, MoneyFormatter.parseToPaise("-0.05"))
        assertEquals(-50000L, MoneyFormatter.parseToPaise("- ₹500"))
        assertEquals(1234L, MoneyFormatter.parseToPaise("12.349"))
    }

    @Test
    fun formatPaise_respectsFlagsAndSymbols() {
        assertEquals("₹12", MoneyFormatter.formatPaise(1250L, showDecimals = false))
        assertEquals("₹0", MoneyFormatter.formatPaise(0L, showDecimals = false))
        assertEquals("12.50", MoneyFormatter.formatPaise(1250L, includeSymbol = false))
        assertEquals("12", MoneyFormatter.formatPaise(1250L, includeSymbol = false, showDecimals = false))
        assertEquals("$12.50", MoneyFormatter.formatPaise(1250L, currencySymbol = "$"))
        assertEquals("- $500.00", MoneyFormatter.formatPaise(-50000L, currencySymbol = "$"))
        assertEquals("€1,00,000.00", MoneyFormatter.formatPaise(10000000L, currencySymbol = "€"))
    }
}
