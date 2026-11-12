package com.knowspends.app.ui.settings.sections

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.knowspends.app.domain.model.UserPreferences
import com.knowspends.app.ui.components.expressive.ExpressiveConnectedButtonGroup
import com.knowspends.app.ui.components.expressive.ExpressiveItemDivider
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsGroup
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsItem
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsSwitch
import com.knowspends.app.util.VyxelHaptics

@Composable
fun RecurringAutomationSection(
    userPreferences: UserPreferences,
    isScanningSms: Boolean,
    onSaveSmsSettings: (enabled: Boolean, directAutoAdd: Boolean) -> Unit,
    onSaveBackTapSettings: (enabled: Boolean, sensitivity: String) -> Unit,
    onScanPastSms: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    var smsEnabled by remember(userPreferences.smsAutoCaptureEnabled) { mutableStateOf(userPreferences.smsAutoCaptureEnabled) }
    var directAutoAdd by remember(userPreferences.smsDirectAutoAdd) { mutableStateOf(userPreferences.smsDirectAutoAdd) }
    var backTapEnabled by remember(userPreferences.backTapEnabled) { mutableStateOf(userPreferences.backTapEnabled) }
    var backTapSensitivity by remember(userPreferences.backTapSensitivity) { mutableStateOf(userPreferences.backTapSensitivity) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            smsEnabled = true
            onSaveSmsSettings(true, directAutoAdd)
            VyxelHaptics.success(context)
        } else {
            smsEnabled = false
            onSaveSmsSettings(false, directAutoAdd)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. Subscriptions Hub Link ─────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Recurring Commitments") {
            ExpressiveSettingsItem(
                title = "Subscriptions & Fixed Bills",
                subtitle = "Manage Netflix, Spotify, rent, SIP investments, and projected burn",
                icon = Icons.Default.Repeat,
                showChevron = true,
                onClick = onNavigateToSubscriptions
            )
        }

        // ── 2. Bank SMS Auto-Detection ─────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Bank SMS & UPI Automation") {
            ExpressiveSettingsSwitch(
                title = "Auto-Capture Bank SMS",
                subtitle = "Parse debit and credit transactions instantly on arrival",
                icon = Icons.Default.Sms,
                checked = smsEnabled,
                onCheckedChange = { checked ->
                    if (checked) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                            smsEnabled = true
                            onSaveSmsSettings(true, directAutoAdd)
                        } else {
                            smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                        }
                    } else {
                        smsEnabled = false
                        onSaveSmsSettings(false, directAutoAdd)
                    }
                }
            )

            if (smsEnabled) {
                ExpressiveItemDivider()

                ExpressiveSettingsSwitch(
                    title = "Direct Auto-Add to Ledger",
                    subtitle = "Skip inbox review and log recognized bank transactions directly",
                    icon = Icons.Default.FlashOn,
                    checked = directAutoAdd,
                    onCheckedChange = {
                        directAutoAdd = it
                        onSaveSmsSettings(smsEnabled, it)
                    }
                )

                ExpressiveItemDivider()

                ExpressiveSettingsItem(
                    title = "Scan Past 30 Days Bank SMS",
                    subtitle = "Detect historical transactions and import recognized expenses",
                    icon = Icons.Default.ElectricBolt,
                    onClick = if (!isScanningSms) onScanPastSms else null,
                    trailingContent = {
                        if (isScanningSms) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "Scan",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.primary
                            )
                        }
                    }
                )
            }
        }

        // ── 3. Quick Spend Gesture ────────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Quick Logging Gestures") {
            ExpressiveSettingsSwitch(
                title = "Back-Tap Quick Logger",
                subtitle = "Double-tap device back to summon floating logger over any app",
                icon = Icons.Default.TouchApp,
                checked = backTapEnabled,
                onCheckedChange = {
                    backTapEnabled = it
                    onSaveBackTapSettings(it, backTapSensitivity)
                }
            )

            if (backTapEnabled) {
                ExpressiveItemDivider()

                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Sensor Sensitivity",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = "Calibrates accelerometer threshold for physical tap detection",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )

                    val sensitivities = listOf("high" to "High", "normal" to "Normal", "low" to "Low")
                    val sensIdx = sensitivities.indexOfFirst { it.first == backTapSensitivity }.coerceAtLeast(0)

                    ExpressiveConnectedButtonGroup(
                        options = sensitivities,
                        selectedIndex = sensIdx,
                        onSelectIndex = { idx ->
                            val chosen = sensitivities[idx].first
                            backTapSensitivity = chosen
                            onSaveBackTapSettings(backTapEnabled, chosen)
                        },
                        labelProvider = { it.second },
                        iconProvider = { Icons.Default.Sensors }
                    )
                }
            }
        }
    }
}
