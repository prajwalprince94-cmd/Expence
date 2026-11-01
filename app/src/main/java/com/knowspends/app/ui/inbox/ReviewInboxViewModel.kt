package com.knowspends.app.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowspends.app.data.local.entity.PendingTransactionEntity
import com.knowspends.app.domain.model.Account
import com.knowspends.app.domain.model.Category
import com.knowspends.app.domain.repository.AccountRepository
import com.knowspends.app.domain.repository.CategoryRepository
import com.knowspends.app.domain.repository.PendingTransactionRepository
import com.knowspends.app.domain.util.BankMessageParser
import com.knowspends.app.sms.SmsScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewInboxUiState(
    val pendingTransactions: List<PendingTransactionEntity> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val manualInputText: String = "",
    val isScanningSms: Boolean = false,
    val statusMessage: String? = null
)

@HiltViewModel
class ReviewInboxViewModel @Inject constructor(
    private val pendingRepository: PendingTransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val smsScanner: SmsScanner
) : ViewModel() {

    private val _manualInputText = MutableStateFlow("")
    private val _isScanningSms = MutableStateFlow(false)
    private val _statusMessage = MutableStateFlow<String?>(null)

    private data class DomainData(
        val pending: List<PendingTransactionEntity>,
        val accounts: List<Account>,
        val categories: List<Category>
    )

    private val _domainDataFlow = combine(
        pendingRepository.getPendingTransactions(),
        accountRepository.getActiveAccounts(),
        categoryRepository.getActiveCategories()
    ) { pending, accounts, categories ->
        DomainData(pending, accounts, categories)
    }

    val uiState: StateFlow<ReviewInboxUiState> = combine(
        _domainDataFlow,
        _manualInputText,
        _isScanningSms,
        _statusMessage
    ) { domain, input, isScanning, msg ->
        ReviewInboxUiState(
            pendingTransactions = domain.pending,
            accounts = domain.accounts,
            categories = domain.categories,
            manualInputText = input,
            isScanningSms = isScanning,
            statusMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReviewInboxUiState()
    )

    fun onInputTextChanged(text: String) {
        _manualInputText.value = text
    }

    fun parseAndAddMessage() {
        val text = _manualInputText.value.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            val success = pendingRepository.parseAndAddMessage(text)
            if (success) {
                _manualInputText.value = ""
                _statusMessage.value = "Transaction parsed and added to Review Inbox!"
            } else {
                _statusMessage.value = "Could not parse financial message. Ignored or unrecognized format."
            }
        }
    }

    fun scanSmsInbox(daysBack: Int = 30) {
        if (_isScanningSms.value) return
        viewModelScope.launch {
            _isScanningSms.value = true
            val result = smsScanner.scanInbox(daysBack)
            _isScanningSms.value = false
            if (result.isSuccess) {
                _statusMessage.value = "Scanned ${result.totalScanned} messages: Found ${result.financialFound} transactions (${result.newlyAdded} new added to review)."
            } else {
                _statusMessage.value = result.errorMessage ?: "Failed to scan SMS inbox"
            }
        }
    }

    fun approveTransaction(pendingId: Long, accountId: Long, categoryId: Long?) {
        viewModelScope.launch {
            pendingRepository.approveTransaction(pendingId, accountId, categoryId)
            _statusMessage.value = "Transaction approved and logged to ledger!"
        }
    }

    fun approveAll() {
        val pendingList = uiState.value.pendingTransactions
        if (pendingList.isEmpty()) return

        val accounts = uiState.value.accounts
        val categories = uiState.value.categories
        val defaultAccountId = accounts.firstOrNull()?.id ?: 1L

        viewModelScope.launch {
            var approvedCount = 0
            for (p in pendingList) {
                val parsed = BankMessageParser.parse(p.rawMessage)
                val matchingAccount = accounts.find { acc ->
                    parsed?.accountTail != null && acc.name.contains(parsed.accountTail)
                } ?: accounts.find { acc ->
                    parsed?.bankName != null && acc.name.contains(parsed.bankName, ignoreCase = true)
                } ?: accounts.firstOrNull { it.type.name == "BANK" || it.type.name == "WALLET" }
                val targetAccountId = matchingAccount?.id ?: defaultAccountId

                val matchingCategory = categories.find { cat ->
                    parsed?.suggestedCategoryName != null && cat.name.equals(parsed.suggestedCategoryName, ignoreCase = true)
                }
                val targetCategoryId = matchingCategory?.id

                pendingRepository.approveTransaction(p.id, targetAccountId, targetCategoryId)
                approvedCount++
            }
            _statusMessage.value = "Approved and logged $approvedCount transactions to ledger!"
        }
    }

    fun dismissTransaction(pendingId: Long) {
        viewModelScope.launch {
            pendingRepository.dismissTransaction(pendingId)
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
