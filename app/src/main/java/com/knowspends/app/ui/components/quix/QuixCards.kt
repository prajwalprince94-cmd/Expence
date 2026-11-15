package com.knowspends.app.ui.components.quix

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowspends.app.ui.theme.QuixTokens
import com.knowspends.app.ui.theme.pressScale

/**
 * Quix Studio Obsidian Card
 * Source: https://www.quixstudio.net/free/components
 *
 * Distinctive deep obsidian background (rgb(23, 25, 24)) with subtle 8% white outline
 * and 16dp rounded corners.
 */
@Composable
fun QuixCard(
    modifier: Modifier = Modifier,
    shape: Shape = QuixTokens.CardShape,
    containerColor: Color = QuixTokens.ObsidianCard,
    border: BorderStroke? = QuixTokens.BorderCardStroke,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableMod = if (onClick != null) {
        Modifier
            .pressScale(targetScale = 0.985f)
            .clickable(onClick = onClick)
    } else Modifier

    Surface(
        modifier = modifier.then(clickableMod),
        shape = shape,
        color = containerColor,
        border = border
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

/**
 * Quix Studio Glass Pill Badge
 * 999dp circular pill with subtle glassmorphic outline and optional status dot.
 */
@Composable
fun QuixPillBadge(
    text: String,
    modifier: Modifier = Modifier,
    dotColor: Color? = null,
    accentColor: Color = QuixTokens.TextPrimary,
    containerColor: Color = QuixTokens.ObsidianGlass,
    border: BorderStroke = QuixTokens.BorderPillStroke,
    icon: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(QuixTokens.PillShape)
            .background(containerColor)
            .border(border, QuixTokens.PillShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(dotColor, CircleShape)
                )
            }
            if (icon != null) {
                icon()
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp
                ),
                color = accentColor
            )
        }
    }
}

/**
 * Quix Studio Signature Neon-Lime Primary Pill Button
 */
@Composable
fun QuixNeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = QuixTokens.PillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = QuixTokens.NeonLime,
            contentColor = QuixTokens.ObsidianCanvas,
            disabledContainerColor = QuixTokens.NeonLime.copy(alpha = 0.35f),
            disabledContentColor = QuixTokens.ObsidianCanvas.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        modifier = modifier
            .height(40.dp)
            .pressScale(0.97f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (leadingIcon != null) leadingIcon()
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            )
        }
    }
}
