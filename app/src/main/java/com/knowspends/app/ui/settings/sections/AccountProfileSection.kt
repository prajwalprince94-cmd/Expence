package com.knowspends.app.ui.settings.sections

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.knowspends.app.domain.model.UserPreferences
import com.knowspends.app.ui.components.expressive.ExpressiveCompactTextField
import com.knowspends.app.ui.components.expressive.ExpressiveItemDivider
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsGroup
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsItem
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.pressScale
import com.knowspends.app.util.VyxelHaptics

@Composable
fun AccountProfileSection(
    userPreferences: UserPreferences,
    onSaveProfile: (name: String, tagline: String, avatarUri: String) -> Unit,
    onExportData: (Uri) -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    var nameDraft by remember(userPreferences.profileName) { mutableStateOf(userPreferences.profileName) }
    var taglineDraft by remember(userPreferences.profileTagline) { mutableStateOf(userPreferences.profileTagline) }
    var avatarUriDraft by remember(userPreferences.profileAvatarUri) { mutableStateOf(userPreferences.profileAvatarUri) }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { onExportData(it) }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            avatarUriDraft = it.toString()
            onSaveProfile(nameDraft.trim(), taglineDraft.trim(), it.toString())
            VyxelHaptics.success(context)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. Vault Identity Group ───────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Profile & Vault Identity") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(scheme.surfaceContainerHigh)
                            .clickable {
                                VyxelHaptics.tick(context)
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUriDraft.isNotBlank()) {
                            AsyncImage(
                                model = avatarUriDraft,
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = scheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(scheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Update photo",
                                tint = scheme.onPrimary,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (nameDraft.isNotBlank()) nameDraft else "Personal Vault",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        )
                        Text(
                            text = "Tap photo to change avatar",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Vault / Account Name",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    ExpressiveCompactTextField(
                        value = nameDraft,
                        onValueChange = {
                            nameDraft = it
                            onSaveProfile(it.trim(), taglineDraft.trim(), avatarUriDraft)
                        },
                        placeholder = "e.g. Prajwal's Vault"
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Tagline / Subtitle",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    ExpressiveCompactTextField(
                        value = taglineDraft,
                        onValueChange = {
                            taglineDraft = it
                            onSaveProfile(nameDraft.trim(), it.trim(), avatarUriDraft)
                        },
                        placeholder = "e.g. Personal Expenses & Investments"
                    )
                }
            }
        }

        // ── 2. Vault Architecture ─────────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Local Vault Storage") {
            ExpressiveSettingsItem(
                title = "Primary Ledger Database",
                subtitle = "100% offline SQLite database stored safely on device",
                icon = Icons.Default.Security,
                trailingContent = {
                    Surface(
                        shape = VyxelShapeTokens.Pill,
                        color = scheme.primaryContainer
                    ) {
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            )

            ExpressiveItemDivider()

            ExpressiveSettingsItem(
                title = "Export Vault CSV",
                subtitle = "Save a complete tabular record of all transactions",
                icon = Icons.Default.FileDownload,
                onClick = {
                    csvExportLauncher.launch("knowspends_vault_${System.currentTimeMillis()}.csv")
                }
            )
        }
    }
}
