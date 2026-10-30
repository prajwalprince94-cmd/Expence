package com.knowspends.app.ui.add

import android.app.DatePickerDialog
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.ui.components.CategoryBadge
import com.knowspends.app.ui.components.VyxelSegmentedRow
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.TransferColor
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.pressScale
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    var showNoteDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showTransferAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect {
            onNavigateUp()
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
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
                        viewModel.onEvent(AddTransactionEvent.OnTypeSelected(types[index]))
                    },
                    activeColor = typeColor,
                    activeContentColor = Color.White,
                    labelProvider = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                )
            }

            // ── 2. Hero Amount Display ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.formattedAmount,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = typeColor,
                    letterSpacing = (-1.0).sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ── 3. Context Pills Row (Account, Date, Note) ────────────────────
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
                    icon = Icons.Default.AccountBalanceWallet,
                    text = selectedAccount?.name ?: "Account",
                    onClick = { showAccountDialog = true }
                )

                // Transfer Target Account Pill (if transfer)
                if (uiState.type == TransactionType.TRANSFER) {
                    ContextPill(
                        icon = Icons.Default.SwapHoriz,
                        text = "To: ${selectedTransferAccount?.name ?: "Select Target"}",
                        onClick = { showTransferAccountDialog = true }
                    )
                }

                // Date Pill
                ContextPill(
                    icon = Icons.Default.CalendarToday,
                    text = dateFormatter.format(Date(uiState.dateMillis)),
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = uiState.dateMillis }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val newCal = Calendar.getInstance().apply {
                                    set(year, month, dayOfMonth)
                                }
                                viewModel.onEvent(AddTransactionEvent.OnDateSelected(newCal.timeInMillis))
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                )

                // Note Pill
                ContextPill(
                    icon = Icons.Default.Description,
                    text = if (uiState.note.isNotBlank()) uiState.note else "Note",
                    onClick = { showNoteDialog = true }
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
                onKeyPress = { viewModel.onEvent(AddTransactionEvent.OnKeypadPress(it)) },
                onBackspace = { viewModel.onEvent(AddTransactionEvent.OnBackspace) },
                onSave = { viewModel.onEvent(AddTransactionEvent.OnSave) },
                onSaveAndAddAnother = { viewModel.onEvent(AddTransactionEvent.OnSaveAndAddAnother) },
                isSaving = uiState.isSaving
            )
        }
    }

    // ── Note Modal Dialog ──────────────────────────────────────────────────────
    if (showNoteDialog) {
        var noteDraft by remember { mutableStateOf(uiState.note) }
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = {
                Text(
                    text = "Transaction Note",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = { noteDraft = it },
                    placeholder = { Text("e.g. Lunch with friends, groceries...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(AddTransactionEvent.OnNoteChanged(noteDraft))
                        showNoteDialog = false
                    },
                    shape = VyxelShapeTokens.Pill
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = VyxelShapeTokens.Card
        )
    }

    // ── Account Selector Dialog ────────────────────────────────────────────────
    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = {
                Text(
                    text = "Select Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.accounts.forEach { account ->
                        val isSelected = account.id == uiState.selectedAccountId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pressScale(0.96f)
                                .clip(VyxelShapeTokens.Card)
                                .background(if (isSelected) scheme.primaryContainer else scheme.surfaceContainerHigh)
                                .clickable {
                                    viewModel.onEvent(AddTransactionEvent.OnAccountSelected(account.id))
                                    showAccountDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = if (isSelected) scheme.primary else scheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = scheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("Close")
                }
            },
            shape = VyxelShapeTokens.Card
        )
    }

    // ── Transfer Target Account Selector Dialog ────────────────────────────────
    if (showTransferAccountDialog) {
        AlertDialog(
            onDismissRequest = { showTransferAccountDialog = false },
            title = {
                Text(
                    text = "Select Destination Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.accounts
                        .filter { it.id != uiState.selectedAccountId }
                        .forEach { account ->
                            val isSelected = account.id == uiState.selectedTransferAccountId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pressScale(0.96f)
                                    .clip(VyxelShapeTokens.Card)
                                    .background(if (isSelected) scheme.primaryContainer else scheme.surfaceContainerHigh)
                                    .clickable {
                                        viewModel.onEvent(AddTransactionEvent.OnTransferAccountSelected(account.id))
                                        showTransferAccountDialog = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = if (isSelected) scheme.primary else scheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = account.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = scheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTransferAccountDialog = false }) {
                    Text("Close")
                }
            },
            shape = VyxelShapeTokens.Card
        )
    }
}

@Composable
private fun ContextPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .pressScale(0.96f)
            .clip(VyxelShapeTokens.Pill)
            .background(scheme.surfaceContainerHigh)
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.5f), VyxelShapeTokens.Pill)
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
    }
}

@Composable
private fun VyxelNumericKeypad(
    onKeyPress: (Char) -> Unit,
    onBackspace: () -> Unit,
    onSave: () -> Unit,
    onSaveAndAddAnother: () -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

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
                                    if (char == '⌫') onBackspace() else onKeyPress(char)
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
                                    fontSize = 24.sp,
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
                    onClick = onSave,
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
                        text = "Save",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onSaveAndAddAnother,
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
                        text = "+ Another",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
