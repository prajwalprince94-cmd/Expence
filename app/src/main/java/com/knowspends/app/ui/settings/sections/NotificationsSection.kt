package com.knowspends.app.ui.settings.sections

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun NotificationsSection(
    userPreferences: UserPreferences,
    onSaveNotifications: (dailyReminder: Boolean, reminderTime: String, budgetAlerts: Boolean, billReminders: Boolean, summaryReports: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    var dailyReminder by remember(userPreferences.dailyReminderEnabled) { mutableStateOf(userPreferences.dailyReminderEnabled) }
    var reminderTime by remember(userPreferences.dailyReminderTime) { mutableStateOf(userPreferences.dailyReminderTime) }
    var budgetAlerts by remember(userPreferences.budgetAlertEnabled) { mutableStateOf(userPreferences.budgetAlertEnabled) }
    var billReminders by remember(userPreferences.billRemindersEnabled) { mutableStateOf(userPreferences.billRemindersEnabled) }
    var summaryReports by remember(userPreferences.summaryReportsEnabled) { mutableStateOf(userPreferences.summaryReportsEnabled) }

    fun update() {
        onSaveNotifications(dailyReminder, reminderTime, budgetAlerts, billReminders, summaryReports)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 1. Daily Logging Reminder ─────────────────────────────────────────
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Daily Expense Logger Reminder", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                        Spacer(Modifier.height(2.dp))
                        Text(text = "A gentle evening reminder to log your daily cash spends.", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = dailyReminder,
                        onCheckedChange = {
                            VyxelHaptics.tick(context)
                            dailyReminder = it
                            update()
                        }
                    )
                }

                if (dailyReminder) {
                    val parts = reminderTime.split(":")
                    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 20
                    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressScale(0.97f)
                            .clip(VyxelShapeTokens.CardSmall)
                            .background(scheme.surfaceContainerHigh.copy(alpha = 0.5f))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        reminderTime = String.format(java.util.Locale.US, "%02d:%02d", h, m)
                                        VyxelHaptics.click(context)
                                        update()
                                    },
                                    hour,
                                    minute,
                                    false
                                ).show()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = scheme.primary)
                            Text(text = "Reminder Time", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = scheme.onSurface)
                        }

                        Surface(shape = VyxelShapeTokens.Pill, color = scheme.primaryContainer) {
                            Text(
                                text = formatTimeAmPm(reminderTime),
                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                color = scheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── 2. Budget Limit & Overspend Alerts ─────────────────────────────────
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
                    Text(text = "Budget Limit & Threshold Alerts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                    Spacer(Modifier.height(2.dp))
                    Text(text = "Alerts when category spending hits 80% or exceeds 100% of limits.", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = budgetAlerts,
                    onCheckedChange = {
                        VyxelHaptics.tick(context)
                        budgetAlerts = it
                        update()
                    }
                )
            }
        }

        // ── 3. Bill & EMI Due-Date Reminders ──────────────────────────────────
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
                    Text(text = "Subscription & Bill Reminders", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                    Spacer(Modifier.height(2.dp))
                    Text(text = "Advance notice 1 day before recurring bills, rent, or EMI are charged.", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = billReminders,
                    onCheckedChange = {
                        VyxelHaptics.tick(context)
                        billReminders = it
                        update()
                    }
                )
            }
        }

        // ── 4. Weekly Summary Reports ─────────────────────────────────────────
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
                    Text(text = "Weekly Financial Digest", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                    Spacer(Modifier.height(2.dp))
                    Text(text = "Every Sunday evening summary of weekly burn and top category spends.", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = summaryReports,
                    onCheckedChange = {
                        VyxelHaptics.tick(context)
                        summaryReports = it
                        update()
                    }
                )
            }
        }
    }
}

private fun formatTimeAmPm(timeStr: String): String {
    return try {
        val parts = timeStr.split(":")
        val h = parts[0].toInt()
        val m = parts[1].toInt()
        val amPm = if (h >= 12) "PM" else "AM"
        val hour12 = if (h == 0) 12 else if (h > 12) h - 12 else h
        String.format(java.util.Locale.US, "%d:%02d %s", hour12, m, amPm)
    } catch (_: Exception) {
        timeStr
    }
}
