package com.knowspends.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knowspends.app.ui.navigation.Screen
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.glassSurface
import com.knowspends.app.ui.theme.pressScale

/**
 * Floating Pill Navigation Bar faithfully built with Vyxel Apps architecture and aesthetics.
 *
 * Design & Physics Specifications:
 * - Floating pill shape (`RoundedCornerShape(percent = 50)`).
 * - Generous touch targets (min 44.dp) and standard 24.dp navigation icons.
 * - Fluid, zero-lag switching: Snappy FastOutSlowInEasing interpolations guarantee 120 FPS
 *   frame-drop-free tab switches while preserving tactile depth.
 * - Active item expands an animated primary pill (`colorScheme.primary`) with bold label.
 * - Inactive items comfortably preserve standard touch sizing and never squish off-screen.
 * - Crystal clear high-contrast container with zero child blur (`glassSurface`).
 */
@Composable
fun VyxelFloatingNavBar(
    items: List<Screen>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    badgeMap: Map<String, Int> = emptyMap()
) {
    val pillShape = VyxelShapeTokens.Pill
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = pillShape,
                spotColor = Color.Black.copy(alpha = 0.22f),
                ambientColor = Color.Black.copy(alpha = 0.16f)
            )
            .glassSurface(
                shape = pillShape,
                containerColor = scheme.surfaceContainerHigh,
                borderColor = scheme.outlineVariant.copy(alpha = 0.35f),
                borderWidth = 1.dp
            )
            .padding(horizontal = 7.dp, vertical = 6.dp)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = FastOutSlowInEasing
                )
            )
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items.forEach { screen ->
            val isSelected = currentRoute == screen.route
            val badge = badgeMap[screen.route] ?: 0

            VyxelNavItem(
                screen = screen,
                selected = isSelected,
                badge = badge,
                onClick = { onItemClick(screen) }
            )
        }
    }
}

@Composable
private fun VyxelNavItem(
    screen: Screen,
    selected: Boolean,
    badge: Int,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val scheme = MaterialTheme.colorScheme
    val pillShape = VyxelShapeTokens.Pill

    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "navIconScale"
    )

    val pillBgColor by animateColorAsState(
        targetValue = if (selected) scheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "navPillBgColor"
    )

    val iconTint by animateColorAsState(
        targetValue = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "navIconTint"
    )

    Row(
        modifier = Modifier
            .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
            .pressScale(0.96f)
            .clip(pillShape)
            .background(pillBgColor)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(
                horizontal = if (selected) 13.dp else 10.dp,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            screen.icon?.let { iconVector ->
                Icon(
                    imageVector = iconVector,
                    contentDescription = screen.title,
                    tint = iconTint,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )
            }

            if (badge > 0) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(scheme.error)
                )
            }
        }

        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(animationSpec = tween(durationMillis = 130, easing = FastOutSlowInEasing)) +
                expandHorizontally(animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)) +
                shrinkHorizontally(animationSpec = tween(durationMillis = 130, easing = FastOutSlowInEasing))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(5.dp))
                Text(
                    text = screen.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
