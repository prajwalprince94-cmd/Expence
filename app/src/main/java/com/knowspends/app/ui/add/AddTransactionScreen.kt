package com.knowspends.app.ui.add

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.knowspends.app.ui.components.CategoryBadge
import com.knowspends.app.ui.theme.ExpressiveShapes
import com.knowspends.app.ui.theme.pressScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.TransferColor
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (uiState.transactionId != null) "Edit Transaction" else "Add Transaction",
                        style = MaterialTheme.typography.titleMedium
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
            // Transaction Type Tabs (Expense, Income, Transfer)
            val tabs = listOf(
                TransactionType.EXPENSE to "Expense",
                TransactionType.INCOME to "Income",
                TransactionType.TRANSFER to "Transfer"
            )
            val selectedTabIndex = tabs.indexOfFirst { it.first == uiState.type }

            TabRow(
                selectedTabIndex = selectedTabIndex.coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, (type, label) ->
                    val color = when (type) {
                        TransactionType.EXPENSE -> ExpenseColor
                        TransactionType.INCOME -> IncomeColor
                        TransactionType.TRANSFER -> TransferColor
                    }
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.onEvent(AddTransactionEvent.OnTypeSelected(type)) },
                        text = {
                            Text(
                                text = label,
                                color = if (selectedTabIndex == index) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Scrollable Options Content (Amount, Accounts, Categories, Date, Note)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Large Formatted Amount Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AMOUNT",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.formattedAmount,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (uiState.type) {
                                    TransactionType.EXPENSE -> ExpenseColor
                                    TransactionType.INCOME -> IncomeColor
                                    TransactionType.TRANSFER -> TransferColor
                                }
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Account Selection Row
                Text(
                    text = if (uiState.type == TransactionType.TRANSFER) "From Account" else "Account",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.accounts.forEach { account ->
                        FilterChip(
                            selected = uiState.selectedAccountId == account.id,
                            onClick = { viewModel.onEvent(AddTransactionEvent.OnAccountSelected(account.id)) },
                            label = { Text(account.name) }
                        )
                    }
                }

                // Transfer Destination Account
                if (uiState.type == TransactionType.TRANSFER) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To Account",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.accounts
                            .filter { it.id != uiState.selectedAccountId }
                            .forEach { account ->
                                FilterChip(
                                    selected = uiState.selectedTransferAccountId == account.id,
                                    onClick = { viewModel.onEvent(AddTransactionEvent.OnTransferAccountSelected(account.id)) },
                                    label = { Text(account.name) }
                                )
                            }
                    }
                }

                // Category Chips (for Expense & Income)
                if (uiState.type != TransactionType.TRANSFER && uiState.categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.categories.forEach { category ->
                            val isSelected = uiState.selectedCategoryId == category.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onEvent(AddTransactionEvent.OnCategorySelected(category.id)) },
                                leadingIcon = {
                                    CategoryBadge(
                                        categoryName = category.name,
                                        iconKey = category.iconKey,
                                        color = Color(category.colour),
                                        size = 18.dp
                                    )
                                },
                                label = { Text(category.name) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Date Picker & Note Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date Button
                    FilterChip(
                        selected = false,
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
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Date",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(dateFormatter.format(Date(uiState.dateMillis))) }
                    )

                    // Note text field
                    OutlinedTextField(
                        value = uiState.note,
                        onValueChange = { viewModel.onEvent(AddTransactionEvent.OnNoteChanged(it)) },
                        placeholder = { Text("Note (optional)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            // Built-in Instant Numeric Keypad (Under 5 seconds & 3 taps)
            NumericKeypad(
                onKeyPress = { viewModel.onEvent(AddTransactionEvent.OnKeypadPress(it)) },
                onBackspace = { viewModel.onEvent(AddTransactionEvent.OnBackspace) },
                onSave = { viewModel.onEvent(AddTransactionEvent.OnSave) },
                onSaveAndAddAnother = { viewModel.onEvent(AddTransactionEvent.OnSaveAndAddAnother) },
                isSaving = uiState.isSaving
            )
        }
    }
}

@Composable
private fun NumericKeypad(
    onKeyPress: (Char) -> Unit,
    onBackspace: () -> Unit,
    onSave: () -> Unit,
    onSaveAndAddAnother: () -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                .defaultMinSize(minHeight = 52.dp)
                                .pressScale(0.94f)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .clickable {
                                    if (char == '⌫') onBackspace() else onKeyPress(char)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (char == '⌫') {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Backspace",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = char.toString(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Expressive Action Buttons: Save & Quick Add Another
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 52.dp)
                        .pressScale(0.97f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                OutlinedButton(
                    onClick = onSaveAndAddAnother,
                    enabled = !isSaving,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 52.dp)
                        .pressScale(0.97f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+ Another",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}
