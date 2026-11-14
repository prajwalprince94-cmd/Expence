package com.knowspends.app.ui.components.states

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.pressScale
import com.knowspends.app.util.VyxelHaptics
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────
// 1. PERMISSION DENIED CARD & SETTINGS LINK
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Standardized permission denied card with local-first privacy transparency
 * and direct intent to Android System App Settings.
 */
@Composable
fun ExpressivePermissionCard(
    title: String,
    description: String,
    icon: ImageVector = Icons.Default.Security,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
    isPermanentlyDenied: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = VyxelShapeTokens.Card,
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(VyxelShapeTokens.CardSmall)
                    .background(scheme.surfaceContainerHighest)
                    .border(1.dp, scheme.primary.copy(alpha = 0.25f), VyxelShapeTokens.CardSmall),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Local privacy badge
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = VyxelShapeTokens.Pill,
                color = scheme.surfaceContainerHighest.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = scheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "100% On-Device • Zero Cloud Transmission",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isPermanentlyDenied) {
                Button(
                    onClick = {
                        VyxelHaptics.click(context)
                        openAppSettings(context)
                    },
                    shape = VyxelShapeTokens.Pill,
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                    modifier = Modifier.height(42.dp).pressScale(0.97f)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = scheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Open App Settings",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onPrimary
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            VyxelHaptics.click(context)
                            onRequestPermission()
                        },
                        shape = VyxelShapeTokens.Pill,
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                        modifier = Modifier.height(42.dp).pressScale(0.97f)
                    ) {
                        Text(
                            text = "Grant Permission",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            VyxelHaptics.click(context)
                            openAppSettings(context)
                        },
                        shape = VyxelShapeTokens.Pill,
                        modifier = Modifier.height(42.dp).pressScale(0.97f)
                    ) {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. FORM VALIDATION: SHAKE MODIFIER
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Spring-based horizontal oscillation modifier for rejected/invalid form submissions.
 * Triggered whenever [trigger] increments.
 */
fun Modifier.shake(trigger: Int): Modifier = composed {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            animatable.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.25f,
                    stiffness = Spring.StiffnessMedium
                ),
                initialVelocity = 40f
            )
        }
    }

    this.offset { IntOffset(x = animatable.value.roundToInt(), y = 0) }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. SUCCESS FEEDBACK MICRO-INTERACTION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Animated celebratory success pill with checkmark and spring scale.
 */
@Composable
fun ExpressiveSuccessPill(
    message: String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "success_scale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = VyxelShapeTokens.Pill,
        color = scheme.primaryContainer,
        border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(scheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = message,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onPrimaryContainer
            )
        }
    }
}
