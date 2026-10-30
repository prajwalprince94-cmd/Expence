package com.knowspends.app.domain.repository

import com.knowspends.app.domain.model.BackupPayload
import com.knowspends.app.domain.model.RestoreResult
import java.io.InputStream
import java.io.OutputStream

interface BackupRepository {
    suspend fun createBackupPayload(): BackupPayload
    suspend fun exportBackupJson(outputStream: OutputStream)
    suspend fun restoreBackupJson(inputStream: InputStream): RestoreResult
}
