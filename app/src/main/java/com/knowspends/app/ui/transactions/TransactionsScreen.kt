package com.knowspends.app.ui.transactions

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.semantics.Role
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.model.TransactionWithDetails
import com.knowspends.app.domain.util.MoneyFormatter
import com.knowspends.app.ui.components.CategoryBadge
import com.knowspends.app.ui.components.VyxelNavBarBottomClearance
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.KnowSpendsMotion
import com.knowspends.app.ui.theme.TransferColor
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.fadingEdges
import com.knowspends.app.ui.theme.glassSurface
import com.knowspends.app.ui.theme.pressScale
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onEditTransaction: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Row: Title + Live Summary Pill
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
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        )

                        // Count badge
                        Box(
                            modifier = Modifier
                                .clip(VyxelShapeTokens.Pill)
                                .background(scheme.surfaceContainerHighest)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${uiState.totalTransactionsCount}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.primary
                            )
                        }
                    }

                    // Cashflow indicator for active selection
                    val summaryText = when (uiState.selectedType) {
                        TransactionType.EXPENSE -> "Spent: ${uiState.formattedExpenseTotal}"
                        TransactionType.INCOME -> "Income: ${uiState.formattedIncomeTotal}"
                        TransactionType.TRANSFER -> "Transfers: ${uiState.totalTransactionsCount}"
                        null -> "-${uiState.formattedExpenseTotal}"
                    }
                    val summaryColor = when (uiState.selectedType) {
                        TransactionType.EXPENSE -> ExpenseColor
                        TransactionType.INCOME -> IncomeColor
                        TransactionType.TRANSFER -> TransferColor
                        null -> scheme.primary
                    }

                    Box(
                        modifier = Modifier
                            .clip(VyxelShapeTokens.Pill)
                            .background(summaryColor.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = summaryText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = summaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Smart Minimal Search Bar (Compact 40.dp height)
                VyxelSmartSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    resultCount = if (uiState.searchQuery.isNotBlank()) uiState.totalTransactionsCount else null
                )

                // Transaction Type Segmented Pill Row
                TransactionTypeSegmentedRow(
                    selectedType = uiState.selectedType,
                    onTypeSelected = viewModel::onTypeFilterChanged
                )

                // Cashflow Quick Overview Ribbon
                TransactionsCashflowRibbon(
                    incomeFormatted = uiState.formattedIncomeTotal,
                    expenseFormatted = uiState.formattedExpenseTotal,
                    netFormatted = uiState.formattedNetCashflow,
                    netMinor = uiState.netCashflowMinor
                )

                // Optional Account Filter Chips (if multiple accounts exist)
                if (uiState.accounts.size > 1) {
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedAccountId == null,
                            onClick = { viewModel.onAccountFilterChanged(null) },
                            label = { Text("All Accounts") },
                            shape = VyxelShapeTokens.Pill,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = scheme.primaryContainer,
                                selectedLabelColor = scheme.onPrimaryContainer
                            )
                        )

                        uiState.accounts.forEach { account ->
                            val isSelected = uiState.selectedAccountId == account.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.onAccountFilterChanged(if (isSelected) null else account.id)
                                },
                                label = { Text(account.name) },
                                shape = VyxelShapeTokens.Pill,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = scheme.primaryContainer,
                                    selectedLabelColor = scheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
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
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (uiState.searchQuery.isNotBlank()) "No matching records found" else "No transactions yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (uiState.searchQuery.isNotBlank())
                            "Try searching with different keywords or clearing active filters"
                        else
                            "Tap the floating + button to log an expense or income",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    if (uiState.searchQuery.isNotBlank() || uiState.selectedType != null || uiState.selectedAccountId != null) {
                        Spacer(modifier = Modifier.height(14.dp))
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
                                modifier = Modifier.size(15.dp)
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
                    .fadingEdges(topFade = 8.dp, bottomFade = 24.dp)
                    .padding(innerPadding),
                contentPadding = PaddingValues(top = 4.dp, bottom = VyxelNavBarBottomClearance),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.dayGroups.forEach { dayGroup ->
                    stickyHeader(key = dayGroup.dateLabel) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp),
                            color = scheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.55f)),
                            shape = VyxelShapeTokens.CardSmall
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = dayGroup.dateLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = scheme.primary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(VyxelShapeTokens.Pill)
                                            .background(scheme.surfaceContainerHighest)
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
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
 * Cashflow overview ribbon displaying real-time Inflow, Outflow, and Net Margin.
 */
@Composable
fun TransactionsCashflowRibbon(
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
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Inflow item
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = IncomeColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Inflow",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = incomeFormatted,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = IncomeColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(26.dp)
                    .background(scheme.outlineVariant.copy(alpha = 0.6f))
            )

            // Outflow item
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = ExpenseColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Outflow",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = expenseFormatted,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(26.dp)
                    .background(scheme.outlineVariant.copy(alpha = 0.6f))
            )

            // Net Margin item
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = netColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Net Margin",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = netFormatted,
                    style = MaterialTheme.typography.labelMedium,
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

private data class TypeOptionItem(
    val type: TransactionType?,
    val label: String,
    val icon: ImageVector? = null,
    val accentColor: Color? = null
)

@Composable
private fun TransactionTypeSegmentedRow(
    selectedType: TransactionType?,
    onTypeSelected: (TransactionType?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val options = listOf(
        TypeOptionItem(null, "All"),
        TypeOptionItem(TransactionType.EXPENSE, "Expense", Icons.Default.ArrowDownward, ExpenseColor),
        TypeOptionItem(TransactionType.INCOME, "Income", Icons.Default.ArrowUpward, IncomeColor),
        TypeOptionItem(TransactionType.TRANSFER, "Transfer", Icons.Default.SwapHoriz, TransferColor)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(VyxelShapeTokens.Pill)
            .background(scheme.surfaceContainerLow)
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.6f), VyxelShapeTokens.Pill)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEach { opt ->
            val isSelected = selectedType == opt.type
            val targetBg = if (isSelected) {
                opt.accentColor ?: scheme.primary
            } else {
                Color.Transparent
            }
            val targetFg = if (isSelected) {
                if (opt.accentColor != null) Color.White else scheme.onPrimary
            } else {
                scheme.onSurfaceVariant
            }

            val bgAnim by animateColorAsState(targetBg, KnowSpendsMotion.expressive(), label = "typeBg")
            val fgAnim by animateColorAsState(targetFg, KnowSpendsMotion.expressive(), label = "typeFg")

            Row(
                modifier = Modifier
                    .weight(1f)
                    .pressScale(0.96f)
                    .clip(VyxelShapeTokens.Pill)
                    .background(bgAnim)
                    .clickable(role = Role.Tab) { onTypeSelected(opt.type) }
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (opt.icon != null) {
                    Icon(
                        imageVector = opt.icon,
                        contentDescription = null,
                        tint = fgAnim,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                }
                Text(
                    text = opt.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = fgAnim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TransactionListItem(
    item: TransactionWithDetails,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current

    val badgeColor = item.categoryColour?.let { Color(it) }
        ?: when (item.transaction.type) {
            TransactionType.EXPENSE -> ExpenseColor
            TransactionType.INCOME -> IncomeColor
            TransactionType.TRANSFER -> TransferColor
        }

    val (amountText, amountColor, bgTint) = when (item.transaction.type) {
        TransactionType.INCOME -> Triple(
            "+ ${MoneyFormatter.formatPaise(item.transaction.amountMinor)}",
            IncomeColor,
            IncomeColor.copy(alpha = 0.12f)
        )
        TransactionType.EXPENSE -> Triple(
            "- ${MoneyFormatter.formatPaise(item.transaction.amountMinor)}",
            ExpenseColor,
            ExpenseColor.copy(alpha = 0.12f)
        )
        TransactionType.TRANSFER -> Triple(
            MoneyFormatter.formatPaise(item.transaction.amountMinor),
            TransferColor,
            TransferColor.copy(alpha = 0.12f)
        )
    }

    val timeFormatted = remember(item.transaction.date) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(item.transaction.date))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .pressScale(0.98f)
            .shadow(2.dp, VyxelShapeTokens.CardSmall)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.65f)),
        shape = VyxelShapeTokens.CardSmall
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Badge with expressive rounded container
            CategoryBadge(
                categoryName = item.categoryName ?: item.transaction.type.name,
                iconKey = item.categoryIcon,
                color = badgeColor,
                size = 42.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Transaction Details Column
            Column(modifier = Modifier.weight(1f)) {
                val title = when (item.transaction.type) {
                    TransactionType.TRANSFER -> "Transfer to ${item.transferAccountName ?: "Account"}"
                    else -> item.transaction.payee.ifBlank { item.categoryName ?: "Uncategorized" }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.accountName,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (item.transaction.note.isNotBlank()) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = item.transaction.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    Text(
                        text = "• $timeFormatted",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount pill badge
            Box(
                modifier = Modifier
                    .clip(VyxelShapeTokens.Pill)
                    .background(bgTint)
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Sleek Delete button
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = scheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
