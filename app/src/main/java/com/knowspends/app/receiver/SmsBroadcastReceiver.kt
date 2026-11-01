package com.knowspends.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.knowspends.app.domain.model.Transaction
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.repository.AccountRepository
import com.knowspends.app.domain.repository.CategoryRepository
import com.knowspends.app.domain.repository.PendingTransactionRepository
import com.knowspends.app.domain.repository.TransactionRepository
import com.knowspends.app.domain.repository.UserPreferencesRepository
import com.knowspends.app.domain.util.BankMessageParser
import com.knowspends.app.domain.util.MoneyFormatter
import com.knowspends.app.util.NotificationHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmsBroadcastReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SmsReceiverEntryPoint {
        fun userPreferencesRepository(): UserPreferencesRepository
        fun pendingTransactionRepository(): PendingTransactionRepository
        fun transactionRepository(): TransactionRepository
        fun accountRepository(): AccountRepository
        fun categoryRepository(): CategoryRepository
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val fullBody = messages.joinToString("") { it.messageBody ?: "" }
        val sender = messages.firstOrNull()?.originatingAddress ?: ""

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SmsReceiverEntryPoint::class.java
        )

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = entryPoint.userPreferencesRepository().userPreferencesFlow.first()
                if (!prefs.smsAutoCaptureEnabled) {
                    return@launch
                }

                val parsed = BankMessageParser.parse(fullBody, sender) ?: return@launch
                Log.d("SmsReceiver", "Parsed financial SMS from $sender: amount=${parsed.amountMinor}, payee=${parsed.merchantOrPayee}, bank=${parsed.bankName}")

                if (prefs.smsDirectAutoAdd) {
                    // Direct Auto-Add Mode: Find or match account, category, and insert directly to ledger
                    val accounts = entryPoint.accountRepository().getActiveAccounts().first()
                    val matchingAccount = accounts.find { acc ->
                        parsed.accountTail != null && acc.name.contains(parsed.accountTail)
                    } ?: accounts.find { acc ->
                        parsed.bankName != null && acc.name.contains(parsed.bankName, ignoreCase = true)
                    } ?: accounts.find { it.type.name == "BANK" || it.type.name == "WALLET" } ?: accounts.firstOrNull()

                    val categories = entryPoint.categoryRepository().getActiveCategories().first()
                    val matchingCategory = categories.find { cat ->
                        parsed.suggestedCategoryName != null && cat.name.equals(parsed.suggestedCategoryName, ignoreCase = true)
                    } ?: categories.find { it.kind == parsed.type }

                    val accountId = matchingAccount?.id ?: 1L
                    val categoryId = matchingCategory?.id

                    val noteParts = mutableListOf<String>()
                    parsed.bankName?.let { noteParts.add(it) }
                    parsed.accountTail?.let { noteParts.add("A/c $it") }
                    parsed.upiOrWalletApp?.let { noteParts.add("via $it") }
                    val note = if (noteParts.isNotEmpty()) noteParts.joinToString(" • ") else "Auto-captured SMS"

                    val tx = Transaction(
                        accountId = accountId,
                        categoryId = categoryId,
                        amountMinor = parsed.amountMinor,
                        type = parsed.type,
                        payee = parsed.merchantOrPayee,
                        note = note,
                        date = parsed.dateMillis
                    )
                    entryPoint.transactionRepository().saveTransaction(tx)

                    // Update account balance
                    matchingAccount?.let { acc ->
                        val balanceDelta = if (parsed.type == TransactionType.INCOME) parsed.amountMinor else -parsed.amountMinor
                        entryPoint.accountRepository().saveAccount(
                            acc.copy(currentBalanceMinor = acc.currentBalanceMinor + balanceDelta)
                        )
                    }

                    val formatted = MoneyFormatter.formatPaise(parsed.amountMinor, includeSymbol = true)
                    val actionLabel = if (parsed.type == TransactionType.INCOME) "Received" else "Spent"
                    val catLabel = matchingCategory?.name?.let { " ($it)" } ?: ""
                    NotificationHelper.showSmsTransactionNotification(
                        context = context,
                        title = "Auto-Logged: $formatted",
                        message = "$actionLabel at ${parsed.merchantOrPayee}$catLabel from ${parsed.bankName ?: "Bank"}",
                        route = "transactions"
                    )
                } else {
                    // Smart Review Inbox Mode (Default): Add to pending_transactions for 1-tap ledger approval
                    entryPoint.pendingTransactionRepository().parseAndAddMessage(fullBody, sender, parsed.dateMillis)
                    val formatted = MoneyFormatter.formatPaise(parsed.amountMinor, includeSymbol = true)
                    NotificationHelper.showSmsTransactionNotification(
                        context = context,
                        title = "New Transaction ($formatted)",
                        message = "${parsed.merchantOrPayee} • ${parsed.bankName ?: "Bank SMS"}. Tap to review and log.",
                        route = "inbox"
                    )
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error processing SMS", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
