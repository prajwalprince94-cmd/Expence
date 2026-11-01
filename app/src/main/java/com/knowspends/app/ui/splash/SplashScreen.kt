package com.knowspends.app.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowspends.app.ui.theme.KnowSpendsMotion
import com.knowspends.app.ui.theme.LocalMotionIntensity
import com.knowspends.app.ui.theme.VyxelShapeTokens
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val motionIntensity = LocalMotionIntensity.current

    val scaleAnim = remember { Animatable(if (motionIntensity <= 0f) 1f else 0.45f) }
    val alphaAnim = remember { Animatable(if (motionIntensity <= 0f) 1f else 0f) }

    LaunchedEffect(motionIntensity) {
        if (motionIntensity <= 0f) {
            scaleAnim.snapTo(1f)
        } else {
            // Entrance spring bounce
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.58f,
                    stiffness = 380f
                )
            )
        }
    }

    LaunchedEffect(motionIntensity) {
        if (motionIntensity <= 0f) {
            alphaAnim.snapTo(1f)
            delay(500L)
        } else {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
            // Splash hold duration before transitioning
            delay(1250L)
        }
        onSplashFinished()
    }

    // Ambient pulsing glow (gated behind motion intensity for a11y compliance)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by if (motionIntensity > 0f) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    val dotAlpha by if (motionIntensity > 0f) {
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dotAlpha"
        )
    } else {
        remember { mutableStateOf(0.7f) }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSplashFinished
            ),
        color = scheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scaleAnim.value
                        scaleY = scaleAnim.value
                        alpha = alphaAnim.value
                    }
                    .padding(32.dp)
            ) {
                // Hero App Monogram Squircle Badge
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .scale(pulseScale)
                        .shadow(
                            elevation = 16.dp,
                            shape = VyxelShapeTokens.Card,
                            spotColor = scheme.primary.copy(alpha = 0.35f),
                            ambientColor = scheme.primary.copy(alpha = 0.20f)
                        )
                        .clip(VyxelShapeTokens.Card)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    scheme.primary,
                                    scheme.tertiary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "KnowSpends",
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Brand Title
                Text(
                    text = "KnowSpends",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.8).sp
                    ),
                    color = scheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Expressive Subtitle / Tagline
                Text(
                    text = "Zero-Network Private Personal Ledger",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.6.sp
                    ),
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Minimal Ambient Activity Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(scheme.primary.copy(alpha = dotAlpha))
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(scheme.primary.copy(alpha = (1f - dotAlpha).coerceIn(0.2f, 1f)))
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(scheme.primary.copy(alpha = dotAlpha))
                    )
                }
            }
        }
    }
}
