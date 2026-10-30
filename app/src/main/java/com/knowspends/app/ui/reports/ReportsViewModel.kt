package com.knowspends.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowspends.app.domain.model.CategoryReportItem
import com.knowspends.app.domain.model.DateRangeType
import com.knowspends.app.domain.model.MonthlyTrendItem
import com.knowspends.app.domain.model.PayeeReportItem
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.repository.TransactionRepository
import com.knowspends.app.domain.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

enum class ReportTab(val title: String) {
    CATEGORIES("Categories"),
    MONTHLY_TRENDS("Trends"),
    TOP_PAYEES("Payees")
}

data class ReportsUiState(
    val selectedRange: DateRangeType = DateRangeType.THIS_MONTH,
    val selectedTab: ReportTab = ReportTab.CATEGORIES,
    val totalExpenseMinor: Long = 0L,
    val totalIncomeMinor: Long = 0L,
    val categories: List<CategoryReportItem> = emptyList(),
    val topPayees: List<PayeeReportItem> = emptyList(),
    val monthlyTrends: List<MonthlyTrendItem> = emptyList(),
    val isLoading: Boolean = false
) {
    val netSavingsMinor: Long
        get() = totalIncomeMinor - totalExpenseMinor

    val savingsRatePercent: Float
        get() = if (totalIncomeMinor > 0L) {
            ((netSavingsMinor.toFloat() / totalIncomeMinor.toFloat()) * 100f).coerceAtLeast(0f)
        } else 0f

    val formattedExpense: String
        get() = MoneyFormatter.formatPaise(totalExpenseMinor)

    val formattedIncome: String
        get() = MoneyFormatter.formatPaise(totalIncomeMinor)

    val formattedNetSavings: String
        get() = MoneyFormatter.formatPaise(netSavingsMinor)
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(DateRangeType.THIS_MONTH)
    private val _selectedTab = MutableStateFlow(ReportTab.CATEGORIES)

    val uiState: StateFlow<ReportsUiState> = combine(_selectedRange, _selectedTab) { range, tab ->
        range to tab
    }.flatMapLatest { (range, tab) ->
        val (startMillis, endMillis) = computeDateBounds(range)

        combine(
            transactionRepository.getMonthlySummary(startMillis, endMillis),
            transactionRepository.getCategoryBreakdown(startMillis, endMillis, TransactionType.EXPENSE),
            transactionRepository.getTopPayees(startMillis, endMillis, limit = 10),
            transactionRepository.getMonthlyTrends(startMillis, endMillis)
        ) { summary, categories, payees, trends ->
            ReportsUiState(
                selectedRange = range,
                selectedTab = tab,
                totalExpenseMinor = summary.totalExpenseMinor,
                totalIncomeMinor = summary.totalIncomeMinor,
                categories = categories,
                topPayees = payees,
                monthlyTrends = trends,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState(isLoading = true)
    )

    fun onRangeSelected(range: DateRangeType) {
        _selectedRange.value = range
    }

    fun onTabSelected(tab: ReportTab) {
        _selectedTab.value = tab
    }

    private fun computeDateBounds(range: DateRangeType): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        return when (range) {
            DateRangeType.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                start to end
            }
            DateRangeType.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                start to end
            }
            DateRangeType.LAST_3_MONTHS -> {
                calendar.add(Calendar.MONTH, -2)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startOf(calendar) to now
            }
            DateRangeType.THIS_YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis to now
            }
            DateRangeType.ALL_TIME -> {
                0L to now
            }
        }
    }

    private fun startOf(calendar: Calendar): Long {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
