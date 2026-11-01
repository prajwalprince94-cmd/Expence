package com.knowspends.app.ui.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowspends.app.domain.model.Account
import com.knowspends.app.domain.model.Category
import com.knowspends.app.domain.model.Transaction
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.repository.AccountRepository
import com.knowspends.app.domain.repository.CategoryRepository
import com.knowspends.app.domain.repository.TransactionRepository
import com.knowspends.app.domain.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTransactionUiState(
    val transactionId: Long? = null,
    val amountInput: String = "0", // Raw numeric keypad input
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedAccountId: Long? = null,
    val selectedTransferAccountId: Long? = null,
    val selectedCategoryId: Long? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    val payee: String = "",
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false,
    val navigateUp: Boolean = false,
    val errorMessage: String? = null
) {
    val amountMinor: Long
        get() = MoneyFormatter.parseToPaise(amountInput)

    val formattedAmount: String
        get() = MoneyFormatter.formatPaise(amountMinor, includeSymbol = true)
}

sealed interface AddTransactionEvent {
    data class OnKeypadPress(val char: Char) : AddTransactionEvent
    object OnBackspace : AddTransactionEvent
    object OnClear : AddTransactionEvent
    data class OnTypeSelected(val type: TransactionType) : AddTransactionEvent
    data class OnAccountSelected(val accountId: Long) : AddTransactionEvent
    data class OnTransferAccountSelected(val accountId: Long) : AddTransactionEvent
    data class OnCategorySelected(val categoryId: Long) : AddTransactionEvent
    data class OnDateSelected(val dateMillis: Long) : AddTransactionEvent
    data class OnNoteChanged(val note: String) : AddTransactionEvent
    data class OnPayeeChanged(val payee: String) : AddTransactionEvent
    object OnSave : AddTransactionEvent
    object OnSaveAndAddAnother : AddTransactionEvent
}

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editTransactionId: Long? = savedStateHandle.get<Long>("transactionId")?.takeIf { it > 0 }
    private val initialType: TransactionType = savedStateHandle.get<String>("type")?.let {
        runCatching { TransactionType.valueOf(it) }.getOrNull()
    } ?: TransactionType.EXPENSE

    private val _uiState = MutableStateFlow(
        AddTransactionUiState(
            transactionId = editTransactionId,
            type = initialType
        )
    )
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun onNavigatedUp() {
        _uiState.update { it.copy(navigateUp = false) }
    }

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                accountRepository.getActiveAccounts(),
                categoryRepository.getActiveCategories()
            ) { accounts, categories ->
                accounts to categories
            }.collect { (accounts, categories) ->
                _uiState.update { current ->
                    val defaultAccount = current.selectedAccountId ?: accounts.firstOrNull()?.id
                    val filteredCategories = categories.filter { it.kind == current.type }
                    val defaultCategory = current.selectedCategoryId ?: filteredCategories.firstOrNull()?.id

                    current.copy(
                        accounts = accounts,
                        categories = filteredCategories,
                        selectedAccountId = defaultAccount,
                        selectedCategoryId = if (current.type == TransactionType.TRANSFER) null else defaultCategory
                    )
                }
            }
        }

        // Load existing transaction if editing
        if (editTransactionId != null) {
            viewModelScope.launch {
                val details = transactionRepository.getTransactionWithDetails(editTransactionId).firstOrNull()
                details?.let { detail ->
                    val t = detail.transaction
                    val rupeesPart = t.amountMinor / 100
                    val paisePart = t.amountMinor % 100
                    val amountStr = if (paisePart == 0L) rupeesPart.toString() else "$rupeesPart.${paisePart.toString().padStart(2, '0')}"

                    _uiState.update { current ->
                        current.copy(
                            amountInput = amountStr,
                            type = t.type,
                            selectedAccountId = t.accountId,
                            selectedTransferAccountId = t.transferAccountId,
                            selectedCategoryId = t.categoryId,
                            dateMillis = t.date,
                            note = t.note,
                            payee = t.payee
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: AddTransactionEvent) {
        when (event) {
            is AddTransactionEvent.OnKeypadPress -> handleKeypadInput(event.char)
            AddTransactionEvent.OnBackspace -> handleBackspace()
            AddTransactionEvent.OnClear -> _uiState.update { it.copy(amountInput = "0") }
            is AddTransactionEvent.OnTypeSelected -> handleTypeChange(event.type)
            is AddTransactionEvent.OnAccountSelected -> _uiState.update { it.copy(selectedAccountId = event.accountId) }
            is AddTransactionEvent.OnTransferAccountSelected -> _uiState.update { it.copy(selectedTransferAccountId = event.accountId) }
            is AddTransactionEvent.OnCategorySelected -> _uiState.update { it.copy(selectedCategoryId = event.categoryId) }
            is AddTransactionEvent.OnDateSelected -> _uiState.update { it.copy(dateMillis = event.dateMillis) }
            is AddTransactionEvent.OnNoteChanged -> _uiState.update { it.copy(note = event.note) }
            is AddTransactionEvent.OnPayeeChanged -> _uiState.update { it.copy(payee = event.payee) }
            AddTransactionEvent.OnSave -> saveTransaction(andAddAnother = false)
            AddTransactionEvent.OnSaveAndAddAnother -> saveTransaction(andAddAnother = true)
        }
    }

    private fun handleKeypadInput(char: Char) {
        _uiState.update { current ->
            val cur = current.amountInput
            val next = when {
                cur == "0" && char != '.' -> char.toString()
                char == '.' && cur.contains('.') -> cur // prevent multiple decimals
                cur.contains('.') && cur.substringAfter('.').length >= 2 -> cur // max 2 decimal places
                cur.length >= 9 -> cur // limit length
                else -> cur + char
            }
            current.copy(amountInput = next)
        }
    }

    private fun handleBackspace() {
        _uiState.update { current ->
            val cur = current.amountInput
            val next = when {
                cur.length <= 1 -> "0"
                else -> cur.dropLast(1)
            }
            current.copy(amountInput = next)
        }
    }

    private fun handleTypeChange(newType: TransactionType) {
        viewModelScope.launch {
            val allCategories = categoryRepository.getActiveCategories().firstOrNull() ?: emptyList()
            val filtered = allCategories.filter { it.kind == newType }

            _uiState.update { current ->
                current.copy(
                    type = newType,
                    categories = filtered,
                    selectedCategoryId = if (newType == TransactionType.TRANSFER) null else filtered.firstOrNull()?.id,
                    selectedTransferAccountId = if (newType == TransactionType.TRANSFER) {
                        current.accounts.firstOrNull { it.id != current.selectedAccountId }?.id
                    } else null
                )
            }
        }
    }

    private fun saveTransaction(andAddAnother: Boolean = false) {
        val state = _uiState.value
        val amount = state.amountMinor
        if (amount <= 0) {
            _uiState.update { it.copy(errorMessage = "Amount must be greater than zero") }
            return
        }

        val accountId = state.selectedAccountId
        if (accountId == null) {
            _uiState.update { it.copy(errorMessage = "Please select an account") }
            return
        }

        if (state.type == TransactionType.TRANSFER && state.selectedTransferAccountId == null) {
            _uiState.update { it.copy(errorMessage = "Please select destination account") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val transaction = Transaction(
                    id = state.transactionId ?: 0L,
                    accountId = accountId,
                    categoryId = if (state.type == TransactionType.TRANSFER) null else state.selectedCategoryId,
                    amountMinor = amount,
                    type = state.type,
                    transferAccountId = if (state.type == TransactionType.TRANSFER) state.selectedTransferAccountId else null,
                    date = state.dateMillis,
                    note = state.note.trim(),
                    payee = state.payee.trim()
                )
                transactionRepository.saveTransaction(transaction)
                if (andAddAnother) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            amountInput = "0",
                            note = "",
                            payee = "",
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update { it.copy(isSaving = false, navigateUp = true) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.localizedMessage ?: "Failed to save") }
            }
        }
    }
}
