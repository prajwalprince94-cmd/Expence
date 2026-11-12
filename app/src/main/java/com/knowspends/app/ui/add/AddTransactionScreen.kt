package com.knowspends.app.ui.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowspends.app.domain.model.Account
import com.knowspends.app.domain.model.AccountType
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.util.MoneyFormatter
import com.knowspends.app.ui.components.CategoryBadge
import com.knowspends.app.ui.components.VyxelSegmentedRow
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.TransferColor
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.pressScale
import com.knowspends.app.util.VyxelHaptics
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }
    var showTransferAccountSheet by remember { mutableStateOf(false) }
    var showDetailsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.navigateUp) {
        if (uiState.navigateUp) {
            onNavigateUp()
            viewModel.onNavigatedUp()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val typeColor = when (uiState.type) {
        TransactionType.EXPENSE -> ExpenseColor
        TransactionType.INCOME -> IncomeColor
        TransactionType.TRANSFER -> TransferColor
    }

    val selectedAccount = uiState.accounts.find { it.id == uiState.selectedAccountId }
    val selectedTransferAccount = uiState.accounts.find { it.id == uiState.selectedTransferAccountId }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (uiState.transactionId != null) "Edit Transaction" else "Add Transaction",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            VyxelHaptics.click(context)
                            onNavigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.amountInput != "0") {
                        IconButton(
                            onClick = {
                                VyxelHaptics.heavy(context)
                                viewModel.onEvent(AddTransactionEvent.OnClear)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = scheme.onSurfaceVariant
                            )
                        }
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
            // ── 1. Type Switcher (Expense | Income | Transfer) ─────────────────
            val types = listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER)
            val selectedTypeIndex = types.indexOf(uiState.type).coerceAtLeast(0)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                VyxelSegmentedRow(
                    items = types,
                    selectedIndex = selectedTypeIndex,
                    onSelectIndex = { index ->
                        VyxelHaptics.tick(context)
                        viewModel.onEvent(AddTransactionEvent.OnTypeSelected(types[index]))
                    },
                    activeColor = typeColor,
                    activeContentColor = Color.White,
                    labelProvider = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                )
            }

            // ── 2. Hero Monospace Tabular Amount Display ──────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.formattedAmount,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontFeatureSettings = "tnum"
                    ),
                    color = if (uiState.amountInput == "0") scheme.onSurfaceVariant.copy(alpha = 0.5f) else typeColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ── 3. Context Pills Row (Account, Transfer, Date, Note/Payee) ────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Account Pill
                ContextPill(
                    icon = getAccountIcon(selectedAccount?.type),
                    text = selectedAccount?.name ?: "Select Account",
                    badge = selectedAccount?.let {
                        MoneyFormatter.formatPaise(it.currentBalanceMinor, includeSymbol = true)
                    },
                    onClick = {
                        VyxelHaptics.tick(context)
                        showAccountSheet = true
                    }
                )

                // Transfer Target Account Pill (if transfer)
                if (uiState.type == TransactionType.TRANSFER) {
                    ContextPill(
                        icon = Icons.Default.SwapHoriz,
                        text = "To: ${selectedTransferAccount?.name ?: "Target A/c"}",
                        onClick = {
                            VyxelHaptics.tick(context)
                            showTransferAccountSheet = true
                        }
                    )
                }

                // Date Pill
                ContextPill(
                    icon = Icons.Default.CalendarToday,
                    text = dateFormatter.format(Date(uiState.dateMillis)),
                    onClick = {
                        VyxelHaptics.tick(context)
                        showDatePickerDialog = true
                    }
                )

                // Details (Payee / Note) Pill
                val detailsLabel = when {
                    uiState.payee.isNotBlank() && uiState.note.isNotBlank() -> "${uiState.payee} • ${uiState.note}"
                    uiState.payee.isNotBlank() -> uiState.payee
                    uiState.note.isNotBlank() -> uiState.note
                    else -> "Note / Payee"
                }
                ContextPill(
                    icon = Icons.Default.Description,
                    text = detailsLabel,
                    onClick = {
                        VyxelHaptics.tick(context)
                        showDetailsSheet = true
                    }
                )
            }

            // ── 4. Category Quick Rail (For Expense & Income) ─────────────────
            if (uiState.type != TransactionType.TRANSFER && uiState.categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    uiState.categories.forEach { category ->
                        val isSelected = uiState.selectedCategoryId == category.id
                        val categoryBg = if (isSelected) {
                            scheme.primaryContainer
                        } else {
                            scheme.surfaceContainerHigh.copy(alpha = 0.65f)
                        }
                        val categoryBorder = if (isSelected) {
                            BorderStroke(1.2.dp, scheme.primary)
                        } else {
                            BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f))
                        }

                        Row(
                            modifier = Modifier
                                .pressScale(0.96f)
                                .clip(VyxelShapeTokens.Pill)
                                .background(categoryBg)
                                .border(categoryBorder, VyxelShapeTokens.Pill)
                                .clickable {
                                    VyxelHaptics.tick(context)
                                    viewModel.onEvent(AddTransactionEvent.OnCategorySelected(category.id))
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CategoryBadge(
                                categoryName = category.name,
                                iconKey = category.iconKey,
                                color = Color(category.colour),
                                size = 16.dp
                            )
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── 5. Tactile Numeric Keypad ──────────────────────────────────────
            VyxelNumericKeypad(
                onKeyPress = { char ->
                    viewModel.onEvent(AddTransactionEvent.OnKeypadPress(char))
                },
                onBackspace = {
                    viewModel.onEvent(AddTransactionEvent.OnBackspace)
                },
                onSave = {
                    viewModel.onEvent(AddTransactionEvent.OnSave)
                },
                onSaveAndAddAnother = {
                    viewModel.onEvent(AddTransactionEvent.OnSaveAndAddAnother)
                },
                isSaving = uiState.isSaving,
                isEditing = uiState.transactionId != null
            )
        }
    }

    // ── Modern Compose DatePickerDialog ───────────────────────────────────────
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            // Preserve local calendar date correctly
                            val calUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = utcMillis
                            }
                            val localCal = Calendar.getInstance().apply {
                                set(
                                    calUtc.get(Calendar.YEAR),
                                    calUtc.get(Calendar.MONTH),
                                    calUtc.get(Calendar.DAY_OF_MONTH)
                                )
                            }
                            viewModel.onEvent(AddTransactionEvent.OnDateSelected(localCal.timeInMillis))
                            VyxelHaptics.click(context)
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("Select", fontWeight = FontWeight.Bold, color = scheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = VyxelShapeTokens.Card
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }

    // ── Account Selection BottomSheet ──────────────────────────────────────────
    if (showAccountSheet) {
        AccountSelectionSheet(
            title = "Select Source Account",
            subtitle = "Choose the account used for this transaction",
            accounts = uiState.accounts,
            selectedAccountId = uiState.selectedAccountId,
            onSelectAccount = { accountId ->
                VyxelHaptics.click(context)
                viewModel.onEvent(AddTransactionEvent.OnAccountSelected(accountId))
                showAccountSheet = false
            },
            onDismiss = { showAccountSheet = false }
        )
    }

    // ── Transfer Destination Account BottomSheet ───────────────────────────────
    if (showTransferAccountSheet) {
        AccountSelectionSheet(
            title = "Select Destination Account",
            subtitle = "Choose where the funds are transferred to",
            accounts = uiState.accounts.filter { it.id != uiState.selectedAccountId },
            selectedAccountId = uiState.selectedTransferAccountId,
            onSelectAccount = { accountId ->
                VyxelHaptics.click(context)
                viewModel.onEvent(AddTransactionEvent.OnTransferAccountSelected(accountId))
                showTransferAccountSheet = false
            },
            onDismiss = { showTransferAccountSheet = false }
        )
    }

    // ── Details (Payee / Note) BottomSheet ─────────────────────────────────────
    if (showDetailsSheet) {
        DetailsInputSheet(
            initialPayee = uiState.payee,
            initialNote = uiState.note,
            onDone = { newPayee, newNote ->
                VyxelHaptics.click(context)
                viewModel.onEvent(AddTransactionEvent.OnPayeeChanged(newPayee))
                viewModel.onEvent(AddTransactionEvent.OnNoteChanged(newNote))
                showDetailsSheet = false
            },
            onDismiss = { showDetailsSheet = false }
        )
    }
}

@Composable
private fun ContextPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .pressScale(0.96f)
            .clip(VyxelShapeTokens.Pill)
            .background(scheme.surfaceContainerHigh.copy(alpha = 0.65f))
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.45f), VyxelShapeTokens.Pill)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = scheme.primary,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (badge != null) {
            Surface(
                shape = VyxelShapeTokens.Pill,
                color = scheme.surfaceContainerHighest.copy(alpha = 0.7f)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontFeatureSettings = "tnum",
                        fontSize = 10.sp
                    ),
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSelectionSheet(
    title: String,
    subtitle: String,
    accounts: List<Account>,
    selectedAccountId: Long?,
    onSelectAccount: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scheme = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = scheme.surfaceContainerLowest,
        shape = VyxelShapeTokens.CardLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(accounts, key = { it.id }) { account ->
                    val isSelected = account.id == selectedAccountId
                    val bg = if (isSelected) scheme.primaryContainer.copy(alpha = 0.45f) else scheme.surfaceContainerHigh.copy(alpha = 0.45f)
                    val border = if (isSelected) BorderStroke(1.5.dp, scheme.primary) else BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.35f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressScale(0.97f)
                            .clip(VyxelShapeTokens.Card)
                            .background(bg)
                            .border(border, VyxelShapeTokens.Card)
                            .clickable { onSelectAccount(account.id) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) scheme.primary else scheme.surfaceContainerHighest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getAccountIcon(account.type),
                                contentDescription = null,
                                tint = if (isSelected) scheme.onPrimary else scheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = scheme.onSurface
                            )
                            Text(
                                text = MoneyFormatter.formatPaise(account.currentBalanceMinor, includeSymbol = true),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = scheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = scheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsInputSheet(
    initialPayee: String,
    initialNote: String,
    onDone: (payee: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scheme = MaterialTheme.colorScheme
    var payeeDraft by remember { mutableStateOf(initialPayee) }
    var noteDraft by remember { mutableStateOf(initialNote) }

    val quickTags = listOf("Groceries", "Coffee", "Dining", "Fuel", "Bills", "Shopping", "Entertainment", "Transit")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = scheme.surfaceContainerLowest,
        shape = VyxelShapeTokens.CardLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Transaction Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )

            OutlinedTextField(
                value = payeeDraft,
                onValueChange = { payeeDraft = it },
                label = { Text("Payee / Merchant") },
                placeholder = { Text("e.g. Starbucks, Uber, Swiggy") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = scheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = VyxelShapeTokens.Card
            )

            OutlinedTextField(
                value = noteDraft,
                onValueChange = { noteDraft = it },
                label = { Text("Note / Description") },
                placeholder = { Text("e.g. Lunch with team, monthly subscription") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = scheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = VyxelShapeTokens.Card
            )

            // Quick suggestion tags
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Quick Suggestion",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickTags.forEach { tag ->
                        Surface(
                            shape = VyxelShapeTokens.Pill,
                            color = scheme.surfaceContainerHigh.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .pressScale(0.96f)
                                .clickable {
                                    if (noteDraft.isBlank()) {
                                        noteDraft = tag
                                    } else {
                                        noteDraft = "$noteDraft, $tag"
                                    }
                                }
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = { onDone(payeeDraft.trim(), noteDraft.trim()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 50.dp)
                    .pressScale(0.96f),
                shape = VyxelShapeTokens.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
            ) {
                Text(
                    text = "Apply Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun VyxelNumericKeypad(
    onKeyPress: (Char) -> Unit,
    onBackspace: () -> Unit,
    onSave: () -> Unit,
    onSaveAndAddAnother: () -> Unit,
    isSaving: Boolean,
    isEditing: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = scheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val rows = listOf(
                listOf('1', '2', '3'),
                listOf('4', '5', '6'),
                listOf('7', '8', '9'),
                listOf('.', '0', '⌫')
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { char ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 54.dp)
                                .pressScale(0.95f)
                                .clip(VyxelShapeTokens.Card)
                                .background(scheme.surfaceContainerHigh.copy(alpha = 0.65f))
                                .border(1.dp, scheme.outlineVariant.copy(alpha = 0.5f), VyxelShapeTokens.Card)
                                .clickable {
                                    if (char == '⌫') {
                                        VyxelHaptics.heavy(context)
                                        onBackspace()
                                    } else {
                                        VyxelHaptics.click(context)
                                        onKeyPress(char)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (char == '⌫') {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Backspace",
                                    tint = scheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(
                                    text = char.toString(),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    fontWeight = FontWeight.SemiBold,
                                    color = scheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Action Row: Save & + Another
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        VyxelHaptics.success(context)
                        onSave()
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 50.dp)
                        .pressScale(0.96f),
                    shape = VyxelShapeTokens.Pill,
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEditing) "Update Transaction" else "Save Transaction",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!isEditing) {
                    OutlinedButton(
                        onClick = {
                            VyxelHaptics.success(context)
                            onSaveAndAddAnother()
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 50.dp)
                            .pressScale(0.96f),
                        shape = VyxelShapeTokens.Pill,
                        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Another",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

private fun getAccountIcon(type: AccountType?): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        AccountType.CASH -> Icons.Default.Payments
        AccountType.BANK -> Icons.Default.AccountBalance
        AccountType.WALLET -> Icons.Default.AccountBalanceWallet
        AccountType.CARD -> Icons.Default.CreditCard
        AccountType.LOAN -> Icons.Default.TrendingDown
        null -> Icons.Default.AccountBalanceWallet
    }
}
