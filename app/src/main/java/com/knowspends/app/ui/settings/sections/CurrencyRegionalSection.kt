package com.knowspends.app.ui.settings.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.sp
import com.knowspends.app.domain.model.UserPreferences
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

    val popularCurrencies = listOf(
        "₹" to "INR (Indian Rupee)",
        "$" to "USD (US Dollar)",
        "€" to "EUR (Euro)",
        "£" to "GBP (British Pound)",
        "¥" to "JPY (Japanese Yen)",
        "AED" to "AED (UAE Dirham)",
        "C$" to "CAD (Canadian Dollar)",
        "A$" to "AUD (Australian Dollar)",
        "CHF" to "CHF (Swiss Franc)",
        "S$" to "SGD (Singapore Dollar)"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 1. Default Currency Picker ─────────────────────────────────────────
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
                Text(
                    text = "PRIMARY CURRENCY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    popularCurrencies.forEach { (symbol, name) ->
                        val isSelected = selectedCurrency == symbol
                        val bg = if (isSelected) scheme.primaryContainer else scheme.surfaceContainerHigh.copy(alpha = 0.6f)
                        val border = if (isSelected) BorderStroke(1.5.dp, scheme.primary) else BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.35f))

                        Row(
                            modifier = Modifier
                                .pressScale(0.96f)
                                .clip(VyxelShapeTokens.Pill)
                                .background(bg)
                                .border(border, VyxelShapeTokens.Pill)
                                .clickable {
                                    VyxelHaptics.tick(context)
                                    selectedCurrency = symbol
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = symbol,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface
                            )
                            Text(
                                text = name.substringBefore(" "),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ── 2. Number Format (Lakhs/Crores vs Millions) ───────────────────────
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
                Text(
                    text = "NUMBER FORMAT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary
                )

                val formats = listOf(
                    "indian" to "Indian System (Lakhs & Crores)",
                    "international" to "International (Millions & Billions)"
                )

                formats.forEach { (id, label) ->
                    val isSelected = selectedNumberFormat == id
                    val example = if (id == "indian") "$selectedCurrency 1,50,000.00" else "$selectedCurrency 150,000.00"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressScale(0.97f)
                            .clip(VyxelShapeTokens.CardSmall)
                            .background(if (isSelected) scheme.primaryContainer.copy(alpha = 0.4f) else scheme.surfaceContainerHigh.copy(alpha = 0.4f))
                            .border(
                                if (isSelected) BorderStroke(1.5.dp, scheme.primary) else BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.3f)),
                                VyxelShapeTokens.CardSmall
                            )
                            .clickable {
                                VyxelHaptics.tick(context)
                                selectedNumberFormat = id
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = scheme.onSurface
                            )
                            Text(
                                text = "Preview: $example",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = scheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = scheme.primary)
                        }
                    }
                }
            }
        }

        // ── 3. Month Cycle Start Day (Salary / Payday) ────────────────────────
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
                    text = "MONTH START DATE (BUDGET CYCLE)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary
                )

                Text(
                    text = "KnowSpends resets your monthly budget calculations and report cycles on this day (e.g. 1st of month, or 25th if that's when salary is credited).",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = {
                            if (monthStartDay > 1) {
                                monthStartDay--
                                VyxelHaptics.tick(context)
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(scheme.surfaceContainerHigh)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
                    }

                    Spacer(Modifier.width(20.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = getOrdinal(monthStartDay),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = scheme.primary
                        )
                        Text(
                            text = "of every month",
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.width(20.dp))

                    IconButton(
                        onClick = {
                            if (monthStartDay < 31) {
                                monthStartDay++
                                VyxelHaptics.tick(context)
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(scheme.surfaceContainerHigh)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }
        }

        // ── 4. First Day of Week ──────────────────────────────────────────────
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
                    text = "FIRST DAY OF WEEK",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("monday" to "Monday (ISO)", "sunday" to "Sunday").forEach { (id, label) ->
                        val isSelected = selectedFirstDay == id
                        Button(
                            onClick = {
                                VyxelHaptics.tick(context)
                                selectedFirstDay = id
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

        // ── Apply Button ──────────────────────────────────────────────────────
        Button(
            onClick = {
                VyxelHaptics.success(context)
                onSaveRegional(selectedCurrency, selectedNumberFormat, selectedDateFormat, selectedFirstDay, monthStartDay)
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
            Text("Apply Regional Preferences", fontWeight = FontWeight.Bold)
        }
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
