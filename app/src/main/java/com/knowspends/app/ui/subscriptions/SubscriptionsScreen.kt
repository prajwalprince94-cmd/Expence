package com.knowspends.app.ui.subscriptions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowspends.app.domain.model.RecurrenceFrequency
import com.knowspends.app.domain.model.RecurringRule
import com.knowspends.app.domain.util.MoneyFormatter
import com.knowspends.app.ui.components.CategoryBadge
import com.knowspends.app.ui.components.VyxelExpressiveChip
import com.knowspends.app.ui.components.bouncyClickable
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.VyxelTypographyTokens
import com.knowspends.app.ui.theme.auroraGlow
import com.knowspends.app.ui.theme.pressScale
import com.knowspends.app.ui.theme.progressiveScrollBlur
import com.knowspends.app.util.VyxelHaptics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SubscriptionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Recurring & Subscriptions",
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
                },
                actions = {
                    IconButton(onClick = { viewModel.showAddEditDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Subscription",
                            tint = scheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .progressiveScrollBlur(topBlur = 12.dp, bottomBlur = 48.dp, scrimColor = scheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Hero Burn Rate Card (Copilot Money Benchmark Aesthetic) ────────
            item(key = "hero_burn_rate") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .auroraGlow(alpha = 0.15f),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = scheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MONTHLY RECURRING COMMITMENTS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                                letterSpacing = VyxelTypographyTokens.TrackingUppercase
                            )

                            Surface(
                                shape = VyxelShapeTokens.Pill,
                                color = scheme.primaryContainer,
                                border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "${uiState.activeSubscriptionsCount} Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Monumental Burn Amount
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = uiState.formattedMonthlyBurn,
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                                color = ExpenseColor
                            )
                            Text(
                                text = "Fixed monthly recurring burn across all active subscriptions",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }

                        // Next Due Date Badge (if any active rule exists)
                        uiState.nextDueSubscription?.let { nextDue ->
                            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.35f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(scheme.primary.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = scheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                val daysText = when (val d = uiState.daysUntilNextDue) {
                                    null, 0 -> "Due today"
                                    1 -> "Due tomorrow"
                                    else -> "Due in $d days"
                                }
                                Text(
                                    text = "Next: ${nextDue.payee} (${MoneyFormatter.formatPaise(nextDue.amountMinor)}) • $daysText",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = scheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // ── Filter Carousel ──────────────────────────────────────────────
            item(key = "filter_row") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("All", "Monthly", "Yearly", "Active").forEach { filter ->
                        VyxelExpressiveChip(
                            selected = uiState.selectedFilter == filter,
                            onClick = { viewModel.onFilterSelected(filter) },
                            label = filter
                        )
                    }
                }
            }

            // ── Subscriptions & Bills List ────────────────────────────────────
            if (uiState.filteredRules.isEmpty()) {
                item(key = "empty_state") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = VyxelShapeTokens.Card,
                        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
                        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(scheme.surfaceContainerHighest),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    tint = scheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "No Subscriptions Tracked Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface
                            )
                            Text(
                                text = "Keep tabs on Netflix, Spotify, broadband, rent, EMIs, and SIP investments automatically.",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { viewModel.showAddEditDialog() },
                                shape = VyxelShapeTokens.Pill
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Add First Subscription")
                            }
                        }
                    }
                }
            } else {
                items(
                    items = uiState.filteredRules,
                    key = { it.id }
                ) { rule ->
                    SubscriptionItemCard(
                        rule = rule,
                        onToggleActive = { isActive ->
                            VyxelHaptics.tick(context)
                            viewModel.toggleSubscriptionActive(rule.id, isActive)
                        },
                        onEdit = { viewModel.showAddEditDialog(rule) },
                        onDelete = {
                            VyxelHaptics.click(context)
                            viewModel.deleteSubscription(rule.id)
                        }
                    )
                }
            }

            item(key = "bottom_clearance") {
                Spacer(Modifier.height(80.dp))
            }
        }
    }

    // ── Add / Edit Subscription Dialog ───────────────────────────────────────
    if (uiState.showAddEditDialog) {
        AddEditSubscriptionDialog(
            editingRule = uiState.editingRule,
            accounts = uiState.accounts,
            categories = uiState.categories,
            onDismiss = { viewModel.dismissDialog() },
            onSave = { id, payee, amountMinor, accountId, categoryId, freq, day, autoPost ->
                viewModel.saveSubscription(id, payee, amountMinor, accountId, categoryId, freq, day, autoPost)
            }
        )
    }
}

@Composable
fun SubscriptionItemCard(
    rule: RecurringRule,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val dueDateStr = remember(rule.nextDueDate) { dateFormat.format(Date(rule.nextDueDate)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(0.99f)
            .clickable(onClick = onEdit),
        shape = VyxelShapeTokens.Card,
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isActive) scheme.surfaceContainerLowest else scheme.surfaceContainerLowest.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = if (rule.isActive) 0.5f else 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category/Monogram Badge
            CategoryBadge(
                categoryName = rule.categoryName ?: rule.payee,
                color = rule.categoryColour?.let { Color(it) } ?: scheme.primary,
                size = 40.dp
            )

            Spacer(Modifier.width(12.dp))

            // Middle Column: Payee, Frequency, Account
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = rule.payee.ifBlank { rule.categoryName ?: "Subscription" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (rule.isActive) scheme.onSurface else scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = VyxelShapeTokens.Pill,
                        color = scheme.surfaceContainerHighest.copy(alpha = 0.7f),
                        modifier = Modifier.height(18.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                            Text(
                                text = rule.frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = scheme.primary
                            )
                        }
                    }

                    Text(
                        text = "Due $dueDateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Right Column: Amount & Active Switch
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = MoneyFormatter.formatPaise(rule.amountMinor, includeSymbol = true),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (rule.isActive) ExpenseColor else scheme.onSurfaceVariant
                )

                Switch(
                    checked = rule.isActive,
                    onCheckedChange = onToggleActive,
                    modifier = Modifier.size(width = 38.dp, height = 24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionDialog(
    editingRule: RecurringRule?,
    accounts: List<com.knowspends.app.domain.model.Account>,
    categories: List<com.knowspends.app.domain.model.Category>,
    onDismiss: () -> Unit,
    onSave: (id: Long, payee: String, amountMinor: Long, accountId: Long, categoryId: Long?, freq: RecurrenceFrequency, dayOfMonth: Int, autoPost: Boolean) -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    var payee by remember { mutableStateOf(editingRule?.payee ?: "") }
    var amountStr by remember {
        mutableStateOf(
            if (editingRule != null) (editingRule.amountMinor / 100).toString() else ""
        )
    }
    var selectedAccountId by remember {
        mutableStateOf(editingRule?.accountId ?: accounts.firstOrNull()?.id ?: 0L)
    }
    var selectedCategoryId by remember {
        mutableStateOf(editingRule?.categoryId ?: categories.firstOrNull()?.id)
    }
    var selectedFrequency by remember {
        mutableStateOf(editingRule?.frequency ?: RecurrenceFrequency.MONTHLY)
    }
    var dayOfMonth by remember {
        mutableIntStateOf(editingRule?.dayOfMonth ?: 1)
    }
    var autoPost by remember {
        mutableStateOf(editingRule?.autoPost ?: true)
    }

    val quickPayees = listOf(
        "Netflix" to "Entertainment",
        "Spotify" to "Entertainment",
        "Amazon Prime" to "Entertainment",
        "YouTube Premium" to "Entertainment",
        "Airtel Wi-Fi" to "Bills & Utilities",
        "Gym Membership" to "Health & Fitness",
        "House Rent" to "Housing",
        "Mutual Fund SIP" to "Investments"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = scheme.surfaceContainerLow,
        title = {
            Text(
                text = if (editingRule != null) "Edit Subscription" else "New Recurring Commitment",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick suggestion chips
                if (editingRule == null) {
                    Text(
                        text = "Quick Presets:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickPayees.forEach { (presetName, presetCat) ->
                            FilterChip(
                                selected = payee == presetName,
                                onClick = {
                                    payee = presetName
                                    categories.find { it.name.contains(presetCat, ignoreCase = true) }?.let {
                                        selectedCategoryId = it.id
                                    }
                                },
                                label = { Text(presetName) },
                                shape = VyxelShapeTokens.Pill
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = payee,
                    onValueChange = { payee = it },
                    label = { Text("Service or Bill Name") },
                    placeholder = { Text("e.g. Netflix, Rent, Spotify") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                // Frequency Selector
                Text(
                    text = "Recurrence Frequency",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        RecurrenceFrequency.MONTHLY to "Monthly",
                        RecurrenceFrequency.YEARLY to "Yearly",
                        RecurrenceFrequency.WEEKLY to "Weekly"
                    ).forEach { (freq, label) ->
                        FilterChip(
                            selected = selectedFrequency == freq,
                            onClick = { selectedFrequency = freq },
                            label = { Text(label) },
                            shape = VyxelShapeTokens.Pill
                        )
                    }
                }

                // Day of Month
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Due Day of Month:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(1, 5, 10, 15, 20, 25, 28).forEach { d ->
                            Surface(
                                onClick = { dayOfMonth = d },
                                shape = CircleShape,
                                color = if (dayOfMonth == d) scheme.primary else scheme.surfaceContainerHighest,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$d",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dayOfMonth == d) scheme.onPrimary else scheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Account Selection
                if (accounts.isNotEmpty()) {
                    Text(
                        text = "Charge from Account:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accounts.forEach { acc ->
                            FilterChip(
                                selected = selectedAccountId == acc.id,
                                onClick = { selectedAccountId = acc.id },
                                label = { Text(acc.name) },
                                shape = VyxelShapeTokens.Pill
                            )
                        }
                    }
                }

                // Auto-Post Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Record Transaction",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Automatically logs transaction to your ledger when due date arrives",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = autoPost, onCheckedChange = { autoPost = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountPaise = runCatching { (amountStr.toDouble() * 100).toLong() }.getOrDefault(0L)
                    if (payee.isNotBlank() && amountPaise > 0) {
                        onSave(
                            editingRule?.id ?: 0L,
                            payee,
                            amountPaise,
                            selectedAccountId,
                            selectedCategoryId,
                            selectedFrequency,
                            dayOfMonth,
                            autoPost
                        )
                    }
                },
                enabled = payee.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0.0
            ) {
                Text(if (editingRule != null) "Save Changes" else "Add Subscription")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
