package com.knowspends.app.ui.settings.sections

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.pressScale
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 1. Recurring Subscriptions & Bills Link ───────────────────────────
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Repeat, contentDescription = null, tint = scheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Recurring & Subscriptions Hub", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                        Text(text = "Manage Netflix, Spotify, rent, EMI, and annual projections.", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                    }
                }

                OutlinedButton(
                    onClick = {
                        VyxelHaptics.tick(context)
                        onNavigateToSubscriptions()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(0.97f),
                    shape = VyxelShapeTokens.Pill
                ) {
                    Text("Open Subscriptions & Bills")
                    Spacer(Modifier.weight(1f))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        // ── 2. Bank SMS Auto-Detection ─────────────────────────────────────────
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Auto-Detect Bank SMS & UPI", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                        Spacer(Modifier.height(2.dp))
                        Text(text = "Automatically parse bank debit/credit messages as they arrive.", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = smsEnabled,
                        onCheckedChange = { checked ->
                            VyxelHaptics.tick(context)
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
                }

                if (smsEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Direct Auto-Add to Ledger", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = scheme.onSurface)
                            Text(text = "Skip Review Inbox and log recognized SMS directly into your ledger.", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.width(12.dp))
                        Switch(
                            checked = directAutoAdd,
                            onCheckedChange = {
                                VyxelHaptics.tick(context)
                                directAutoAdd = it
                                onSaveSmsSettings(smsEnabled, it)
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            VyxelHaptics.tick(context)
                            onScanPastSms()
                        },
                        enabled = !isScanningSms,
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressScale(0.97f),
                        shape = VyxelShapeTokens.Pill
                    ) {
                        if (isScanningSms) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Scanning Device SMS...")
                        } else {
                            Icon(imageVector = Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Scan Past 30 Days Bank SMS")
                        }
                    }
                }
            }
        }

        // ── 3. Back-Tap & Dynamic Island Quick-Spend ───────────────────────────
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Back-Tap Quick Spend Pill", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                        Spacer(Modifier.height(2.dp))
                        Text(text = "Double-tap back of phone to summon the floating Dynamic Island spend logger anywhere.", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = backTapEnabled,
                        onCheckedChange = {
                            VyxelHaptics.tick(context)
                            backTapEnabled = it
                            onSaveBackTapSettings(it, backTapSensitivity)
                        }
                    )
                }

                if (backTapEnabled) {
                    Text(text = "SENSOR SENSITIVITY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("high" to "High", "normal" to "Normal", "low" to "Low").forEach { (sens, label) ->
                            val isSelected = backTapSensitivity == sens
                            Button(
                                onClick = {
                                    VyxelHaptics.tick(context)
                                    backTapSensitivity = sens
                                    onSaveBackTapSettings(backTapEnabled, sens)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .pressScale(0.96f),
                                shape = VyxelShapeTokens.Pill,
                                colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = scheme.primary)
                                else ButtonDefaults.buttonColors(containerColor = scheme.surfaceContainerHigh)
                            ) {
                                Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) scheme.onPrimary else scheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}
