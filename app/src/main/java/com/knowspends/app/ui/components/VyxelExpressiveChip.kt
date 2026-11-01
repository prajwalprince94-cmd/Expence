package com.knowspends.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.util.VyxelHaptics

/**
 * Material 3 Expressive Filter & Action Chip.
 * Features:
 * - Fluid spring press animations & tactile haptic tick
 * - Animated leading checkmark when selected
 * - Tonal expressive color transitions
 * - Soft pill / squircle silhouette
 */
@Composable
fun VyxelExpressiveChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    badgeCount: Int? = null,
    shape: Shape = VyxelShapeTokens.Pill,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    unselectedContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    val containerColor by animateColorAsState(
        targetValue = if (selected) selectedContainerColor else unselectedContainerColor,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f),
        label = "chipContainer"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) selectedContentColor else unselectedContentColor,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f),
        label = "chipContent"
    )

    val border = if (selected) {
        BorderStroke(1.2.dp, scheme.primary.copy(alpha = 0.45f))
    } else {
        BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.40f))
    }

    Surface(
        shape = shape,
        color = containerColor,
        border = border,
        modifier = modifier
            .defaultMinSize(minHeight = 34.dp)
            .bouncyClickable {
                VyxelHaptics.tick(context)
                onClick()
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Animated Checkmark or Custom Leading Icon
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn() + scaleIn(spring(dampingRatio = 0.65f, stiffness = 400f)),
                exit = fadeOut() + scaleOut(spring(dampingRatio = 0.85f, stiffness = 500f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = contentColor
                    )
                    Spacer(Modifier.width(5.dp))
                }
            }

            if (!selected && icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = contentColor
                )
                Spacer(Modifier.width(6.dp))
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )

            // Optional trailing badge (e.g. unreviewed count)
            if (badgeCount != null && badgeCount > 0) {
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = if (selected) scheme.primary else scheme.surfaceContainerHighest,
                    modifier = Modifier.size(18.dp)
                ) {
                    Text(
                        text = "$badgeCount",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) scheme.onPrimary else scheme.primary,
                        modifier = Modifier.padding(1.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
