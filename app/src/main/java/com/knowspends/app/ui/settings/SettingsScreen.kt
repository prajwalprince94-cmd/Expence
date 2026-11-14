package com.knowspends.app.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.knowspends.app.domain.model.UserPreferences
import com.knowspends.app.ui.components.VyxelNavBarBottomClearance
import com.knowspends.app.ui.settings.sections.AboutSupportSection
import com.knowspends.app.ui.settings.sections.AccountProfileSection
import com.knowspends.app.ui.settings.sections.AppearanceSection
import com.knowspends.app.ui.settings.sections.BudgetPreferencesSection
import com.knowspends.app.ui.settings.sections.CategoriesAccountsSection
import com.knowspends.app.ui.settings.sections.CurrencyRegionalSection
import com.knowspends.app.ui.settings.sections.DataManagementSection
import com.knowspends.app.ui.settings.sections.NotificationsSection
import com.knowspends.app.ui.settings.sections.RecurringAutomationSection
import com.knowspends.app.ui.settings.sections.SecurityPrivacySection
import com.knowspends.app.ui.settings.sections.SyncCloudSection
import com.knowspends.app.ui.components.expressive.ExpressiveItemDivider
import com.knowspends.app.ui.components.expressive.ExpressiveSearchPill
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsGroup
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsItem
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.pressScale
import com.knowspends.app.util.VyxelHaptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToBudgets: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToMerchantRules: () -> Unit,
    onNavigateToInbox: () -> Unit,
    modifier: Modifier = Modifier,
    onSubSectionStateChanged: (Boolean) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    var activeSectionId by rememberSaveable { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(activeSectionId) {
        onSubSectionStateChanged(activeSectionId != null)
    }

    DisposableEffect(Unit) {
        onDispose {
            onSubSectionStateChanged(false)
        }
    }

    // Intercept system back press when inside a sub-screen
    BackHandler(enabled = activeSectionId != null) {
        activeSectionId = null
    }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val activeSection = activeSectionId?.let { id ->
        SettingsSection.entries.find { it.name == id }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
            )
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = activeSection?.title ?: "Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (activeSection != null) {
                        IconButton(
                            onClick = {
                                VyxelHaptics.tick(context)
                                activeSectionId = null
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Settings Hub"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = activeSection,
            transitionSpec = {
                if (targetState != null) {
                    slideInHorizontally(tween(220, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(180)) togetherWith
                        slideOutHorizontally(tween(180, easing = FastOutSlowInEasing)) { -it / 4 } + fadeOut(tween(120))
                } else {
                    slideInHorizontally(tween(220, easing = FastOutSlowInEasing)) { -it / 4 } + fadeIn(tween(180)) togetherWith
                        slideOutHorizontally(tween(180, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(120))
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "SettingsSectionTransition"
        ) { currentSection ->
            if (currentSection == null) {
                // ── Settings Hub Index View ───────────────────────────────────
                SettingsHubView(
                    userPreferences = uiState.userPreferences,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSelectSection = { section ->
                        VyxelHaptics.click(context)
                        activeSectionId = section.name
                    }
                )
            } else {
                // ── Dedicated Sub-Screen View ─────────────────────────────────
                when (currentSection) {
                    SettingsSection.ACCOUNT_PROFILE -> {
                        AccountProfileSection(
                            userPreferences = uiState.userPreferences,
                            onSaveProfile = { name, tagline, avatarUri ->
                                viewModel.updateProfile(name, tagline, avatarUri, "", uiState.userPreferences.currencySymbol, uiState.userPreferences.monthlySavingsGoal, uiState.userPreferences.financialYearStart)
                            },
                            onExportData = { uri -> viewModel.exportCsv(uri) },
                            onClearAllData = { viewModel.resetAllLedgerData() }
                        )
                    }

                    SettingsSection.CURRENCY_REGIONAL -> {
                        CurrencyRegionalSection(
                            userPreferences = uiState.userPreferences,
                            onSaveRegional = { currency, numberFormat, dateFormat, firstDay, monthStart ->
                                viewModel.setRegionalPreferences(currency, numberFormat, dateFormat, firstDay, monthStart)
                            }
                        )
                    }

                    SettingsSection.BUDGET_PREFERENCES -> {
                        BudgetPreferencesSection(
                            userPreferences = uiState.userPreferences,
                            onSaveBudgetPreferences = { period, rollover, threshold, goal ->
                                viewModel.setBudgetPreferences(period, rollover, threshold, goal)
                            },
                            onNavigateToBudgets = onNavigateToBudgets
                        )
                    }

                    SettingsSection.CATEGORIES_ACCOUNTS -> {
                        CategoriesAccountsSection(
                            accounts = uiState.accounts,
                            categories = uiState.categories,
                            currencySymbol = uiState.userPreferences.currencySymbol,
                            onSaveAccount = { viewModel.saveAccount(it) },
                            onArchiveAccount = { id, archived -> viewModel.archiveAccount(id, archived) },
                            onSaveCategory = { viewModel.saveCategory(it) },
                            onArchiveCategory = { id, archived -> viewModel.archiveCategory(id, archived) },
                            onNavigateToMerchantRules = onNavigateToMerchantRules
                        )
                    }

                    SettingsSection.RECURRING_AUTOMATION -> {
                        RecurringAutomationSection(
                            userPreferences = uiState.userPreferences,
                            isScanningSms = uiState.isScanningSms,
                            onSaveSmsSettings = { enabled, direct ->
                                viewModel.setSmsSettings(enabled, direct)
                            },
                            onSaveBackTapSettings = { enabled, sens ->
                                viewModel.setBackTapSettings(enabled, sens)
                            },
                            onScanPastSms = { viewModel.scanPastSms(30) },
                            onNavigateToSubscriptions = onNavigateToSubscriptions
                        )
                    }

                    SettingsSection.NOTIFICATIONS -> {
                        NotificationsSection(
                            userPreferences = uiState.userPreferences,
                            onSaveNotifications = { daily, time, budget, bills, summary ->
                                viewModel.setNotificationsPreferences(daily, time, budget, bills, summary)
                            }
                        )
                    }

                    SettingsSection.SECURITY_PRIVACY -> {
                        SecurityPrivacySection(
                            userPreferences = uiState.userPreferences,
                            securityConfig = uiState.securityConfig,
                            onSetBiometric = { viewModel.toggleBiometric(it) },
                            onSetSecureScreen = { viewModel.toggleSecureScreen(it) },
                            onSetLockTimeout = { viewModel.setLockTimeout(it) },
                            onSavePrivacy = { hide, block ->
                                viewModel.setSecurityPrivacyPreferences(hide, block)
                            }
                        )
                    }

                    SettingsSection.APPEARANCE -> {
                        AppearanceSection(
                            userPreferences = uiState.userPreferences,
                            onSetThemeMode = { viewModel.setThemeMode(it) },
                            onSetDynamicColor = { viewModel.setDynamicColor(it) },
                            onSetHaptics = { viewModel.setHapticsEnabled(it) },
                            onSetFontId = { viewModel.setFontId(it) }
                        )
                    }

                    SettingsSection.DATA_MANAGEMENT -> {
                        DataManagementSection(
                            userPreferences = uiState.userPreferences,
                            isLoading = uiState.isLoading,
                            onCreateBackup = { uri -> viewModel.exportBackup(uri) },
                            onRestoreBackup = { uri -> viewModel.requestRestore(uri) },
                            onExportCsv = { uri -> viewModel.exportCsv(uri) },
                            onImportCsv = { uri -> viewModel.importCsv(uri) },
                            onClearCache = { viewModel.clearAppCache(context) },
                            onResetAllData = { viewModel.resetAllLedgerData() },
                            onSetAutoBackupFrequency = { freq ->
                                viewModel.setAutoBackupFrequency(freq)
                            }
                        )
                    }

                    SettingsSection.SYNC_CLOUD -> {
                        SyncCloudSection()
                    }

                    SettingsSection.ABOUT_SUPPORT -> {
                        AboutSupportSection(
                            isCheckingUpdate = uiState.isCheckingUpdate,
                            onCheckForUpdates = { viewModel.checkForUpdates() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsHubView(
    userPreferences: UserPreferences,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectSection: (SettingsSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    val filteredSections = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val q = searchQuery.trim().lowercase()
            SettingsSection.entries.filter { section ->
                section.title.lowercase().contains(q) ||
                    section.subtitle.lowercase().contains(q) ||
                    section.keywords.any { it.lowercase().contains(q) }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. Hero Profile Header Card ───────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .pressScale(0.98f)
                .clip(VyxelShapeTokens.CardSmall)
                .clickable { onSelectSection(SettingsSection.ACCOUNT_PROFILE) },
            shape = VyxelShapeTokens.CardSmall,
            colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
            border = BorderStroke(0.8.dp, scheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(scheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (userPreferences.profileAvatarUri.isNotBlank()) {
                        AsyncImage(
                            model = userPreferences.profileAvatarUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = scheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (userPreferences.profileName.isNotBlank()) userPreferences.profileName else "Personal Vault",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = if (userPreferences.profileTagline.isNotBlank()) userPreferences.profileTagline else "Offline Ledger • Tap to edit profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // ── 2. Settings Search Field (Compact Pill) ───────────────────────────
        ExpressiveSearchPill(
            query = searchQuery,
            onQueryChange = onSearchQueryChange
        )

        // ── 3. Search Results or Grouped Categories ───────────────────────────
        if (searchQuery.isNotBlank()) {
            ExpressiveSettingsGroup(title = "SEARCH RESULTS (${filteredSections.size})") {
                if (filteredSections.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No settings found for \"$searchQuery\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                } else {
                    filteredSections.forEachIndexed { index, section ->
                        SettingsSectionRow(
                            section = section,
                            onClick = { onSelectSection(section) }
                        )
                        if (index < filteredSections.size - 1) {
                            ExpressiveItemDivider()
                        }
                    }
                }
            }
        } else {
            // Grouped Material 3 Expressive Containers
            SettingsGroup.entries.forEach { group ->
                val groupSections = SettingsSection.entries.filter { it.group == group }
                if (groupSections.isNotEmpty()) {
                    ExpressiveSettingsGroup(title = group.title) {
                        groupSections.forEachIndexed { index, section ->
                            SettingsSectionRow(
                                section = section,
                                onClick = { onSelectSection(section) }
                            )
                            if (index < groupSections.size - 1) {
                                ExpressiveItemDivider()
                            }
                        }
                    }
                }
            }
        }

        // ── 4. Local-First Transparency Footer ────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = IncomeColor, modifier = Modifier.size(13.dp))
                Text(
                    text = "100% Offline & Private • Zero Trackers",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
            }
            Text(
                text = "KnowSpends v1.4.2 • Pure Local SQLite Ledger",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                color = scheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(VyxelNavBarBottomClearance).navigationBarsPadding())
    }
}

@Composable
private fun SettingsSectionRow(
    section: SettingsSection,
    onClick: () -> Unit
) {
    ExpressiveSettingsItem(
        title = section.title,
        subtitle = section.subtitle,
        icon = section.icon,
        showChevron = true,
        onClick = onClick
    )
}
