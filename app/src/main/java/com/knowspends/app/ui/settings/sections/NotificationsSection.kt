package com.knowspends.app.ui.settings.sections

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import com.knowspends.app.ui.components.VyxelNavBarBottomClearance
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowspends.app.domain.model.UserPreferences
import com.knowspends.app.ui.components.expressive.ExpressiveItemDivider
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsGroup
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsItem
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsSwitch
import com.knowspends.app.ui.theme.VyxelShapeTokens
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

    fun commit(
        daily: Boolean = dailyReminder,
        time: String = reminderTime,
        budget: Boolean = budgetAlerts,
        bills: Boolean = billReminders,
        summary: Boolean = summaryReports
    ) {
        onSaveNotifications(daily, time, budget, bills, summary)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. Daily Logging Habit ────────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Daily Logging Habit") {
            ExpressiveSettingsSwitch(
                title = "Daily Evening Reminder",
                subtitle = "A gentle reminder to record unlogged cash spends",
                icon = Icons.Default.Notifications,
                checked = dailyReminder,
                onCheckedChange = {
                    dailyReminder = it
                    commit(daily = it)
                }
            )

            if (dailyReminder) {
                ExpressiveItemDivider()

                val parts = reminderTime.split(":")
                val hour = parts.getOrNull(0)?.toIntOrNull() ?: 20
                val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

                ExpressiveSettingsItem(
                    title = "Reminder Time",
                    subtitle = "Tap to modify scheduled notification time",
                    icon = Icons.Default.AccessTime,
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                val newTime = String.format(java.util.Locale.US, "%02d:%02d", h, m)
                                reminderTime = newTime
                                commit(time = newTime)
                                VyxelHaptics.click(context)
                            },
                            hour,
                            minute,
                            false
                        ).show()
                    },
                    trailingContent = {
                        Surface(
                            shape = VyxelShapeTokens.Pill,
                            color = scheme.primaryContainer
                        ) {
                            Text(
                                text = formatTimeAmPm(reminderTime),
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                fontWeight = FontWeight.Bold,
                                color = scheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                )
            }
        }

        // ── 2. Alerts & Digests ───────────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Alerts & Digest Summaries") {
            ExpressiveSettingsSwitch(
                title = "Budget Threshold Alerts",
                subtitle = "Notifies when category spending reaches 80% or exceeds limits",
                icon = Icons.Default.Warning,
                checked = budgetAlerts,
                onCheckedChange = {
                    budgetAlerts = it
                    commit(budget = it)
                }
            )

            ExpressiveItemDivider()

            ExpressiveSettingsSwitch(
                title = "Subscription & Bill Reminders",
                subtitle = "Advance notice 1 day prior to recurring debits and rent charges",
                icon = Icons.Default.EventNote,
                checked = billReminders,
                onCheckedChange = {
                    billReminders = it
                    commit(bills = it)
                }
            )

            ExpressiveItemDivider()

            ExpressiveSettingsSwitch(
                title = "Weekly Financial Digest",
                subtitle = "Sunday evening summary of top categories and burn velocity",
                icon = Icons.Default.Assessment,
                checked = summaryReports,
                onCheckedChange = {
                    summaryReports = it
                    commit(summary = it)
                }
            )
        }

        Spacer(modifier = Modifier.height(VyxelNavBarBottomClearance).navigationBarsPadding())
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
