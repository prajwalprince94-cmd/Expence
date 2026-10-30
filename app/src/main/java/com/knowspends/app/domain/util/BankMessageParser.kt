package com.knowspends.app.domain.util

import com.knowspends.app.domain.model.TransactionType
import java.util.regex.Pattern

data class ParsedFinancialMessage(
    val amountMinor: Long,
    val type: TransactionType,
    val merchantOrPayee: String,
    val accountTail: String? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val rawMessage: String
)

object BankMessageParser {

    private val OTP_PATTERNS = listOf(
        Pattern.compile("(?i)\\b(otp|verification code|one time password|secret code|do not share)\\b"),
        Pattern.compile("(?i)\\b(pre-approved|apply now|congratulations|click here to claim)\\b")
    )

    fun isIgnoredMessage(text: String): Boolean {
        for (pattern in OTP_PATTERNS) {
            if (pattern.matcher(text).find()) {
                return true
            }
        }
        return false
    }

    fun parse(rawText: String): ParsedFinancialMessage? {
        val trimmed = rawText.trim()
        if (isIgnoredMessage(trimmed)) {
            return null
        }

        // 1. Google Pay / PhonePe / Paytm / BHIM: "Paid ₹450 to Starbucks"
        val upiExpenseRegex = Pattern.compile("(?i)(?:paid|sent|transfer(?:red)?)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9,.]+)\\s*(?:to|towards)\\s+([^.,\n]+)")
        val upiExpenseMatcher = upiExpenseRegex.matcher(trimmed)
        if (upiExpenseMatcher.find()) {
            val amountStr = upiExpenseMatcher.group(1) ?: "0"
            val payee = upiExpenseMatcher.group(2)?.trim() ?: "UPI Merchant"
            val amountMinor = MoneyFormatter.parseToPaise(amountStr)
            if (amountMinor > 0) {
                return ParsedFinancialMessage(
                    amountMinor = amountMinor,
                    type = TransactionType.EXPENSE,
                    merchantOrPayee = cleanMerchant(payee),
                    rawMessage = trimmed
                )
            }
        }

        // 2. UPI Received / Income: "Received ₹2,000 from Rohit"
        val upiIncomeRegex = Pattern.compile("(?i)(?:received|credited with)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9,.]+)\\s*(?:from)?\\s+([^.,\n]+)?")
        val upiIncomeMatcher = upiIncomeRegex.matcher(trimmed)
        if (upiIncomeMatcher.find()) {
            val amountStr = upiIncomeMatcher.group(1) ?: "0"
            val sender = upiIncomeMatcher.group(2)?.trim() ?: "Payer"
            val amountMinor = MoneyFormatter.parseToPaise(amountStr)
            if (amountMinor > 0) {
                return ParsedFinancialMessage(
                    amountMinor = amountMinor,
                    type = TransactionType.INCOME,
                    merchantOrPayee = cleanMerchant(sender),
                    rawMessage = trimmed
                )
            }
        }

        // 3. Bank Debit (HDFC, SBI, ICICI, Axis):
        // e.g. "Rs.1,500.00 debited from HDFC Bank A/c **1234 to SWIGGY on 28-OCT-26"
        val bankDebitRegex = Pattern.compile("(?i)(?:rs\\.?|inr|₹)?\\s*([0-9,.]+)\\s+(?:debited|spent|withdrawn).*?(?:to|at|towards|info:?)\\s+([^.,\n]+)")
        val bankDebitMatcher = bankDebitRegex.matcher(trimmed)
        if (bankDebitMatcher.find()) {
            val amountStr = bankDebitMatcher.group(1) ?: "0"
            val merchant = bankDebitMatcher.group(2)?.trim() ?: "Merchant"
            val amountMinor = MoneyFormatter.parseToPaise(amountStr)
            val accountTail = extractAccountTail(trimmed)

            if (amountMinor > 0) {
                return ParsedFinancialMessage(
                    amountMinor = amountMinor,
                    type = TransactionType.EXPENSE,
                    merchantOrPayee = cleanMerchant(merchant),
                    accountTail = accountTail,
                    rawMessage = trimmed
                )
            }
        }

        // 4. Bank Credit / Salary (HDFC, SBI, ICICI):
        // e.g. "INR 75,000.00 credited to Acct XX9876 on 30-OCT-26 by SALARY"
        val bankCreditRegex = Pattern.compile("(?i)(?:rs\\.?|inr|₹)?\\s*([0-9,.]+)\\s+credited.*?(?:by|from|info:?)\\s+([^.,\n]+)")
        val bankCreditMatcher = bankCreditRegex.matcher(trimmed)
        if (bankCreditMatcher.find()) {
            val amountStr = bankCreditMatcher.group(1) ?: "0"
            val sender = bankCreditMatcher.group(2)?.trim() ?: "Income Source"
            val amountMinor = MoneyFormatter.parseToPaise(amountStr)
            val accountTail = extractAccountTail(trimmed)

            if (amountMinor > 0) {
                return ParsedFinancialMessage(
                    amountMinor = amountMinor,
                    type = TransactionType.INCOME,
                    merchantOrPayee = cleanMerchant(sender),
                    accountTail = accountTail,
                    rawMessage = trimmed
                )
            }
        }

        return null
    }

    private fun extractAccountTail(text: String): String? {
        val tailRegex = Pattern.compile("(?i)(?:a/c|acct|card|account)\\s*(?:no\\.?)?\\s*(?:ending\\s+with|is|:)?\\s*([X*]*[0-9]{3,4})")
        val matcher = tailRegex.matcher(text)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun cleanMerchant(raw: String): String {
        return raw.replace(Regex("(?i)\\b(on|ref|upi|ref no|bal|avl|txn|is|via|using)\\b.*"), "")
            .replace(Regex("[^A-Za-z0-9 &'-]"), "")
            .trim()
            .ifBlank { "Merchant" }
    }
}
