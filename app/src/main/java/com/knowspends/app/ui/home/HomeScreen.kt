package com.knowspends.app.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.knowspends.app.ui.components.CategoryBadge
import com.knowspends.app.ui.theme.KnowSpendsMotion
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.VyxelTypographyTokens
import com.knowspends.app.ui.theme.auroraGlow
import com.knowspends.app.ui.theme.fadingEdges
import com.knowspends.app.ui.theme.pressScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowspends.app.domain.model.Account
import com.knowspends.app.domain.model.AccountType
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.model.TransactionWithDetails
import com.knowspends.app.domain.util.MoneyFormatter
import com.knowspends.app.ui.theme.ChartPalette
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.TransferColor

@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit = {},
    onTransactionClick: (Long) -> Unit = {},
    onNavigateToInbox: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .fadingEdges(topFade = 8.dp, bottomFade = 24.dp)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App Header: Clean Title & Month Label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "KnowSpends",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = uiState.currentMonthLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Monthly Summary Card with Enriched Cashflow Analytics
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .auroraGlow(alpha = 0.16f),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header: Clean Label
                Text(
                    text = "NET MONTHLY SAVINGS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = VyxelTypographyTokens.TrackingUppercase
                )

                // Main Metric: Formatted Net Savings
                Text(
                    text = uiState.formattedNetSavings,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                    color = if (uiState.monthlySummary.netSavingsMinor >= 0) IncomeColor else ExpenseColor
                )

                // Visual Cashflow Bar
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 6.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(uiState.spendRatePercent.coerceIn(0f, 1f))
                                .heightIn(min = 6.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(if (uiState.spendRatePercent > 0.9f) ExpenseColor else IncomeColor)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Savings Rate: ${uiState.savingsRatePercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${uiState.daysRemainingInMonth} days left in ${uiState.currentMonthLabel.split(" ")[0]}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                // Balanced 3-Column Stats (Auto-Layout: Income, Spent, Daily Average)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Total Income Stat
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(IncomeColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = IncomeColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "Income",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.formattedIncome,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IncomeColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight(0.65f)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    )

                    // Total Spent Stat
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(ExpenseColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = ExpenseColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "Spent",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.formattedExpense,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight(0.65f)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    )

                    // Daily Average Stat
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = "Daily",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.dailyAverageExpense,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Expense Visual Analytics (7-Day Bar Chart & Category Donut Chart)
        ExpenseAnalyticsCard(
            uiState = uiState,
            onTabSelected = { viewModel.onAnalyticsTabSelected(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Accounts Section with Individual Tap-to-Edit Functionality
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Accounts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap account to edit",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.accounts.forEach { account ->
                AccountCard(
                    account = account,
                    onClick = {
                        viewModel.onSelectAccountForAction(account)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Activity Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (uiState.recentTransactions.isNotEmpty()) {
                TextButton(onClick = onNavigateToTransactions) {
                    Text("See all")
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (uiState.recentTransactions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent transactions. Tap + below to add your first expense.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.recentTransactions.forEach { item ->
                    RecentTransactionItem(
                        item = item,
                        onClick = { onTransactionClick(item.transaction.id) }
                    )
                }
            }
        }

        // Adaptive bottom clearance for the floating navigation bar and + button
        val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Spacer(modifier = Modifier.height(navBarPadding + 80.dp))
    }

    // Individual Account Editing Modal Flow
    uiState.selectedAccountForAction?.let { account ->
        when (uiState.activeEditField) {
            AccountEditField.OPTIONS -> {
                AccountActionOptionsDialog(
                    account = account,
                    onSelectField = { field -> viewModel.onSelectAccountEditField(field) },
                    onDismiss = { viewModel.onDismissAccountAction() }
                )
            }
            AccountEditField.BALANCE -> {
                AccountEditBalanceDialog(
                    account = account,
                    onSave = { newBalancePaise ->
                        viewModel.updateAccountBalance(account.id, newBalancePaise)
                        viewModel.onDismissAccountAction()
                    },
                    onBack = { viewModel.onSelectAccountEditField(AccountEditField.OPTIONS) },
                    onDismiss = { viewModel.onDismissAccountAction() }
                )
            }
            AccountEditField.NAME -> {
                AccountEditNameDialog(
                    account = account,
                    onSave = { newName ->
                        viewModel.updateAccountName(account.id, newName)
                        viewModel.onDismissAccountAction()
                    },
                    onBack = { viewModel.onSelectAccountEditField(AccountEditField.OPTIONS) },
                    onDismiss = { viewModel.onDismissAccountAction() }
                )
            }
            AccountEditField.TYPE -> {
                AccountEditTypeDialog(
                    account = account,
                    onSave = { newType ->
                        viewModel.updateAccountType(account.id, newType)
                        viewModel.onDismissAccountAction()
                    },
                    onBack = { viewModel.onSelectAccountEditField(AccountEditField.OPTIONS) },
                    onDismiss = { viewModel.onDismissAccountAction() }
                )
            }
            null -> {}
        }
    }
}

enum class AccountEditField {
    OPTIONS,
    BALANCE,
    NAME,
    TYPE
}

@Composable
fun AccountCard(
    account: Account,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .pressScale(0.97f)
            .padding(vertical = 4.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getAccountIcon(account.type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Account",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = account.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = MoneyFormatter.formatPaise(account.currentBalanceMinor),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AccountActionOptionsDialog(
    account: Account,
    onSelectField: (AccountEditField) -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = scheme.surfaceContainerLow,
        title = {
            Column {
                Text(
                    text = "Manage ${account.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Text(
                    text = "Select an attribute to edit individually:",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Option 1: Balance
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(0.98f)
                        .clickable(role = Role.Button) { onSelectField(AccountEditField.BALANCE) },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLowest),
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(scheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = scheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Edit Balance Only",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface
                            )
                            Text(
                                text = "Current: ${MoneyFormatter.formatPaise(account.currentBalanceMinor)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Option 2: Name
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(0.98f)
                        .clickable(role = Role.Button) { onSelectField(AccountEditField.NAME) },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLowest),
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(scheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = scheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Rename Account Only",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface
                            )
                            Text(
                                text = "Current: ${account.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Option 3: Type
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(0.98f)
                        .clickable(role = Role.Button) { onSelectField(AccountEditField.TYPE) },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLowest),
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(scheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = scheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Change Type Only",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface
                            )
                            Text(
                                text = "Current: ${account.type.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}

@Composable
fun AccountEditBalanceDialog(
    account: Account,
    onSave: (Long) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    var balanceText by remember {
        mutableStateOf(
            if (account.openingBalanceMinor == 0L) "0"
            else (account.openingBalanceMinor / 100.0).toString()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        title = {
            Text(
                text = "Update Balance • ${account.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enter new opening balance amount for this account:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Balance (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val balancePaise = ((balanceText.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    onSave(balancePaise)
                },
                shape = VyxelShapeTokens.Pill
            ) {
                Text("Save Balance", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onBack) {
                Text("Back")
            }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}

@Composable
fun AccountEditNameDialog(
    account: Account,
    onSave: (String) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(account.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        title = {
            Text(
                text = "Rename Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enter new title or nickname for this account:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onSave(name.trim())
                },
                shape = VyxelShapeTokens.Pill
            ) {
                Text("Save Name", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onBack) {
                Text("Back")
            }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}

@Composable
fun AccountEditTypeDialog(
    account: Account,
    onSave: (AccountType) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(account.type) }
    val scheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = scheme.surfaceContainerLow,
        title = {
            Text(
                text = "Select Account Type",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Choose the ledger classification for ${account.name}:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )

                val accountTypes = listOf(
                    AccountType.CASH to Pair("Cash", Icons.Default.Payments),
                    AccountType.BANK to Pair("Bank", Icons.Default.AccountBalance),
                    AccountType.WALLET to Pair("Wallet", Icons.Default.AccountBalanceWallet),
                    AccountType.CARD to Pair("Card", Icons.Default.CreditCard)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    accountTypes.chunked(2).forEach { rowPair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPair.forEach { (type, info) ->
                                val (name, icon) = info
                                val isSelected = selectedType == type
                                val borderStroke = if (isSelected) {
                                    BorderStroke(1.5.dp, scheme.primary)
                                } else {
                                    BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f))
                                }

                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .pressScale(0.96f)
                                        .clip(VyxelShapeTokens.Card)
                                        .background(
                                            if (isSelected) scheme.primaryContainer
                                            else scheme.surfaceContainerHigh
                                        )
                                        .border(borderStroke, VyxelShapeTokens.Card)
                                        .clickable { selectedType = type }
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) scheme.onPrimaryContainer else scheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedType) },
                shape = VyxelShapeTokens.Pill
            ) {
                Text("Save Type", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onBack) {
                Text("Back")
            }
        },
        shape = VyxelShapeTokens.Card
    )
}

@Composable
fun ExpenseAnalyticsCard(
    uiState: HomeUiState,
    onTabSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedTab = uiState.selectedAnalyticsTab
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = "Expense Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = if (selectedTab == 0) "Last 7 days daily pattern" else "Current month breakdown",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    modifier = Modifier
                        .clip(VyxelShapeTokens.Pill)
                        .background(scheme.surfaceContainerHigh)
                        .border(1.dp, scheme.outlineVariant.copy(alpha = 0.55f), VyxelShapeTokens.Pill)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    listOf("7 Days", "Categories").forEachIndexed { index, label ->
                        val isSelected = selectedTab == index
                        val bgAnim by animateColorAsState(
                            targetValue = if (isSelected) scheme.primary else Color.Transparent,
                            animationSpec = KnowSpendsMotion.expressive(),
                            label = "tabBg"
                        )
                        val fgAnim by animateColorAsState(
                            targetValue = if (isSelected) scheme.onPrimary else scheme.onSurfaceVariant,
                            animationSpec = KnowSpendsMotion.expressive(),
                            label = "tabFg"
                        )
                        Box(
                            modifier = Modifier
                                .pressScale(0.96f)
                                .clip(VyxelShapeTokens.Pill)
                                .background(bgAnim)
                                .clickable { onTabSelected(index) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = fgAnim,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                SevenDaySpendingChart(dailyItems = uiState.dailyExpenses7Days)
            } else {
                CategoryBreakdownDonutChart(
                    shares = uiState.categoryExpenses,
                    totalExpenseFormatted = uiState.formattedExpense
                )
            }
        }
    }
}

@Composable
fun SevenDaySpendingChart(
    dailyItems: List<DayExpenseItem>,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val maxAmount = remember(dailyItems) {
        dailyItems.maxOfOrNull { it.amountMinor }?.coerceAtLeast(100L) ?: 100L
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            dailyItems.forEach { item ->
                val ratio = (item.amountMinor.toFloat() / maxAmount.toFloat()).coerceIn(0.06f, 1f)
                val animatedRatio by animateFloatAsState(
                    targetValue = ratio,
                    animationSpec = tween(durationMillis = 400),
                    label = "barHeight"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    if (item.amountMinor > 0) {
                        Text(
                            text = item.formattedAmount.replace("₹", "").split(".")[0],
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.isToday) scheme.primary else scheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .fillMaxHeight(animatedRatio)
                            .clip(VyxelShapeTokens.ChartBar)
                            .background(
                                if (item.isToday) {
                                    Brush.verticalGradient(
                                        listOf(scheme.primary, scheme.tertiary)
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            scheme.primary.copy(alpha = 0.55f),
                                            scheme.primary.copy(alpha = 0.25f)
                                        )
                                    )
                                }
                            )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (item.isToday) scheme.primary else scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownDonutChart(
    shares: List<CategoryExpenseShare>,
    totalExpenseFormatted: String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    if (shares.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No category expense records this month yet.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }
        return
    }

    val defaultPalette = ChartPalette

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 20.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val topLeft = Offset(
                    (size.width - 2 * radius) / 2,
                    (size.height - 2 * radius) / 2
                )
                val arcSize = Size(radius * 2, radius * 2)

                var currentStartAngle = -90f
                shares.forEachIndexed { index, share ->
                    val sweepAngle = (share.percentage / 100f) * 360f
                    val arcColor = share.colorHex?.let { Color(it) } ?: defaultPalette[index % defaultPalette.size]

                    drawArc(
                        color = arcColor,
                        startAngle = currentStartAngle,
                        sweepAngle = (sweepAngle - 2f).coerceAtLeast(1f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    currentStartAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
                Text(
                    text = totalExpenseFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            shares.forEachIndexed { index, share ->
                val arcColor = share.colorHex?.let { Color(it) } ?: defaultPalette[index % defaultPalette.size]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(arcColor)
                        )
                        Text(
                            text = share.categoryName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = scheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "${share.percentage.toInt()}% (${share.formattedAmount})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RecentTransactionItem(
    item: TransactionWithDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val timeFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(0.98f)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val badgeColor = item.categoryColour?.let { Color(it) }
                    ?: when (item.transaction.type) {
                        TransactionType.EXPENSE -> ExpenseColor
                        TransactionType.INCOME -> IncomeColor
                        TransactionType.TRANSFER -> TransferColor
                    }

                CategoryBadge(
                    categoryName = item.categoryName ?: item.transaction.type.name,
                    iconKey = item.categoryIcon,
                    color = badgeColor,
                    size = 42.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.transaction.payee.ifBlank {
                            item.categoryName ?: "Transaction"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.transaction.note.isNotBlank()) {
                        Text(
                            text = item.transaction.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val amountFormatted = MoneyFormatter.formatPaise(item.transaction.amountMinor)
                val (amountText, amountColor) = when (item.transaction.type) {
                    TransactionType.INCOME -> "+ $amountFormatted" to IncomeColor
                    TransactionType.EXPENSE -> "- $amountFormatted" to ExpenseColor
                    TransactionType.TRANSFER -> amountFormatted to TransferColor
                }

                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            }

            // Bottom Badges Row: Date/Time Badge + Account Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(scheme.surfaceContainerHigh)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = getAccountIcon(
                            when {
                                item.accountName.contains("bank", ignoreCase = true) -> AccountType.BANK
                                item.accountName.contains("card", ignoreCase = true) -> AccountType.CARD
                                item.accountName.contains("wallet", ignoreCase = true) -> AccountType.WALLET
                                else -> AccountType.CASH
                            }
                        ),
                        contentDescription = null,
                        tint = scheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = item.accountName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = scheme.onSurface
                    )
                }

                Text(
                    text = timeFormat.format(Date(item.transaction.date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
            }
        }
    }
}

private fun getAccountIcon(type: AccountType): ImageVector = when (type) {
    AccountType.CASH -> Icons.Default.Payments
    AccountType.BANK -> Icons.Default.AccountBalance
    AccountType.WALLET -> Icons.Default.Wallet
    AccountType.CARD -> Icons.Default.CreditCard
    AccountType.LOAN -> Icons.Default.Payments
}
