package com.knowspends.app.domain.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Utility for money formatting and parsing.
 * Rule: Money is always stored as Long minor units (paise). ₹12.50 = 1250L.
 * Formatted with Indian digit grouping (e.g., ₹12,34,567.00).
 */
object MoneyFormatter {

    private val inrLocale = Locale("en", "IN")

    /**
     * Formats minor units (paise) into INR currency string.
     * Example: 1250L -> "₹12.50", 123456700L -> "₹12,34,567.00"
     */
    fun formatPaise(
        amountPaise: Long,
        includeSymbol: Boolean = true,
        showDecimals: Boolean = true
    ): String {
        val isNegative = amountPaise < 0
        val absPaise = Math.abs(amountPaise)
        val rupees = absPaise / 100
        val paise = absPaise % 100

        val formattedRupees = formatIndianGrouping(rupees)
        val prefix = if (isNegative) "- " else ""
        val symbol = if (includeSymbol) "₹" else ""

        return if (showDecimals) {
            val paiseString = paise.toString().padStart(2, '0')
            "$prefix$symbol$formattedRupees.$paiseString"
        } else {
            "$prefix$symbol$formattedRupees"
        }
    }

    /**
     * Parses user text input (e.g., "12.50", "1250", "₹ 1,234.50") into minor units (paise).
     * Never uses floating point math to prevent precision loss.
     */
    fun parseToPaise(input: String): Long {
        val cleaned = input.filter { it.isDigit() || it == '.' || it == '-' }
        if (cleaned.isBlank() || cleaned == "-") return 0L

        val isNegative = cleaned.startsWith("-")
        val numericPart = if (isNegative) cleaned.substring(1) else cleaned

        val parts = numericPart.split('.')
        val rupeesPart = parts.getOrNull(0)?.toLongOrNull() ?: 0L
        val paisePart = when {
            parts.size > 1 -> {
                val rawDecimals = parts[1].take(2)
                rawDecimals.padEnd(2, '0').toLongOrNull() ?: 0L
            }
            else -> 0L
        }

        val total = (rupeesPart * 100) + paisePart
        return if (isNegative) -total else total
    }

    /**
     * Formats whole rupees using standard Indian numbering system:
     * Last 3 digits grouped, then groups of 2 digits (e.g. 12,34,567).
     */
    fun formatIndianGrouping(rupees: Long): String {
        val s = rupees.toString()
        if (s.length <= 3) return s

        val last3 = s.takeLast(3)
        val remaining = s.dropLast(3)

        val groups = mutableListOf<String>()
        var i = remaining.length
        while (i > 0) {
            val start = Math.max(0, i - 2)
            groups.add(0, remaining.substring(start, i))
            i -= 2
        }

        return groups.joinToString(",") + "," + last3
    }
}
