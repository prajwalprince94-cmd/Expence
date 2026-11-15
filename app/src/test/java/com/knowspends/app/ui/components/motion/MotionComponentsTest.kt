package com.knowspends.app.ui.components.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionComponentsTest {

    @Test
    fun formatCountUpCurrency_zeroPaise() {
        val result = formatCountUpCurrency(0L, "₹")
        assertEquals("₹0.00", result)
    }

    @Test
    fun formatCountUpCurrency_positiveAmount() {
        val result = formatCountUpCurrency(150050L, "₹")
        assertEquals("₹1,500.50", result)
    }

    @Test
    fun formatCountUpCurrency_positiveAmountWithSign() {
        val result = formatCountUpCurrency(150050L, "₹", showPositiveSign = true)
        assertEquals("+₹1,500.50", result)
    }

    @Test
    fun formatCountUpCurrency_negativeAmount() {
        val result = formatCountUpCurrency(-250000L, "₹")
        assertEquals("-₹2,500.00", result)
    }

    @Test
    fun formatCountUpCurrency_exceedsFloatPrecisionPreservesExactPaise() {
        // ₹2,00,000.50 = 20,000,050 paise (> 2^24 = 16,777,216 paise where 32-bit Float fails)
        val amount = 20000050L
        val result = formatCountUpCurrency(amount, "₹")
        assertEquals("₹2,00,000.50", result)

        // ₹10,00,000.75 = 100,000,075 paise
        val tenLakhs = 100000075L
        val resultTenLakhs = formatCountUpCurrency(tenLakhs, "₹")
        assertEquals("₹10,00,000.75", resultTenLakhs)

        // ₹1 Crore = 1,00,00,000.99 = 1,000,000,099 paise
        val oneCrore = 1000000099L
        val resultCrore = formatCountUpCurrency(oneCrore, "₹")
        assertEquals("₹1,00,00,000.99", resultCrore)
    }

    @Test
    fun formatCountUpCurrency_customCurrencySymbol() {
        val result = formatCountUpCurrency(9900L, "$")
        assertEquals("$99.00", result)
    }

    @Test
    fun formatCountUpCurrency_boundaryMinLongGraceful() {
        val result = formatCountUpCurrency(Long.MIN_VALUE, "₹")
        assertTrue(result.startsWith("-₹"))
    }

    @Test
    fun sparklineNormalization_identicalPointsPreventDivisionByZero() {
        val points = listOf(100f, 100f, 100f)
        val range = SparklineMath.calculateRange(points)
        assertEquals(1f, range, 0.0001f)
    }

    @Test
    fun sparklineNormalization_handlesNaNAndInfinite() {
        val raw = listOf(Float.NaN, 50f, Float.POSITIVE_INFINITY, 100f)
        val clean = SparklineMath.cleanPoints(raw)
        assertEquals(listOf(0f, 50f, 0f, 100f), clean)
    }

    @Test
    fun sparklineNormalization_handlesEmptyAndSingle() {
        val emptyClean = SparklineMath.cleanPoints(emptyList())
        assertTrue(emptyClean.isEmpty())
        val emptyRange = SparklineMath.calculateRange(emptyList())
        assertEquals(1f, emptyRange, 0.0001f)

        val singleClean = SparklineMath.cleanPoints(listOf(42f))
        assertEquals(listOf(42f), singleClean)
        val singleRange = SparklineMath.calculateRange(singleClean)
        assertEquals(1f, singleRange, 0.0001f)
    }
}
