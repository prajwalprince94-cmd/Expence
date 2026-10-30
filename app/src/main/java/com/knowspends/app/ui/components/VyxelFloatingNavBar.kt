package com.knowspends.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import com.knowspends.app.ui.navigation.Screen
import com.knowspends.app.ui.theme.KnowSpendsMotion
import com.knowspends.app.ui.theme.glassSurface
import com.knowspends.app.ui.theme.pressScale

/**
 * Floating Pill Navigation Bar faithfully integrated from Vyxel Apps and Material 3 Expressive.
 *
 * Characteristics:
 * - Floats gracefully as a unified frosted glass pill over application content.
 * - Active tab expands with spring physics revealing its typography label on a punchy primary pill.
 * - Inactive tabs collapse to icon-only mode to prevent visual clutter and screen edge collision.
 * - Micro-interaction feedback: Bouncy icon scale animation and tactile spring press scale on touch.
 */
@Composable
fun VyxelFloatingNavBar(
    items: List<Screen>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    badgeMap: Map<String, Int> = emptyMap()
) {
    val pillShape = RoundedCornerShape(percent = 50)
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = pillShape,
                spotColor = colorScheme.primary.copy(alpha = 0.20f),
                ambientColor = Color.Black.copy(alpha = 0.25f)
            )
            .glassSurface(
                shape = pillShape,
                containerColor = colorScheme.surfaceContainerHigh.copy(alpha = 0.90f),
                borderColor = colorScheme.outlineVariant.copy(alpha = 0.32f),
                borderWidth = 1.dp,
                blurRadius = 24f
            )
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
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
    val pillShape = RoundedCornerShape(percent = 50)

    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.10f else 1.0f,
        animationSpec = KnowSpendsMotion.bouncy(),
        label = "vyxelNavIconScale"
    )

    Row(
        modifier = Modifier
            .pressScale(0.955f)
            .clip(pillShape)
            .background(if (selected) scheme.primary else Color.Transparent)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(
                horizontal = if (selected) 14.dp else 11.dp,
                vertical = 10.dp
            )
            .animateContentSize(animationSpec = KnowSpendsMotion.expressive()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            screen.icon?.let { iconVector ->
                Icon(
                    imageVector = iconVector,
                    contentDescription = screen.title,
                    tint = if (selected) scheme.onPrimary else scheme.onSurfaceVariant.copy(alpha = 0.85f),
                    modifier = Modifier
                        .size(20.dp)
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

        AnimatedContent(
            targetState = selected,
            transitionSpec = {
                (fadeIn(KnowSpendsMotion.fade(180)) + scaleIn(KnowSpendsMotion.expressive(), 0.7f))
                    .togetherWith(fadeOut(KnowSpendsMotion.fade(100)))
            },
            label = "vyxelNavLabel"
        ) { isSelected ->
            if (isSelected) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.2.sp
                        ),
                        color = scheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Spacer(Modifier.width(0.dp))
            }
        }
    }
}
