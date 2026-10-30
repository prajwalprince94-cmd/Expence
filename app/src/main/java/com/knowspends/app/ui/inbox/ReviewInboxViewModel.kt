package com.knowspends.app.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowspends.app.data.local.entity.PendingTransactionEntity
import com.knowspends.app.domain.model.Account
import com.knowspends.app.domain.model.Category
import com.knowspends.app.domain.repository.AccountRepository
import com.knowspends.app.domain.repository.CategoryRepository
import com.knowspends.app.domain.repository.PendingTransactionRepository
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
    val statusMessage: String? = null
)

@HiltViewModel
class ReviewInboxViewModel @Inject constructor(
    private val pendingRepository: PendingTransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _manualInputText = MutableStateFlow("")
    private val _statusMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ReviewInboxUiState> = combine(
        pendingRepository.getPendingTransactions(),
        accountRepository.getActiveAccounts(),
        categoryRepository.getActiveCategories(),
        _manualInputText,
        _statusMessage
    ) { pending, accounts, categories, input, msg ->
        ReviewInboxUiState(
            pendingTransactions = pending,
            accounts = accounts,
            categories = categories,
            manualInputText = input,
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

    fun approveTransaction(pendingId: Long, accountId: Long, categoryId: Long?) {
        viewModelScope.launch {
            pendingRepository.approveTransaction(pendingId, accountId, categoryId)
            _statusMessage.value = "Transaction approved and logged to ledger!"
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
