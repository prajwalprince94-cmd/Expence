package com.knowspends.app.ui.rules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import com.knowspends.app.ui.components.states.ExpressiveEmptyState
import com.knowspends.app.ui.components.states.ExpressiveNoSearchResults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowspends.app.domain.model.Category
import com.knowspends.app.domain.model.MerchantRule
import com.knowspends.app.ui.components.CategoryBadge
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.fadingEdges

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantRulesScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MerchantRulesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Merchant Rules",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.rules.isNotEmpty()) {
                            Surface(
                                shape = VyxelShapeTokens.Pill,
                                color = scheme.primaryContainer
                            ) {
                                Text(
                                    text = "${uiState.rules.size} rules",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openAddDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Rule",
                            tint = scheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                shape = CircleShape,
                containerColor = scheme.primary,
                contentColor = scheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Merchant Rule"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Intelligent Category Memory Info Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = VyxelShapeTokens.CardSmall,
                colors = CardDefaults.cardColors(
                    containerColor = scheme.surfaceContainerHigh
                ),
                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
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
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = scheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Automatic Category Memory",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        )
                        Text(
                            text = "KnowSpends routes future SMS & transactions to your preferred categories automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Search Filter Box
            if (uiState.rules.isNotEmpty()) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = { Text("Search merchants or categories...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = scheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = VyxelShapeTokens.Pill,
                    singleLine = true
                )
            }

            // Rules List
            if (uiState.searchQuery.isNotEmpty() && uiState.filteredRules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ExpressiveNoSearchResults(
                        query = uiState.searchQuery,
                        onClearSearch = { viewModel.setSearchQuery("") },
                        suggestedQueries = listOf("Swiggy", "Amazon", "Uber", "Zomato", "Electricity"),
                        onSuggestionClick = { viewModel.setSearchQuery(it) }
                    )
                }
            } else if (uiState.filteredRules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ExpressiveEmptyState(
                        icon = Icons.Default.Rule,
                        title = "No Merchant Rules Yet",
                        subtitle = "Add pattern rules to auto-categorize recurring payees, or approve SMS transactions in your Review Inbox to build memory automatically.",
                        actionLabel = "Create First Rule",
                        onActionClick = { viewModel.openAddDialog() }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .fadingEdges(topFade = 8.dp, bottomFade = 32.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredRules, key = { it.id }) { rule ->
                        MerchantRuleCard(
                            rule = rule,
                            onDelete = { viewModel.deleteRule(rule.id, rule.merchantPattern) }
                        )
                    }
                }
            }
        }
    }

    // Add Rule Dialog
    if (uiState.isAddDialogOpen) {
        AddRuleDialog(
            categories = uiState.categories,
            onDismiss = { viewModel.closeAddDialog() },
            onConfirm = { pattern, catId, catName, isExact ->
                viewModel.addRule(pattern, catId, catName, isExact)
            }
        )
    }
}

@Composable
fun MerchantRuleCard(
    rule: MerchantRule,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = VyxelShapeTokens.CardSmall,
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = scheme.primary
                    )
                    Text(
                        text = rule.merchantPattern,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = VyxelShapeTokens.Pill,
                        color = scheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = if (rule.isExactMatch) "Exact match" else "Contains keyword",
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = scheme.onSurfaceVariant
                    )

                    Surface(
                        shape = VyxelShapeTokens.Pill,
                        color = scheme.secondaryContainer
                    ) {
                        Text(
                            text = rule.categoryName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Rule",
                    tint = scheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRuleDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (pattern: String, categoryId: Long, categoryName: String, isExactMatch: Boolean) -> Unit
) {
    var pattern by remember { mutableStateOf("") }
    var selectedCategoryId by remember(categories) {
        mutableStateOf(categories.firstOrNull()?.id ?: 1L)
    }
    var isExactMatch by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme

    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Merchant Rule",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "When a transaction payee contains this keyword, automatically assign the category.",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text("Merchant / Payee Keyword") },
                    placeholder = { Text("e.g. Swiggy, Uber, Shell Petrol") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CategoryBadge(
                                            categoryName = cat.name,
                                            iconKey = cat.iconKey,
                                            color = Color(cat.colour),
                                            size = 20.dp
                                        )
                                        Text(cat.name, style = MaterialTheme.typography.bodyMedium)
                                    }
                                },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Exact Match Only",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isExactMatch) "Matches full payee string exactly" else "Matches any payee containing keyword",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isExactMatch,
                        onCheckedChange = { isExactMatch = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pattern.isNotBlank() && selectedCategory != null) {
                        onConfirm(pattern.trim(), selectedCategory.id, selectedCategory.name, isExactMatch)
                    }
                },
                enabled = pattern.isNotBlank()
            ) {
                Text("Save Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
