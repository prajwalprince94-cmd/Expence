package com.knowspends.app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.model.TransactionWithDetails
import com.knowspends.app.domain.repository.AccountRepository
import com.knowspends.app.domain.repository.TransactionRepository
import com.knowspends.app.domain.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DayGroup(
    val dateLabel: String,
    val dayTotalMinor: Long,
    val formattedDayTotal: String,
    val transactions: List<TransactionWithDetails>
)

data class TransactionsUiState(
    val searchQuery: String = "",
    val selectedType: TransactionType? = null,
    val selectedAccountId: Long? = null,
    val dayGroups: List<DayGroup> = emptyList(),
    val totalTransactionsCount: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedType = MutableStateFlow<TransactionType?>(null)
    private val _selectedAccountId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<TransactionsUiState> = combine(
        transactionRepository.getAllTransactionsWithDetails(),
        _searchQuery,
        _selectedType,
        _selectedAccountId
    ) { allTransactions, query, typeFilter, accountFilter ->
        val filtered = allTransactions.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.transaction.note.contains(query, ignoreCase = true) ||
                    item.transaction.payee.contains(query, ignoreCase = true) ||
                    (item.categoryName?.contains(query, ignoreCase = true) == true) ||
                    item.accountName.contains(query, ignoreCase = true)

            val matchesType = typeFilter == null || item.transaction.type == typeFilter
            val matchesAccount = accountFilter == null ||
                    item.transaction.accountId == accountFilter ||
                    item.transaction.transferAccountId == accountFilter

            matchesQuery && matchesType && matchesAccount
        }

        val groups = groupTransactionsByDay(filtered)

        TransactionsUiState(
            searchQuery = query,
            selectedType = typeFilter,
            selectedAccountId = accountFilter,
            dayGroups = groups,
            totalTransactionsCount = filtered.size,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionsUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onTypeFilterChanged(type: TransactionType?) {
        _selectedType.value = type
    }

    fun onAccountFilterChanged(accountId: Long?) {
        _selectedAccountId.value = accountId
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            transactionRepository.softDeleteTransaction(id)
        }
    }

    fun undoDelete(id: Long) {
        viewModelScope.launch {
            transactionRepository.restoreTransaction(id)
        }
    }

    private fun groupTransactionsByDay(list: List<TransactionWithDetails>): List<DayGroup> {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }

        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())

        val grouped = list.groupBy {
            dayFormat.format(Date(it.transaction.date))
        }

        return grouped.map { (_, dayItems) ->
            val firstDate = Date(dayItems.first().transaction.date)
            calendar.time = firstDate

            val label = when {
                isSameDay(calendar, today) -> "Today"
                isSameDay(calendar, yesterday) -> "Yesterday"
                else -> displayFormat.format(firstDate)
            }

            // Sum only expenses for daily total display
            val dayExpenses = dayItems.filter { it.transaction.type == TransactionType.EXPENSE }
                .sumOf { it.transaction.amountMinor }

            DayGroup(
                dateLabel = label,
                dayTotalMinor = dayExpenses,
                formattedDayTotal = MoneyFormatter.formatPaise(dayExpenses, includeSymbol = true),
                transactions = dayItems
            )
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
