package com.knowspends.app.ui.budgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import com.knowspends.app.ui.components.CategoryBadge
import com.knowspends.app.ui.components.ExpressiveWavyProgressIndicator
import com.knowspends.app.ui.theme.fadingEdges
import com.knowspends.app.ui.theme.pressScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowspends.app.domain.model.BudgetStatus
import com.knowspends.app.domain.model.CategoryBudget
import com.knowspends.app.domain.util.MoneyFormatter
import com.knowspends.app.ui.theme.ErrorDark
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    modifier: Modifier = Modifier,
    viewModel: BudgetsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Budgets (${uiState.periodLabel})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.openSetBudgetDialog() }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Set Budget")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Overall Month Budget Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "TOTAL MONTHLY BUDGET",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.formattedTotalBudgeted,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ExpressiveWavyProgressIndicator(
                        progress = uiState.totalProgressRatio,
                        modifier = Modifier.fillMaxWidth(),
                        color = when {
                            uiState.totalSpentMinor > uiState.totalBudgetedMinor -> ExpenseColor
                            uiState.totalProgressRatio >= 0.8f -> Color(0xFFF57C00)
                            else -> IncomeColor
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Spent: ${uiState.formattedTotalSpent}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (uiState.totalRemainingMinor >= 0) "Left: ${uiState.formattedTotalRemaining}" else "Over by: ${MoneyFormatter.formatPaise(-uiState.totalRemainingMinor)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (uiState.totalRemainingMinor >= 0) IncomeColor else ExpenseColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Budgets List
            if (uiState.budgets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No budgets set for this month",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(onClick = { viewModel.openSetBudgetDialog() }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Set Category Budget")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .fadingEdges(topFade = 8.dp, bottomFade = 24.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.budgets,
                        key = { it.budgetId }
                    ) { budget ->
                        CategoryBudgetCard(
                            budget = budget,
                            onClick = { viewModel.openSetBudgetDialog(budget) }
                        )
                    }
                }
            }
        }

        // Set / Edit Budget Dialog
        if (uiState.isSetBudgetDialogOpen) {
            SetBudgetDialog(
                editingBudget = uiState.selectedBudgetForEdit,
                categories = uiState.categories,
                onDismiss = viewModel::closeSetBudgetDialog,
                onSave = { categoryId, amountMinor, rollover ->
                    viewModel.saveBudget(categoryId, amountMinor, rollover)
                },
                onDelete = { budgetId ->
                    viewModel.deleteBudget(budgetId)
                }
            )
        }
    }
}

@Composable
fun CategoryBudgetCard(
    budget: CategoryBudget,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(0.97f)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Category badge & Name + Budget Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(
                    categoryName = budget.categoryName,
                    iconKey = null,
                    color = Color(budget.categoryColour),
                    size = 36.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = budget.categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${MoneyFormatter.formatPaise(budget.spentAmountMinor)} / ${MoneyFormatter.formatPaise(budget.budgetAmountMinor)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expressive Wavy Progress Bar
            ExpressiveWavyProgressIndicator(
                progress = budget.progressRatio,
                modifier = Modifier.fillMaxWidth(),
                color = when (budget.status) {
                    BudgetStatus.SAFE -> IncomeColor
                    BudgetStatus.WARNING -> Color(0xFFF57C00)
                    BudgetStatus.OVERSPENT -> ExpenseColor
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Accessibility Status Indicator: Colour + Icon + Text (per §7 & §9)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (budget.status) {
                    BudgetStatus.SAFE -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Safe",
                                tint = IncomeColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${MoneyFormatter.formatPaise(budget.remainingAmountMinor)} left",
                                style = MaterialTheme.typography.labelSmall,
                                color = IncomeColor
                            )
                        }
                    }
                    BudgetStatus.WARNING -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = Color(0xFFF57C00),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${(budget.progressRatio * 100).toInt()}% used",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFF57C00)
                            )
                        }
                    }
                    BudgetStatus.OVERSPENT -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Overspent",
                                tint = ExpenseColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Overspent by ${MoneyFormatter.formatPaise(-budget.remainingAmountMinor)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseColor
                            )
                        }
                    }
                }

                if (budget.rollover) {
                    Text(
                        text = "Rollover active",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBudgetDialog(
    editingBudget: CategoryBudget?,
    categories: List<com.knowspends.app.domain.model.Category>,
    onDismiss: () -> Unit,
    onSave: (categoryId: Long, amountMinor: Long, rollover: Boolean) -> Unit,
    onDelete: (budgetId: Long) -> Unit
) {
    var selectedCategoryId by remember {
        mutableStateOf(editingBudget?.categoryId ?: categories.firstOrNull()?.id ?: 0L)
    }

    val initialAmount = editingBudget?.let {
        (it.budgetAmountMinor / 100).toString()
    } ?: ""
    var amountText by remember { mutableStateOf(initialAmount) }
    var rollover by remember { mutableStateOf(editingBudget?.rollover ?: false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        title = {
            Text(if (editingBudget != null) "Edit Budget" else "Set Category Budget")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Category Picker
                if (editingBudget == null) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "Select Category",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        selectedCategoryId = cat.id
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Category: ${editingBudget.categoryName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Monthly Budget Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Rollover Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Rollover unspent amount",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Carry over balance to next month",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = rollover,
                        onCheckedChange = { rollover = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountMinor = MoneyFormatter.parseToPaise(amountText)
                    if (selectedCategoryId > 0 && amountMinor > 0) {
                        onSave(selectedCategoryId, amountMinor, rollover)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (editingBudget != null) {
                    TextButton(
                        onClick = { onDelete(editingBudget.budgetId) },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
