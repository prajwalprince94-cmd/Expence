package com.knowspends.app.ui.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.knowspends.app.domain.model.UserPreferences
import com.knowspends.app.ui.components.expressive.ExpressiveItemDivider
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsGroup
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsItem
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsSwitch
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.VyxelShapeTokens

@Composable
fun SyncCloudSection(
    userPreferences: UserPreferences,
    onSaveSyncPreferences: (wifiOnly: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    var wifiOnly by remember(userPreferences.syncWifiOnly) { mutableStateOf(userPreferences.syncWifiOnly) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. Local-First Guarantee ──────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Local-First Architecture") {
            ExpressiveSettingsItem(
                title = "100% Offline & Private",
                subtitle = "Zero analytics trackers, zero telemetry, and zero remote cloud databases",
                icon = Icons.Default.CloudDone,
                iconTint = IncomeColor,
                trailingContent = {
                    Surface(
                        shape = VyxelShapeTokens.Pill,
                        color = IncomeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Air-Gapped",
                            style = MaterialTheme.typography.labelSmall,
                            color = IncomeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            )

            ExpressiveItemDivider()

            ExpressiveSettingsItem(
                title = "Storage Access Framework",
                subtitle = "You retain complete custody of all exported backups and reports",
                icon = Icons.Default.Lock
            )
        }

        // ── 2. Local-First Guarantees ──────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Local-First Guarantees") {
            ExpressiveSettingsItem(
                title = "No Cloud Telemetry or Trackers",
                subtitle = "Zero outbound network telemetry or analytics SDKs embedded in KnowSpends",
                icon = Icons.Default.Lock
            )
        }
    }
}
