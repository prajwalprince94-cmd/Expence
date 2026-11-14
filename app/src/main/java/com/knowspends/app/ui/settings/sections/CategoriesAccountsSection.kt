package com.knowspends.app.ui.settings.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.knowspends.app.ui.components.VyxelNavBarBottomClearance
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knowspends.app.domain.model.Account
import com.knowspends.app.domain.model.AccountType
import com.knowspends.app.domain.model.Category
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.util.MoneyFormatter
import com.knowspends.app.ui.components.CategoryBadge
import com.knowspends.app.ui.components.expressive.ExpressiveItemDivider
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsGroup
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsItem
import com.knowspends.app.ui.components.resolveCategoryIcon
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.VyxelShapeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesAccountsSection(
    accounts: List<Account>,
    categories: List<Category>,
    currencySymbol: String = "₹",
    onSaveAccount: (Account) -> Unit,
    onArchiveAccount: (Long, Boolean) -> Unit,
    onSaveCategory: (Category) -> Unit,
    onArchiveCategory: (Long, Boolean) -> Unit,
    onNavigateToMerchantRules: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    var showCategoriesSheet by remember { mutableStateOf(false) }
    var showAccountsSheet by remember { mutableStateOf(false) }

    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var showCreateAccountDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<Account?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. Categories Group ───────────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Categories Architecture") {
            ExpressiveSettingsItem(
                title = "Expense & Income Categories",
                subtitle = "Manage category taxonomy, color dots, and icons",
                icon = Icons.Default.Category,
                showChevron = true,
                onClick = { showCategoriesSheet = true },
                trailingContent = {
                    Surface(
                        shape = VyxelShapeTokens.Pill,
                        color = scheme.primaryContainer
                    ) {
                        Text(
                            text = "${categories.size} Active",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            )
        }

        // ── 2. Payment Sources Group ──────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Wallets & Accounts") {
            ExpressiveSettingsItem(
                title = "Connected Accounts & Payment Sources",
                subtitle = "Bank accounts, liquid cash wallets, and credit cards",
                icon = Icons.Default.AccountBalance,
                showChevron = true,
                onClick = { showAccountsSheet = true },
                trailingContent = {
                    Surface(
                        shape = VyxelShapeTokens.Pill,
                        color = scheme.secondaryContainer
                    ) {
                        Text(
                            text = "${accounts.size} Accounts",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            )

            ExpressiveItemDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccountTypePill(icon = Icons.Default.AccountBalance, label = "Bank Accounts")
                AccountTypePill(icon = Icons.Default.Payments, label = "Cash")
                AccountTypePill(icon = Icons.Default.CreditCard, label = "Credit Cards")
            }
        }

        // ── 3. Smart Categorization Rules ─────────────────────────────────────
        ExpressiveSettingsGroup(title = "Automated Categorization") {
            ExpressiveSettingsItem(
                title = "Merchant Categorization Rules",
                subtitle = "Auto-assign categories from merchant keywords (e.g. Swiggy → Food)",
                icon = Icons.Default.Rule,
                showChevron = true,
                onClick = onNavigateToMerchantRules
            )
        }

        Spacer(modifier = Modifier.height(VyxelNavBarBottomClearance).navigationBarsPadding())
    }

    // ── Categories Management Modal Bottom Sheet ─────────────────────────────
    if (showCategoriesSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCategoriesSheet = false },
            sheetState = sheetState,
            containerColor = scheme.surfaceContainerLowest,
            shape = VyxelShapeTokens.CardLarge
        ) {
            ManageCategoriesSheetContent(
                categories = categories,
                onAddCategoryClick = { showCreateCategoryDialog = true },
                onArchiveCategory = onArchiveCategory,
                onDismiss = { showCategoriesSheet = false }
            )
        }
    }

    // ── Accounts Management Modal Bottom Sheet ───────────────────────────────
    if (showAccountsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAccountsSheet = false },
            sheetState = sheetState,
            containerColor = scheme.surfaceContainerLowest,
            shape = VyxelShapeTokens.CardLarge
        ) {
            ManageAccountsSheetContent(
                accounts = accounts,
                currencySymbol = currencySymbol,
                onAddAccountClick = { showCreateAccountDialog = true },
                onEditAccountClick = { accountToEdit = it },
                onArchiveAccount = onArchiveAccount,
                onDismiss = { showAccountsSheet = false }
            )
        }
    }

    // ── Create Category Dialog ───────────────────────────────────────────────
    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateCategoryDialog = false },
            onSave = { newCat ->
                onSaveCategory(newCat)
                showCreateCategoryDialog = false
            }
        )
    }

    // ── Create Account Dialog ────────────────────────────────────────────────
    if (showCreateAccountDialog) {
        CreateAccountDialog(
            currencySymbol = currencySymbol,
            onDismiss = { showCreateAccountDialog = false },
            onSave = { newAcc ->
                onSaveAccount(newAcc)
                showCreateAccountDialog = false
            }
        )
    }

    // ── Edit Account Dialog ──────────────────────────────────────────────────
    accountToEdit?.let { acc ->
        EditAccountDetailsDialog(
            account = acc,
            currencySymbol = currencySymbol,
            onDismiss = { accountToEdit = null },
            onSave = { updatedAcc ->
                onSaveAccount(updatedAcc)
                accountToEdit = null
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Manage Categories Sheet Content
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ManageCategoriesSheetContent(
    categories: List<Category>,
    onAddCategoryClick: () -> Unit,
    onArchiveCategory: (Long, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var selectedFilter by remember { mutableStateOf<TransactionType?>(null) }
    var categoryToArchive by remember { mutableStateOf<Category?>(null) }

    val filteredList = remember(categories, selectedFilter) {
        if (selectedFilter == null) categories
        else categories.filter { it.kind == selectedFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Categories Architecture",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Text(
                    text = "${categories.size} categories configured",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("All (${categories.size})") },
                shape = VyxelShapeTokens.Pill
            )
            FilterChip(
                selected = selectedFilter == TransactionType.EXPENSE,
                onClick = { selectedFilter = TransactionType.EXPENSE },
                label = { Text("Expense (${categories.count { it.kind == TransactionType.EXPENSE }})") },
                shape = VyxelShapeTokens.Pill,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ExpenseColor.copy(alpha = 0.2f),
                    selectedLabelColor = ExpenseColor
                )
            )
            FilterChip(
                selected = selectedFilter == TransactionType.INCOME,
                onClick = { selectedFilter = TransactionType.INCOME },
                label = { Text("Income (${categories.count { it.kind == TransactionType.INCOME }})") },
                shape = VyxelShapeTokens.Pill,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IncomeColor.copy(alpha = 0.2f),
                    selectedLabelColor = IncomeColor
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Categories List
        LazyColumn(
            modifier = Modifier
                .weight(1f, fill = false)
                .height(340.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredList, key = { it.id }) { cat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = VyxelShapeTokens.CardSmall,
                    colors = CardDefaults.cardColors(
                        containerColor = scheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CategoryBadge(
                                categoryName = cat.name,
                                iconKey = cat.iconKey,
                                color = Color(cat.colour),
                                size = 38.dp
                            )
                            Column {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = scheme.onSurface
                                )
                                Text(
                                    text = cat.kind.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (cat.kind == TransactionType.EXPENSE) ExpenseColor else IncomeColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(onClick = { categoryToArchive = cat }) {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = "Archive Category",
                                tint = scheme.onSurfaceVariant.copy(alpha = 0.65f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Add Category Button
        Button(
            onClick = onAddCategoryClick,
            modifier = Modifier.fillMaxWidth(),
            shape = VyxelShapeTokens.Pill,
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create New Category", fontWeight = FontWeight.Bold)
        }
    }

    // Archive Confirmation
    categoryToArchive?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToArchive = null },
            title = { Text("Archive Category?") },
            text = { Text("Are you sure you want to archive \"${cat.name}\"? It will be hidden from new transaction entries.") },
            confirmButton = {
                Button(
                    onClick = {
                        onArchiveCategory(cat.id, true)
                        categoryToArchive = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.error),
                    shape = VyxelShapeTokens.Pill
                ) {
                    Text("Archive", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToArchive = null }) {
                    Text("Cancel")
                }
            },
            shape = VyxelShapeTokens.Card
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Manage Accounts Sheet Content
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ManageAccountsSheetContent(
    accounts: List<Account>,
    currencySymbol: String,
    onAddAccountClick: () -> Unit,
    onEditAccountClick: (Account) -> Unit,
    onArchiveAccount: (Long, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var accountToArchive by remember { mutableStateOf<Account?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Wallets & Accounts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Text(
                    text = "${accounts.size} payment accounts configured",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Accounts List
        LazyColumn(
            modifier = Modifier
                .weight(1f, fill = false)
                .height(340.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(accounts, key = { it.id }) { acc ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditAccountClick(acc) },
                    shape = VyxelShapeTokens.CardSmall,
                    colors = CardDefaults.cardColors(
                        containerColor = scheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = VyxelShapeTokens.IconSmall,
                                color = scheme.primaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = getAccountIcon(acc.type),
                                        contentDescription = null,
                                        tint = scheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = scheme.onSurface
                                )
                                Text(
                                    text = acc.type.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = MoneyFormatter.formatPaise(acc.currentBalanceMinor, currencySymbol = currencySymbol),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = scheme.onSurface
                                )
                                Text(
                                    text = "Tap to edit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.primary
                                )
                            }

                            IconButton(onClick = { accountToArchive = acc }) {
                                Icon(
                                    imageVector = Icons.Default.Archive,
                                    contentDescription = "Archive Account",
                                    tint = scheme.onSurfaceVariant.copy(alpha = 0.65f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Add Account Button
        Button(
            onClick = onAddAccountClick,
            modifier = Modifier.fillMaxWidth(),
            shape = VyxelShapeTokens.Pill,
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Payment Account", fontWeight = FontWeight.Bold)
        }
    }

    // Archive Confirmation
    accountToArchive?.let { acc ->
        AlertDialog(
            onDismissRequest = { accountToArchive = null },
            title = { Text("Archive Account?") },
            text = { Text("Are you sure you want to archive \"${acc.name}\"? Its historical transactions will remain safe in your ledger.") },
            confirmButton = {
                Button(
                    onClick = {
                        onArchiveAccount(acc.id, true)
                        accountToArchive = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.error),
                    shape = VyxelShapeTokens.Pill
                ) {
                    Text("Archive", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { accountToArchive = null }) {
                    Text("Cancel")
                }
            },
            shape = VyxelShapeTokens.Card
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Create Category Dialog
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CreateCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedIconKey by remember { mutableStateOf("restaurant") }
    var selectedColor by remember { mutableLongStateOf(0xFFE53935L) }

    val availableIcons = remember {
        listOf(
            "restaurant", "shopping_cart", "directions_car", "receipt_long",
            "shopping_bag", "movie", "medical_services", "payments",
            "work", "trending_up"
        )
    }

    val availableColors = remember {
        listOf(
            0xFFE53935L, 0xFF43A047L, 0xFF1E88E5L, 0xFFFB8C00L,
            0xFF8E24AAL, 0xFF00ACC1L, 0xFFFDD835L, 0xFFD81B60L
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Category", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                // Type selector
                Text("Category Type", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = kind == TransactionType.EXPENSE,
                        onClick = { kind = TransactionType.EXPENSE },
                        label = { Text("Expense") },
                        shape = VyxelShapeTokens.Pill,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ExpenseColor.copy(alpha = 0.2f),
                            selectedLabelColor = ExpenseColor
                        )
                    )
                    FilterChip(
                        selected = kind == TransactionType.INCOME,
                        onClick = { kind = TransactionType.INCOME },
                        label = { Text("Income") },
                        shape = VyxelShapeTokens.Pill,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IncomeColor.copy(alpha = 0.2f),
                            selectedLabelColor = IncomeColor
                        )
                    )
                }

                // Color selector
                Text("Color Dot", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableColors.forEach { colorValue ->
                        val isSelected = selectedColor == colorValue
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorValue))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorValue },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Icon selector
                Text("Icon", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableIcons.forEach { iconKey ->
                        val isSelected = selectedIconKey == iconKey
                        val iconVector = resolveCategoryIcon(iconKey) ?: Icons.Default.Category
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { selectedIconKey = iconKey }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            Category(
                                name = name.trim(),
                                kind = kind,
                                iconKey = selectedIconKey,
                                colour = selectedColor
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                shape = VyxelShapeTokens.Pill
            ) {
                Text("Create", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = VyxelShapeTokens.Card
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Create Account Dialog
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CreateAccountDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.BANK) }
    var balanceText by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Payment Account", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name (e.g. HDFC Bank)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Text("Account Type", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccountType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name) },
                            shape = VyxelShapeTokens.Pill
                        )
                    }
                }

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Opening Balance ($currencySymbol)") },
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
                    if (name.isNotBlank()) {
                        val openingPaise = MoneyFormatter.parseToPaise(balanceText)
                        onSave(
                            Account(
                                name = name.trim(),
                                type = selectedType,
                                openingBalanceMinor = openingPaise,
                                currentBalanceMinor = openingPaise
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                shape = VyxelShapeTokens.Pill
            ) {
                Text("Save Account", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = VyxelShapeTokens.Card
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Edit Account Details Dialog
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EditAccountDetailsDialog(
    account: Account,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit
) {
    var name by remember { mutableStateOf(account.name) }
    var selectedType by remember { mutableStateOf(account.type) }
    var balanceText by remember {
        mutableStateOf(
            if (account.openingBalanceMinor == 0L) "0"
            else (account.openingBalanceMinor / 100.0).toString()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Account • ${account.name}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Text("Account Type", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccountType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name) },
                            shape = VyxelShapeTokens.Pill
                        )
                    }
                }

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Opening Balance ($currencySymbol)") },
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
                    if (name.isNotBlank()) {
                        val openingPaise = MoneyFormatter.parseToPaise(balanceText)
                        onSave(
                            account.copy(
                                name = name.trim(),
                                type = selectedType,
                                openingBalanceMinor = openingPaise
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                shape = VyxelShapeTokens.Pill
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = VyxelShapeTokens.Card
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AccountTypePill(icon: ImageVector, label: String) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = VyxelShapeTokens.Pill,
        color = scheme.surfaceContainerHigh.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(13.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = scheme.onSurface)
        }
    }
}

private fun getAccountIcon(type: AccountType): ImageVector = when (type) {
    AccountType.BANK -> Icons.Default.AccountBalance
    AccountType.CASH -> Icons.Default.Payments
    AccountType.WALLET -> Icons.Default.AccountBalanceWallet
    AccountType.CARD -> Icons.Default.CreditCard
    AccountType.LOAN -> Icons.Default.ReceiptLong
}
