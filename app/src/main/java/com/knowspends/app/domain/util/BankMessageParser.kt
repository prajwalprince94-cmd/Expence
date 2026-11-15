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

    private const val MAX_SMS_LENGTH = 1000

    private val IGNORED_PATTERNS = listOf(
        Pattern.compile("(?i)\\b(otp|verification code|one time password|secret code|security code|do not share|valid for|is your secret)\\b"),
        Pattern.compile("(?i)\\b(pre-approved|apply now|congratulations|click here to claim|win up to)\\b"),
        Pattern.compile("(?i)\\b(failed|declined|unsuccessful|reversed|insufficient funds|declined due to|mandate revoked|transaction expired)\\b")
    )

    private const val CURRENCY_PREFIX = "(?:rs\\.?|inr|₹|usd|\\$|eur|€|gbp|£)?"

    private val PAID_REGEX = Pattern.compile("(?i)(?:paid|sent|transfer(?:red)?)\\s*$CURRENCY_PREFIX\\s*([0-9,.]+)(?:.*?(?:\\bto\\b|\\bat\\b|\\btowards\\b)\\s+([^.,\n]+))?")
    private val VERB_BEFORE_DEBIT_REGEX = Pattern.compile("(?i)(?:(?:auto[- ]?)?debit(?:ed)?\\s*(?:of|by|for|with|towards)?|deducted\\s*(?:by|for)?|spent\\s*(?:an amount of)?|withdrawn\\s*(?:by|of)?)\\s*$CURRENCY_PREFIX\\s*([0-9,.]+)(?:.*?(?:\\bto\\b|\\bat\\b|\\btowards\\b|\\binfo:?|\\btransfer to\\b|\\bvpa\\b)\\s+([^.,\n]+))?")
    private val VERB_AFTER_DEBIT_REGEX = Pattern.compile("(?i)$CURRENCY_PREFIX\\s*([0-9,.]+)\\s+(?:has been|is)?\\s*(?:debited|spent|withdrawn|deducted)(?:.*?(?:\\bto\\b|\\bat\\b|\\btowards\\b|\\binfo:?|\\btransfer to\\b|\\bvpa\\b)\\s+([^.,\n]+))?")
    private val VERB_BEFORE_CREDIT_REGEX = Pattern.compile("(?i)(?:credited\\s*(?:with|by|for|to)?|received)\\s*$CURRENCY_PREFIX\\s*([0-9,.]+)(?:.*?(?:\\bby\\b|\\bfrom\\b|\\binfo:?)\\s+([^.,\n]+))?")
    private val VERB_AFTER_CREDIT_REGEX = Pattern.compile("(?i)$CURRENCY_PREFIX\\s*([0-9,.]+)\\s+(?:has been|is)?\\s*(?:credited|received)(?:.*?(?:\\bby\\b|\\bfrom\\b|\\binfo:?)\\s+([^.,\n]+))?")

    private val ACCOUNT_TAIL_REGEX = Pattern.compile("(?i)(?:a/c|acct|card|account)\\s*(?:no\\.?)?\\s*(?:ending\\s+with|is|:)?\\s*([X*]*[0-9]{3,4})\\b")

    private val LEADING_PREFIX_REGEX = Pattern.compile("(?i)^(?:vpa|upi[-/:]*|to|at|towards|info:?)\\s*")
    private val TRAILING_NOISE_REGEX = Pattern.compile("(?i)\\b(ref\\s*no|ref|rrn|upi\\s*ref|bal\\b|avl\\s*bal|avail\\s*bal|balance|txn\\s*id|txnid|txn|via|using|on\\s+\\d{1,2}[-/\\s]|on\\s+[0-9]{1,2}(?:st|nd|rd|th)?\\s+[a-z]{3})\\b.*")
    private val NON_ALPHANUM_REGEX = Pattern.compile("[^A-Za-z0-9 &'-]")
    private val MULTI_SPACE_REGEX = Pattern.compile("\\s+")
    private val DASH_SLASH_REGEX = Pattern.compile("[-/]")

    private val BANK_PATTERNS = listOf(
        "HDFC Bank" to Pattern.compile("(?i)\\b(hdfc|hdfcbk)\\b"),
        "State Bank of India" to Pattern.compile("(?i)\\b(sbi|sbin|state bank)\\b"),
        "ICICI Bank" to Pattern.compile("(?i)\\b(icici|icicib)\\b"),
        "Axis Bank" to Pattern.compile("(?i)\\b(axis|axisbk)\\b"),
        "Kotak Mahindra Bank" to Pattern.compile("(?i)\\b(kotak|kkbk)\\b"),
        "Punjab National Bank" to Pattern.compile("(?i)\\b(pnb|punb|punjab national)\\b"),
        "Bank of Baroda" to Pattern.compile("(?i)\\b(bob|barb|bank of baroda)\\b"),
        "Canara Bank" to Pattern.compile("(?i)\\b(canara|cnrb)\\b"),
        "Union Bank of India" to Pattern.compile("(?i)\\b(union bank|ubi)\\b"),
        "IndusInd Bank" to Pattern.compile("(?i)\\b(indusind|indb)\\b"),
        "Federal Bank" to Pattern.compile("(?i)\\b(federal|fdrl)\\b"),
        "IDFC First Bank" to Pattern.compile("(?i)\\b(idfc|idfb)\\b"),
        "Yes Bank" to Pattern.compile("(?i)\\b(yes bank|yesb)\\b"),
        "RBL Bank" to Pattern.compile("(?i)\\b(rbl|ratnakar)\\b"),
        "AU Bank" to Pattern.compile("(?i)\\b(au small finance|aubank)\\b"),
        "Paytm Payments Bank" to Pattern.compile("(?i)\\b(paytm bank|paytmb)\\b"),
        "Jio Payments Bank" to Pattern.compile("(?i)\\b(jio bank|jiobp)\\b"),
        "Airtel Payments Bank" to Pattern.compile("(?i)\\b(airtel bank|airtel payments)\\b")
    )

    private val UPI_APP_PATTERNS = listOf(
        "Google Pay" to Pattern.compile("(?i)\\b(google pay|gpay|okhdfcbank|okaxis|oksbi|okicici)\\b"),
        "PhonePe" to Pattern.compile("(?i)\\b(phonepe|ybl|ibl|axl)\\b"),
        "Paytm" to Pattern.compile("(?i)\\b(paytm|ptm)\\b"),
        "CRED" to Pattern.compile("(?i)\\b(cred|credpay)\\b"),
        "Amazon Pay" to Pattern.compile("(?i)\\b(amazon pay|amazonpay|apl)\\b"),
        "BHIM" to Pattern.compile("(?i)\\b(bhim)\\b"),
        "WhatsApp Pay" to Pattern.compile("(?i)\\b(whatsapp)\\b"),
        "Mobikwik" to Pattern.compile("(?i)\\b(mobikwik)\\b"),
        "Freecharge" to Pattern.compile("(?i)\\b(freecharge)\\b")
    )

    fun isIgnoredMessage(text: String): Boolean {
        for (pattern in IGNORED_PATTERNS) {
            if (pattern.matcher(text).find()) {
                return true
            }
        }
        return false
    }

    fun parse(rawText: String, sender: String? = null, messageDate: Long = System.currentTimeMillis()): ParsedFinancialMessage? {
        if (rawText.length > MAX_SMS_LENGTH) return null
        val trimmed = rawText.trim()
        if (isIgnoredMessage(trimmed)) {
            return null
        }

        val bankName = extractBankName(trimmed, sender)
        val upiOrWalletApp = extractUpiOrWalletApp(trimmed, sender)
        val accountTail = extractAccountTail(trimmed)

        // 1. Paid/Sent/Transfer Expense: "Paid Rs.450 to Starbucks" / "Sent $350 from A/c to rahul@upi"
        val paidMatcher = PAID_REGEX.matcher(trimmed)
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

        // 2. Debited with verb BEFORE amount: "debited by 500.00" / "debited for USD 1,250" / "debited with Rs 899" / "spent EUR 1,500"
        val verbBeforeMatcher = VERB_BEFORE_DEBIT_REGEX.matcher(trimmed)
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

        // 3. Debited with verb AFTER amount: "Rs.1,500.00 debited from HDFC A/c to Swiggy" / "$500 spent at Starbucks"
        val verbAfterMatcher = VERB_AFTER_DEBIT_REGEX.matcher(trimmed)
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

        // 4. Credited with verb BEFORE amount: "credited with Rs 2,000" / "received $5,000"
        val verbBeforeCreditMatcher = VERB_BEFORE_CREDIT_REGEX.matcher(trimmed)
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

        // 5. Credited with verb AFTER amount: "Rs 50,000 credited to account XX1234 by Salary" / "€200 received from Alice"
        val verbAfterCreditMatcher = VERB_AFTER_CREDIT_REGEX.matcher(trimmed)
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

        // Fast path by sender code
        if (senderUpper.contains("HDFC")) return "HDFC Bank"
        if (senderUpper.contains("SBI")) return "State Bank of India"
        if (senderUpper.contains("ICICI")) return "ICICI Bank"
        if (senderUpper.contains("AXIS")) return "Axis Bank"
        if (senderUpper.contains("KOTAK")) return "Kotak Mahindra Bank"
        if (senderUpper.contains("PNB")) return "Punjab National Bank"
        if (senderUpper.contains("BOB")) return "Bank of Baroda"
        if (senderUpper.contains("CAN")) return "Canara Bank"
        if (senderUpper.contains("UNION")) return "Union Bank of India"
        if (senderUpper.contains("INDUS")) return "IndusInd Bank"
        if (senderUpper.contains("FDRL") || senderUpper.contains("FED")) return "Federal Bank"
        if (senderUpper.contains("IDFC")) return "IDFC First Bank"
        if (senderUpper.contains("YES")) return "Yes Bank"
        if (senderUpper.contains("RBL")) return "RBL Bank"
        if (senderUpper.contains("AU")) return "AU Bank"
        if (senderUpper.contains("PAYTM")) return "Paytm Payments Bank"
        if (senderUpper.contains("JIO")) return "Jio Payments Bank"
        if (senderUpper.contains("AIRTEL")) return "Airtel Payments Bank"

        for ((bank, pattern) in BANK_PATTERNS) {
            if (pattern.matcher(combined).find()) {
                return bank
            }
        }
        return null
    }

    private fun extractUpiOrWalletApp(text: String, sender: String?): String? {
        val combined = "$text ${sender ?: ""}"
        for ((app, pattern) in UPI_APP_PATTERNS) {
            if (pattern.matcher(combined).find()) {
                return app
            }
        }
        return null
    }

    private fun extractAccountTail(text: String): String? {
        val matcher = ACCOUNT_TAIL_REGEX.matcher(text)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun cleanMerchant(raw: String): String {
        var str = raw.trim()

        // 1. Strip leading prefixes like "VPA ", "UPI/ ", "to ", "at ", "towards ", "Info: "
        str = LEADING_PREFIX_REGEX.matcher(str).replaceFirst("")

        // 2. Strip trailing metadata starting with on <date>, ref, rrn, bal, avl, txn, via, using
        str = TRAILING_NOISE_REGEX.matcher(str).replaceFirst("")

        // 3. If it's a VPA handle like swiggy@icici or SWIGGY-swiggy@axis, extract the merchant name
        if (str.contains("@")) {
            val handle = str.substringBefore("@").trim()
            if (handle.contains("-") || handle.contains("/")) {
                val parts = DASH_SLASH_REGEX.split(handle).map { it.trim() }.filter { it.isNotBlank() }
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
        str = NON_ALPHANUM_REGEX.matcher(str).replaceAll(" ").trim()

        // 6. Clean whitespace and preserve original case
        val words = MULTI_SPACE_REGEX.split(str).filter { it.isNotBlank() }
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
