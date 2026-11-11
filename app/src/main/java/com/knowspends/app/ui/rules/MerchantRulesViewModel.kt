package com.knowspends.app.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowspends.app.domain.model.Category
import com.knowspends.app.domain.model.MerchantRule
import com.knowspends.app.domain.repository.CategoryRepository
import com.knowspends.app.domain.repository.MerchantRuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MerchantRulesUiState(
    val rules: List<MerchantRule> = emptyList(),
    val filteredRules: List<MerchantRule> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val isAddDialogOpen: Boolean = false,
    val statusMessage: String? = null
)

@HiltViewModel
class MerchantRulesViewModel @Inject constructor(
    private val merchantRuleRepository: MerchantRuleRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isAddDialogOpen = MutableStateFlow(false)
    private val _statusMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MerchantRulesUiState> = combine(
        merchantRuleRepository.getRules(),
        categoryRepository.getActiveCategories(),
        _searchQuery,
        _isAddDialogOpen,
        _statusMessage
    ) { rules, categories, query, isAddOpen, statusMsg ->
        val filtered = if (query.isBlank()) {
            rules
        } else {
            rules.filter {
                it.merchantPattern.contains(query, ignoreCase = true) ||
                it.categoryName.contains(query, ignoreCase = true)
            }
        }
        MerchantRulesUiState(
            rules = rules,
            filteredRules = filtered,
            categories = categories,
            searchQuery = query,
            isAddDialogOpen = isAddOpen,
            statusMessage = statusMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MerchantRulesUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun openAddDialog() {
        _isAddDialogOpen.value = true
    }

    fun closeAddDialog() {
        _isAddDialogOpen.value = false
    }

    fun addRule(pattern: String, categoryId: Long, categoryName: String, isExactMatch: Boolean = false) {
        if (pattern.isBlank()) {
            _statusMessage.value = "Merchant pattern cannot be blank"
            return
        }
        viewModelScope.launch {
            merchantRuleRepository.saveRule(
                pattern = pattern.trim(),
                categoryId = categoryId,
                categoryName = categoryName,
                isExactMatch = isExactMatch
            )
            _isAddDialogOpen.value = false
            _statusMessage.value = "Rule saved for \"$pattern\""
        }
    }

    fun deleteRule(id: String, pattern: String) {
        viewModelScope.launch {
            merchantRuleRepository.deleteRule(id)
            _statusMessage.value = "Removed rule for \"$pattern\""
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
