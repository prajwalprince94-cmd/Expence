package com.knowspends.app.sms

import android.content.Context
import android.net.Uri
import android.util.Log
import com.knowspends.app.di.IoDispatcher
import com.knowspends.app.domain.repository.PendingTransactionRepository
import com.knowspends.app.domain.util.BankMessageParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class SmsScanResult(
    val totalScanned: Int,
    val financialFound: Int,
    val newlyAdded: Int,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

@Singleton
class SmsScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pendingTransactionRepository: PendingTransactionRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    companion object {
        private const val TAG = "SmsScanner"
        private val SMS_INBOX_URI = Uri.parse("content://sms/inbox")
    }

    suspend fun scanInbox(daysBack: Int = 30): SmsScanResult = withContext(ioDispatcher) {
        val cutoffTimestamp = System.currentTimeMillis() - (daysBack.toLong() * 24L * 60L * 60L * 1000L)

        var totalScanned = 0
        var financialFound = 0
        var newlyAdded = 0

        try {
            val projection = arrayOf("_id", "address", "body", "date")
            val selection = "date >= ?"
            val selectionArgs = arrayOf(cutoffTimestamp.toString())
            val sortOrder = "date DESC"

            context.contentResolver.query(
                SMS_INBOX_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val bodyIndex = cursor.getColumnIndex("body")
                val addressIndex = cursor.getColumnIndex("address")

                while (cursor.moveToNext()) {
                    totalScanned++
                    val body = if (bodyIndex != -1) cursor.getString(bodyIndex) else ""
                    val address = if (addressIndex != -1) cursor.getString(addressIndex) else ""

                    if (body.isNotBlank() && !BankMessageParser.isIgnoredMessage(body)) {
                        val parsed = BankMessageParser.parse(body, address)
                        if (parsed != null) {
                            financialFound++
                            val added = pendingTransactionRepository.parseAndAddMessage(body)
                            if (added) {
                                newlyAdded++
                            }
                        }
                    }
                }
            }

            Log.d(TAG, "SMS Scan complete: $totalScanned scanned, $financialFound financial, $newlyAdded newly added.")
            SmsScanResult(
                totalScanned = totalScanned,
                financialFound = financialFound,
                newlyAdded = newlyAdded,
                isSuccess = true
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "READ_SMS permission not granted", e)
            SmsScanResult(
                totalScanned = 0,
                financialFound = 0,
                newlyAdded = 0,
                isSuccess = false,
                errorMessage = "SMS permission required to scan your inbox"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning SMS inbox", e)
            SmsScanResult(
                totalScanned = totalScanned,
                financialFound = financialFound,
                newlyAdded = newlyAdded,
                isSuccess = false,
                errorMessage = e.localizedMessage ?: "Failed to scan SMS inbox"
            )
        }
    }
}
