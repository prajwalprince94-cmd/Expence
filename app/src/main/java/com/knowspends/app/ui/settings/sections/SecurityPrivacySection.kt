package com.knowspends.app.ui.settings.sections

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.knowspends.app.ui.components.VyxelNavBarBottomClearance
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.knowspends.app.domain.model.SecurityConfig
import com.knowspends.app.domain.model.UserPreferences
import com.knowspends.app.ui.components.expressive.ExpressiveConnectedButtonGroup
import com.knowspends.app.ui.components.expressive.ExpressiveItemDivider
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsGroup
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsItem
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsSwitch
import com.knowspends.app.ui.theme.IncomeColor
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. Biometrics & Lock Group ────────────────────────────────────────
        ExpressiveSettingsGroup(title = "App Lock & Authentication") {
            ExpressiveSettingsSwitch(
                title = "Biometric Lock",
                subtitle = "Require fingerprint or face recognition when opening app",
                icon = Icons.Default.Fingerprint,
                checked = securityConfig.isBiometricEnabled,
                onCheckedChange = {
                    onSetBiometric(it)
                }
            )

            if (securityConfig.isBiometricEnabled) {
                ExpressiveItemDivider()

                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Auto-Lock Timeout",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = "Time outside app before authentication is required again",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )

                    val timeouts = listOf(0 to "Immediately", 1 to "1 min", 5 to "5 min", 15 to "15 min")
                    val currentIdx = timeouts.indexOfFirst { it.first == securityConfig.lockTimeoutMinutes }.coerceAtLeast(0)

                    ExpressiveConnectedButtonGroup(
                        options = timeouts,
                        selectedIndex = currentIdx,
                        onSelectIndex = { idx ->
                            onSetLockTimeout(timeouts[idx].first)
                        },
                        labelProvider = { it.second },
                        iconProvider = { Icons.Default.Timer }
                    )
                }
            }
        }

        // ── 2. Screen Privacy ─────────────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Screen Privacy & Defense") {
            ExpressiveSettingsSwitch(
                title = "Hide Balances on Home",
                subtitle = "Obscures sensitive figures (••••••) in public places until tapped",
                icon = Icons.Default.VisibilityOff,
                checked = hideBalances,
                onCheckedChange = {
                    hideBalances = it
                    onSavePrivacy(it, secureScreen)
                }
            )

            ExpressiveItemDivider()

            ExpressiveSettingsSwitch(
                title = "Block Screenshots & Recents Preview",
                subtitle = "Blurs app card in Android task switcher and blocks capture (FLAG_SECURE)",
                icon = Icons.Default.Lock,
                checked = secureScreen,
                onCheckedChange = {
                    secureScreen = it
                    onSetSecureScreen(it)
                    onSavePrivacy(hideBalances, it)
                }
            )
        }

        // ── 3. Permission Transparency ────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Device Permissions Transparency") {
            val hasSms = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
            val hasNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
            val hasOverlay = Settings.canDrawOverlays(context)

            PermissionItem(
                name = "SMS Parsing (READ_SMS)",
                reason = "Parses bank debit & credit alerts offline on-device",
                isGranted = hasSms
            )

            ExpressiveItemDivider()

            PermissionItem(
                name = "Notifications (POST_NOTIFICATIONS)",
                reason = "Delivers budget alerts and daily logging reminders",
                isGranted = hasNotif
            )

            ExpressiveItemDivider()

            PermissionItem(
                name = "Display Over Other Apps (SYSTEM_ALERT_WINDOW)",
                reason = "Required to summon Quick Spend Island over any app on back-tap",
                isGranted = hasOverlay,
                onClick = if (!hasOverlay) {
                    {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                } else null
            )

            ExpressiveItemDivider()

            PermissionItem(
                name = "Vibration Engine (VIBRATE)",
                reason = "Provides sensory micro-clicks on keypad and interactions",
                isGranted = true
            )
        }

        Spacer(modifier = Modifier.height(VyxelNavBarBottomClearance).navigationBarsPadding())
    }
}

@Composable
private fun PermissionItem(
    name: String,
    reason: String,
    isGranted: Boolean,
    onClick: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.RemoveCircleOutline,
            contentDescription = null,
            tint = if (isGranted) IncomeColor else scheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
            Text(
                text = reason,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }

        Text(
            text = if (isGranted) "Granted" else if (onClick != null) "Tap to Grant" else "Optional",
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = if (isGranted) IncomeColor else if (onClick != null) scheme.primary else scheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}
