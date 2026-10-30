package com.knowspends.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowspends.app.domain.model.Account
import com.knowspends.app.domain.model.MonthlySummary
import com.knowspends.app.domain.model.TransactionWithDetails
import com.knowspends.app.domain.repository.AccountRepository
import com.knowspends.app.domain.repository.TransactionRepository
import com.knowspends.app.domain.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class HomeUiState(
    val currentMonthLabel: String = "",
    val monthlySummary: MonthlySummary = MonthlySummary(),
    val accounts: List<Account> = emptyList(),
    val totalNetWorthMinor: Long = 0L,
    val recentTransactions: List<TransactionWithDetails> = emptyList(),
    val isLoading: Boolean = false
) {
    val formattedTotalNetWorth: String
        get() = MoneyFormatter.formatPaise(totalNetWorthMinor)

    val formattedExpense: String
        get() = MoneyFormatter.formatPaise(monthlySummary.totalExpenseMinor)

    val formattedIncome: String
        get() = MoneyFormatter.formatPaise(monthlySummary.totalIncomeMinor)

    val formattedNetSavings: String
        get() = MoneyFormatter.formatPaise(monthlySummary.netSavingsMinor)

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
            return MoneyFormatter.formatPaise(avg)
        }

    val daysRemainingInMonth: Int
        get() {
            val cal = Calendar.getInstance()
            return maxOf(0, cal.getActualMaximum(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_MONTH))
        }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
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

    val uiState: StateFlow<HomeUiState> = combine(
        transactionRepository.getMonthlySummary(monthStartMillis, monthEndMillis),
        accountRepository.getActiveAccountsWithBalances(),
        transactionRepository.getAllTransactionsWithDetails()
    ) { summary, accounts, transactions ->
        val netWorth = accounts.sumOf { it.currentBalanceMinor }
        val recents = transactions.take(5)

        HomeUiState(
            currentMonthLabel = currentMonthLabel,
            monthlySummary = summary,
            accounts = accounts,
            totalNetWorthMinor = netWorth,
            recentTransactions = recents,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true, currentMonthLabel = currentMonthLabel)
    )
}
