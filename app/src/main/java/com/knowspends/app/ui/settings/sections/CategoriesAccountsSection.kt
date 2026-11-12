package com.knowspends.app.ui.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowspends.app.ui.components.expressive.ExpressiveItemDivider
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsGroup
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsItem
import com.knowspends.app.ui.theme.VyxelShapeTokens

@Composable
fun CategoriesAccountsSection(
    accountCount: Int,
    categoryCount: Int,
    onNavigateToMerchantRules: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. Categories Group ───────────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Categories Architecture") {
            ExpressiveSettingsItem(
                title = "Expense & Income Categories",
                subtitle = "Manage category taxonomy, color dots, and icons",
                icon = Icons.Default.Category,
                trailingContent = {
                    Surface(
                        shape = VyxelShapeTokens.Pill,
                        color = scheme.primaryContainer
                    ) {
                        Text(
                            text = "$categoryCount Active",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            )
        }

        // ── 2. Payment Sources Group ──────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Wallets & Accounts") {
            ExpressiveSettingsItem(
                title = "Connected Accounts & Payment Sources",
                subtitle = "Bank accounts, liquid cash wallets, and credit cards",
                icon = Icons.Default.AccountBalance,
                trailingContent = {
                    Surface(
                        shape = VyxelShapeTokens.Pill,
                        color = scheme.secondaryContainer
                    ) {
                        Text(
                            text = "$accountCount Accounts",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            )

            ExpressiveItemDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccountTypePill(icon = Icons.Default.AccountBalance, label = "Bank Accounts")
                AccountTypePill(icon = Icons.Default.Payments, label = "Cash")
                AccountTypePill(icon = Icons.Default.CreditCard, label = "Credit Cards")
            }
        }

        // ── 3. Smart Categorization Rules ─────────────────────────────────────
        ExpressiveSettingsGroup(title = "Automated Categorization") {
            ExpressiveSettingsItem(
                title = "Merchant Categorization Rules",
                subtitle = "Auto-assign categories from merchant keywords (e.g. Swiggy → Food)",
                icon = Icons.Default.Rule,
                showChevron = true,
                onClick = onNavigateToMerchantRules
            )
        }
    }
}

@Composable
private fun AccountTypePill(icon: ImageVector, label: String) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = VyxelShapeTokens.Pill,
        color = scheme.surfaceContainerHigh.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(13.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = scheme.onSurface)
        }
    }
}
