package com.knowspends.app.ui.settings.sections

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowspends.app.BuildConfig
import com.knowspends.app.ui.components.expressive.ExpressiveItemDivider
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsGroup
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsItem
import com.knowspends.app.ui.theme.VyxelShapeTokens

@Composable
fun AboutSupportSection(
    isCheckingUpdate: Boolean,
    onCheckForUpdates: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    val versionName = BuildConfig.VERSION_NAME
    val versionCode = BuildConfig.VERSION_CODE

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. App Identity Card ──────────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Application") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(VyxelShapeTokens.IconSmall)
                        .background(scheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = scheme.onPrimaryContainer
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "KnowSpends",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        )
                        Surface(shape = VyxelShapeTokens.Pill, color = scheme.surfaceContainerHigh) {
                            Text(
                                text = "v$versionName",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = scheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Build $versionCode • Offline SQLite Financial Engine",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            ExpressiveItemDivider()

            ExpressiveSettingsItem(
                title = "Check for Updates",
                subtitle = "Query latest release on GitHub",
                icon = Icons.Default.Sync,
                onClick = onCheckForUpdates,
                trailingContent = {
                    if (isCheckingUpdate) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Check",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.primary
                        )
                    }
                }
            )
        }

        // ── 2. Support & Source Code ──────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Open Source & Support") {
            ExpressiveSettingsItem(
                title = "Report an Issue / Bug",
                subtitle = "Open a bug ticket or request a feature on GitHub",
                icon = Icons.Default.BugReport,
                trailingContent = {
                    Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = scheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                },
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/prajwalprince94-cmd/Expence/issues"))
                    context.startActivity(intent)
                }
            )

            ExpressiveItemDivider()

            ExpressiveSettingsItem(
                title = "Source Code Repository",
                subtitle = "Explore full source and contribute on GitHub",
                icon = Icons.Default.Code,
                trailingContent = {
                    Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = scheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                },
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/prajwalprince94-cmd/Expence"))
                    context.startActivity(intent)
                }
            )

            ExpressiveItemDivider()

            ExpressiveSettingsItem(
                title = "Offline Privacy Guarantee",
                subtitle = "Zero analytics, zero tracking cookies, air-gapped",
                icon = Icons.Default.Policy
            )
        }
    }
}
