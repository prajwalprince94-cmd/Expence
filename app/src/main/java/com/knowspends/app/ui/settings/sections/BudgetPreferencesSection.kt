package com.knowspends.app.ui.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.knowspends.app.domain.model.UserPreferences
import com.knowspends.app.ui.components.expressive.ExpressiveConnectedButtonGroup
import com.knowspends.app.ui.components.expressive.ExpressiveItemDivider
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsGroup
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsItem
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsSwitch
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.util.VyxelHaptics

@Composable
fun BudgetPreferencesSection(
    userPreferences: UserPreferences,
    onSaveBudgetPreferences: (budgetPeriod: String, rollover: Boolean, alertThreshold: Int, savingsGoal: String) -> Unit,
    onNavigateToBudgets: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    var selectedPeriod by remember(userPreferences.budgetPeriod) { mutableStateOf(userPreferences.budgetPeriod) }
    var rolloverEnabled by remember(userPreferences.budgetRolloverEnabled) { mutableStateOf(userPreferences.budgetRolloverEnabled) }
    var alertThreshold by remember(userPreferences.budgetAlertThreshold) { mutableIntStateOf(userPreferences.budgetAlertThreshold) }
    var savingsGoalDraft by remember(userPreferences.monthlySavingsGoal) { mutableStateOf(userPreferences.monthlySavingsGoal) }

    fun commit(
        period: String = selectedPeriod,
        rollover: Boolean = rolloverEnabled,
        threshold: Int = alertThreshold,
        goal: String = savingsGoalDraft
    ) {
        onSaveBudgetPreferences(period, rollover, threshold, goal)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. Calculation Cycle & Rollover ───────────────────────────────────
        ExpressiveSettingsGroup(title = "Budget Calculation Period") {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Evaluation Cycle",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Text(
                    text = "Defines how expense envelopes and burn rates are aggregated",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )

                val periods = listOf("monthly" to "Monthly Envelopes", "weekly" to "Weekly Envelopes")
                val periodIdx = if (selectedPeriod == "weekly") 1 else 0

                ExpressiveConnectedButtonGroup(
                    options = periods,
                    selectedIndex = periodIdx,
                    onSelectIndex = { idx ->
                        val chosen = periods[idx].first
                        selectedPeriod = chosen
                        commit(period = chosen)
                    },
                    labelProvider = { it.second },
                    iconProvider = { Icons.Default.Timeline }
                )
            }

            ExpressiveItemDivider()

            ExpressiveSettingsSwitch(
                title = "Rollover Unspent Budget",
                subtitle = "Carry over surpluses or deficits into the subsequent cycle",
                icon = Icons.Default.Refresh,
                checked = rolloverEnabled,
                onCheckedChange = {
                    rolloverEnabled = it
                    commit(rollover = it)
                }
            )
        }

        // ── 2. Warnings & Target Alerts ───────────────────────────────────────
        ExpressiveSettingsGroup(title = "Alerts & Envelopes") {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Warning Threshold",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Text(
                    text = "Alerts trigger when category spend crosses this budget percentage",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )

                val thresholds = listOf(70, 80, 90, 100)
                val threshIdx = thresholds.indexOf(alertThreshold).coerceAtLeast(0)

                ExpressiveConnectedButtonGroup(
                    options = thresholds,
                    selectedIndex = threshIdx,
                    onSelectIndex = { idx ->
                        val chosen = thresholds[idx]
                        alertThreshold = chosen
                        commit(threshold = chosen)
                    },
                    labelProvider = { "$it%" },
                    iconProvider = { Icons.Default.NotificationsActive }
                )
            }

            ExpressiveItemDivider()

            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Monthly Savings Target",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Text(
                    text = "Expected monthly unallocated reserve savings goal",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = savingsGoalDraft,
                    onValueChange = {
                        val filtered = it.filter { c -> c.isDigit() || c == '.' }
                        savingsGoalDraft = filtered
                        commit(goal = filtered)
                    },
                    placeholder = { Text("e.g. 25000") },
                    prefix = {
                        Text(
                            text = "${userPreferences.currencySymbol} ",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            fontWeight = FontWeight.Bold,
                            color = scheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = VyxelShapeTokens.CardSmall,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                )
            }
        }

        // ── 3. Quick Jump to Active Budgets ───────────────────────────────────
        ExpressiveSettingsGroup(title = "Category Envelopes") {
            ExpressiveSettingsItem(
                title = "Manage Active Budgets",
                subtitle = "View envelopes, allocation bars, and spending health",
                icon = Icons.Default.AccountBalanceWallet,
                showChevron = true,
                onClick = onNavigateToBudgets
            )
        }
    }
}
