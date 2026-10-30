package com.knowspends.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knowspends.app.ui.navigation.Screen
import com.knowspends.app.ui.theme.KnowSpendsMotion
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.glassSurface
import com.knowspends.app.ui.theme.pressScale

/**
 * Floating Pill Navigation Bar faithfully built with Vyxel Apps architecture and aesthetics.
 *
 * Design & Physics Specifications:
 * - Floating pill shape (`RoundedCornerShape(percent = 50)`).
 * - Smooth physics: `animateColorAsState` for background pill & icon tint,
 *   `KnowSpendsMotion.SizeSpring` for whole-bar fluid resizing,
 *   `KnowSpendsMotion.bouncy` for icon pop, and tactile `pressScale(0.94f)`.
 * - Active item grows an animated primary pill (`colorScheme.primary`), scaling its icon by 1.08x
 *   and revealing its label via spring-driven `AnimatedContent` with `labelLarge` typography.
 * - Inactive items collapse to 21.dp icon-only mode with `onSurfaceVariant` tint.
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
            .padding(horizontal = 5.dp, vertical = 5.dp)
            .animateContentSize(animationSpec = KnowSpendsMotion.SizeSpring)
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
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
        targetValue = if (selected) 1.08f else 1.0f,
        animationSpec = KnowSpendsMotion.bouncy(),
        label = "navIconScale"
    )

    val pillBgColor by animateColorAsState(
        targetValue = if (selected) scheme.primary else Color.Transparent,
        animationSpec = KnowSpendsMotion.expressive(),
        label = "navPillBgColor"
    )

    val iconTint by animateColorAsState(
        targetValue = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
        animationSpec = KnowSpendsMotion.expressive(),
        label = "navIconTint"
    )

    Row(
        modifier = Modifier
            .defaultMinSize(minWidth = 36.dp, minHeight = 36.dp)
            .pressScale(0.95f)
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
                horizontal = if (selected) 10.dp else 7.dp,
                vertical = 7.dp
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
                        .clip(pillShape)
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
            label = "navLabel"
        ) { isSelected ->
            if (isSelected) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(4.dp))
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
            } else {
                Spacer(Modifier.width(0.dp))
            }
        }
    }
}
