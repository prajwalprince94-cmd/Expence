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
        Pattern.compile("(?i)\\b(otp|verification code|one time password|secret code|security code|do not share|valid for|is your secret)\\b"),
        Pattern.compile("(?i)\\b(pre-approved|apply now|congratulations|click here to claim|win up to)\\b")
    )

    fun isIgnoredMessage(text: String): Boolean {
        for (pattern in OTP_PATTERNS) {
            if (pattern.matcher(text).find()) {
                return true
            }
        }
        return false
    }

    fun parse(rawText: String, sender: String? = null, messageDate: Long = System.currentTimeMillis()): ParsedFinancialMessage? {
        val trimmed = rawText.trim()
        if (isIgnoredMessage(trimmed)) {
            return null
        }

        val bankName = extractBankName(trimmed, sender)
        val upiOrWalletApp = extractUpiOrWalletApp(trimmed, sender)
        val accountTail = extractAccountTail(trimmed)

        // 1. Paid/Sent/Transfer Expense: "Paid Rs.450 to Starbucks" / "Sent Rs.350 from A/c to rahul@upi"
        val paidRegex = Pattern.compile("(?i)(?:paid|sent|transfer(?:red)?)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9,.]+)(?:.*?(?:to|towards)\\s+([^.,\n]+))?")
        val paidMatcher = paidRegex.matcher(trimmed)
        if (paidMatcher.find()) {
            val amountStr = paidMatcher.group(1) ?: "0"
            val rawPayee = paidMatcher.group(2)?.trim()
            val amountMinor = MoneyFormatter.parseToPaise(amountStr)
            if (amountMinor > 0) {
                val cleanPayee = cleanMerchant(rawPayee ?: (bankName ?: "UPI Transfer"))
                return ParsedFinancialMessage(
                    amountMinor = amountMinor,
                    type = TransactionType.EXPENSE,
                    merchantOrPayee = cleanPayee,
                    bankName = bankName,
                    upiOrWalletApp = upiOrWalletApp ?: "UPI",
                    accountTail = accountTail,
                    suggestedCategoryName = inferCategory(cleanPayee, trimmed, TransactionType.EXPENSE),
                    dateMillis = messageDate,
                    rawMessage = trimmed
                )
            }
        }

        // 2. Debited with verb BEFORE amount: "debited by 500.00" / "debited for INR 1,250" / "debited with Rs 899" / "spent INR 1,500"
        val verbBeforeDebitRegex = Pattern.compile("(?i)(?:debited\\s*(?:by|for|with|towards)?|deducted\\s*(?:by|for)?|spent\\s*(?:an amount of)?|withdrawn\\s*(?:by|of)?)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9,.]+)(?:.*?(?:to|at|towards|info:?|transfer to|vpa)\\s+([^.,\n]+))?")
        val verbBeforeMatcher = verbBeforeDebitRegex.matcher(trimmed)
        if (verbBeforeMatcher.find()) {
            val amountStr = verbBeforeMatcher.group(1) ?: "0"
            val rawPayee = verbBeforeMatcher.group(2)?.trim()
            val amountMinor = MoneyFormatter.parseToPaise(amountStr)
            if (amountMinor > 0) {
                val cleanPayee = cleanMerchant(rawPayee ?: (bankName ?: "Bank Debit"))
                return ParsedFinancialMessage(
                    amountMinor = amountMinor,
                    type = TransactionType.EXPENSE,
                    merchantOrPayee = cleanPayee,
                    bankName = bankName,
                    upiOrWalletApp = upiOrWalletApp,
                    accountTail = accountTail,
                    suggestedCategoryName = inferCategory(cleanPayee, trimmed, TransactionType.EXPENSE),
                    dateMillis = messageDate,
                    rawMessage = trimmed
                )
            }
        }

        // 3. Debited with verb AFTER amount: "Rs.1,500.00 debited from HDFC A/c to Swiggy" / "INR 500 spent at Starbucks"
        val verbAfterDebitRegex = Pattern.compile("(?i)(?:rs\\.?|inr|₹)?\\s*([0-9,.]+)\\s+(?:has been|is)?\\s*(?:debited|spent|withdrawn|deducted)(?:.*?(?:to|at|towards|info:?|transfer to|vpa)\\s+([^.,\n]+))?")
        val verbAfterMatcher = verbAfterDebitRegex.matcher(trimmed)
        if (verbAfterMatcher.find()) {
            val amountStr = verbAfterMatcher.group(1) ?: "0"
            val rawPayee = verbAfterMatcher.group(2)?.trim()
            val amountMinor = MoneyFormatter.parseToPaise(amountStr)
            if (amountMinor > 0) {
                val cleanPayee = cleanMerchant(rawPayee ?: (bankName ?: "Bank Debit"))
                return ParsedFinancialMessage(
                    amountMinor = amountMinor,
                    type = TransactionType.EXPENSE,
                    merchantOrPayee = cleanPayee,
                    bankName = bankName,
                    upiOrWalletApp = upiOrWalletApp,
                    accountTail = accountTail,
                    suggestedCategoryName = inferCategory(cleanPayee, trimmed, TransactionType.EXPENSE),
                    dateMillis = messageDate,
                    rawMessage = trimmed
                )
            }
        }

        // 4. Credited with verb BEFORE amount: "credited with Rs 2,000" / "received INR 5,000"
        val verbBeforeCreditRegex = Pattern.compile("(?i)(?:credited\\s*(?:with|by|for|to)?|received)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9,.]+)(?:.*?(?:by|from|info:?)\\s+([^.,\n]+))?")
        val verbBeforeCreditMatcher = verbBeforeCreditRegex.matcher(trimmed)
        if (verbBeforeCreditMatcher.find()) {
            val amountStr = verbBeforeCreditMatcher.group(1) ?: "0"
            val rawSender = verbBeforeCreditMatcher.group(2)?.trim()
            val amountMinor = MoneyFormatter.parseToPaise(amountStr)
            if (amountMinor > 0) {
                val cleanSender = cleanMerchant(rawSender ?: (bankName ?: "Income"))
                return ParsedFinancialMessage(
                    amountMinor = amountMinor,
                    type = TransactionType.INCOME,
                    merchantOrPayee = cleanSender,
                    bankName = bankName,
                    upiOrWalletApp = upiOrWalletApp ?: "UPI",
                    accountTail = accountTail,
                    suggestedCategoryName = inferCategory(cleanSender, trimmed, TransactionType.INCOME),
                    dateMillis = messageDate,
                    rawMessage = trimmed
                )
            }
        }

        // 5. Credited with verb AFTER amount: "Rs 50,000 credited to account XX1234 by Salary"
        val verbAfterCreditRegex = Pattern.compile("(?i)(?:rs\\.?|inr|₹)?\\s*([0-9,.]+)\\s+(?:has been|is)?\\s*(?:credited|received)(?:.*?(?:by|from|info:?)\\s+([^.,\n]+))?")
        val verbAfterCreditMatcher = verbAfterCreditRegex.matcher(trimmed)
        if (verbAfterCreditMatcher.find()) {
            val amountStr = verbAfterCreditMatcher.group(1) ?: "0"
            val rawSender = verbAfterCreditMatcher.group(2)?.trim()
            val amountMinor = MoneyFormatter.parseToPaise(amountStr)
            if (amountMinor > 0) {
                val cleanSender = cleanMerchant(rawSender ?: (bankName ?: "Income"))
                return ParsedFinancialMessage(
                    amountMinor = amountMinor,
                    type = TransactionType.INCOME,
                    merchantOrPayee = cleanSender,
                    bankName = bankName,
                    upiOrWalletApp = upiOrWalletApp ?: "UPI",
                    accountTail = accountTail,
                    suggestedCategoryName = inferCategory(cleanSender, trimmed, TransactionType.INCOME),
                    dateMillis = messageDate,
                    rawMessage = trimmed
                )
            }
        }

        return null
    }

    private fun extractBankName(text: String, sender: String?): String? {
        val combined = "$text ${sender ?: ""}"
        val senderUpper = sender?.uppercase() ?: ""
        return when {
            senderUpper.contains("HDFC") || Pattern.compile("(?i)\\b(hdfc|hdfcbk)\\b").matcher(combined).find() -> "HDFC Bank"
            senderUpper.contains("SBI") || Pattern.compile("(?i)\\b(sbi|sbin|state bank)\\b").matcher(combined).find() -> "State Bank of India"
            senderUpper.contains("ICICI") || Pattern.compile("(?i)\\b(icici|icicib)\\b").matcher(combined).find() -> "ICICI Bank"
            senderUpper.contains("AXIS") || Pattern.compile("(?i)\\b(axis|axisbk)\\b").matcher(combined).find() -> "Axis Bank"
            senderUpper.contains("KOTAK") || Pattern.compile("(?i)\\b(kotak|kkbk)\\b").matcher(combined).find() -> "Kotak Mahindra Bank"
            senderUpper.contains("PNB") || Pattern.compile("(?i)\\b(pnb|punb|punjab national)\\b").matcher(combined).find() -> "Punjab National Bank"
            senderUpper.contains("BOB") || Pattern.compile("(?i)\\b(bob|barb|bank of baroda)\\b").matcher(combined).find() -> "Bank of Baroda"
            senderUpper.contains("CAN") || Pattern.compile("(?i)\\b(canara|cnrb)\\b").matcher(combined).find() -> "Canara Bank"
            senderUpper.contains("UNION") || Pattern.compile("(?i)\\b(union bank|ubi)\\b").matcher(combined).find() -> "Union Bank of India"
            senderUpper.contains("INDUS") || Pattern.compile("(?i)\\b(indusind|indb)\\b").matcher(combined).find() -> "IndusInd Bank"
            senderUpper.contains("FDRL") || senderUpper.contains("FED") || Pattern.compile("(?i)\\b(federal|fdrl)\\b").matcher(combined).find() -> "Federal Bank"
            senderUpper.contains("IDFC") || Pattern.compile("(?i)\\b(idfc|idfb)\\b").matcher(combined).find() -> "IDFC First Bank"
            senderUpper.contains("YES") || Pattern.compile("(?i)\\b(yes bank|yesb)\\b").matcher(combined).find() -> "Yes Bank"
            senderUpper.contains("RBL") || Pattern.compile("(?i)\\b(rbl|ratnakar)\\b").matcher(combined).find() -> "RBL Bank"
            senderUpper.contains("AU") || Pattern.compile("(?i)\\b(au small finance|aubank)\\b").matcher(combined).find() -> "AU Bank"
            senderUpper.contains("PAYTM") || Pattern.compile("(?i)\\b(paytm bank|paytmb)\\b").matcher(combined).find() -> "Paytm Payments Bank"
            senderUpper.contains("JIO") || Pattern.compile("(?i)\\b(jio bank|jiobp)\\b").matcher(combined).find() -> "Jio Payments Bank"
            senderUpper.contains("AIRTEL") || Pattern.compile("(?i)\\b(airtel bank|airtel payments)\\b").matcher(combined).find() -> "Airtel Payments Bank"
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
            Pattern.compile("(?i)\\b(freecharge)\\b").matcher(combined).find() -> "Freecharge"
            else -> null
        }
    }

    private fun extractAccountTail(text: String): String? {
        val tailRegex = Pattern.compile("(?i)(?:a/c|acct|card|account)\\s*(?:no\\.?)?\\s*(?:ending\\s+with|is|:)?\\s*([X*]*[0-9]{3,4})")
        val matcher = tailRegex.matcher(text)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun cleanMerchant(raw: String): String {
        var str = raw.trim()

        // 1. Strip leading prefixes like "VPA ", "UPI/ ", "to ", "at ", "towards ", "Info: "
        str = str.replace(Regex("(?i)^(?:vpa|upi[-/:]*|to|at|towards|info:?)\\s*"), "")

        // 2. Strip trailing metadata starting with on/ref/rrn/bal/avl/txn/via/using
        str = str.replace(Regex("(?i)\\b(on|ref|ref no|rrn|upi ref|bal|avl|avail|avl bal|txn|is|via|using)\\b.*"), "")

        // 3. If it's a VPA handle like swiggy@icici or SWIGGY-swiggy@axis, extract the merchant name
        if (str.contains("@")) {
            val handle = str.substringBefore("@").trim()
            if (handle.contains("-") || handle.contains("/")) {
                val parts = handle.split(Regex("[-/]")).map { it.trim() }.filter { it.isNotBlank() }
                val nonNumeric = parts.filter { !it.all { c -> c.isDigit() } }
                val candidate = nonNumeric.firstOrNull() ?: parts.firstOrNull()
                if (!candidate.isNullOrBlank()) {
                    str = candidate
                }
            } else if (handle.isNotBlank() && handle.length >= 3) {
                str = handle
            }
        }

        // 4. Strip technical merchant codes like "UPI/12345/SWIGGY" -> "SWIGGY"
        if (str.contains("/")) {
            val parts = str.split("/").map { it.trim() }.filter { it.isNotBlank() }
            str = parts.lastOrNull { !it.all { c -> c.isDigit() } } ?: parts.last()
        }

        // 5. Clean special characters except letters, digits, space, &, -
        str = str.replace(Regex("[^A-Za-z0-9 &'-]"), " ").trim()

        // 6. Clean whitespace and preserve original case
        val words = str.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val cleaned = words.joinToString(" ")

        return cleaned.ifBlank { "Merchant" }
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
            listOf("swiggy", "zomato", "starbucks", "mcdonald", "burger king", "kfc", "domino", "pizza", "subway", "haldiram", "cafe", "restaurant", "bakery", "chai", "bar", "dhabha", "dining", "biryani", "food").any { text.contains(it) } -> "Food & Dining"

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
