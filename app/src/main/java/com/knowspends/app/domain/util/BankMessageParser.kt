package com.knowspends.app.domain.util

import com.knowspends.app.domain.model.TransactionType
import java.util.regex.Pattern

data class ParsedFinancialMessage(
    val amountMinor: Long,
    val type: TransactionType,
    val merchantOrPayee: String,
    val bankName: String? = null,
    val upiOrWalletApp: String? = null,
    val accountTail: String? = null,
    val suggestedCategoryName: String? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val rawMessage: String
)

typealias ParsedBankMessage = ParsedFinancialMessage

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

    fun parse(rawText: String, sender: String? = null): ParsedFinancialMessage? {
        val trimmed = rawText.trim()
        if (isIgnoredMessage(trimmed)) {
            return null
        }

        val bankName = extractBankName(trimmed, sender)
        val upiOrWalletApp = extractUpiOrWalletApp(trimmed, sender)
        val accountTail = extractAccountTail(trimmed)

        // 1. Google Pay / PhonePe / Paytm / BHIM: "Paid ₹450 to Starbucks"
        val upiExpenseRegex = Pattern.compile("(?i)(?:paid|sent|transfer(?:red)?)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9,.]+)\\s*(?:to|towards)\\s+([^.,\n]+)")
        val upiExpenseMatcher = upiExpenseRegex.matcher(trimmed)
        if (upiExpenseMatcher.find()) {
            val amountStr = upiExpenseMatcher.group(1) ?: "0"
            val payee = upiExpenseMatcher.group(2)?.trim() ?: "UPI Merchant"
            val amountMinor = MoneyFormatter.parseToPaise(amountStr)
            if (amountMinor > 0) {
                val cleanPayee = cleanMerchant(payee)
                return ParsedFinancialMessage(
                    amountMinor = amountMinor,
                    type = TransactionType.EXPENSE,
                    merchantOrPayee = cleanPayee,
                    bankName = bankName,
                    upiOrWalletApp = upiOrWalletApp ?: "UPI",
                    accountTail = accountTail,
                    suggestedCategoryName = inferCategory(cleanPayee, trimmed, TransactionType.EXPENSE),
                    rawMessage = trimmed
                )
            }
        }

        // 2. UPI Received / Income: "Received ₹2,000 from Rohit"
        val upiIncomeRegex = Pattern.compile("(?i)(?:received|credited with)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9,.]+)\\s*(?:from)?\\s+([^.,\n]+)?")
        val upiIncomeMatcher = upiIncomeRegex.matcher(trimmed)
        if (upiIncomeMatcher.find()) {
            val amountStr = upiIncomeMatcher.group(1) ?: "0"
            val senderName = upiIncomeMatcher.group(2)?.trim() ?: "Payer"
            val amountMinor = MoneyFormatter.parseToPaise(amountStr)
            if (amountMinor > 0) {
                val cleanSender = cleanMerchant(senderName)
                return ParsedFinancialMessage(
                    amountMinor = amountMinor,
                    type = TransactionType.INCOME,
                    merchantOrPayee = cleanSender,
                    bankName = bankName,
                    upiOrWalletApp = upiOrWalletApp ?: "UPI",
                    accountTail = accountTail,
                    suggestedCategoryName = inferCategory(cleanSender, trimmed, TransactionType.INCOME),
                    rawMessage = trimmed
                )
            }
        }

        // 3. Bank Debit (HDFC, SBI, ICICI, Axis, Kotak, etc.):
        // e.g. "Rs.1,500.00 debited from HDFC Bank A/c ending with **1234 to SWIGGY on 28-OCT-26"
        val bankDebitRegex = Pattern.compile("(?i)(?:rs\\.?|inr|₹)?\\s*([0-9,.]+)\\s+(?:debited|spent|withdrawn).*?(?:to|at|towards|info:?)\\s+([^.,\n]+)")
        val bankDebitMatcher = bankDebitRegex.matcher(trimmed)
        if (bankDebitMatcher.find()) {
            val amountStr = bankDebitMatcher.group(1) ?: "0"
            val merchant = bankDebitMatcher.group(2)?.trim() ?: "Merchant"
            val amountMinor = MoneyFormatter.parseToPaise(amountStr)

            if (amountMinor > 0) {
                val cleanMerch = cleanMerchant(merchant)
                return ParsedFinancialMessage(
                    amountMinor = amountMinor,
                    type = TransactionType.EXPENSE,
                    merchantOrPayee = cleanMerch,
                    bankName = bankName,
                    upiOrWalletApp = upiOrWalletApp,
                    accountTail = accountTail,
                    suggestedCategoryName = inferCategory(cleanMerch, trimmed, TransactionType.EXPENSE),
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
            val senderPayer = bankCreditMatcher.group(2)?.trim() ?: "Income Source"
            val amountMinor = MoneyFormatter.parseToPaise(amountStr)

            if (amountMinor > 0) {
                val cleanPayer = cleanMerchant(senderPayer)
                return ParsedFinancialMessage(
                    amountMinor = amountMinor,
                    type = TransactionType.INCOME,
                    merchantOrPayee = cleanPayer,
                    bankName = bankName,
                    upiOrWalletApp = upiOrWalletApp,
                    accountTail = accountTail,
                    suggestedCategoryName = inferCategory(cleanPayer, trimmed, TransactionType.INCOME),
                    rawMessage = trimmed
                )
            }
        }

        // 5. Fallback debit pattern: "debited by ₹500" or "spent ₹120"
        val genericDebitRegex = Pattern.compile("(?i)(?:debited\\s+by|spent)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9,.]+)")
        val genericDebitMatcher = genericDebitRegex.matcher(trimmed)
        if (genericDebitMatcher.find()) {
            val amountStr = genericDebitMatcher.group(1) ?: "0"
            val amountMinor = MoneyFormatter.parseToPaise(amountStr)
            if (amountMinor > 0) {
                return ParsedFinancialMessage(
                    amountMinor = amountMinor,
                    type = TransactionType.EXPENSE,
                    merchantOrPayee = bankName ?: "Bank Debit",
                    bankName = bankName,
                    upiOrWalletApp = upiOrWalletApp,
                    accountTail = accountTail,
                    suggestedCategoryName = inferCategory("Debit", trimmed, TransactionType.EXPENSE),
                    rawMessage = trimmed
                )
            }
        }

        return null
    }

    private fun extractBankName(text: String, sender: String?): String? {
        val combined = "$text ${sender ?: ""}"
        return when {
            Pattern.compile("(?i)\\b(hdfc|hdfcbk)\\b").matcher(combined).find() -> "HDFC Bank"
            Pattern.compile("(?i)\\b(sbi|sbin|state bank)\\b").matcher(combined).find() -> "State Bank of India"
            Pattern.compile("(?i)\\b(icici|icicib)\\b").matcher(combined).find() -> "ICICI Bank"
            Pattern.compile("(?i)\\b(axis|axisbk)\\b").matcher(combined).find() -> "Axis Bank"
            Pattern.compile("(?i)\\b(kotak|kkbk)\\b").matcher(combined).find() -> "Kotak Mahindra Bank"
            Pattern.compile("(?i)\\b(pnb|punb|punjab national)\\b").matcher(combined).find() -> "Punjab National Bank"
            Pattern.compile("(?i)\\b(bob|barb|bank of baroda)\\b").matcher(combined).find() -> "Bank of Baroda"
            Pattern.compile("(?i)\\b(canara|cnrb)\\b").matcher(combined).find() -> "Canara Bank"
            Pattern.compile("(?i)\\b(union bank|ubi)\\b").matcher(combined).find() -> "Union Bank of India"
            Pattern.compile("(?i)\\b(indusind|indb)\\b").matcher(combined).find() -> "IndusInd Bank"
            Pattern.compile("(?i)\\b(federal|fdrl)\\b").matcher(combined).find() -> "Federal Bank"
            Pattern.compile("(?i)\\b(idfc|idfb)\\b").matcher(combined).find() -> "IDFC First Bank"
            Pattern.compile("(?i)\\b(yes bank|yesb)\\b").matcher(combined).find() -> "Yes Bank"
            Pattern.compile("(?i)\\b(rbl|ratnakar)\\b").matcher(combined).find() -> "RBL Bank"
            Pattern.compile("(?i)\\b(au small finance|aubank)\\b").matcher(combined).find() -> "AU Bank"
            else -> null
        }
    }

    private fun extractUpiOrWalletApp(text: String, sender: String?): String? {
        val combined = "$text ${sender ?: ""}"
        return when {
            Pattern.compile("(?i)\\b(google pay|gpay|okhdfcbank|okaxis|oksbi|okicici)\\b").matcher(combined).find() -> "Google Pay"
            Pattern.compile("(?i)\\b(phonepe|ybl|ibl|axl)\\b").matcher(combined).find() -> "PhonePe"
            Pattern.compile("(?i)\\b(paytm|ptm)\\b").matcher(combined).find() -> "Paytm"
            Pattern.compile("(?i)\\b(cred|credpay)\\b").matcher(combined).find() -> "CRED"
            Pattern.compile("(?i)\\b(amazon pay|amazonpay|apl)\\b").matcher(combined).find() -> "Amazon Pay"
            Pattern.compile("(?i)\\b(bhim)\\b").matcher(combined).find() -> "BHIM"
            Pattern.compile("(?i)\\b(whatsapp)\\b").matcher(combined).find() -> "WhatsApp Pay"
            Pattern.compile("(?i)\\b(mobikwik)\\b").matcher(combined).find() -> "Mobikwik"
            else -> null
        }
    }

    private fun extractAccountTail(text: String): String? {
        val tailRegex = Pattern.compile("(?i)(?:a/c|acct|card|account)\\s*(?:no\\.?)?\\s*(?:ending\\s+with|is|:)?\\s*([X*]*[0-9]{3,4})")
        val matcher = tailRegex.matcher(text)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun cleanMerchant(raw: String): String {
        return raw.replace(Regex("(?i)\\b(on|ref|upi|ref no|bal|avl|txn|is|via|using|vpa)\\b.*"), "")
            .replace(Regex("[^A-Za-z0-9 &'-]"), "")
            .trim()
            .ifBlank { "Merchant" }
    }

    fun inferCategory(merchant: String, rawMessage: String, type: TransactionType): String {
        if (type == TransactionType.INCOME) {
            val incomeCombined = "$merchant $rawMessage".lowercase()
            return when {
                incomeCombined.contains("salary") || incomeCombined.contains("payroll") || incomeCombined.contains("stipend") -> "Salary"
                incomeCombined.contains("dividend") || incomeCombined.contains("interest") || incomeCombined.contains("zerodha") || incomeCombined.contains("groww") -> "Investments"
                else -> "Freelance / Extra"
            }
        }

        val text = "$merchant $rawMessage".lowercase()
        return when {
            // Food & Dining
            listOf("swiggy", "zomato", "starbucks", "mcdonald", "burger king", "kfc", "domino", "pizza", "subway", "haldiram", "cafe", "restaurant", "bakery", "chai", "bar", "dhabha", "dining", "biryani").any { text.contains(it) } -> "Food & Dining"

            // Groceries
            listOf("blinkit", "zepto", "instamart", "bigbasket", "dmart", "spencer", "nature's basket", "more", "supermarket", "grocery", "kirana", "vegetable", "fruits", "milk", "dairy", "mother dairy", "country delight").any { text.contains(it) } -> "Groceries"

            // Transportation & Fuel
            listOf("uber", "ola", "rapido", "namma yatri", "metro", "irctc", "railway", "shell", "hpcl", "bpcl", "iocl", "petrol", "fuel", "diesel", "cng", "fastag", "toll", "parking").any { text.contains(it) } -> "Transportation"

            // Shopping
            listOf("amazon", "flipkart", "myntra", "ajio", "zara", "h&m", "nykaa", "meesho", "tata cliq", "reliance digital", "croma", "ikea", "decathlon", "lenskart", "uniqlo", "retail", "clothing").any { text.contains(it) } -> "Shopping"

            // Entertainment
            listOf("netflix", "spotify", "hotstar", "prime video", "youtube", "pvr", "inox", "bookmyshow", "cinepolis", "disney", "sonyliv", "zee5", "gaming", "steam", "playstation", "cinema").any { text.contains(it) } -> "Entertainment"

            // Bills & Utilities
            listOf("electricity", "bescom", "mseb", "tneb", "gas", "indane", "bharat gas", "airtel", "jio", "vodafone", "vi", "bsnl", "broadband", "act fibernet", "tata play", "dth", "recharge", "billdesk", "insurance", "lic").any { text.contains(it) } -> "Bills & Utilities"

            // Health
            listOf("apollo", "1mg", "netmeds", "pharmeasy", "medplus", "hospital", "pharmacy", "clinic", "pathlabs", "healthians", "dental", "chemist", "doctor").any { text.contains(it) } -> "Health"

            else -> "Shopping"
        }
    }
}
