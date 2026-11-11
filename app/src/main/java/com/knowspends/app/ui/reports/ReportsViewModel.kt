package com.knowspends.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowspends.app.domain.model.CategoryReportItem
import com.knowspends.app.domain.model.DateRangeType
import com.knowspends.app.domain.model.InsightType
import com.knowspends.app.domain.model.MonthlyTrendItem
import com.knowspends.app.domain.model.NarrativeInsight
import com.knowspends.app.domain.model.PayeeReportItem
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.model.TransactionWithDetails
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
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class ReportTab(val title: String) {
    INSIGHTS("Insights"),
    CATEGORIES("Categories"),
    MONTHLY_TRENDS("Trends"),
    TOP_PAYEES("Payees")
}

data class ReportsUiState(
    val selectedRange: DateRangeType = DateRangeType.THIS_MONTH,
    val selectedTab: ReportTab = ReportTab.INSIGHTS,
    val totalExpenseMinor: Long = 0L,
    val totalIncomeMinor: Long = 0L,
    val categories: List<CategoryReportItem> = emptyList(),
    val topPayees: List<PayeeReportItem> = emptyList(),
    val monthlyTrends: List<MonthlyTrendItem> = emptyList(),
    val narrativeInsights: List<NarrativeInsight> = emptyList(),
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
    private val _selectedTab = MutableStateFlow(ReportTab.INSIGHTS)

    val uiState: StateFlow<ReportsUiState> = combine(_selectedRange, _selectedTab) { range, tab ->
        range to tab
    }.flatMapLatest { (range, tab) ->
        val (startMillis, endMillis) = computeDateBounds(range)

        combine(
            transactionRepository.getMonthlySummary(startMillis, endMillis),
            transactionRepository.getCategoryBreakdown(startMillis, endMillis, TransactionType.EXPENSE),
            transactionRepository.getTopPayees(startMillis, endMillis, limit = 10),
            transactionRepository.getMonthlyTrends(startMillis, endMillis),
            transactionRepository.getTransactionsBetween(startMillis, endMillis)
        ) { summary, categories, payees, trends, transactions ->
            val insights = generateNarrativeInsights(
                transactions = transactions,
                totalExpenseMinor = summary.totalExpenseMinor,
                totalIncomeMinor = summary.totalIncomeMinor,
                categories = categories,
                topPayees = payees,
                range = range,
                startMillis = startMillis,
                endMillis = endMillis
            )

            ReportsUiState(
                selectedRange = range,
                selectedTab = tab,
                totalExpenseMinor = summary.totalExpenseMinor,
                totalIncomeMinor = summary.totalIncomeMinor,
                categories = categories,
                topPayees = payees,
                monthlyTrends = trends,
                narrativeInsights = insights,
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

    private fun generateNarrativeInsights(
        transactions: List<TransactionWithDetails>,
        totalExpenseMinor: Long,
        totalIncomeMinor: Long,
        categories: List<CategoryReportItem>,
        topPayees: List<PayeeReportItem>,
        range: DateRangeType,
        startMillis: Long,
        endMillis: Long
    ): List<NarrativeInsight> {
        val list = mutableListOf<NarrativeInsight>()
        val expenses = transactions.filter { it.transaction.type == TransactionType.EXPENSE }

        if (expenses.isEmpty()) {
            list.add(
                NarrativeInsight(
                    id = "no_data",
                    title = "No Expense Data",
                    narrative = "No expenses recorded for this time range. Add or import transactions to unlock personalized narrative insights.",
                    iconKey = "auto_awesome",
                    type = InsightType.NEUTRAL
                )
            )
            return list
        }

        // 1. Savings Rate / Cashflow Story
        val netSavings = totalIncomeMinor - totalExpenseMinor
        if (totalIncomeMinor > 0L) {
            val savingsRate = ((netSavings.toFloat() / totalIncomeMinor.toFloat()) * 100f)
            if (savingsRate >= 25f) {
                list.add(
                    NarrativeInsight(
                        id = "savings_star",
                        title = "Super Savings Pace",
                        narrative = "Outstanding financial discipline! You retained ${String.format(Locale.getDefault(), "%.1f", savingsRate)}% of your earnings (${MoneyFormatter.formatPaise(netSavings, includeSymbol = true)}) during this period.",
                        iconKey = "savings",
                        type = InsightType.POSITIVE,
                        metricHighlight = "+${String.format(Locale.getDefault(), "%.1f", savingsRate)}%"
                    )
                )
            } else if (savingsRate >= 0f) {
                list.add(
                    NarrativeInsight(
                        id = "savings_positive",
                        title = "Positive Cashflow",
                        narrative = "You maintained a positive balance with ${String.format(Locale.getDefault(), "%.1f", savingsRate)}% net savings (${MoneyFormatter.formatPaise(netSavings, includeSymbol = true)}).",
                        iconKey = "savings",
                        type = InsightType.POSITIVE,
                        metricHighlight = "+${String.format(Locale.getDefault(), "%.1f", savingsRate)}%"
                    )
                )
            } else {
                list.add(
                    NarrativeInsight(
                        id = "savings_deficit",
                        title = "Cashflow Deficit",
                        narrative = "Outflows exceeded total earnings by ${MoneyFormatter.formatPaise(-netSavings, includeSymbol = true)}. Consider reviewing top recurring expenses or high-spend categories.",
                        iconKey = "warning",
                        type = InsightType.CAUTION,
                        metricHighlight = "-${MoneyFormatter.formatPaise(-netSavings, includeSymbol = true)}"
                    )
                )
            }
        }

        // 2. Largest Single Outflow
        val largestTx = expenses.maxByOrNull { it.transaction.amountMinor }
        if (largestTx != null && largestTx.transaction.amountMinor > 0L) {
            val dateFormat = java.text.SimpleDateFormat("MMM dd", Locale.getDefault())
            val dateStr = dateFormat.format(Date(largestTx.transaction.date))
            val payeeStr = largestTx.transaction.payee.ifBlank { largestTx.categoryName ?: "Merchant" }
            list.add(
                NarrativeInsight(
                    id = "largest_tx",
                    title = "Largest Single Purchase",
                    narrative = "Your biggest outflow was ${MoneyFormatter.formatPaise(largestTx.transaction.amountMinor, includeSymbol = true)} at \"$payeeStr\" on $dateStr.",
                    iconKey = "star",
                    type = InsightType.HIGHLIGHT,
                    metricHighlight = MoneyFormatter.formatPaise(largestTx.transaction.amountMinor, includeSymbol = true)
                )
            )
        }

        // 3. Peak Spending Day of the Week
        val dayNames = arrayOf("", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val cal = Calendar.getInstance()
        val spendByDay = IntArray(8)
        val spendSumByDay = LongArray(8)
        expenses.forEach { item ->
            cal.timeInMillis = item.transaction.date
            val day = cal.get(Calendar.DAY_OF_WEEK)
            spendByDay[day]++
            spendSumByDay[day] += item.transaction.amountMinor
        }
        var peakDay = 1
        var peakSpend = 0L
        for (d in 1..7) {
            if (spendSumByDay[d] > peakSpend) {
                peakSpend = spendSumByDay[d]
                peakDay = d
            }
        }
        if (peakSpend > 0L && totalExpenseMinor > 0L) {
            val dayPercent = ((peakSpend.toFloat() / totalExpenseMinor.toFloat()) * 100f)
            val dayName = dayNames[peakDay]
            list.add(
                NarrativeInsight(
                    id = "peak_day",
                    title = "Peak Spending Day",
                    narrative = "${dayName}s are your heaviest spending days, accounting for ${String.format(Locale.getDefault(), "%.1f", dayPercent)}% (${MoneyFormatter.formatPaise(peakSpend, includeSymbol = true)}) of your total outflows.",
                    iconKey = "calendar",
                    type = InsightType.NEUTRAL,
                    metricHighlight = "${dayName}s"
                )
            )
        }

        // 4. Weekend vs. Weekday Split
        val weekendSpend = spendSumByDay[Calendar.SATURDAY] + spendSumByDay[Calendar.SUNDAY]
        val weekdaySpend = totalExpenseMinor - weekendSpend
        if (totalExpenseMinor > 0L) {
            val weekendPct = ((weekendSpend.toFloat() / totalExpenseMinor.toFloat()) * 100f)
            if (weekendPct >= 50f) {
                list.add(
                    NarrativeInsight(
                        id = "weekend_habit",
                        title = "Weekend Spender",
                        narrative = "You spend ${String.format(Locale.getDefault(), "%.1f", weekendPct)}% of your budget on weekends (${MoneyFormatter.formatPaise(weekendSpend, includeSymbol = true)}) compared to ${MoneyFormatter.formatPaise(weekdaySpend, includeSymbol = true)} on weekdays.",
                        iconKey = "trending_up",
                        type = InsightType.NEUTRAL,
                        metricHighlight = "${String.format(Locale.getDefault(), "%.0f", weekendPct)}% Weekend"
                    )
                )
            } else {
                list.add(
                    NarrativeInsight(
                        id = "weekday_habit",
                        title = "Weekday Outflows",
                        narrative = "Weekdays take up ${String.format(Locale.getDefault(), "%.1f", 100f - weekendPct)}% of your expenses (${MoneyFormatter.formatPaise(weekdaySpend, includeSymbol = true)}), keeping weekend outflows modest.",
                        iconKey = "trending_up",
                        type = InsightType.NEUTRAL,
                        metricHighlight = "${String.format(Locale.getDefault(), "%.0f", 100f - weekendPct)}% Weekdays"
                    )
                )
            }
        }

        // 5. Category Dominance
        val topCategory = categories.firstOrNull()
        if (topCategory != null && topCategory.percentage >= 25f) {
            list.add(
                NarrativeInsight(
                    id = "category_dominance",
                    title = "Category Anchor",
                    narrative = "\"${topCategory.categoryName}\" consumed ${String.format(Locale.getDefault(), "%.1f", topCategory.percentage)}% of your spending (${MoneyFormatter.formatPaise(topCategory.totalAmountMinor, includeSymbol = true)}) across ${topCategory.transactionCount} transactions.",
                    iconKey = "pie",
                    type = if (topCategory.percentage >= 45f) InsightType.CAUTION else InsightType.HIGHLIGHT,
                    metricHighlight = "${String.format(Locale.getDefault(), "%.1f", topCategory.percentage)}%"
                )
            )
        }

        // 6. Most Frequent Merchant
        val topMerchant = topPayees.firstOrNull { it.payeeName.isNotBlank() && it.payeeName != "Unspecified Payee" }
        if (topMerchant != null && topMerchant.transactionCount >= 2) {
            list.add(
                NarrativeInsight(
                    id = "merchant_frequency",
                    title = "Most Frequented Payee",
                    narrative = "You transacted with \"${topMerchant.payeeName}\" ${topMerchant.transactionCount} times for a total of ${MoneyFormatter.formatPaise(topMerchant.totalAmountMinor, includeSymbol = true)}.",
                    iconKey = "store",
                    type = InsightType.NEUTRAL,
                    metricHighlight = "${topMerchant.transactionCount} visits"
                )
            )
        }

        // 7. Average Daily Outflow Pace
        val calendarNow = Calendar.getInstance()
        val daysCount = when (range) {
            DateRangeType.THIS_MONTH -> calendarNow.get(Calendar.DAY_OF_MONTH).toLong().coerceAtLeast(1L)
            DateRangeType.LAST_MONTH -> 30L
            DateRangeType.LAST_3_MONTHS -> 90L
            DateRangeType.THIS_YEAR -> calendarNow.get(Calendar.DAY_OF_YEAR).toLong().coerceAtLeast(1L)
            DateRangeType.ALL_TIME -> ((endMillis - startMillis) / (24L * 60L * 60L * 1000L)).coerceAtLeast(1L)
        }
        val avgPerDay = totalExpenseMinor / daysCount
        list.add(
            NarrativeInsight(
                id = "daily_pace",
                title = "Average Daily Pace",
                narrative = "Over this period, your average daily spending pace was ${MoneyFormatter.formatPaise(avgPerDay, includeSymbol = true)} per day.",
                iconKey = "auto_awesome",
                type = InsightType.NEUTRAL,
                metricHighlight = "${MoneyFormatter.formatPaise(avgPerDay, includeSymbol = true)} / day"
            )
        )

        return list
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
