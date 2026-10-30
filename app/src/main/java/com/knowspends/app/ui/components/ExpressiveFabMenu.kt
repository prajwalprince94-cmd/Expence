package com.knowspends.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.ExpressivePolygons
import com.knowspends.app.ui.theme.ExpressiveShapes
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.KnowSpendsMotion
import com.knowspends.app.ui.theme.MorphShape
import com.knowspends.app.ui.theme.TransferColor
import com.knowspends.app.ui.theme.pressScale

/**
 * Material 3 Expressive Floating Action Button Menu.
 *
 * Expands upward with snappy physics springs, presenting quick actions:
 * - Add Expense
 * - Add Income
 * - Transfer
 *
 * Rotates the primary trigger icon 135° into a dismiss 'X' when active.
 */
@Composable
fun ExpressiveFabMenu(
    expanded: Boolean,
    onExpandChanged: (Boolean) -> Unit,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onAddTransfer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 135f else 0f,
        animationSpec = KnowSpendsMotion.snappy(),
        label = "fabRotation"
    )

    // Algorithmic morph from Scallop (cookie) to Circle as FAB expands
    val morphProgress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = KnowSpendsMotion.expressive(),
        label = "fabMorphProgress"
    )

    val fabShape = remember(morphProgress) {
        MorphShape(ExpressivePolygons.ScallopToCircleMorph, morphProgress)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Action Items (revealed when expanded)
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = KnowSpendsMotion.expressive()) +
                    slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = KnowSpendsMotion.expressive()
                    ),
            exit = fadeOut(animationSpec = KnowSpendsMotion.snappy()) +
                    slideOutVertically(
                        targetOffsetY = { it / 2 },
                        animationSpec = KnowSpendsMotion.snappy()
                    )
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Transfer action with 8-Point Star shape
                FabMenuItem(
                    label = "Transfer",
                    icon = Icons.Default.SwapHoriz,
                    containerColor = TransferColor,
                    shape = ExpressiveShapes.Star8,
                    onClick = {
                        onExpandChanged(false)
                        onAddTransfer()
                    }
                )

                // Income action with 4-Leaf Clover shape
                FabMenuItem(
                    label = "Add Income",
                    icon = Icons.Default.ArrowDownward,
                    containerColor = IncomeColor,
                    shape = ExpressiveShapes.Clover,
                    onClick = {
                        onExpandChanged(false)
                        onAddIncome()
                    }
                )

                // Expense action with 4-Point Starburst shape
                FabMenuItem(
                    label = "Add Expense",
                    icon = Icons.Default.ArrowUpward,
                    containerColor = ExpenseColor,
                    shape = ExpressiveShapes.Star4,
                    onClick = {
                        onExpandChanged(false)
                        onAddExpense()
                    }
                )
            }
        }

        // Primary FAB with algorithmic morphing Scallop-to-Circle shape
        FloatingActionButton(
            onClick = { onExpandChanged(!expanded) },
            shape = fabShape,
            containerColor = if (expanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (expanded) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier
                .size(60.dp)
                .pressScale(0.92f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (expanded) "Close Menu" else "Add Transaction",
                modifier = Modifier
                    .size(26.dp)
                    .rotate(rotation)
            )
        }
    }
}

@Composable
private fun FabMenuItem(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    shape: Shape = ExpressiveShapes.Circle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .pressScale(0.93f)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // Expressive Label Chip
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
            tonalElevation = 3.dp,
            modifier = Modifier.defaultMinSize(minHeight = 32.dp)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Algorithmic Shape Mini FAB Icon
        SmallFloatingActionButton(
            onClick = onClick,
            shape = shape,
            containerColor = containerColor,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
