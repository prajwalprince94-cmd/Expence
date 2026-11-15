package com.knowspends.app.ui.components.motion

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowspends.app.ui.theme.QuixColors

/**
 * Transitions.dev / HeroUI-inspired Morphing Segmented Pill Track:
 * Hosts mutually exclusive filter options within a capsule track.
 * A highlighted pill slides fluidly between selections using physics-based spring choreography.
 * Fully accessible with Role.Tab selection semantics.
 */
@Composable
fun <T> MorphingSegmentedPillTrack(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: (T) -> String,
    icon: ((T) -> ImageVector?)? = null,
    trackHeight: Dp = 44.dp,
    trackColor: Color = QuixColors.ObsidianSurfaceCard,
    activePillColor: Color = QuixColors.NeonLime,
    activeTextColor: Color = QuixColors.ObsidianDeep,
    inactiveTextColor: Color = QuixColors.TextMuted,
    borderColor: Color = QuixColors.BorderSubtle
) {
    if (items.isEmpty()) return

    val selectedIndex = items.indexOf(selectedItem).coerceIn(0, items.size - 1)
    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "pill_track_slide"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .clip(CircleShape)
            .background(trackColor)
            .border(BorderStroke(1.dp, borderColor), CircleShape)
            .padding(4.dp)
    ) {
        val totalWidth = maxWidth
        val count = items.size
        val pillWidth = totalWidth / count
        val slidingOffset = pillWidth * animatedIndex

        // Fluid sliding indicator pill
        Box(
            modifier = Modifier
                .offset(x = slidingOffset)
                .width(pillWidth)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(activePillColor)
        )

        // Interactive item labels with accessibility semantics
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) activeTextColor else inactiveTextColor,
                    animationSpec = tween(durationMillis = 200),
                    label = "pill_text_color"
                )

                val itemIcon = icon?.invoke(item)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .selectable(
                            selected = isSelected,
                            role = Role.Tab,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onItemSelected(item) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        if (itemIcon != null) {
                            Icon(
                                imageVector = itemIcon,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = label(item),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
