package com.knowspends.app.ui.settings.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import com.knowspends.app.ui.components.VyxelNavBarBottomClearance
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.knowspends.app.ui.components.expressive.ExpressiveConnectedButtonGroup
import com.knowspends.app.ui.components.expressive.ExpressiveInlineStepper
import com.knowspends.app.ui.components.expressive.ExpressiveItemDivider
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsGroup
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsItem
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.pressScale
import com.knowspends.app.util.VyxelHaptics

@Composable
fun CurrencyRegionalSection(
    userPreferences: UserPreferences,
    onSaveRegional: (currency: String, numberFormat: String, dateFormat: String, firstDayOfWeek: String, monthStartDay: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    var selectedCurrency by remember(userPreferences.currencySymbol) { mutableStateOf(userPreferences.currencySymbol) }
    var selectedNumberFormat by remember(userPreferences.numberFormat) { mutableStateOf(userPreferences.numberFormat) }
    var selectedDateFormat by remember(userPreferences.dateFormat) { mutableStateOf(userPreferences.dateFormat) }
    var selectedFirstDay by remember(userPreferences.firstDayOfWeek) { mutableStateOf(userPreferences.firstDayOfWeek) }
    var monthStartDay by remember(userPreferences.monthStartDay) { mutableIntStateOf(userPreferences.monthStartDay) }

    fun commit(
        curr: String = selectedCurrency,
        numFmt: String = selectedNumberFormat,
        dateFmt: String = selectedDateFormat,
        firstDay: String = selectedFirstDay,
        mStart: Int = monthStartDay
    ) {
        onSaveRegional(curr, numFmt, dateFmt, firstDay, mStart)
    }

    val popularCurrencies = listOf(
        "₹" to "INR (₹)",
        "$" to "USD ($)",
        "€" to "EUR (€)",
        "£" to "GBP (£)",
        "¥" to "JPY (¥)",
        "AED" to "AED",
        "C$" to "CAD (C$)",
        "A$" to "AUD (A$)",
        "CHF" to "CHF",
        "S$" to "SGD (S$)"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. Primary Currency Group ─────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Primary Currency") {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select default denomination for accounts and transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    popularCurrencies.forEach { (symbol, label) ->
                        val isSelected = selectedCurrency == symbol
                        val bg = if (isSelected) scheme.primaryContainer else scheme.surfaceContainerHigh.copy(alpha = 0.5f)
                        val border = if (isSelected) BorderStroke(1.2.dp, scheme.primary) else BorderStroke(0.8.dp, scheme.outlineVariant.copy(alpha = 0.3f))

                        Surface(
                            onClick = {
                                VyxelHaptics.tick(context)
                                selectedCurrency = symbol
                                commit(curr = symbol)
                            },
                            shape = VyxelShapeTokens.Pill,
                            color = bg,
                            border = border,
                            modifier = Modifier.pressScale(0.96f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = symbol,
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) scheme.onPrimaryContainer else scheme.primary
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── 2. Number System & Grouping ───────────────────────────────────────
        ExpressiveSettingsGroup(title = "Number Format & Tabular Style") {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Controls digit separation in reports, balance cards, and exports",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )

                val formats = listOf(
                    "indian" to "Indian (1,50,000.00)",
                    "international" to "International (150,000.00)"
                )
                val selectedIdx = if (selectedNumberFormat == "international") 1 else 0

                ExpressiveConnectedButtonGroup(
                    options = formats,
                    selectedIndex = selectedIdx,
                    onSelectIndex = { idx ->
                        val chosen = formats[idx].first
                        selectedNumberFormat = chosen
                        commit(numFmt = chosen)
                    },
                    labelProvider = { it.second },
                    iconProvider = { Icons.Default.Numbers }
                )

                Surface(
                    shape = VyxelShapeTokens.CardSmall,
                    color = scheme.surfaceContainerHigh.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Preview Amount",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                        val sample = if (selectedNumberFormat == "indian") "$selectedCurrency 1,50,000.00" else "$selectedCurrency 150,000.00"
                        Text(
                            text = sample,
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                            fontWeight = FontWeight.Bold,
                            color = scheme.primary
                        )
                    }
                }
            }
        }

        // ── 3. Budget Cycle & Timing ──────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Budget Calendar Cycle") {
            // Month Start Date Row with Inline Stepper
            ExpressiveSettingsItem(
                title = "Budget Month Start Day",
                subtitle = "Calculates monthly envelopes from this day (e.g. salary date)",
                icon = Icons.Default.CalendarToday,
                trailingContent = {
                    ExpressiveInlineStepper(
                        value = monthStartDay,
                        onValueChange = {
                            monthStartDay = it
                            commit(mStart = it)
                        },
                        range = 1..31,
                        valueFormatter = { getOrdinal(it) }
                    )
                }
            )

            ExpressiveItemDivider()

            // First Day of Week
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "First Day of Week",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Text(
                    text = "Used for weekly expense summaries and digest reports",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )

                val days = listOf("monday" to "Monday (ISO)", "sunday" to "Sunday")
                val dayIdx = if (selectedFirstDay == "sunday") 1 else 0

                ExpressiveConnectedButtonGroup(
                    options = days,
                    selectedIndex = dayIdx,
                    onSelectIndex = { idx ->
                        val chosen = days[idx].first
                        selectedFirstDay = chosen
                        commit(firstDay = chosen)
                    },
                    labelProvider = { it.second },
                    iconProvider = { Icons.Default.Today }
                )
            }
        }

        Spacer(modifier = Modifier.height(VyxelNavBarBottomClearance).navigationBarsPadding())
    }
}

private fun getOrdinal(day: Int): String {
    return when {
        day in 11..13 -> "${day}th"
        day % 10 == 1 -> "${day}st"
        day % 10 == 2 -> "${day}nd"
        day % 10 == 3 -> "${day}rd"
        else -> "${day}th"
    }
}
