package com.knowspends.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowspends.app.domain.model.Account
import com.knowspends.app.domain.model.AccountType
import com.knowspends.app.domain.model.MonthlySummary
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.model.TransactionWithDetails
import com.knowspends.app.domain.repository.AccountRepository
import com.knowspends.app.domain.repository.PendingTransactionRepository
import com.knowspends.app.domain.repository.TransactionRepository
import com.knowspends.app.domain.repository.UserPreferencesRepository
import com.knowspends.app.domain.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class DayExpenseItem(
    val dayLabel: String,
    val amountMinor: Long,
    val formattedAmount: String,
    val isToday: Boolean
)

data class CategoryExpenseShare(
    val categoryName: String,
    val colorHex: Long?,
    val amountMinor: Long,
    val percentage: Float,
    val formattedAmount: String
)

data class HomeUiState(
    val currentMonthLabel: String = "",
    val monthlySummary: MonthlySummary = MonthlySummary(),
    val accounts: List<Account> = emptyList(),
    val totalNetWorthMinor: Long = 0L,
    val recentTransactions: List<TransactionWithDetails> = emptyList(),
    val dailyExpenses7Days: List<DayExpenseItem> = emptyList(),
    val categoryExpenses: List<CategoryExpenseShare> = emptyList(),
    val selectedAnalyticsTab: Int = 0,
    val selectedAccountForAction: Account? = null,
    val activeEditField: AccountEditField? = null,
    val pendingSmsCount: Int = 0,
    val currencySymbol: String = "₹",
    val isLoading: Boolean = false
) {
    val formattedTotalNetWorth: String
        get() = MoneyFormatter.formatPaise(totalNetWorthMinor, currencySymbol = currencySymbol)

    val formattedExpense: String
        get() = MoneyFormatter.formatPaise(monthlySummary.totalExpenseMinor, currencySymbol = currencySymbol)

    val formattedIncome: String
        get() = MoneyFormatter.formatPaise(monthlySummary.totalIncomeMinor, currencySymbol = currencySymbol)

    val formattedNetSavings: String
        get() = MoneyFormatter.formatPaise(monthlySummary.netSavingsMinor, currencySymbol = currencySymbol)

    val savingsRatePercent: Int
        get() = if (monthlySummary.totalIncomeMinor > 0) {
            ((monthlySummary.netSavingsMinor.toFloat() / monthlySummary.totalIncomeMinor.toFloat()) * 100).toInt().coerceIn(-100, 100)
        } else 0

    val spendRatePercent: Float
        get() = if (monthlySummary.totalIncomeMinor > 0) {
            (monthlySummary.totalExpenseMinor.toFloat() / monthlySummary.totalIncomeMinor.toFloat()).coerceIn(0f, 1.5f)
        } else if (monthlySummary.totalExpenseMinor > 0) 1.0f else 0.0f

    val dailyAverageExpense: String
        get() {
            val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            val avg = monthlySummary.totalExpenseMinor / maxOf(1, day)
            return MoneyFormatter.formatPaise(avg, currencySymbol = currencySymbol)
        }

    val daysRemainingInMonth: Int
        get() {
            val cal = Calendar.getInstance()
            return maxOf(0, cal.getActualMaximum(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_MONTH))
        }

    val safeToSpendTodayMinor: Long
        get() {
            val daysLeft = maxOf(1, daysRemainingInMonth + 1)
            return if (monthlySummary.netSavingsMinor > 0) {
                monthlySummary.netSavingsMinor / daysLeft
            } else {
                0L
            }
        }

    val formattedSafeToSpendToday: String
        get() = MoneyFormatter.formatPaise(safeToSpendTodayMinor, includeSymbol = true, currencySymbol = currencySymbol)

    val isSafeToSpendPositive: Boolean
        get() = safeToSpendTodayMinor > 0
}

private data class HomeControlsState(
    val selectedTab: Int,
    val actionAccount: Account?,
    val editField: AccountEditField?,
    val currencySymbol: String
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val pendingTransactionRepository: PendingTransactionRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    private val monthStartMillis: Long
    private val monthEndMillis: Long
    val currentMonthLabel: String

    init {
        currentMonthLabel = monthFormat.format(calendar.time)

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        monthStartMillis = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        monthEndMillis = calendar.timeInMillis
    }

    private val _selectedAnalyticsTab = MutableStateFlow(0)
    private val _selectedAccountForAction = MutableStateFlow<Account?>(null)
    private val _activeEditField = MutableStateFlow<AccountEditField?>(null)

    private val _uiControlsFlow = combine(
        _selectedAnalyticsTab,
        _selectedAccountForAction,
        _activeEditField,
        userPreferencesRepository.userPreferencesFlow
    ) { tab, account, field, prefs ->
        HomeControlsState(tab, account, field, prefs.currencySymbol)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        transactionRepository.getMonthlySummary(monthStartMillis, monthEndMillis),
        accountRepository.getActiveAccountsWithBalances(),
        transactionRepository.getAllTransactionsWithDetails(),
        _uiControlsFlow,
        pendingTransactionRepository.getPendingCount()
    ) { summary, accounts, transactions, controls, pendingCount ->
        val netWorth = accounts.sumOf { it.currentBalanceMinor }
        val recents = transactions.take(6)

        // Calculate 7-Day Spending Trend (Today and previous 6 days)
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val dailyItems = mutableListOf<DayExpenseItem>()
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startDayMillis = dayCal.timeInMillis
            val endDayMillis = startDayMillis + 86400000L - 1L

            val dayExpensesMinor = transactions
                .filter { it.transaction.type == TransactionType.EXPENSE && it.transaction.date in startDayMillis..endDayMillis }
                .sumOf { it.transaction.amountMinor }

            val dayLabel = if (i == 0) "Today" else dayNames[dayCal.get(Calendar.DAY_OF_WEEK) - 1]
            dailyItems.add(
                DayExpenseItem(
                    dayLabel = dayLabel,
                    amountMinor = dayExpensesMinor,
                    formattedAmount = MoneyFormatter.formatPaise(dayExpensesMinor),
                    isToday = (i == 0)
                )
            )
        }

        // Calculate Category Breakdown for the current month
        val monthlyExpenses = transactions.filter {
            it.transaction.type == TransactionType.EXPENSE && it.transaction.date in monthStartMillis..monthEndMillis
        }
        val totalMonthlyExpenseMinor = monthlyExpenses.sumOf { it.transaction.amountMinor }

        val categoryShares = if (totalMonthlyExpenseMinor > 0) {
            monthlyExpenses
                .groupBy { it.categoryName ?: "General" }
                .map { (catName, txList) ->
                    val sumMinor = txList.sumOf { it.transaction.amountMinor }
                    val color = txList.firstOrNull()?.categoryColour
                    CategoryExpenseShare(
                        categoryName = catName,
                        colorHex = color,
                        amountMinor = sumMinor,
                        percentage = (sumMinor.toFloat() / totalMonthlyExpenseMinor.toFloat()) * 100f,
                        formattedAmount = MoneyFormatter.formatPaise(sumMinor)
                    )
                }
                .sortedByDescending { it.amountMinor }
                .take(5)
        } else emptyList()

        HomeUiState(
            currentMonthLabel = currentMonthLabel,
            monthlySummary = summary,
            accounts = accounts,
            totalNetWorthMinor = netWorth,
            recentTransactions = recents,
            dailyExpenses7Days = dailyItems,
            categoryExpenses = categoryShares,
            selectedAnalyticsTab = controls.selectedTab,
            selectedAccountForAction = controls.actionAccount,
            activeEditField = controls.editField,
            pendingSmsCount = pendingCount,
            currencySymbol = controls.currencySymbol,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true, currentMonthLabel = currentMonthLabel)
    )

    fun onAnalyticsTabSelected(tab: Int) {
        _selectedAnalyticsTab.value = tab
    }

    fun onSelectAccountForAction(account: Account?, field: AccountEditField? = AccountEditField.OPTIONS) {
        _selectedAccountForAction.value = account
        _activeEditField.value = field
    }

    fun onDismissAccountAction() {
        _selectedAccountForAction.value = null
        _activeEditField.value = null
    }

    fun onSelectAccountEditField(field: AccountEditField) {
        _activeEditField.value = field
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            accountRepository.saveAccount(account)
        }
    }

    fun updateAccountBalance(accountId: Long, newBalanceMinor: Long) {
        viewModelScope.launch {
            val acc = uiState.value.accounts.find { it.id == accountId } ?: return@launch
            val delta = acc.currentBalanceMinor - acc.openingBalanceMinor
            val adjustedOpeningBalance = newBalanceMinor - delta
            accountRepository.saveAccount(acc.copy(openingBalanceMinor = adjustedOpeningBalance))
        }
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

    fun updateAccountName(accountId: Long, newName: String) {
        viewModelScope.launch {
            val acc = uiState.value.accounts.find { it.id == accountId } ?: return@launch
            accountRepository.saveAccount(acc.copy(name = newName.trim()))
        }
    }

    fun updateAccountType(accountId: Long, newType: AccountType) {
        viewModelScope.launch {
            val acc = uiState.value.accounts.find { it.id == accountId } ?: return@launch
            accountRepository.saveAccount(acc.copy(type = newType))
        }
    }
}
