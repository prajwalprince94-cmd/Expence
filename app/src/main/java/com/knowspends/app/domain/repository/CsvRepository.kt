package com.knowspends.app.domain.repository

import java.io.InputStream
import java.io.OutputStream

data class CsvImportResult(
    val totalRowsRead: Int,
    val importedCount: Int,
    val skippedDuplicatesCount: Int,
    val batchId: String? = null,
    val errorMessage: String? = null
)

interface CsvRepository {
    suspend fun exportTransactionsCsv(outputStream: OutputStream)
    suspend fun importTransactionsCsv(inputStream: InputStream): CsvImportResult
    suspend fun undoImportBatch(batchId: String): Int
}
