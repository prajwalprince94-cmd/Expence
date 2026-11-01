package com.knowspends.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.knowspends.app.ui.theme.ExpressiveShapes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/**
 * Expressive category badge with deterministic gradient monogram fallback (inspired by Vyxel Apps).
 */
@Composable
fun CategoryBadge(
    categoryName: String,
    iconKey: String?,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val icon = remember(iconKey) { resolveCategoryIcon(iconKey) }
    val shape = remember(iconKey, categoryName) { resolveCategoryShape(iconKey, categoryName) }

    if (icon != null) {
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(color.copy(alpha = 0.22f))
                .border(1.dp, color.copy(alpha = 0.28f), shape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = categoryName,
                tint = color,
                modifier = Modifier.size(size * 0.52f)
            )
        }
    } else {
        // Deterministic two-tone monogram fallback with algorithmic shape
        val brush = remember(categoryName) { monogramBrush(categoryName) }
        val initial = remember(categoryName) {
            categoryName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        }

        val textSizeSp = with(LocalDensity.current) { (size * 0.45f).toSp() }

        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(brush)
                .semantics { contentDescription = categoryName },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontSize = textSizeSp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Resolves an algorithmic shape from Material 3 Expressive based on the category semantic type.
 */
fun resolveCategoryShape(iconKey: String?, categoryName: String): Shape {
    val key = (iconKey?.lowercase()?.trim() ?: "") + " " + categoryName.lowercase().trim()
    return when {
        key.contains("restaurant") || key.contains("food") || key.contains("dining") || key.contains("grocer") -> ExpressiveShapes.Scallop
        key.contains("payment") || key.contains("salary") || key.contains("income") || key.contains("work") -> ExpressiveShapes.Clover
        key.contains("car") || key.contains("transport") || key.contains("travel") -> ExpressiveShapes.Bun
        key.contains("shopping") || key.contains("entertainment") || key.contains("movie") || key.contains("fun") -> ExpressiveShapes.Star8
        key.contains("invest") || key.contains("trend") || key.contains("bill") || key.contains("receipt") -> ExpressiveShapes.Star4
        else -> ExpressiveShapes.Clover
    }
}

/**
 * Maps known icon keys to Material 3 Vector Icons.
 */
fun resolveCategoryIcon(iconKey: String?): ImageVector? {
    if (iconKey.isNullOrBlank()) return null
    return when (iconKey.lowercase().trim()) {
        "restaurant", "food", "dining" -> Icons.Default.Restaurant
        "shopping_cart", "groceries", "grocery" -> Icons.Default.ShoppingCart
        "directions_car", "transport", "transportation", "travel" -> Icons.Default.DirectionsCar
        "receipt_long", "bills", "utilities", "bill" -> Icons.Default.ReceiptLong
        "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
        "movie", "entertainment", "fun" -> Icons.Default.Movie
        "medical_services", "health", "medical" -> Icons.Default.MedicalServices
        "payments", "salary", "income" -> Icons.Default.Payments
        "work", "freelance" -> Icons.Default.Work
        "trending_up", "investments", "investment" -> Icons.Default.TrendingUp
        else -> null
    }
}

/**
 * Generates a deterministic two-tone gradient brush using the string's hashcode.
 */
fun monogramBrush(name: String): Brush {
    val hash = abs(name.hashCode())
    val hue1 = (hash % 360).toFloat()
    val hue2 = ((hash + 45) % 360).toFloat()

    val c1 = Color.hsl(hue1, 0.65f, 0.48f)
    val c2 = Color.hsl(hue2, 0.75f, 0.38f)

    return Brush.linearGradient(listOf(c1, c2))
}
