package com.knowspends.app.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowspends.app.domain.model.Category
import com.knowspends.app.domain.model.CategoryBudget
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.repository.BudgetRepository
import com.knowspends.app.domain.repository.CategoryRepository
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
import java.util.Locale
import javax.inject.Inject

data class BudgetsUiState(
    val currentPeriod: String = "",
    val periodLabel: String = "",
    val budgets: List<CategoryBudget> = emptyList(),
    val categories: List<Category> = emptyList(),
    val totalBudgetedMinor: Long = 0L,
    val totalSpentMinor: Long = 0L,
    val isSetBudgetDialogOpen: Boolean = false,
    val selectedBudgetForEdit: CategoryBudget? = null,
    val isLoading: Boolean = false
) {
    val totalRemainingMinor: Long
        get() = totalBudgetedMinor - totalSpentMinor

    val totalProgressRatio: Float
        get() = if (totalBudgetedMinor <= 0) 0f else (totalSpentMinor.toFloat() / totalBudgetedMinor.toFloat()).coerceAtLeast(0f)

    val formattedTotalBudgeted: String
        get() = MoneyFormatter.formatPaise(totalBudgetedMinor)

    val formattedTotalSpent: String
        get() = MoneyFormatter.formatPaise(totalSpentMinor)

    val formattedTotalRemaining: String
        get() = MoneyFormatter.formatPaise(totalRemainingMinor)
}

data class BudgetDialogState(
    val isOpen: Boolean = false,
    val selectedBudget: CategoryBudget? = null
)

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val periodFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    val currentPeriod: String
    val currentPeriodLabel: String
    private val periodStartMillis: Long
    private val periodEndMillis: Long

    private val _dialogState = MutableStateFlow(BudgetDialogState())

    init {
        currentPeriod = periodFormat.format(calendar.time)
        currentPeriodLabel = displayFormat.format(calendar.time)

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        periodStartMillis = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        periodEndMillis = calendar.timeInMillis
    }

    val uiState: StateFlow<BudgetsUiState> = combine(
        budgetRepository.getBudgetsForPeriod(currentPeriod, periodStartMillis, periodEndMillis),
        categoryRepository.getCategoriesByKind(TransactionType.EXPENSE),
        _dialogState
    ) { budgets, categories, dialogState ->
        val totalBudget = budgets.sumOf { it.budgetAmountMinor }
        val totalSpent = budgets.sumOf { it.spentAmountMinor }

        BudgetsUiState(
            currentPeriod = currentPeriod,
            periodLabel = currentPeriodLabel,
            budgets = budgets,
            categories = categories,
            totalBudgetedMinor = totalBudget,
            totalSpentMinor = totalSpent,
            isSetBudgetDialogOpen = dialogState.isOpen,
            selectedBudgetForEdit = dialogState.selectedBudget,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetsUiState(isLoading = true, periodLabel = currentPeriodLabel)
    )

    fun openSetBudgetDialog(budget: CategoryBudget? = null) {
        _dialogState.value = BudgetDialogState(isOpen = true, selectedBudget = budget)
    }

    fun closeSetBudgetDialog() {
        _dialogState.value = BudgetDialogState(isOpen = false, selectedBudget = null)
    }

    fun saveBudget(categoryId: Long, amountMinor: Long, rollover: Boolean) {
        viewModelScope.launch {
            budgetRepository.setBudget(
                categoryId = categoryId,
                period = currentPeriod,
                amountMinor = amountMinor,
                rollover = rollover
            )
            closeSetBudgetDialog()
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budgetId)
            closeSetBudgetDialog()
        }
    }
}
