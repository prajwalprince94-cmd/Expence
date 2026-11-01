package com.knowspends.app.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat
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
        private val FALLBACK_INBOX_URI = Uri.parse("content://sms/inbox")
        private val FALLBACK_SMS_URI = Uri.parse("content://sms")
    }

    suspend fun scanInbox(daysBack: Int = 30): SmsScanResult = withContext(ioDispatcher) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return@withContext SmsScanResult(
                totalScanned = 0,
                financialFound = 0,
                newlyAdded = 0,
                isSuccess = false,
                errorMessage = "SMS permission required. Tap 'Grant SMS Permission' to allow scanning."
            )
        }

        val cutoffTimestamp = System.currentTimeMillis() - (daysBack.toLong() * 24L * 60L * 60L * 1000L)

        var totalScanned = 0
        var financialFound = 0
        var newlyAdded = 0

        try {
            val projection = arrayOf("_id", "address", "body", "date")
            val urisToTry = listOf(
                Telephony.Sms.Inbox.CONTENT_URI,
                FALLBACK_INBOX_URI,
                FALLBACK_SMS_URI
            )

            var scannedAny = false

            for (targetUri in urisToTry) {
                if (scannedAny) break

                // First try with date cutoff filter
                var cursor = try {
                    context.contentResolver.query(
                        targetUri,
                        projection,
                        "date >= ?",
                        arrayOf(cutoffTimestamp.toString()),
                        "date DESC"
                    )
                } catch (e: Exception) {
                    null
                }

                // If date filter returned empty (OEM seconds vs ms issue), fallback to latest 250 messages
                if (cursor == null || cursor.count == 0) {
                    cursor?.close()
                    cursor = try {
                        context.contentResolver.query(
                            targetUri,
                            projection,
                            null,
                            null,
                            "date DESC LIMIT 250"
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                cursor?.use { c ->
                    val bodyIndex = c.getColumnIndex("body")
                    val addressIndex = c.getColumnIndex("address")
                    val dateIndex = c.getColumnIndex("date")

                    while (c.moveToNext()) {
                        totalScanned++
                        scannedAny = true

                        val body = if (bodyIndex != -1) c.getString(bodyIndex) ?: "" else ""
                        val address = if (addressIndex != -1) c.getString(addressIndex) ?: "" else ""
                        val rawDate = if (dateIndex != -1) c.getLong(dateIndex) else System.currentTimeMillis()
                        // Normalize seconds timestamp to ms
                        val date = if (rawDate in 1L..9999999999L) rawDate * 1000L else rawDate

                        if (body.isNotBlank() && !BankMessageParser.isIgnoredMessage(body)) {
                            val parsed = BankMessageParser.parse(body, address, date)
                            if (parsed != null) {
                                financialFound++
                                val added = pendingTransactionRepository.parseAndAddMessage(
                                    rawText = body,
                                    sender = address,
                                    dateMillis = date
                                )
                                if (added) {
                                    newlyAdded++
                                }
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
