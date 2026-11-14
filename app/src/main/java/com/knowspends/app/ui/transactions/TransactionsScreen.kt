package com.knowspends.app.ui.transactions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.knowspends.app.ui.components.states.ExpressiveEmptyState
import com.knowspends.app.ui.components.states.ExpressiveNoSearchResults
import com.knowspends.app.ui.components.states.ExpressiveTransactionListSkeleton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.model.TransactionWithDetails
import com.knowspends.app.domain.util.MoneyFormatter
import com.knowspends.app.ui.components.CategoryBadge
import com.knowspends.app.ui.components.VyxelExpressiveChip
import com.knowspends.app.ui.components.VyxelNavBarBottomClearance
import com.knowspends.app.ui.components.bouncyClickable
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.TransferColor
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.pressScale
import com.knowspends.app.ui.theme.progressiveScrollBlur
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onEditTransaction: (Long) -> Unit = {},
    onNavigateToInbox: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scheme = MaterialTheme.colorScheme
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedTransactionForDetail by remember { mutableStateOf<TransactionWithDetails?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 92.dp, start = 16.dp, end = 16.dp)
            )
        },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scheme.background)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Animated Morph between Clean Title Row and Integrated Search Bar
                AnimatedContent(
                    targetState = isSearchExpanded || uiState.searchQuery.isNotBlank(),
                    label = "TransactionsHeaderTransition"
                ) { isSearching ->
                    if (isSearching) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = {
                                    isSearchExpanded = false
                                    viewModel.onSearchQueryChanged("")
                                },
                                shape = CircleShape,
                                color = scheme.surfaceContainerHighest,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Exit Search",
                                        tint = scheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            VyxelSmartSearchBar(
                                query = uiState.searchQuery,
                                onQueryChange = viewModel::onSearchQueryChanged,
                                resultCount = if (uiState.searchQuery.isNotBlank()) uiState.totalTransactionsCount else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Transactions",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.3).sp
                                    ),
                                    color = scheme.onSurface
                                )

                                Surface(
                                    shape = VyxelShapeTokens.Pill,
                                    color = scheme.surfaceContainerHighest.copy(alpha = 0.8f)
                                ) {
                                    Text(
                                        text = "${uiState.totalTransactionsCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = scheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Review Inbox Pill Button (Direct Access when new SMS arrives)
                                if (uiState.unreviewedSmsCount > 0) {
                                    Surface(
                                        onClick = onNavigateToInbox,
                                        shape = VyxelShapeTokens.Pill,
                                        color = scheme.primaryContainer,
                                        border = BorderStroke(
                                            1.dp,
                                            scheme.primary.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier.bouncyClickable(onClick = onNavigateToInbox)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ReceiptLong,
                                                contentDescription = "Review Inbox",
                                                tint = scheme.onPrimaryContainer,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = "Inbox (${uiState.unreviewedSmsCount})",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = scheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }

                                // Search Toggle Button
                                Surface(
                                    onClick = { isSearchExpanded = true },
                                    shape = CircleShape,
                                    color = scheme.surfaceContainerHighest,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = scheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Balanced 3-Column Auto-Layout Cashflow Ribbon
                TransactionsCashflowPill(
                    incomeFormatted = uiState.formattedIncomeTotal,
                    expenseFormatted = uiState.formattedExpenseTotal,
                    netFormatted = uiState.formattedNetCashflow,
                    netMinor = uiState.netCashflowMinor
                )

                // Unified Single-Row Expressive Filter Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Pill Button when any filter is active
                    if (uiState.selectedType != null || uiState.selectedAccountId != null) {
                        Surface(
                            onClick = viewModel::clearAllFilters,
                            shape = VyxelShapeTokens.Pill,
                            color = scheme.errorContainer.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, scheme.error.copy(alpha = 0.4f)),
                            modifier = Modifier.bouncyClickable(onClick = viewModel::clearAllFilters)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Filters",
                                    tint = scheme.onErrorContainer,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Reset",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onErrorContainer
                                )
                            }
                        }
                    }

                    VyxelExpressiveChip(
                        selected = uiState.selectedType == null,
                        onClick = { viewModel.onTypeFilterChanged(null) },
                        label = "All"
                    )
                    VyxelExpressiveChip(
                        selected = uiState.selectedType == TransactionType.EXPENSE,
                        onClick = { viewModel.onTypeFilterChanged(TransactionType.EXPENSE) },
                        label = "Expenses",
                        icon = Icons.Default.ArrowDownward
                    )
                    VyxelExpressiveChip(
                        selected = uiState.selectedType == TransactionType.INCOME,
                        onClick = { viewModel.onTypeFilterChanged(TransactionType.INCOME) },
                        label = "Income",
                        icon = Icons.Default.ArrowUpward
                    )
                    VyxelExpressiveChip(
                        selected = uiState.selectedType == TransactionType.TRANSFER,
                        onClick = { viewModel.onTypeFilterChanged(TransactionType.TRANSFER) },
                        label = "Transfers",
                        icon = Icons.Default.SwapHoriz
                    )

                    if (uiState.accounts.size > 1) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .width(1.dp)
                                .height(20.dp)
                                .background(scheme.outlineVariant.copy(alpha = 0.5f))
                        )

                        VyxelExpressiveChip(
                            selected = uiState.selectedAccountId == null,
                            onClick = { viewModel.onAccountFilterChanged(null) },
                            label = "All Accounts"
                        )

                        uiState.accounts.forEach { account ->
                            val isSelected = uiState.selectedAccountId == account.id
                            VyxelExpressiveChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.onAccountFilterChanged(if (isSelected) null else account.id)
                                },
                                label = account.name,
                                icon = Icons.Default.AccountBalance
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                ExpressiveTransactionListSkeleton(count = 6)
            }
        } else if (uiState.searchQuery.isNotBlank() && uiState.dayGroups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                ExpressiveNoSearchResults(
                    query = uiState.searchQuery,
                    onClearSearch = { viewModel.onSearchQueryChanged("") },
                    suggestedQueries = listOf("UPI", "Swiggy", "Zomato", "Amazon", "Salary"),
                    onSuggestionClick = { viewModel.onSearchQueryChanged(it) }
                )
            }
        } else if (uiState.dayGroups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                val isSearching = uiState.searchQuery.isNotBlank()
                val hasFilter = uiState.selectedType != null || uiState.selectedAccountId != null

                ExpressiveEmptyState(
                    icon = if (isSearching) Icons.Default.Search else Icons.Default.ReceiptLong,
                    title = if (isSearching) "No matching transactions" else "No transactions found",
                    subtitle = when {
                        isSearching -> "No results found for \"${uiState.searchQuery}\". Check spelling or clear search."
                        hasFilter -> "No transactions match the selected account or category filters."
                        else -> "No spending records found. Tap the button below to log your first transaction."
                    },
                    actionLabel = when {
                        isSearching -> "Clear Search"
                        hasFilter -> "Clear Filters"
                        else -> "Log Transaction"
                    },
                    onActionClick = {
                        when {
                            isSearching -> viewModel.onSearchQueryChanged("")
                            hasFilter -> viewModel.clearAllFilters()
                            else -> onEditTransaction(0L)
                        }
                    }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .progressiveScrollBlur(topBlur = 12.dp, bottomBlur = 48.dp, scrimColor = scheme.background)
                    .padding(innerPadding),
                contentPadding = PaddingValues(top = 4.dp, bottom = VyxelNavBarBottomClearance),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                uiState.dayGroups.forEach { dayGroup ->
                    stickyHeader(key = dayGroup.dateLabel) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(scheme.background.copy(alpha = 0.95f))
                                .padding(horizontal = 16.dp, vertical = 7.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = dayGroup.dateLabel.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        color = scheme.onSurfaceVariant.copy(alpha = 0.85f)
                                    )
                                    Surface(
                                        shape = VyxelShapeTokens.Pill,
                                        color = scheme.surfaceContainerHighest.copy(alpha = 0.7f)
                                    ) {
                                        Text(
                                            text = "${dayGroup.count}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = scheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (dayGroup.dayIncomeMinor > 0) {
                                        Surface(
                                            shape = VyxelShapeTokens.Pill,
                                            color = IncomeColor.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, IncomeColor.copy(alpha = 0.25f))
                                        ) {
                                            Text(
                                                text = "+ ${dayGroup.formattedDayIncome}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = IncomeColor,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (dayGroup.dayTotalMinor > 0) {
                                        Surface(
                                            shape = VyxelShapeTokens.Pill,
                                            color = ExpenseColor.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, ExpenseColor.copy(alpha = 0.25f))
                                        ) {
                                            Text(
                                                text = "- ${dayGroup.formattedDayTotal}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ExpenseColor,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    items(
                        items = dayGroup.transactions,
                        key = { it.transaction.id }
                    ) { item ->
                        TransactionListItem(
                            item = item,
                            onClick = { selectedTransactionForDetail = item },
                            onDelete = {
                                val txId = item.transaction.id
                                viewModel.deleteTransaction(txId)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Transaction deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.undoDelete(txId)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    selectedTransactionForDetail?.let { item ->
        TransactionDetailSheet(
            item = item,
            onDismiss = { selectedTransactionForDetail = null },
            onEdit = {
                val id = item.transaction.id
                selectedTransactionForDetail = null
                onEditTransaction(id)
            },
            onDelete = {
                val txId = item.transaction.id
                selectedTransactionForDetail = null
                viewModel.deleteTransaction(txId)
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Transaction deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete(txId)
                    }
                }
            }
        )
    }
}

/**
 * Ultra-clean, modern 3-column auto-layout cashflow ribbon summarizing spent, received, and net balance.
 */
@Composable
fun TransactionsCashflowPill(
    incomeFormatted: String,
    expenseFormatted: String,
    netFormatted: String,
    netMinor: Long,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val netColor = when {
        netMinor > 0 -> IncomeColor
        netMinor < 0 -> ExpenseColor
        else -> scheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = VyxelShapeTokens.CardSmall,
        color = scheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 7.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Outflow / Spent
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "SPENT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.75f),
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = expenseFormatted,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(scheme.outlineVariant.copy(alpha = 0.45f))
            )

            // Inflow / Received
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "RECEIVED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.75f),
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = incomeFormatted,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = IncomeColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(scheme.outlineVariant.copy(alpha = 0.45f))
            )

            // Net Cashflow
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "NET",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.75f),
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = netFormatted,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = netColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Smart search bar with minimal, ergonomic pill height (40.dp) and quick clear action.
 */
@Composable
fun VyxelSmartSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search note, payee, category...",
    resultCount: Int? = null
) {
    val scheme = MaterialTheme.colorScheme
    val pillShape = VyxelShapeTokens.Pill

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 40.dp)
            .clip(pillShape)
            .background(scheme.surfaceContainerLowest)
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.7f), pillShape)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = scheme.primary,
            modifier = Modifier.size(17.dp)
        )

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(scheme.primary),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (query.isNotEmpty()) {
            if (resultCount != null) {
                Box(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(scheme.primaryContainer)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$resultCount found",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(6.dp))
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceContainerHighest)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = "Clear search query"
                    ) { onQueryChange("") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Modern transaction list item with auto layout, clear typography hierarchy,
 * gesture-based swipe to delete, and long-press confirmation dialog.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionListItem(
    item: TransactionWithDetails,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val badgeColor = item.categoryColour?.let { Color(it) }
        ?: when (item.transaction.type) {
            TransactionType.EXPENSE -> ExpenseColor
            TransactionType.INCOME -> IncomeColor
            TransactionType.TRANSFER -> TransferColor
        }

    val (amountText, amountColor) = when (item.transaction.type) {
        TransactionType.INCOME -> Pair(
            "+ ${MoneyFormatter.formatPaise(item.transaction.amountMinor)}",
            IncomeColor
        )
        TransactionType.EXPENSE -> Pair(
            "- ${MoneyFormatter.formatPaise(item.transaction.amountMinor)}",
            ExpenseColor
        )
        TransactionType.TRANSFER -> Pair(
            MoneyFormatter.formatPaise(item.transaction.amountMinor),
            TransferColor
        )
    }

    val timeFormatted = remember(item.transaction.date) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(item.transaction.date))
    }

    val title = when (item.transaction.type) {
        TransactionType.TRANSFER -> "Transfer to ${item.transferAccountName ?: "Account"}"
        else -> item.transaction.payee.ifBlank { item.categoryName ?: "Uncategorized" }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Transaction?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this $title transaction for $amountText?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = ExpenseColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        modifier = modifier,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val isSwipingEdit = direction == SwipeToDismissBoxValue.StartToEnd || dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd
            val isSwipingDelete = direction == SwipeToDismissBoxValue.EndToStart || dismissState.targetValue == SwipeToDismissBoxValue.EndToStart

            val targetColor = when {
                isSwipingEdit -> scheme.primary
                isSwipingDelete -> ExpenseColor
                else -> Color.Transparent
            }

            val animatedBgColor by animateColorAsState(
                targetValue = targetColor,
                animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                label = "swipeBgColor"
            )

            val isTriggered = dismissState.targetValue != SwipeToDismissBoxValue.Settled
            val iconScale by animateFloatAsState(
                targetValue = if (isTriggered) 1.12f else 0.95f,
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "swipeIconScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .clip(VyxelShapeTokens.CardSmall)
                    .background(animatedBgColor),
                contentAlignment = if (isSwipingEdit) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (isSwipingEdit) {
                    Row(
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Transaction",
                            tint = scheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimary
                        )
                    }
                } else if (isSwipingDelete) {
                    Row(
                        modifier = Modifier
                            .padding(end = 20.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Transaction",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .pressScale(0.98f)
                .clip(VyxelShapeTokens.CardSmall)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDeleteDialog = true
                    }
                ),
            colors = CardDefaults.cardColors(
                containerColor = scheme.surfaceContainerLowest
            ),
            border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f)),
            shape = VyxelShapeTokens.CardSmall
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Badge (40dp auto-layout container)
                CategoryBadge(
                    categoryName = item.categoryName ?: item.transaction.type.name,
                    iconKey = item.categoryIcon,
                    color = badgeColor,
                    size = 40.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Auto-layout middle column: Title, Account tag, Note
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = VyxelShapeTokens.Pill,
                            color = scheme.surfaceContainerHighest.copy(alpha = 0.65f),
                            modifier = Modifier.height(18.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            ) {
                                Text(
                                    text = item.accountName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = scheme.primary,
                                    maxLines = 1
                                )
                            }
                        }

                        if (item.transaction.note.isNotBlank()) {
                            Text(
                                text = item.transaction.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Auto-layout right column: Amount & Time
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = amountText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = amountColor,
                        maxLines = 1
                    )

                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
