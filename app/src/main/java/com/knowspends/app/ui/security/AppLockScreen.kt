package com.knowspends.app.ui.security

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.pressScale
import com.knowspends.app.util.VyxelHaptics

/**
 * Material 3 Expressive App Lock & Session Expiration Screen.
 * Displays whether the lock was triggered on launch or via inactivity session timeout.
 */
@Composable
fun AppLockScreen(
    onUnlockClicked: () -> Unit,
    modifier: Modifier = Modifier,
    isSessionTimeout: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    val lockScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "lock_icon_scale"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = scheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lock Icon Squircle with Amber Accent Glow
            Box(
                modifier = Modifier
                    .scale(lockScale)
                    .size(80.dp)
                    .clip(VyxelShapeTokens.Card)
                    .background(scheme.surfaceContainerHigh)
                    .border(1.5.dp, scheme.primary.copy(alpha = 0.35f), VyxelShapeTokens.Card),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSessionTimeout) Icons.Default.Timer else Icons.Default.Lock,
                    contentDescription = "App Locked",
                    tint = scheme.primary,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Session timeout pill indicator
            if (isSessionTimeout) {
                Surface(
                    shape = VyxelShapeTokens.Pill,
                    color = scheme.tertiaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, scheme.tertiary.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = scheme.tertiary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Session Inactivity Timeout",
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Text(
                text = if (isSessionTimeout) "Session Expired" else "KnowSpends is Locked",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isSessionTimeout)
                    "Your session timed out due to inactivity. Authenticate to resume."
                else
                    "Your financial vault is encrypted on this device. Authenticate to access.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = {
                    VyxelHaptics.click(context)
                    onUnlockClicked()
                },
                shape = VyxelShapeTokens.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(50.dp)
                    .pressScale(0.97f)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = scheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Unlock with Biometrics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimary
                )
            }
        }
    }
}
