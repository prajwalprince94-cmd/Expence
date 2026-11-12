package com.knowspends.app.ui.settings.sections

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.knowspends.app.domain.model.SecurityConfig
import com.knowspends.app.domain.model.UserPreferences
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.pressScale
import com.knowspends.app.util.VyxelHaptics

@Composable
fun SecurityPrivacySection(
    userPreferences: UserPreferences,
    securityConfig: SecurityConfig,
    onSetBiometric: (Boolean) -> Unit,
    onSetSecureScreen: (Boolean) -> Unit,
    onSetLockTimeout: (Int) -> Unit,
    onSavePrivacy: (hideBalances: Boolean, blockScreenshots: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    var hideBalances by remember(userPreferences.hideBalancesOnHome) { mutableStateOf(userPreferences.hideBalancesOnHome) }
    var secureScreen by remember(securityConfig.isSecureScreenEnabled) { mutableStateOf(securityConfig.isSecureScreenEnabled) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 1. App Lock & Biometrics ──────────────────────────────────────────
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = scheme.primary)
                            Text(text = "Biometric App Lock", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(text = "Require fingerprint or face recognition when opening KnowSpends.", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = securityConfig.isBiometricEnabled,
                        onCheckedChange = {
                            VyxelHaptics.tick(context)
                            onSetBiometric(it)
                        }
                    )
                }

                if (securityConfig.isBiometricEnabled) {
                    Text(text = "AUTO-LOCK TIMEOUT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0 to "Immediately", 1 to "1 Min", 5 to "5 Min", 15 to "15 Min").forEach { (min, label) ->
                            val isSelected = securityConfig.lockTimeoutMinutes == min
                            Button(
                                onClick = {
                                    VyxelHaptics.tick(context)
                                    onSetLockTimeout(min)
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

        // ── 2. Hide Balances on Home ──────────────────────────────────────────
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.VisibilityOff, contentDescription = null, tint = scheme.primary)
                        Text(text = "Hide Balances on Home", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(text = "Obscures sensitive balances (••••••) in public places until tapped.", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = hideBalances,
                    onCheckedChange = {
                        VyxelHaptics.tick(context)
                        hideBalances = it
                        onSavePrivacy(it, secureScreen)
                    }
                )
            }
        }

        // ── 3. Screen Privacy & Anti-Screenshot ───────────────────────────────
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = scheme.primary)
                        Text(text = "Block Screenshots & Recents Preview", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(text = "Prevents screen capture and blurs ledger preview in Android recent apps switcher (FLAG_SECURE).", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = secureScreen,
                    onCheckedChange = {
                        VyxelHaptics.tick(context)
                        secureScreen = it
                        onSetSecureScreen(it)
                        onSavePrivacy(hideBalances, it)
                    }
                )
            }
        }

        // ── 4. Permission Transparency Audit ──────────────────────────────────
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = scheme.primary)
                    Text(text = "Permission Transparency Audit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                }

                Text(
                    text = "KnowSpends never transmits data off your device. Here is what permissions are used for:",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )

                PermissionRow(
                    name = "Read SMS (READ_SMS)",
                    reason = "Parses bank debit/credit alerts offline directly on your device.",
                    isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionRow(
                        name = "Notifications (POST_NOTIFICATIONS)",
                        reason = "Delivers budget threshold alerts and daily evening logging reminders.",
                        isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    )
                }

                PermissionRow(
                    name = "Haptic Vibration (VIBRATE)",
                    reason = "Provides tactile micro-feedback on keypad typing and gestures.",
                    isGranted = true
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(name: String, reason: String, isGranted: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(VyxelShapeTokens.CardSmall)
            .background(scheme.surfaceContainerHigh.copy(alpha = 0.4f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.RemoveCircleOutline,
            contentDescription = null,
            tint = if (isGranted) IncomeColor else scheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = scheme.onSurface)
            Text(text = reason, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}
