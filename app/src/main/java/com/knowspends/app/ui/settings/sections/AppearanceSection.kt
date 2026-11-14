package com.knowspends.app.ui.settings.sections

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowspends.app.domain.model.UserPreferences
import com.knowspends.app.ui.components.bouncyClickable
import com.knowspends.app.ui.components.VyxelNavBarBottomClearance
import com.knowspends.app.ui.components.expressive.ExpressiveConnectedButtonGroup
import com.knowspends.app.ui.components.expressive.ExpressiveItemDivider
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsGroup
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsSwitch
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.KnowSpendsMotion
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.getFontFamily
import com.knowspends.app.util.VyxelHaptics

@Composable
fun AppearanceSection(
    userPreferences: UserPreferences,
    onSetThemeMode: (String) -> Unit,
    onSetDynamicColor: (Boolean) -> Unit,
    onSetHaptics: (Boolean) -> Unit,
    onSetFontId: (String) -> Unit,
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
                    text = "KnowSpends is calibrated for high-contrast OLED dark surfaces and battery efficiency",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )

                val themes = listOf(
                    Triple("system", "System", Icons.Default.PhoneAndroid),
                    Triple("dark", "Dark", Icons.Default.DarkMode),
                    Triple("amoled", "AMOLED", Icons.Default.Contrast),
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

        // ── 2. Typography & Font Family ───────────────────────────────────────
        ExpressiveSettingsGroup(title = "Typography & Typeface") {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Application Typeface",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Text(
                    text = "Select your preferred font family. Changes are applied instantly across the entire application interface.",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )

                // Live Preview Card
                Surface(
                    shape = VyxelShapeTokens.CardMedium,
                    color = scheme.surfaceContainerHigh.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE PREVIEW",
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = VyxelShapeTokens.Pill,
                                color = scheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = userPreferences.fontId.replace("_", " ").replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "${userPreferences.currencySymbol} 1,42,850.00",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        )
                        Text(
                            text = "Net worth across 4 liquid accounts and investments",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "+ ${userPreferences.currencySymbol} 45,000.00 Inflow",
                                style = MaterialTheme.typography.labelSmall,
                                color = IncomeColor,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant
                            )
                            Text(
                                text = "- ${userPreferences.currencySymbol} 18,250.00 Burn",
                                style = MaterialTheme.typography.labelSmall,
                                color = ExpenseColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Font Options List
                val fontList = listOf(
                    Triple("plus_jakarta_sans", "Plus Jakarta Sans", "Geometric Neo-Grotesk (Default)"),
                    Triple("inter", "Inter", "Ultra-Clean Neutral Interface"),
                    Triple("outfit", "Outfit", "Friendly Rounded Modern"),
                    Triple("share_tech_mono", "Share Tech Mono", "Technical Financial Monospace"),
                    Triple("orbitron", "Orbitron", "Futuristic Sci-Fi Display"),
                    Triple("kilo", "Kilo", "Bold Contemporary Sans"),
                    Triple("chococooky", "Chococooky", "Playful Handwritten"),
                    Triple("default", "System Default", "Android Platform Native")
                )

                fontList.forEach { (id, name, desc) ->
                    val isSelected = userPreferences.fontId == id
                    val itemBg by animateColorAsState(
                        targetValue = if (isSelected) scheme.primaryContainer.copy(alpha = 0.35f) else scheme.surfaceContainerHigh.copy(alpha = 0.25f),
                        animationSpec = KnowSpendsMotion.snappy,
                        label = "fontBg_$id"
                    )
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) scheme.primary else scheme.outlineVariant.copy(alpha = 0.25f),
                        animationSpec = KnowSpendsMotion.snappy,
                        label = "fontBorder_$id"
                    )

                    Surface(
                        shape = VyxelShapeTokens.CardSmall,
                        color = itemBg,
                        border = BorderStroke(if (isSelected) 1.5.dp else 0.8.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .bouncyClickable(scaleDown = 0.98f) {
                                onSetFontId(id)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) scheme.primary else scheme.surfaceContainerHighest
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = scheme.onPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    fontFamily = getFontFamily(id),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) scheme.primary else scheme.onSurface
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "Aa 123",
                                fontFamily = getFontFamily(id),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) scheme.primary else scheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ── 3. Tactile Feedback ───────────────────────────────────────────────
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

        // ── 4. Monospace Financial Rhythm Verification ────────────────────────
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
