package com.knowspends.app.ui.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowspends.app.domain.model.CategoryReportItem
import com.knowspends.app.domain.model.DateRangeType
import com.knowspends.app.domain.model.InsightType
import com.knowspends.app.domain.model.MonthlyTrendItem
import com.knowspends.app.domain.model.NarrativeInsight
import com.knowspends.app.domain.model.PayeeReportItem
import com.knowspends.app.domain.util.MoneyFormatter
import com.knowspends.app.ui.components.CategoryBadge
import com.knowspends.app.ui.components.VyxelExpressiveChip
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.pressScale
import com.knowspends.app.ui.theme.progressiveScrollBlur

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Reports & Analytics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Period Range Selector (Apple Segmented Style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateRangeType.values().forEach { range ->
                    VyxelExpressiveChip(
                        selected = uiState.selectedRange == range,
                        onClick = { viewModel.onRangeSelected(range) },
                        label = range.label
                    )
                }
            }

            // Unified Executive Analytics Dashboard Feed
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .progressiveScrollBlur(
                        topBlur = 8.dp,
                        bottomBlur = 32.dp,
                        scrimColor = scheme.background
                    ),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── 1. Hero Cashflow & Pacing Card ──────────────────────────
                item(key = "hero_cashflow") {
                    CashflowHeroCard(uiState = uiState)
                }

                // ── 2. Category Spending Donut & Distribution ───────────────
                if (uiState.categories.isNotEmpty()) {
                    item(key = "category_donut") {
                        CategorySpendingCard(
                            categories = uiState.categories,
                            totalExpenseMinor = uiState.totalExpenseMinor
                        )
                    }
                }

                // ── 3. Narrative Financial Intelligence Cards ─────────────────
                val validInsights = uiState.narrativeInsights.filter { it.id != "no_data" }
                if (validInsights.isNotEmpty()) {
                    item(key = "narrative_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = scheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "FINANCIAL INTELLIGENCE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.primary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    items(validInsights, key = { it.id }) { insight ->
                        NarrativeInsightCard(insight = insight)
                    }
                }

                // ── 4. Top Merchants & Outflows ──────────────────────────────
                if (uiState.topPayees.isNotEmpty()) {
                    item(key = "payees_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = scheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "TOP MERCHANTS & OUTFLOWS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.secondary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    items(uiState.topPayees.take(6), key = { it.payeeName }) { payee ->
                        PayeeRowCard(
                            payee = payee,
                            totalExpenseMinor = uiState.totalExpenseMinor
                        )
                    }
                }

                // ── 5. Monthly Cashflow Trends ───────────────────────────────
                if (uiState.monthlyTrends.isNotEmpty()) {
                    item(key = "trends_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = scheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "MONTHLY CASHFLOW PACING",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.tertiary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    items(uiState.monthlyTrends, key = { it.yearMonth }) { trend ->
                        MonthlyTrendRowCard(trend = trend)
                    }
                }

                // Empty state if completely no data across all metrics
                if (uiState.categories.isEmpty() && uiState.topPayees.isEmpty() && uiState.totalExpenseMinor == 0L && uiState.totalIncomeMinor == 0L) {
                    item(key = "empty_state") {
                        EmptyReportView(message = "No transactions found for the selected time range")
                    }
                }
            }
        }
    }
}

@Composable
private fun CashflowHeroCard(uiState: ReportsUiState) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = VyxelShapeTokens.Card,
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NET SAVINGS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant
                )
                if (uiState.savingsRatePercent > 0) {
                    Surface(
                        shape = VyxelShapeTokens.Pill,
                        color = IncomeColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, IncomeColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "${String.format("%.1f", uiState.savingsRatePercent)}% Saved",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = IncomeColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = uiState.formattedNetSavings,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = if (uiState.netSavingsMinor >= 0) IncomeColor else ExpenseColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Visual bi-directional flow bar
            val totalCashflow = (uiState.totalIncomeMinor + uiState.totalExpenseMinor).toFloat()
            val incomeRatio = if (totalCashflow > 0f) (uiState.totalIncomeMinor.toFloat() / totalCashflow).coerceIn(0f, 1f) else 0.5f
            val expenseRatio = if (totalCashflow > 0f) (uiState.totalExpenseMinor.toFloat() / totalCashflow).coerceIn(0f, 1f) else 0.5f

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(VyxelShapeTokens.Pill)
                    .background(scheme.surfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier
                        .weight(incomeRatio.coerceAtLeast(0.01f))
                        .height(8.dp)
                        .background(IncomeColor)
                )
                Box(
                    modifier = Modifier
                        .weight(expenseRatio.coerceAtLeast(0.01f))
                        .height(8.dp)
                        .background(ExpenseColor)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Inflow",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                    Text(
                        text = uiState.formattedIncome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IncomeColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Outflow",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                    Text(
                        text = uiState.formattedExpense,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseColor
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySpendingCard(
    categories: List<CategoryReportItem>,
    totalExpenseMinor: Long
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = VyxelShapeTokens.Card,
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "CATEGORY DISTRIBUTION",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Smooth Donut Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val strokeWidth = 26.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    var startAngle = -90f
                    categories.forEach { item ->
                        val sweepAngle = (item.percentage / 100f) * 360f
                        if (sweepAngle > 0f) {
                            drawArc(
                                color = Color(item.categoryColour),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            startAngle += sweepAngle
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                    Text(
                        text = MoneyFormatter.formatPaise(totalExpenseMinor),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ranked Category Breakdown Rows
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                categories.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryBadge(
                            categoryName = item.categoryName,
                            iconKey = null,
                            color = Color(item.categoryColour),
                            size = 28.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.categoryName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = scheme.onSurface
                                )
                                Text(
                                    text = MoneyFormatter.formatPaise(item.totalAmountMinor),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(VyxelShapeTokens.Pill),
                                    color = Color(item.categoryColour),
                                    trackColor = scheme.surfaceContainerHighest
                                )
                                Text(
                                    text = "${String.format("%.1f", item.percentage)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NarrativeInsightCard(insight: NarrativeInsight) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(0.98f),
        shape = VyxelShapeTokens.Card,
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f))
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    val (iconBg, iconTint, vector) = when (insight.iconKey) {
                        "savings" -> Triple(IncomeColor.copy(alpha = 0.15f), IncomeColor, Icons.Default.Savings)
                        "star" -> Triple(scheme.primaryContainer, scheme.primary, Icons.Default.Star)
                        "calendar" -> Triple(scheme.secondaryContainer, scheme.secondary, Icons.Default.CalendarToday)
                        "trending_up" -> Triple(scheme.tertiaryContainer, scheme.tertiary, Icons.Default.TrendingUp)
                        "pie" -> Triple(scheme.primaryContainer, scheme.primary, Icons.Default.PieChart)
                        "store" -> Triple(scheme.surfaceContainerHighest, scheme.onSurfaceVariant, Icons.Default.Store)
                        "warning" -> Triple(ExpenseColor.copy(alpha = 0.15f), ExpenseColor, Icons.Default.Warning)
                        else -> Triple(scheme.primaryContainer, scheme.primary, Icons.Default.AutoAwesome)
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = vector,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = iconTint
                        )
                    }

                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                }

                if (insight.metricHighlight != null) {
                    val metricColor = when (insight.type) {
                        InsightType.POSITIVE -> IncomeColor
                        InsightType.CAUTION -> ExpenseColor
                        InsightType.HIGHLIGHT -> scheme.primary
                        InsightType.NEUTRAL -> scheme.onSurface
                    }
                    Surface(
                        shape = VyxelShapeTokens.Pill,
                        color = when (insight.type) {
                            InsightType.POSITIVE -> IncomeColor.copy(alpha = 0.12f)
                            InsightType.CAUTION -> ExpenseColor.copy(alpha = 0.12f)
                            else -> scheme.surfaceContainerHighest
                        }
                    ) {
                        Text(
                            text = insight.metricHighlight,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = metricColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = insight.narrative,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun PayeeRowCard(
    payee: PayeeReportItem,
    totalExpenseMinor: Long
) {
    val scheme = MaterialTheme.colorScheme
    val percentOfTotal = if (totalExpenseMinor > 0L) {
        ((payee.totalAmountMinor.toFloat() / totalExpenseMinor.toFloat()) * 100f).coerceIn(0f, 100f)
    } else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(0.98f),
        shape = VyxelShapeTokens.CardSmall,
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(VyxelShapeTokens.CardSmall)
                    .background(scheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = scheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payee.payeeName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${payee.transactionCount} transactions • ${String.format("%.1f", percentOfTotal)}% of spend",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }

            Text(
                text = MoneyFormatter.formatPaise(payee.totalAmountMinor),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
        }
    }
}

@Composable
private fun MonthlyTrendRowCard(trend: MonthlyTrendItem) {
    val scheme = MaterialTheme.colorScheme
    val maxVal = Math.max(trend.incomeMinor, trend.expenseMinor).toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(0.98f),
        shape = VyxelShapeTokens.CardSmall,
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trend.monthDisplayLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Text(
                    text = if (trend.netSavingsMinor >= 0)
                        "+${MoneyFormatter.formatPaise(trend.netSavingsMinor)}"
                    else
                        "-${MoneyFormatter.formatPaise(-trend.netSavingsMinor)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (trend.netSavingsMinor >= 0) IncomeColor else ExpenseColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (maxVal > 0f) {
                // Inflow Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "In",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(26.dp),
                        color = IncomeColor,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { (trend.incomeMinor.toFloat() / maxVal).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(VyxelShapeTokens.Pill),
                        color = IncomeColor,
                        trackColor = scheme.surfaceContainerHighest
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = MoneyFormatter.formatPaise(trend.incomeMinor),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Outflow Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Out",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(26.dp),
                        color = ExpenseColor,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { (trend.expenseMinor.toFloat() / maxVal).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(VyxelShapeTokens.Pill),
                        color = ExpenseColor,
                        trackColor = scheme.surfaceContainerHighest
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = MoneyFormatter.formatPaise(trend.expenseMinor),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyReportView(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.PieChart,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
