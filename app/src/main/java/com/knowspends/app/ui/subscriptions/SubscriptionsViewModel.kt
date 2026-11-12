package com.knowspends.app.ui.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowspends.app.domain.model.RecurrenceFrequency
import com.knowspends.app.domain.model.RecurringRule
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.repository.AccountRepository
import com.knowspends.app.domain.repository.CategoryRepository
import com.knowspends.app.domain.repository.RecurringRuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val recurringRuleRepository: RecurringRuleRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow("All")
    private val _showAddEditDialog = MutableStateFlow(false)
    private val _editingRule = MutableStateFlow<RecurringRule?>(null)

    private data class DialogState(
        val filter: String,
        val showDialog: Boolean,
        val editRule: RecurringRule?
    )

    private val _dialogState = combine(
        _selectedFilter,
        _showAddEditDialog,
        _editingRule
    ) { filter, showDialog, editRule ->
        DialogState(filter, showDialog, editRule)
    }

    val uiState: StateFlow<SubscriptionsUiState> = combine(
        recurringRuleRepository.getAllRules(),
        accountRepository.getAllAccounts(),
        categoryRepository.getActiveCategories(),
        _dialogState
    ) { rules, accounts, categories, dialog ->
        SubscriptionsUiState(
            rules = rules,
            accounts = accounts,
            categories = categories,
            selectedFilter = dialog.filter,
            showAddEditDialog = dialog.showDialog,
            editingRule = dialog.editRule,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SubscriptionsUiState(isLoading = true)
    )

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
    }

    fun showAddEditDialog(rule: RecurringRule? = null) {
        _editingRule.value = rule
        _showAddEditDialog.value = true
    }

    fun dismissDialog() {
        _showAddEditDialog.value = false
        _editingRule.value = null
    }

    fun toggleSubscriptionActive(id: Long, isActive: Boolean) {
        viewModelScope.launch {
            recurringRuleRepository.setRuleActive(id, isActive)
        }
    }

    fun deleteSubscription(id: Long) {
        viewModelScope.launch {
            recurringRuleRepository.deleteRule(id)
        }
    }

    fun saveSubscription(
        id: Long = 0,
        payee: String,
        amountMinor: Long,
        accountId: Long,
        categoryId: Long?,
        frequency: RecurrenceFrequency,
        dayOfMonth: Int,
        autoPost: Boolean
    ) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 9)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            when (frequency) {
                RecurrenceFrequency.DAILY -> {
                    if (calendar.timeInMillis <= System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
                RecurrenceFrequency.WEEKLY -> {
                    calendar.add(Calendar.DAY_OF_YEAR, 7)
                }
                RecurrenceFrequency.MONTHLY -> {
                    val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceIn(1, maxDay))
                    if (calendar.timeInMillis <= System.currentTimeMillis()) {
                        calendar.add(Calendar.MONTH, 1)
                    }
                }
                RecurrenceFrequency.YEARLY -> {
                    val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceIn(1, maxDay))
                    if (calendar.timeInMillis <= System.currentTimeMillis()) {
                        calendar.add(Calendar.YEAR, 1)
                    }
                }
            }
            val nextDueDate = calendar.timeInMillis

            val rule = RecurringRule(
                id = id,
                accountId = accountId,
                categoryId = categoryId,
                amountMinor = amountMinor,
                type = TransactionType.EXPENSE,
                payee = payee.trim(),
                frequency = frequency,
                interval = 1,
                dayOfMonth = dayOfMonth,
                nextDueDate = nextDueDate,
                autoPost = autoPost,
                isActive = true
            )
            recurringRuleRepository.saveRule(rule)
            dismissDialog()
        }
    }
}
