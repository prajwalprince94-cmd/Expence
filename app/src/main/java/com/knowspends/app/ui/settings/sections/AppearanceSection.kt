package com.knowspends.app.ui.settings.sections

import android.os.Build
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowspends.app.domain.model.UserPreferences
import com.knowspends.app.ui.components.expressive.ExpressiveConnectedButtonGroup
import com.knowspends.app.ui.components.expressive.ExpressiveItemDivider
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsGroup
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsSwitch
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.util.VyxelHaptics

@Composable
fun AppearanceSection(
    userPreferences: UserPreferences,
    onSetThemeMode: (String) -> Unit,
    onSetDynamicColor: (Boolean) -> Unit,
    onSetHaptics: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. Theme Mode Group ───────────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Theme & Palette") {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Display Theme",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Text(
                    text = "KnowSpends is calibrated for high-contrast OLED dark surfaces",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )

                val themes = listOf(
                    Triple("system", "System", Icons.Default.PhoneAndroid),
                    Triple("dark", "Dark", Icons.Default.DarkMode),
                    Triple("light", "Light", Icons.Default.LightMode)
                )
                val themeIdx = themes.indexOfFirst { it.first == userPreferences.themeMode }.coerceAtLeast(0)

                ExpressiveConnectedButtonGroup(
                    options = themes,
                    selectedIndex = themeIdx,
                    onSelectIndex = { idx ->
                        onSetThemeMode(themes[idx].first)
                    },
                    labelProvider = { it.second },
                    iconProvider = { it.third }
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ExpressiveItemDivider()

                ExpressiveSettingsSwitch(
                    title = "Dynamic Wallpaper Tint",
                    subtitle = "Extract subtle ambient tints from device wallpaper (Material You)",
                    icon = Icons.Default.AutoAwesome,
                    checked = userPreferences.dynamicColor,
                    onCheckedChange = { onSetDynamicColor(it) }
                )
            }
        }

        // ── 2. Tactile Feedback ───────────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Tactile Engine") {
            ExpressiveSettingsSwitch(
                title = "Sensory Haptic Feedback",
                subtitle = "Subtle micro-vibrations for keypad taps, swipe dismiss, and switches",
                icon = Icons.Default.Vibration,
                checked = userPreferences.hapticsEnabled,
                onCheckedChange = {
                    if (it) VyxelHaptics.success(context)
                    onSetHaptics(it)
                }
            )
        }

        // ── 3. Monospace Financial Rhythm Verification ────────────────────────
        ExpressiveSettingsGroup(title = "Tabular Monospace Standard") {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Financial Precision Alignment",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Text(
                    text = "All amounts and dates utilize tabular monospace digits to ensure zero horizontal jitter during animated balance transitions",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )

                Surface(
                    shape = VyxelShapeTokens.CardSmall,
                    color = scheme.surfaceContainerHigh.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Net Worth", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                            Text("${userPreferences.currencySymbol} 3,42,850.00", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold), color = scheme.onSurface)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Monthly Inflow", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                            Text("+ ${userPreferences.currencySymbol} 85,000.00", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold), color = IncomeColor)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Monthly Burn", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                            Text("- ${userPreferences.currencySymbol} 32,450.00", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold), color = ExpenseColor)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(VyxelNavBarBottomClearance).navigationBarsPadding())
    }
}
