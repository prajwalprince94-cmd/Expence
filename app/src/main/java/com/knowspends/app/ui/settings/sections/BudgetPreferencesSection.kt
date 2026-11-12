package com.knowspends.app.ui.settings.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowspends.app.domain.model.UserPreferences
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.pressScale
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 1. Budget Period Switcher ──────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VyxelShapeTokens.Card,
            colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
            border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "BUDGET CALCULATION CYCLE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("monthly" to "Monthly", "weekly" to "Weekly").forEach { (period, label) ->
                        val isSelected = selectedPeriod == period
                        Button(
                            onClick = {
                                VyxelHaptics.tick(context)
                                selectedPeriod = period
                            },
                            modifier = Modifier
                                .weight(1f)
                                .pressScale(0.96f),
                            shape = VyxelShapeTokens.Pill,
                            colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = scheme.primary)
                            else ButtonDefaults.buttonColors(containerColor = scheme.surfaceContainerHigh)
                        ) {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) scheme.onPrimary else scheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // ── 2. Rollover Unspent Budget Toggle ──────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VyxelShapeTokens.Card,
            colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
            border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Rollover Unspent Budgets",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Automatically carry over leftover budget (or overspending deficits) into the next cycle.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(12.dp))

                Switch(
                    checked = rolloverEnabled,
                    onCheckedChange = {
                        VyxelHaptics.tick(context)
                        rolloverEnabled = it
                    }
                )
            }
        }

        // ── 3. Budget Alert Threshold ──────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VyxelShapeTokens.Card,
            colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
            border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "WARNING THRESHOLD ALERT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary
                )

                Text(
                    text = "KnowSpends warns you when category spending crosses this percentage of your envelope.",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(70, 80, 90, 100).forEach { threshold ->
                        val isSelected = alertThreshold == threshold
                        Button(
                            onClick = {
                                VyxelHaptics.tick(context)
                                alertThreshold = threshold
                            },
                            modifier = Modifier
                                .weight(1f)
                                .pressScale(0.96f),
                            shape = VyxelShapeTokens.Pill,
                            colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = scheme.primary)
                            else ButtonDefaults.buttonColors(containerColor = scheme.surfaceContainerHigh)
                        ) {
                            Text(
                                text = "$threshold%",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) scheme.onPrimary else scheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // ── 4. Monthly Savings Goal ────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VyxelShapeTokens.Card,
            colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
            border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "DEFAULT MONTHLY SAVINGS TARGET",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary
                )

                OutlinedTextField(
                    value = savingsGoalDraft,
                    onValueChange = { savingsGoalDraft = it },
                    label = { Text("Savings Target (${userPreferences.currencySymbol})") },
                    placeholder = { Text("e.g. 20,000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = VyxelShapeTokens.CardSmall,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                )
            }
        }

        // ── Link to Budgets Hub ────────────────────────────────────────────────
        OutlinedButton(
            onClick = onNavigateToBudgets,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .pressScale(0.97f),
            shape = VyxelShapeTokens.Pill
        ) {
            Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Open Active Category Envelopes")
            Spacer(Modifier.weight(1f))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }

        // ── Apply Button ──────────────────────────────────────────────────────
        Button(
            onClick = {
                VyxelHaptics.success(context)
                onSaveBudgetPreferences(selectedPeriod, rolloverEnabled, alertThreshold, savingsGoalDraft.trim())
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .pressScale(0.96f),
            shape = VyxelShapeTokens.Pill,
            colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Save Budget Preferences", fontWeight = FontWeight.Bold)
        }
    }
}
