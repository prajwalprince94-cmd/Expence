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

    private val csvDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    override suspend fun exportTransactionsCsv(outputStream: OutputStream) = withContext(ioDispatcher) {
        val transactions = transactionDao.getAllTransactionsWithDetailsFlow().firstOrNull() ?: emptyList()

        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            // Header
            writer.write("Date,Type,Amount_Minor,Amount_Formatted,Category,Account,Transfer_Account,Payee,Note\n")

            for (item in transactions) {
                val t = item.transaction
                val dateStr = csvDateFormat.format(Date(t.date))
                val formattedAmount = (t.amountMinor / 100.0).toString()

                val line = listOf(
                    dateStr,
                    t.type,
                    t.amountMinor.toString(),
                    formattedAmount,
                    item.categoryName ?: "",
                    item.accountName,
                    item.transferAccountName ?: "",
                    t.payee,
                    t.note
                ).joinToString(",") { escapeCsv(it) }

                writer.write(line)
                writer.write("\n")
            }
            writer.flush()
        }
    }

    override suspend fun importTransactionsCsv(inputStream: InputStream): CsvImportResult = withContext(ioDispatcher) {
        try {
            val lines = inputStream.bufferedReader(Charsets.UTF_8).readLines()
            if (lines.isEmpty()) {
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
            val headerLine = lines.first()
            val headers = parseCsvRow(headerLine).map { it.trim().lowercase() }

            val dateIdx = headers.indexOfFirst { it.contains("date") }
            val typeIdx = headers.indexOfFirst { it.contains("type") }
            val amountIdx = headers.indexOfFirst { it.contains("amount") }
            val catIdx = headers.indexOfFirst { it.contains("cat") }
            val accIdx = headers.indexOfFirst { it.contains("account") }
            val payeeIdx = headers.indexOfFirst { it.contains("payee") || it.contains("merchant") }
            val noteIdx = headers.indexOfFirst { it.contains("note") || it.contains("desc") }

            for (i in 1 until lines.size) {
                val rowText = lines[i].trim()
                if (rowText.isBlank()) continue

                val row = parseCsvRow(rowText)
                if (row.isEmpty()) continue

                val dateStr = if (dateIdx in row.indices) row[dateIdx] else ""
                val typeStr = if (typeIdx in row.indices) row[typeIdx].uppercase() else "EXPENSE"
                val amountStr = if (amountIdx in row.indices) row[amountIdx] else "0"
                val catStr = if (catIdx in row.indices) row[catIdx].trim() else ""
                val accStr = if (accIdx in row.indices) row[accIdx].trim() else ""
                val payeeStr = if (payeeIdx in row.indices) row[payeeIdx].trim() else ""
                val noteStr = if (noteIdx in row.indices) row[noteIdx].trim() else ""

                val amountMinor = MoneyFormatter.parseToPaise(amountStr)
                if (amountMinor <= 0) continue

                val dateMillis = parseDate(dateStr)
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
                totalRowsRead = lines.size - 1,
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

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun parseCsvRow(line: String): List<String> {
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

    private fun parseDate(dateStr: String): Long {
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
            SimpleDateFormat("dd/MM/yyyy", Locale.US),
            SimpleDateFormat("MM/dd/yyyy", Locale.US)
        )
        for (f in formats) {
            val parsed = runCatching { f.parse(dateStr) }.getOrNull()
            if (parsed != null) return parsed.time
        }
        return System.currentTimeMillis()
    }

    private fun computeSourceHash(dateMillis: Long, amountMinor: Long, payee: String, accountId: Long): String {
        val raw = "$dateMillis:$amountMinor:${payee.trim().lowercase()}:$accountId"
        val bytes = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
