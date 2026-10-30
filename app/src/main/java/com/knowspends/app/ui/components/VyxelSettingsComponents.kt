package com.knowspends.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowspends.app.ui.theme.KnowSpendsMotion
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.glassSurface
import com.knowspends.app.ui.theme.pressScale

@Composable
fun VyxelSettingsCard(
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val shape = VyxelShapeTokens.Card

    Column(modifier = modifier.fillMaxWidth()) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = shape,
                    spotColor = Color.Black.copy(alpha = 0.08f),
                    ambientColor = Color.Black.copy(alpha = 0.04f)
                )
                .glassSurface(
                    shape = shape,
                    containerColor = scheme.surfaceContainerLow,
                    borderColor = scheme.outlineVariant.copy(alpha = 0.7f),
                    borderWidth = 1.dp
                )
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun VyxelToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun VyxelActionRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(0.98f)
            .clip(VyxelShapeTokens.CardSmall)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }

        if (trailingContent != null) {
            Spacer(Modifier.width(8.dp))
            trailingContent()
        }
    }
}

@Composable
fun VyxelActionPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isPrimary: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    val pillShape = VyxelShapeTokens.Pill

    val bgColor by animateColorAsState(
        targetValue = when {
            !enabled -> scheme.surfaceContainerHighest.copy(alpha = 0.5f)
            isPrimary -> scheme.primary
            else -> scheme.secondaryContainer
        },
        animationSpec = KnowSpendsMotion.expressive(),
        label = "pillBgColor"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> scheme.onSurfaceVariant.copy(alpha = 0.4f)
            isPrimary -> scheme.onPrimary
            else -> scheme.onSecondaryContainer
        },
        animationSpec = KnowSpendsMotion.expressive(),
        label = "pillTextColor"
    )

    val pillBorder = if (!isPrimary) {
        Modifier.border(1.dp, scheme.outlineVariant.copy(alpha = 0.55f), pillShape)
    } else Modifier

    Box(
        modifier = modifier
            .pressScale(if (enabled) 0.94f else 1f)
            .clip(pillShape)
            .background(bgColor)
            .then(pillBorder)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(5.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

data class ThemeModeOption(
    val id: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun VyxelThemeSegmentedRow(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val options = listOf(
        ThemeModeOption("system", "System", Icons.Default.PhoneAndroid),
        ThemeModeOption("light", "Light", Icons.Default.LightMode),
        ThemeModeOption("dark", "Dark", Icons.Default.DarkMode),
        ThemeModeOption("amoled", "AMOLED", Icons.Default.AutoAwesome)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(VyxelShapeTokens.Pill)
            .background(scheme.surfaceContainerHighest.copy(alpha = 0.5f))
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.6f), VyxelShapeTokens.Pill)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedTheme == option.id

            val bgAnim by animateColorAsState(
                targetValue = if (isSelected) scheme.primary else Color.Transparent,
                animationSpec = KnowSpendsMotion.expressive(),
                label = "themeBg"
            )
            val fgAnim by animateColorAsState(
                targetValue = if (isSelected) scheme.onPrimary else scheme.onSurfaceVariant,
                animationSpec = KnowSpendsMotion.expressive(),
                label = "themeFg"
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .pressScale(0.95f)
                    .clip(VyxelShapeTokens.Pill)
                    .background(bgAnim)
                    .clickable { onThemeSelected(option.id) }
                    .padding(vertical = 7.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = fgAnim,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = fgAnim
                )
            }
        }
    }
}
