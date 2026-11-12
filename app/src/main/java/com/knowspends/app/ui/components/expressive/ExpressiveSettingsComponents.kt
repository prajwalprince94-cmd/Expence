package com.knowspends.app.ui.components.expressive

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowspends.app.ui.theme.KnowSpendsMotion
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.pressScale
import com.knowspends.app.util.VyxelHaptics

/**
 * Material 3 Expressive Grouped Settings Container.
 * Encapsulates multiple settings rows into a single cohesive card surface with subtle borders,
 * eliminating the visual bloat of individual heavy cards.
 */
@Composable
fun ExpressiveSettingsGroup(
    title: String? = null,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = scheme.primary,
                modifier = Modifier.padding(start = 10.dp)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VyxelShapeTokens.CardSmall,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

/**
 * Standardized Material 3 Expressive Settings Row.
 * Height is compact (~52dp), typography follows 14sp SemiBold / 12sp Normal hierarchy.
 */
@Composable
fun ExpressiveSettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconBackground: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    val clickableModifier = if (onClick != null) {
        Modifier
            .pressScale(0.98f)
            .clickable {
                VyxelHaptics.tick(context)
                onClick()
            }
    } else Modifier

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(VyxelShapeTokens.IconSmall)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }

        if (trailingContent != null) {
            trailingContent()
        } else if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Standardized Material 3 Expressive Switch Row.
 * Clicking anywhere toggles state with instant haptic feedback.
 */
@Composable
fun ExpressiveSettingsSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    val context = LocalContext.current

    ExpressiveSettingsItem(
        title = title,
        subtitle = subtitle,
        icon = icon,
        iconTint = iconTint,
        onClick = {
            VyxelHaptics.tick(context)
            onCheckedChange(!checked)
        },
        modifier = modifier,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = {
                    VyxelHaptics.tick(context)
                    onCheckedChange(it)
                }
            )
        }
    )
}

/**
 * Material 3 Expressive Connected Segmented Button Group.
 * Replaces disconnected chunky block buttons with cohesive connected buttons with morphed shapes.
 */
@Composable
fun <T> ExpressiveConnectedButtonGroup(
    options: List<T>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
    labelProvider: (T) -> String,
    iconProvider: ((T) -> ImageVector?)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = selectedIndex == index
            val isFirst = index == 0
            val isLast = index == options.lastIndex

            val shape: Shape = when {
                isFirst && isLast -> VyxelShapeTokens.Pill
                isFirst -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, topEnd = 4.dp, bottomEnd = 4.dp)
                isLast -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp)
                else -> RoundedCornerShape(4.dp)
            }

            val bgAnim by animateColorAsState(
                targetValue = if (isSelected) scheme.primaryContainer else scheme.surfaceContainerHigh.copy(alpha = 0.5f),
                animationSpec = KnowSpendsMotion.expressive(),
                label = "btnBg"
            )

            val textAnim by animateColorAsState(
                targetValue = if (isSelected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                animationSpec = KnowSpendsMotion.expressive(),
                label = "btnText"
            )

            val border = if (isSelected) {
                BorderStroke(1.2.dp, scheme.primary)
            } else {
                BorderStroke(0.8.dp, scheme.outlineVariant.copy(alpha = 0.25f))
            }

            Surface(
                onClick = {
                    VyxelHaptics.tick(context)
                    onSelectIndex(index)
                },
                modifier = Modifier
                    .weight(1f)
                    .pressScale(0.96f),
                shape = shape,
                color = bgAnim,
                border = border
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val icon = iconProvider?.invoke(option)
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = textAnim,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        text = labelProvider(option),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textAnim,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Compact Material 3 Expressive Inline Stepper.
 * Sits cleanly inside a settings row: [-] value [+] with zero screen bloating.
 */
@Composable
fun ExpressiveInlineStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: ClosedRange<Int>,
    modifier: Modifier = Modifier,
    valueFormatter: (Int) -> String = { it.toString() }
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Surface(
        modifier = modifier,
        shape = VyxelShapeTokens.Pill,
        color = scheme.surfaceContainerHigh.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = {
                    if (value > range.start) {
                        VyxelHaptics.tick(context)
                        onValueChange(value - 1)
                    }
                },
                enabled = value > range.start,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = if (value > range.start) scheme.primary else scheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = valueFormatter(value),
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            IconButton(
                onClick = {
                    if (value < range.endInclusive) {
                        VyxelHaptics.tick(context)
                        onValueChange(value + 1)
                    }
                },
                enabled = value < range.endInclusive,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = if (value < range.endInclusive) scheme.primary else scheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Standard divider between items in an ExpressiveSettingsGroup.
 */
@Composable
fun ExpressiveItemDivider(
    modifier: Modifier = Modifier,
    inset: Boolean = true
) {
    val scheme = MaterialTheme.colorScheme
    HorizontalDivider(
        modifier = modifier.padding(
            start = if (inset) 56.dp else 14.dp,
            end = 14.dp
        ),
        thickness = 0.5.dp,
        color = scheme.outlineVariant.copy(alpha = 0.25f)
    )
}
