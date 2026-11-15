package com.knowspends.app.data.repository

import com.knowspends.app.data.local.dao.AccountDao
import com.knowspends.app.data.local.dao.CategoryDao
import com.knowspends.app.data.local.dao.TransactionDao
import com.knowspends.app.data.local.entity.AccountEntity
import com.knowspends.app.data.local.entity.CategoryEntity
import com.knowspends.app.data.local.entity.TransactionEntity
import com.knowspends.app.di.IoDispatcher
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.repository.CsvImportResult
import com.knowspends.app.domain.repository.CsvRepository
import com.knowspends.app.domain.util.MoneyFormatter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CsvRepository {

    companion object {
        private val FORMULA_PREFIXES = charArrayOf('=', '+', '-', '@', '\t', '\r', '|')
    }

    private val csvDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    override suspend fun exportTransactionsCsv(outputStream: OutputStream) = withContext(ioDispatcher) {
        val transactions = transactionDao.getAllTransactionsWithDetailsFlow().firstOrNull() ?: emptyList()

        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            // Write UTF-8 BOM so Microsoft Excel and other spreadsheet apps open as UTF-8 without mangling currency symbols
            writer.write("\uFEFF")
            // Header
            writer.write("Date,Type,Amount_Minor,Amount_Formatted,Category,Account,Transfer_Account,Payee,Note\n")

            for (item in transactions) {
                val t = item.transaction
                val dateStr = csvDateFormat.format(Date(t.date))
                val isNegative = t.amountMinor < 0
                val safePaise = if (t.amountMinor == Long.MIN_VALUE) Long.MAX_VALUE else Math.abs(t.amountMinor)
                val sign = if (isNegative) "-" else ""
                val formattedAmount = "$sign${safePaise / 100}.${(safePaise % 100).toString().padStart(2, '0')}"

                val line = listOf(
                    escapeCsv(dateStr),
                    escapeCsv(t.type),
                    escapeCsv(t.amountMinor.toString(), isNumeric = true),
                    escapeCsv(formattedAmount, isNumeric = true),
                    escapeCsv(item.categoryName ?: ""),
                    escapeCsv(item.accountName),
                    escapeCsv(item.transferAccountName ?: ""),
                    escapeCsv(t.payee),
                    escapeCsv(t.note)
                ).joinToString(",")

                writer.write(line)
                writer.write("\n")
            }
            writer.flush()
        }
    }

    override suspend fun importTransactionsCsv(inputStream: InputStream): CsvImportResult = withContext(ioDispatcher) {
        try {
            val records = parseCsvRecords(inputStream.bufferedReader(Charsets.UTF_8))
            if (records.isEmpty()) {
                return@withContext CsvImportResult(0, 0, 0, errorMessage = "File is empty")
            }

            val existingHashes = transactionDao.getAllSourceHashes().toHashSet()
            val accounts = accountDao.getAllAccountsFlow().firstOrNull() ?: emptyList()
            val defaultAccount = accounts.firstOrNull() ?: run {
                val newAccId = accountDao.insertAccount(
                    AccountEntity(name = "Default Account", type = "BANK", openingBalanceMinor = 0L)
                )
                AccountEntity(id = newAccId, name = "Default Account", type = "BANK", openingBalanceMinor = 0L)
            }
            val accountMap = (accounts + defaultAccount).associateBy { it.name.trim().lowercase() }.toMutableMap()

            val categories = categoryDao.getAllCategoriesFlow().firstOrNull() ?: emptyList()
            val categoryMap = categories.associateBy { it.name.trim().lowercase() }.toMutableMap()

            val batchId = UUID.randomUUID().toString()
            var importedCount = 0
            var skippedDuplicates = 0
            val transactionsToInsert = mutableListOf<TransactionEntity>()

            // Find header indexes
            val headers = records.first().map { it.trim().lowercase() }

            val dateIdx = headers.indexOfFirst { it.contains("date") }
            val typeIdx = headers.indexOfFirst { it.contains("type") }
            val amountMinorIdx = headers.indexOfFirst { it == "amount_minor" || it == "paise" }
            val amountFormattedIdx = headers.indexOfFirst { it == "amount_formatted" }
            val amountIdx = if (amountFormattedIdx != -1) amountFormattedIdx else headers.indexOfFirst { it.contains("amount") && it != "amount_minor" && it != "paise" }
            val catIdx = headers.indexOfFirst { it.contains("cat") }
            val accIdx = headers.indexOfFirst { it.contains("account") }
            val payeeIdx = headers.indexOfFirst { it.contains("payee") || it.contains("merchant") }
            val noteIdx = headers.indexOfFirst { it.contains("note") || it.contains("desc") }

            for (i in 1 until records.size) {
                val row = records[i]
                if (row.isEmpty() || row.all { it.isBlank() }) continue

                val dateStr = if (dateIdx in row.indices) desanitizeFormula(row[dateIdx].trim()) else ""
                val rawTypeStr = if (typeIdx in row.indices) desanitizeFormula(row[typeIdx].trim().uppercase()) else ""
                val catStr = if (catIdx in row.indices) desanitizeFormula(row[catIdx].trim()) else ""
                val accStr = if (accIdx in row.indices) desanitizeFormula(row[accIdx].trim()) else ""
                val payeeStr = if (payeeIdx in row.indices) desanitizeFormula(row[payeeIdx].trim()) else ""
                val noteStr = if (noteIdx in row.indices) desanitizeFormula(row[noteIdx]) else ""

                val rawAmountMinor = when {
                    amountMinorIdx != -1 && amountMinorIdx in row.indices && row[amountMinorIdx].isNotBlank() -> {
                        row[amountMinorIdx].trim().toLongOrNull() ?: (if (amountIdx in row.indices) MoneyFormatter.parseToPaise(row[amountIdx]) else 0L)
                    }
                    amountIdx != -1 && amountIdx in row.indices -> {
                        MoneyFormatter.parseToPaise(row[amountIdx])
                    }
                    else -> 0L
                }
                if (rawAmountMinor == 0L) continue

                val isNegative = rawAmountMinor < 0
                val amountMinor = Math.abs(rawAmountMinor)

                val typeStr = when {
                    rawTypeStr in listOf("EXPENSE", "INCOME", "TRANSFER") -> rawTypeStr
                    rawTypeStr.contains("CR") || rawTypeStr.contains("CREDIT") -> "INCOME"
                    rawTypeStr.contains("DR") || rawTypeStr.contains("DEBIT") -> "EXPENSE"
                    isNegative -> "EXPENSE"
                    else -> "EXPENSE"
                }

                val dateMillis = parseDate(dateStr) ?: continue
                val resolvedAccount = accountMap[accStr.lowercase()] ?: defaultAccount

                val resolvedCategory = if (catStr.isNotBlank()) {
                    categoryMap[catStr.lowercase()] ?: run {
                        val newCatId = categoryDao.insertCategory(
                            CategoryEntity(name = catStr, kind = typeStr, iconKey = "label", colour = 0xFF1976D2)
                        )
                        val newCat = CategoryEntity(id = newCatId, name = catStr, kind = typeStr, iconKey = "label", colour = 0xFF1976D2)
                        categoryMap[catStr.lowercase()] = newCat
                        newCat
                    }
                } else null

                // Compute hash for deduplication: dateMillis + amountMinor + payee + accountId
                val sourceHash = computeSourceHash(dateMillis, amountMinor, payeeStr, resolvedAccount.id)

                if (existingHashes.contains(sourceHash)) {
                    skippedDuplicates++
                    continue
                }

                existingHashes.add(sourceHash)
                transactionsToInsert.add(
                    TransactionEntity(
                        accountId = resolvedAccount.id,
                        categoryId = resolvedCategory?.id,
                        amountMinor = amountMinor,
                        type = if (typeStr in listOf("EXPENSE", "INCOME", "TRANSFER")) typeStr else "EXPENSE",
                        date = dateMillis,
                        note = noteStr,
                        payee = payeeStr,
                        importBatchId = batchId,
                        sourceHash = sourceHash
                    )
                )
                importedCount++
            }

            if (transactionsToInsert.isNotEmpty()) {
                transactionDao.insertTransactions(transactionsToInsert)
            }

            CsvImportResult(
                totalRowsRead = records.size - 1,
                importedCount = importedCount,
                skippedDuplicatesCount = skippedDuplicates,
                batchId = batchId
            )
        } catch (e: Exception) {
            CsvImportResult(0, 0, 0, errorMessage = e.localizedMessage ?: "Failed to import CSV")
        }
    }

    override suspend fun undoImportBatch(batchId: String): Int = withContext(ioDispatcher) {
        transactionDao.deleteBatchTransactions(batchId)
    }

    private fun escapeCsv(value: String, isNumeric: Boolean = false): String {
        var sanitized = value
        // CWE-1236: Prevent CSV formula injection by prepending ' to non-numeric cells starting with trigger chars or single quote
        if (!isNumeric && sanitized.isNotEmpty()) {
            val firstChar = sanitized[0]
            if (firstChar in FORMULA_PREFIXES || firstChar == '\'') {
                sanitized = "'$sanitized"
            }
        }
        return if (sanitized.contains(",") || sanitized.contains("\"") || sanitized.contains("\n") || sanitized.contains("\r")) {
            "\"" + sanitized.replace("\"", "\"\"") + "\""
        } else {
            sanitized
        }
    }

    private fun desanitizeFormula(value: String): String {
        if (value.startsWith("'") && value.length > 1) {
            val secondChar = value[1]
            if (secondChar in FORMULA_PREFIXES || secondChar == '\'') {
                return value.substring(1)
            }
        }
        return value
    }

    private fun parseCsvRecords(reader: java.io.Reader): List<List<String>> {
        val records = mutableListOf<List<String>>()
        val currentRecord = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false
        var isFirstChar = true

        var chInt = reader.read()
        while (chInt != -1) {
            val c = chInt.toChar()

            if (isFirstChar) {
                isFirstChar = false
                if (c == '\uFEFF') {
                    chInt = reader.read()
                    continue
                }
            }

            if (inQuotes) {
                if (c == '"') {
                    reader.mark(1)
                    val nextInt = reader.read()
                    if (nextInt != -1 && nextInt.toChar() == '"') {
                        currentField.append('"')
                    } else {
                        inQuotes = false
                        reader.reset()
                    }
                } else {
                    currentField.append(c)
                }
            } else {
                when (c) {
                    '"' -> inQuotes = true
                    ',' -> {
                        currentRecord.add(currentField.toString())
                        currentField.setLength(0)
                    }
                    '\r' -> {
                        reader.mark(1)
                        val nextInt = reader.read()
                        if (nextInt != -1 && nextInt.toChar() != '\n') {
                            reader.reset()
                        }
                        currentRecord.add(currentField.toString())
                        currentField.setLength(0)
                        if (currentRecord.any { it.isNotBlank() }) {
                            records.add(ArrayList(currentRecord))
                        }
                        currentRecord.clear()
                    }
                    '\n' -> {
                        currentRecord.add(currentField.toString())
                        currentField.setLength(0)
                        if (currentRecord.any { it.isNotBlank() }) {
                            records.add(ArrayList(currentRecord))
                        }
                        currentRecord.clear()
                    }
                    else -> currentField.append(c)
                }
            }
            chInt = reader.read()
        }

        if (currentField.isNotEmpty() || currentRecord.isNotEmpty()) {
            currentRecord.add(currentField.toString())
            if (currentRecord.any { it.isNotBlank() }) {
                records.add(ArrayList(currentRecord))
            }
        }
        return records
    }

    fun parseCsvRow(line: String): List<String> {
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
        return tokens
    }

    private fun parseDate(dateStr: String): Long? {
        val trimmed = dateStr.trim()
        if (trimmed.isBlank()) return null

        trimmed.toLongOrNull()?.let {
            if (it > 0) return it
        }

        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US),
            SimpleDateFormat("dd/MM/yyyy", Locale.US),
            SimpleDateFormat("MM/dd/yyyy", Locale.US),
            SimpleDateFormat("dd-MM-yyyy", Locale.US),
            SimpleDateFormat("yyyy/MM/dd", Locale.US)
        )
        for (f in formats) {
            val parsed = runCatching { f.parse(trimmed) }.getOrNull()
            if (parsed != null) return parsed.time
        }
        return null
    }

    private fun computeSourceHash(dateMillis: Long, amountMinor: Long, payee: String, accountId: Long): String {
        val raw = "$dateMillis:$amountMinor:${payee.trim().lowercase()}:$accountId"
        val bytes = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
