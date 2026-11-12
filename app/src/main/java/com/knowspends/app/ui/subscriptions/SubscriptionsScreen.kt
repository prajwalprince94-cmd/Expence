package com.knowspends.app.ui.subscriptions

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowspends.app.domain.model.Account
import com.knowspends.app.domain.model.Category
import com.knowspends.app.domain.model.RecurrenceFrequency
import com.knowspends.app.domain.model.RecurringRule
import com.knowspends.app.domain.util.MoneyFormatter
import com.knowspends.app.ui.components.AceternitySpotlightCard
import com.knowspends.app.ui.components.CategoryBadge
import com.knowspends.app.ui.components.VyxelExpressiveChip
import com.knowspends.app.ui.components.bouncyClickable
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.VyxelTypographyTokens
import com.knowspends.app.ui.theme.pressScale
import com.knowspends.app.ui.theme.progressiveScrollBlur
import com.knowspends.app.util.VyxelHaptics
import java.text.SimpleDateFormat
import java.util.Calendar
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
    val haptic = LocalHapticFeedback.current

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
                    IconButton(onClick = {
                        VyxelHaptics.tick(context)
                        onNavigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Surface(
                        onClick = {
                            VyxelHaptics.click(context)
                            viewModel.showAddEditDialog()
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = scheme.primaryContainer,
                        border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Commitment",
                                tint = scheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "New",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onPrimaryContainer
                            )
                        }
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
            // ── Aceternity Spotlight Hero Card ────────────────────────────────
            item(key = "hero_burn_rate") {
                AceternitySpotlightCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainer),
                    borderColor = scheme.outlineVariant.copy(alpha = 0.45f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header row: Tracked uppercase title + active badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MONTHLY RECURRING COMMITMENT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                                letterSpacing = VyxelTypographyTokens.TrackingUppercase
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = scheme.primaryContainer,
                                border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "${uiState.activeSubscriptionsCount} Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Prominent Monospaced Tabular Burn Amount
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = uiState.formattedMonthlyBurn,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = ExpenseColor
                            )
                            Text(
                                text = "Projected ${uiState.formattedYearlyProjection} / yr across active recurring commitments",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant.copy(alpha = 0.85f)
                            )
                        }

                        // Next Due Date Inset Highlight (if active rule exists)
                        uiState.nextDueSubscription?.let { nextDue ->
                            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.25f))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = scheme.surfaceContainerHighest.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(scheme.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = null,
                                                tint = scheme.primary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                        Text(
                                            text = "Next: ${nextDue.payee}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = scheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    val days = uiState.daysUntilNextDue ?: 0
                                    val countdownText = when {
                                        days <= 0 -> "Due Today"
                                        days == 1 -> "Tomorrow"
                                        else -> "In $days days"
                                    }
                                    val badgeColor = when {
                                        days <= 0 -> ExpenseColor
                                        days == 1 -> scheme.primary
                                        else -> scheme.onSurfaceVariant
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (days <= 0) ExpenseColor.copy(alpha = 0.12f) else scheme.surfaceContainerHighest,
                                        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            text = "$countdownText • ${MoneyFormatter.formatPaise(nextDue.amountMinor)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Upcoming Renewal Timeline Strip ──────────────────────────────
            if (uiState.upcomingRenewals.isNotEmpty()) {
                item(key = "upcoming_renewals_section") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "UPCOMING RENEWALS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                            letterSpacing = VyxelTypographyTokens.TrackingUppercase,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(
                                items = uiState.upcomingRenewals.take(8),
                                key = { "upcoming_${it.id}" }
                            ) { rule ->
                                UpcomingRenewalCard(
                                    rule = rule,
                                    onClick = { viewModel.showAddEditDialog(rule) }
                                )
                            }
                        }
                    }
                }
            }

            // ── Filter Segmented Carousel ─────────────────────────────────────
            item(key = "filter_row") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("All", "Active", "Monthly", "Yearly", "Paused").forEach { filter ->
                        VyxelExpressiveChip(
                            selected = uiState.selectedFilter == filter,
                            onClick = {
                                VyxelHaptics.tick(context)
                                viewModel.onFilterSelected(filter)
                            },
                            label = filter
                        )
                    }
                }
            }

            // ── Subscriptions & Commitments List ──────────────────────────────
            if (uiState.filteredRules.isEmpty()) {
                item(key = "empty_state") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
                        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(scheme.surfaceContainerHighest),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    tint = scheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                text = if (uiState.selectedFilter == "All") "No Subscriptions Tracked Yet" else "No ${uiState.selectedFilter} Commitments",
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
                                onClick = {
                                    VyxelHaptics.click(context)
                                    viewModel.showAddEditDialog()
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Add First Commitment")
                            }
                        }
                    }
                }
            } else {
                items(
                    items = uiState.filteredRules,
                    key = { it.id }
                ) { rule ->
                    SubscriptionSwipeableItemCard(
                        rule = rule,
                        onToggleActive = { isActive ->
                            VyxelHaptics.tick(context)
                            viewModel.toggleSubscriptionActive(rule.id, isActive)
                        },
                        onEdit = {
                            VyxelHaptics.click(context)
                            viewModel.showAddEditDialog(rule)
                        },
                        onDelete = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deleteSubscription(rule.id)
                        }
                    )
                }
            }

            item(key = "bottom_clearance") {
                Spacer(Modifier.height(84.dp))
            }
        }
    }

    // ── Apple-Grade Modal Bottom Sheet for Add/Edit ──────────────────────────
    if (uiState.showAddEditDialog) {
        AddEditSubscriptionSheet(
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

/**
 * Compact chronological renewal card for the top upcoming timeline carousel.
 */
@Composable
private fun UpcomingRenewalCard(
    rule: RecurringRule,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val diff = rule.nextDueDate - System.currentTimeMillis()
    val days = maxOf(0, (diff / (1000 * 60 * 60 * 24)).toInt())

    val (countdownLabel, badgeBg, badgeTextColor) = when {
        days <= 0 -> Triple("TODAY", ExpenseColor.copy(alpha = 0.15f), ExpenseColor)
        days == 1 -> Triple("TOMORROW", scheme.primary.copy(alpha = 0.15f), scheme.primary)
        days in 2..7 -> Triple("IN ${days}D", scheme.surfaceContainerHighest, scheme.primary)
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            Triple(sdf.format(Date(rule.nextDueDate)).uppercase(), scheme.surfaceContainerHighest, scheme.onSurfaceVariant)
        }
    }

    Card(
        modifier = modifier
            .width(136.dp)
            .pressScale(0.98f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(
                    categoryName = rule.categoryName ?: rule.payee,
                    iconKey = null,
                    color = rule.categoryColour?.let { Color(it) } ?: scheme.primary,
                    size = 30.dp
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badgeBg,
                    border = BorderStroke(0.5.dp, badgeTextColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = countdownLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        fontSize = 9.sp
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = rule.payee.ifBlank { rule.categoryName ?: "Subscription" },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = MoneyFormatter.formatPaise(rule.amountMinor, includeSymbol = true),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = ExpenseColor
                )
            }
        }
    }
}

/**
 * Swipeable Subscription Item Card with bidirectional gestures:
 * - Swipe right: Edit (springs back, triggers edit)
 * - Swipe left: Delete (haptic, triggers delete)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionSwipeableItemCard(
    rule: RecurringRule,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current

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
                    onEdit()
                    false // Spring back cleanly after triggering edit
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
                animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
                label = "swipeIconScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(animatedBgColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (isSwipingEdit) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (isSwipingEdit) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Commitment",
                            tint = scheme.onPrimary,
                            modifier = Modifier.size((22 * iconScale).dp)
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Commitment",
                            tint = Color.White,
                            modifier = Modifier.size((22 * iconScale).dp)
                        )
                    }
                }
            }
        }
    ) {
        SubscriptionItemCard(
            rule = rule,
            onToggleActive = onToggleActive,
            onEdit = onEdit
        )
    }
}

@Composable
fun SubscriptionItemCard(
    rule: RecurringRule,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isActive) scheme.surfaceContainerLowest else scheme.surfaceContainerLowest.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = if (rule.isActive) 0.4f else 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category/Monogram Squircle Badge
            CategoryBadge(
                categoryName = rule.categoryName ?: rule.payee,
                iconKey = null,
                color = rule.categoryColour?.let { Color(it) } ?: scheme.primary,
                size = 42.dp
            )

            Spacer(Modifier.width(12.dp))

            // Middle Column: Payee, Frequency, Account, Due Date
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                    // Cadence Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = scheme.surfaceContainerHighest,
                        border = BorderStroke(0.5.dp, scheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = rule.frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = scheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (rule.isActive) {
                        Text(
                            text = "Due $dueDateStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = scheme.surfaceContainerHighest.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "Paused",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal,
                                color = scheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    if (rule.accountName.isNotBlank()) {
                        Text(
                            text = "• ${rule.accountName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Right Column: Monospaced Tabular Amount & Full Interactive Switch
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = MoneyFormatter.formatPaise(rule.amountMinor, includeSymbol = true),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (rule.isActive) ExpenseColor else scheme.onSurfaceVariant
                )

                Switch(
                    checked = rule.isActive,
                    onCheckedChange = onToggleActive,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * Apple-Grade Modal Bottom Sheet for adding or editing a recurring subscription commitment.
 * Solves clutter, keyboard occlusion, and day-of-month pickability across all 1..31 days.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionSheet(
    editingRule: RecurringRule?,
    accounts: List<Account>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (id: Long, payee: String, amountMinor: Long, accountId: Long, categoryId: Long?, freq: RecurrenceFrequency, dayOfMonth: Int, autoPost: Boolean) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    // Curated high-utility presets for fast 1-tap commitment creation
    data class Preset(val name: String, val category: String, val defaultAmt: String)
    val presets = remember {
        listOf(
            Preset("Netflix", "Entertainment", "649"),
            Preset("Spotify", "Entertainment", "119"),
            Preset("YouTube Premium", "Entertainment", "149"),
            Preset("Amazon Prime", "Entertainment", "299"),
            Preset("iCloud Storage", "Bills & Utilities", "75"),
            Preset("Wi-Fi Broadband", "Bills & Utilities", "999"),
            Preset("Gym Membership", "Health & Fitness", "1500"),
            Preset("House Rent", "Housing", "15000"),
            Preset("Mutual Fund SIP", "Investments", "5000"),
            Preset("Credit Card EMI", "Debt & Loans", "3000")
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = scheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editingRule != null) "Edit Commitment" else "New Recurring Commitment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = scheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.25f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Hero Amount Input ─────────────────────────────────────────
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = scheme.surfaceContainerHighest.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "COMMITMENT AMOUNT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                            letterSpacing = VyxelTypographyTokens.TrackingUppercase
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "₹",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = scheme.primary,
                                modifier = Modifier.padding(end = 4.dp)
                            )

                            OutlinedTextField(
                                value = amountStr,
                                onValueChange = { amountStr = it.filter { c -> c.isDigit() || c == '.' } },
                                placeholder = {
                                    Text(
                                        "0.00",
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = scheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                },
                                textStyle = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseColor,
                                    textAlign = TextAlign.Start
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                modifier = Modifier.width(180.dp)
                            )
                        }
                    }
                }

                // ── Quick Presets (New Only) ──────────────────────────────────
                if (editingRule == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "POPULAR PRESETS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                            letterSpacing = VyxelTypographyTokens.TrackingUppercase
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presets.forEach { preset ->
                                val isSelected = payee.equals(preset.name, ignoreCase = true)
                                Surface(
                                    onClick = {
                                        VyxelHaptics.tick(context)
                                        payee = preset.name
                                        if (amountStr.isBlank()) {
                                            amountStr = preset.defaultAmt
                                        }
                                        categories.find { it.name.contains(preset.category, ignoreCase = true) }?.let {
                                            selectedCategoryId = it.id
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) scheme.primaryContainer else scheme.surfaceContainer,
                                    border = BorderStroke(1.dp, if (isSelected) scheme.primary else scheme.outlineVariant.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Service Name & Recurrence ─────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "DETAILS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                        letterSpacing = VyxelTypographyTokens.TrackingUppercase
                    )

                    OutlinedTextField(
                        value = payee,
                        onValueChange = { payee = it },
                        label = { Text("Service or Bill Name") },
                        placeholder = { Text("e.g. Netflix, Spotify, Rent") },
                        leadingIcon = {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = scheme.primary)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Frequency Segmented Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            RecurrenceFrequency.MONTHLY to "Monthly",
                            RecurrenceFrequency.YEARLY to "Yearly",
                            RecurrenceFrequency.WEEKLY to "Weekly"
                        ).forEach { (freq, label) ->
                            val selected = selectedFrequency == freq
                            Surface(
                                onClick = {
                                    VyxelHaptics.tick(context)
                                    selectedFrequency = freq
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) scheme.primaryContainer else scheme.surfaceContainerHighest,
                                border = BorderStroke(1.dp, if (selected) scheme.primary else scheme.outlineVariant.copy(alpha = 0.35f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Day of Month Interactive Stepper (1..31 Full Range) ────────
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BILLING DAY OF MONTH",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                            letterSpacing = VyxelTypographyTokens.TrackingUppercase
                        )

                        Text(
                            text = "Renews on the ${formatDayOrdinal(dayOfMonth)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.primary
                        )
                    }

                    // Stepper Row: [-] Day Number Display [+]
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = scheme.surfaceContainerHighest.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    VyxelHaptics.tick(context)
                                    dayOfMonth = if (dayOfMonth <= 1) 31 else dayOfMonth - 1
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrement Day", tint = scheme.primary)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "$dayOfMonth",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = scheme.onSurface
                                )
                                Text(
                                    text = "of each month",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    VyxelHaptics.tick(context)
                                    dayOfMonth = if (dayOfMonth >= 31) 1 else dayOfMonth + 1
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increment Day", tint = scheme.primary)
                            }
                        }
                    }

                    // Quick Day Shortcut Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 5, 10, 15, 20, 25, 28, 31).forEach { d ->
                            val isSelected = dayOfMonth == d
                            Surface(
                                onClick = {
                                    VyxelHaptics.tick(context)
                                    dayOfMonth = d
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) scheme.primary else scheme.surfaceContainer,
                                border = BorderStroke(0.5.dp, if (isSelected) scheme.primary else scheme.outlineVariant.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = formatDayOrdinal(d),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) scheme.onPrimary else scheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // ── Account Selector ──────────────────────────────────────────
                if (accounts.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "CHARGE FROM ACCOUNT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                            letterSpacing = VyxelTypographyTokens.TrackingUppercase
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            accounts.forEach { acc ->
                                val selected = selectedAccountId == acc.id
                                Surface(
                                    onClick = {
                                        VyxelHaptics.tick(context)
                                        selectedAccountId = acc.id
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) scheme.primaryContainer else scheme.surfaceContainer,
                                    border = BorderStroke(1.dp, if (selected) scheme.primary else scheme.outlineVariant.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = acc.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) scheme.onPrimaryContainer else scheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Category Selector ─────────────────────────────────────────
                if (categories.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "CATEGORY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                            letterSpacing = VyxelTypographyTokens.TrackingUppercase
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                val selected = selectedCategoryId == cat.id
                                Surface(
                                    onClick = {
                                        VyxelHaptics.tick(context)
                                        selectedCategoryId = cat.id
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) scheme.primaryContainer else scheme.surfaceContainer,
                                    border = BorderStroke(1.dp, if (selected) scheme.primary else scheme.outlineVariant.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(cat.colour))
                                        )
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) scheme.onPrimaryContainer else scheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Auto-Post Setting ─────────────────────────────────────────
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = scheme.surfaceContainerHighest.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-record to ledger",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface
                            )
                            Text(
                                text = "Creates an expense transaction automatically on renewal date",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoPost,
                            onCheckedChange = {
                                VyxelHaptics.tick(context)
                                autoPost = it
                            }
                        )
                    }
                }

                // ── Sticky Primary Action Button ──────────────────────────────
                val isFormValid = payee.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0.0
                Button(
                    onClick = {
                        val amountPaise = runCatching { (amountStr.toDouble() * 100).toLong() }.getOrDefault(0L)
                        if (isFormValid) {
                            VyxelHaptics.click(context)
                            onSave(
                                editingRule?.id ?: 0L,
                                payee.trim(),
                                amountPaise,
                                selectedAccountId,
                                selectedCategoryId,
                                selectedFrequency,
                                dayOfMonth,
                                autoPost
                            )
                        }
                    },
                    enabled = isFormValid,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (editingRule != null) "Save Changes" else "Add Commitment • ₹${amountStr.ifBlank { "0" }}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Format day of month with proper English ordinal suffix (1st, 2nd, 3rd, 4th...).
 */
private fun formatDayOrdinal(day: Int): String {
    return when {
        day in 11..13 -> "${day}th"
        day % 10 == 1 -> "${day}st"
        day % 10 == 2 -> "${day}nd"
        day % 10 == 3 -> "${day}rd"
        else -> "${day}th"
    }
}
