package com.knowspends.app.ui.island

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowspends.app.domain.model.Account
import com.knowspends.app.domain.model.Category
import com.knowspends.app.domain.model.Transaction
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.repository.AccountRepository
import com.knowspends.app.domain.repository.CategoryRepository
import com.knowspends.app.domain.repository.TransactionRepository
import com.knowspends.app.domain.repository.UserPreferencesRepository
import com.knowspends.app.domain.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class IslandInternalState(
    val isExpanded: Boolean = false,
    val isPillVisible: Boolean = true,
    val amountInput: String = "0",
    val selectedAccountId: Long? = null,
    val selectedCategoryId: Long? = null,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class DynamicIslandViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _internal = MutableStateFlow(IslandInternalState())

    val uiState: StateFlow<DynamicIslandUiState> = combine(
        accountRepository.getActiveAccountsWithBalances(),
        categoryRepository.getAllCategories(),
        userPreferencesRepository.userPreferencesFlow,
        _internal
    ) { accounts, categories, prefs, state ->
        val effectiveAccountId = state.selectedAccountId ?: accounts.firstOrNull()?.id
        val effectiveCategoryId = state.selectedCategoryId ?: categories.firstOrNull()?.id

        DynamicIslandUiState(
            isExpanded = state.isExpanded,
            isPillVisible = state.isPillVisible,
            amountInput = state.amountInput,
            selectedAccountId = effectiveAccountId,
            selectedCategoryId = effectiveCategoryId,
            accounts = accounts,
            categories = categories,
            currencySymbol = prefs.currencySymbol,
            isSaving = state.isSaving,
            isSuccess = state.isSuccess,
            errorMessage = state.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DynamicIslandUiState()
    )

    fun expand() {
        _internal.update {
            it.copy(
                isExpanded = true,
                amountInput = "0",
                isSuccess = false,
                errorMessage = null
            )
        }
    }

    fun collapse() {
        _internal.update {
            it.copy(
                isExpanded = false,
                isSuccess = false,
                errorMessage = null
            )
        }
    }

    fun toggle() {
        if (_internal.value.isExpanded) collapse() else expand()
    }

    fun onDigit(char: Char) {
        _internal.update { current ->
            val curr = current.amountInput
            val next = when {
                char == '.' && curr.contains('.') -> curr
                char == '.' && curr == "0" -> "0."
                curr == "0" && char != '.' -> char.toString()
                curr.length >= 8 -> curr
                else -> curr + char
            }
            current.copy(amountInput = next, errorMessage = null)
        }
    }

    fun onBackspace() {
        _internal.update { current ->
            val curr = current.amountInput
            val next = if (curr.length <= 1) "0" else curr.dropLast(1)
            current.copy(amountInput = next, errorMessage = null)
        }
    }

    fun onClear() {
        _internal.update { it.copy(amountInput = "0", errorMessage = null) }
    }

    fun onCategorySelected(categoryId: Long) {
        _internal.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun onAccountSelected(accountId: Long) {
        _internal.update { it.copy(selectedAccountId = accountId) }
    }

    fun saveExpense() {
        val state = uiState.value
        val amountPaise = state.amountMinor

        if (amountPaise <= 0L) {
            _internal.update { it.copy(errorMessage = "Enter an amount") }
            return
        }

        val accountId = state.selectedAccountId
        if (accountId == null) {
            _internal.update { it.copy(errorMessage = "Select an account") }
            return
        }

        val categoryId = state.selectedCategoryId

        viewModelScope.launch {
            _internal.update { it.copy(isSaving = true) }

            try {
                // 1. Save Transaction to Room
                val now = System.currentTimeMillis()
                val tx = Transaction(
                    amountMinor = amountPaise,
                    type = TransactionType.EXPENSE,
                    accountId = accountId,
                    categoryId = categoryId,
                    date = now,
                    note = "Quick Tap Spend",
                    payee = "Quick Add"
                )
                transactionRepository.insertTransaction(tx)

                // 2. Deduct from selected account balance
                val acc = state.selectedAccount
                if (acc != null) {
                    accountRepository.saveAccount(
                        acc.copy(currentBalanceMinor = acc.currentBalanceMinor - amountPaise)
                    )
                }

                // 3. Celebratory checkmark animation
                _internal.update { it.copy(isSaving = false, isSuccess = true) }

                // 4. Spring collapse back to notch
                delay(700)
                collapse()
            } catch (e: Exception) {
                _internal.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.localizedMessage ?: "Failed to save"
                    )
                }
            }
        }
    }
}
