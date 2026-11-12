package com.knowspends.app.ui.budgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import com.knowspends.app.ui.components.states.ExpressiveEmptyState
import com.knowspends.app.ui.components.states.ExpressiveHeroCardSkeleton
import com.knowspends.app.ui.components.states.ExpressiveTransactionListSkeleton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.knowspends.app.domain.model.BudgetStatus
import com.knowspends.app.domain.model.Category
import com.knowspends.app.domain.model.CategoryBudget
import com.knowspends.app.domain.util.MoneyFormatter
import com.knowspends.app.ui.components.AceternitySpotlightCard
import com.knowspends.app.ui.components.CategoryBadge
import com.knowspends.app.ui.components.ExpressiveWavyProgressIndicator
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.VyxelTypographyTokens
import com.knowspends.app.ui.theme.WarningColor
import com.knowspends.app.ui.theme.pressScale
import com.knowspends.app.ui.theme.progressiveScrollBlur
import com.knowspends.app.util.VyxelHaptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: (() -> Unit)? = null,
    viewModel: BudgetsViewModel = hiltViewModel()
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
                        text = "Budgets (${uiState.periodLabel})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (onNavigateUp != null) {
                        IconButton(onClick = {
                            VyxelHaptics.tick(context)
                            onNavigateUp()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        onClick = {
                            VyxelHaptics.click(context)
                            viewModel.openSetBudgetDialog()
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
                                contentDescription = "Set Budget",
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
            item(key = "budget_hero_card") {
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
                        // Header row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL MONTHLY BUDGET",
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
                                    text = "${uiState.budgets.size} Envelopes",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Prominent Budget Amount
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = uiState.formattedTotalBudgeted,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = scheme.onSurface
                            )
                            Text(
                                text = "${uiState.periodLabel} allocation across all tracked categories",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant.copy(alpha = 0.85f)
                            )
                        }

                        // Progress Gauge
                        ExpressiveWavyProgressIndicator(
                            progress = uiState.totalProgressRatio,
                            modifier = Modifier.fillMaxWidth(),
                            color = when {
                                uiState.totalSpentMinor > uiState.totalBudgetedMinor -> ExpenseColor
                                uiState.totalProgressRatio >= 0.8f -> WarningColor
                                else -> IncomeColor
                            }
                        )

                        // Inset Metrics Strip
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
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "SPENT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        letterSpacing = VyxelTypographyTokens.TrackingUppercase,
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        text = uiState.formattedTotalSpent,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                        fontWeight = FontWeight.SemiBold,
                                        color = scheme.onSurface
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = if (uiState.totalRemainingMinor >= 0) "REMAINING" else "OVERSPENT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.totalRemainingMinor >= 0) IncomeColor else ExpenseColor,
                                        letterSpacing = VyxelTypographyTokens.TrackingUppercase,
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        text = if (uiState.totalRemainingMinor >= 0) {
                                            uiState.formattedTotalRemaining
                                        } else {
                                            MoneyFormatter.formatPaise(-uiState.totalRemainingMinor)
                                        },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.totalRemainingMinor >= 0) IncomeColor else ExpenseColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Section Title ─────────────────────────────────────────────────
            item(key = "section_title") {
                Text(
                    text = "CATEGORY ENVELOPES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                    letterSpacing = VyxelTypographyTokens.TrackingUppercase,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (uiState.isLoading) {
                item(key = "loading_skeletons") {
                    ExpressiveTransactionListSkeleton(count = 3)
                }
            } else if (uiState.budgets.isEmpty()) {
                item(key = "empty_state") {
                    ExpressiveEmptyState(
                        icon = Icons.Default.PieChart,
                        title = "No Budgets Set This Month",
                        subtitle = "Set category spending limits to keep your finances disciplined and track progress in real-time.",
                        actionLabel = "Set First Category Budget",
                        onActionClick = { viewModel.openSetBudgetDialog() }
                    )
                }
            } else {
                items(
                    items = uiState.budgets,
                    key = { it.budgetId }
                ) { budget ->
                    CategoryBudgetSwipeableItemCard(
                        budget = budget,
                        onEdit = {
                            VyxelHaptics.click(context)
                            viewModel.openSetBudgetDialog(budget)
                        },
                        onDelete = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deleteBudget(budget.budgetId)
                        }
                    )
                }
            }

            item(key = "bottom_clearance") {
                Spacer(Modifier.height(84.dp))
            }
        }

        // ── Apple-Grade Modal Bottom Sheet for Set/Edit Budget ────────────────
        if (uiState.isSetBudgetDialogOpen) {
            SetBudgetSheet(
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

/**
 * Swipeable Category Budget Card with bidirectional gestures:
 * - Swipe right: Edit (springs back, triggers edit)
 * - Swipe left: Delete (haptic, triggers delete)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBudgetSwipeableItemCard(
    budget: CategoryBudget,
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
                            contentDescription = "Edit Budget",
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
                            contentDescription = "Delete Budget",
                            tint = Color.White,
                            modifier = Modifier.size((22 * iconScale).dp)
                        )
                    }
                }
            }
        }
    ) {
        CategoryBudgetCard(
            budget = budget,
            onClick = onEdit
        )
    }
}

@Composable
fun CategoryBudgetCard(
    budget: CategoryBudget,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    val statusColor = when (budget.status) {
        BudgetStatus.SAFE -> IncomeColor
        BudgetStatus.WARNING -> WarningColor
        BudgetStatus.OVERSPENT -> ExpenseColor
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(0.99f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Category badge & Name + Tabular Amounts
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(
                    categoryName = budget.categoryName,
                    iconKey = null,
                    color = Color(budget.categoryColour),
                    size = 40.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = budget.categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (budget.rollover) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = scheme.surfaceContainerHighest,
                                border = BorderStroke(0.5.dp, scheme.outlineVariant.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "Rollover",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = scheme.primary,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = "${MoneyFormatter.formatPaise(budget.spentAmountMinor)} of ${MoneyFormatter.formatPaise(budget.budgetAmountMinor)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = scheme.onSurfaceVariant
                    )
                }

                // Status capsule pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = when (budget.status) {
                            BudgetStatus.SAFE -> "${MoneyFormatter.formatPaise(budget.remainingAmountMinor)} left"
                            BudgetStatus.WARNING -> "${(budget.progressRatio * 100).toInt()}% used"
                            BudgetStatus.OVERSPENT -> "Over by ${MoneyFormatter.formatPaise(-budget.remainingAmountMinor)}"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Smooth Progress Bar
            LinearProgressIndicator(
                progress = { budget.progressRatio.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = scheme.surfaceContainerHighest.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Apple-Grade Modal Bottom Sheet for Setting or Editing a Category Budget.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBudgetSheet(
    editingBudget: CategoryBudget?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (categoryId: Long, amountMinor: Long, rollover: Boolean) -> Unit,
    onDelete: (budgetId: Long) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedCategoryId by remember {
        mutableStateOf(editingBudget?.categoryId ?: categories.firstOrNull()?.id ?: 0L)
    }

    val initialAmount = editingBudget?.let {
        (it.budgetAmountMinor / 100).toString()
    } ?: ""
    var amountText by remember { mutableStateOf(initialAmount) }
    var rollover by remember { mutableStateOf(editingBudget?.rollover ?: false) }

    val quickPresets = listOf("2000", "5000", "10000", "15000", "25000", "50000")

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
                    text = if (editingBudget != null) "Edit ${editingBudget.categoryName} Budget" else "Set Category Budget",
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
                // ── Hero Amount Header ────────────────────────────────────────
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
                            text = "MONTHLY BUDGET CAP",
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
                                value = amountText,
                                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                                placeholder = {
                                    Text(
                                        "0",
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
                                    color = scheme.onSurface,
                                    textAlign = TextAlign.Start
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                // ── Quick Budget Presets ───────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "QUICK PRESETS",
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
                        quickPresets.forEach { preset ->
                            val isSelected = amountText == preset
                            Surface(
                                onClick = {
                                    VyxelHaptics.tick(context)
                                    amountText = preset
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) scheme.primaryContainer else scheme.surfaceContainer,
                                border = BorderStroke(1.dp, if (isSelected) scheme.primary else scheme.outlineVariant.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "₹$preset",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // ── Category Selection (New Only) ─────────────────────────────
                if (editingBudget == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "SELECT CATEGORY",
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

                // ── Rollover Setting ──────────────────────────────────────────
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
                                text = "Monthly Budget Rollover",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface
                            )
                            Text(
                                text = "Carry over unspent budget balance into the next month",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = rollover,
                            onCheckedChange = {
                                VyxelHaptics.tick(context)
                                rollover = it
                            }
                        )
                    }
                }

                // ── Action Buttons ────────────────────────────────────────────
                val amountMinor = MoneyFormatter.parseToPaise(amountText)
                val isValid = selectedCategoryId > 0 && amountMinor > 0

                Button(
                    onClick = {
                        if (isValid) {
                            VyxelHaptics.click(context)
                            onSave(selectedCategoryId, amountMinor, rollover)
                        }
                    },
                    enabled = isValid,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (editingBudget != null) "Save Changes" else "Set Budget • ₹${amountText.ifBlank { "0" }}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (editingBudget != null) {
                    OutlinedButton(
                        onClick = {
                            VyxelHaptics.click(context)
                            onDelete(editingBudget.budgetId)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseColor),
                        border = BorderStroke(1.dp, ExpenseColor.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Delete Budget")
                    }
                }
            }
        }
    }
}
