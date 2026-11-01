package com.knowspends.app.ui.island

import com.knowspends.app.domain.model.Account
import com.knowspends.app.domain.model.Category
import com.knowspends.app.domain.util.MoneyFormatter

data class DynamicIslandUiState(
    val isExpanded: Boolean = false,
    val isPillVisible: Boolean = true,
    val amountInput: String = "0",
    val selectedAccountId: Long? = null,
    val selectedCategoryId: Long? = null,
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val currencySymbol: String = "₹",
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val amountMinor: Long
        get() = MoneyFormatter.parseToPaise(amountInput)

    val formattedAmount: String
        get() = MoneyFormatter.formatPaise(amountMinor, includeSymbol = true)

    val selectedAccount: Account?
        get() = accounts.find { it.id == selectedAccountId } ?: accounts.firstOrNull()

    val selectedCategory: Category?
        get() = categories.find { it.id == selectedCategoryId } ?: categories.firstOrNull()
}
