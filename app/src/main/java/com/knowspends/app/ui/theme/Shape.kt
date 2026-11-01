package com.knowspends.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Expressive shape scale adapted from Vyxel Apps — noticeably rounder than baseline M3,
 * giving KnowSpends its soft, pill-forward silhouette.
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

object VyxelShapeTokens {
    val Pill = RoundedCornerShape(percent = 50)
    val Card = RoundedCornerShape(24.dp)
    val CardSmall = RoundedCornerShape(16.dp)
    val Chip = RoundedCornerShape(12.dp)
    val IconSmall = RoundedCornerShape(10.dp)
    val ChartBar = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
}
