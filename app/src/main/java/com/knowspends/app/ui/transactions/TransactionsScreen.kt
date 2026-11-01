package com.knowspends.app.ui.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Row: Clean Title + Review Inbox Pill Button + Search Toggle
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
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        )

                        // Count badge
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
                        // Review Inbox Pill Button (Direct Access)
                        Surface(
                            onClick = onNavigateToInbox,
                            shape = VyxelShapeTokens.Pill,
                            color = if (uiState.unreviewedSmsCount > 0) scheme.primaryContainer else scheme.surfaceContainerHighest,
                            border = BorderStroke(
                                1.dp,
                                if (uiState.unreviewedSmsCount > 0) scheme.primary.copy(alpha = 0.5f)
                                else scheme.outlineVariant.copy(alpha = 0.4f)
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
                                    tint = if (uiState.unreviewedSmsCount > 0) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (uiState.unreviewedSmsCount > 0) "Inbox (${uiState.unreviewedSmsCount})" else "Inbox",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.unreviewedSmsCount > 0) scheme.onPrimaryContainer else scheme.onSurfaceVariant
                                )
                            }
                        }

                        // Search Toggle Action
                        Surface(
                            onClick = {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded && uiState.searchQuery.isNotBlank()) {
                                    viewModel.onSearchQueryChanged("")
                                }
                            },
                            shape = CircleShape,
                            color = if (isSearchExpanded || uiState.searchQuery.isNotBlank()) scheme.primaryContainer else scheme.surfaceContainerHighest,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isSearchExpanded || uiState.searchQuery.isNotBlank()) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = if (isSearchExpanded || uiState.searchQuery.isNotBlank()) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Expandable Smart Search Bar
                AnimatedVisibility(
                    visible = isSearchExpanded || uiState.searchQuery.isNotBlank(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    VyxelSmartSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        resultCount = if (uiState.searchQuery.isNotBlank()) uiState.totalTransactionsCount else null
                    )
                }

                // Unified Single-Row Expressive Filter Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                // Balanced 3-Column Auto-Layout Cashflow Ribbon
                TransactionsCashflowPill(
                    incomeFormatted = uiState.formattedIncomeTotal,
                    expenseFormatted = uiState.formattedExpenseTotal,
                    netFormatted = uiState.formattedNetCashflow,
                    netMinor = uiState.netCashflowMinor
                )
            }
        }
    ) { innerPadding ->
        if (uiState.dayGroups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(scheme.surfaceContainerHighest.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = scheme.primary.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (uiState.searchQuery.isNotBlank()) "No matching records found" else "No transactions yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (uiState.searchQuery.isNotBlank())
                            "Try searching with different keywords or clearing active filters"
                        else
                            "Tap the floating + button to log an expense or income",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                    if (uiState.searchQuery.isNotBlank() || uiState.selectedType != null || uiState.selectedAccountId != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = viewModel::clearAllFilters,
                            shape = VyxelShapeTokens.Pill,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = scheme.primaryContainer,
                                contentColor = scheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Clear All Filters",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
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
                                .background(scheme.background.copy(alpha = 0.94f))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
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
                                        text = dayGroup.dateLabel,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = scheme.onSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(VyxelShapeTokens.Pill)
                                            .background(scheme.surfaceContainerHighest.copy(alpha = 0.8f))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${dayGroup.count}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = scheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (dayGroup.dayIncomeMinor > 0) {
                                        Box(
                                            modifier = Modifier
                                                .clip(VyxelShapeTokens.Pill)
                                                .background(IncomeColor.copy(alpha = 0.12f))
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "+ ${dayGroup.formattedDayIncome}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = IncomeColor
                                            )
                                        }
                                    }

                                    if (dayGroup.dayTotalMinor > 0) {
                                        Box(
                                            modifier = Modifier
                                                .clip(VyxelShapeTokens.Pill)
                                                .background(ExpenseColor.copy(alpha = 0.12f))
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "- ${dayGroup.formattedDayTotal}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = ExpenseColor
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
                            onClick = { onEditTransaction(item.transaction.id) },
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
        color = scheme.surfaceContainerLow,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Outflow / Spent
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "SPENT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.75f),
                    letterSpacing = 0.5.sp
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
                    .height(24.dp)
                    .background(scheme.outlineVariant.copy(alpha = 0.45f))
            )

            // Inflow / Received
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "RECEIVED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.75f),
                    letterSpacing = 0.5.sp
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
                    .height(24.dp)
                    .background(scheme.outlineVariant.copy(alpha = 0.45f))
            )

            // Net Cashflow
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "NET",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.75f),
                    letterSpacing = 0.5.sp
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
            if (value == SwipeToDismissBoxValue.EndToStart) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        modifier = modifier,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> ExpenseColor
                    else -> Color.Transparent
                },
                label = "dismissColor"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .clip(VyxelShapeTokens.CardSmall)
                    .background(color)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Row(
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
                            contentDescription = "Delete",
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
