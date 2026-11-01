package com.knowspends.app.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.knowspends.app.domain.model.TransactionType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowspends.app.ui.add.AddTransactionScreen
import com.knowspends.app.ui.budgets.BudgetsScreen
import com.knowspends.app.ui.components.VyxelFloatingNavBar
import com.knowspends.app.ui.components.bouncyClickable
import com.knowspends.app.ui.home.HomeScreen
import com.knowspends.app.ui.inbox.ReviewInboxScreen
import com.knowspends.app.ui.island.DynamicIslandQuickSpend
import com.knowspends.app.ui.island.DynamicIslandViewModel
import com.knowspends.app.ui.reports.ReportsScreen
import com.knowspends.app.ui.settings.SettingsScreen
import com.knowspends.app.ui.theme.KnowSpendsMotion
import com.knowspends.app.ui.transactions.TransactionsScreen

@Composable
fun KnowSpendsAppRoot(
    navController: NavHostController = rememberNavController(),
    initialRoute: String? = null,
    triggerDynamicIsland: Boolean = false,
    onDynamicIslandDismissed: () -> Unit = {},
    dynamicIslandViewModel: DynamicIslandViewModel = hiltViewModel()
) {
    val islandUiState by dynamicIslandViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialRoute) {
        if (initialRoute == "add_transaction") {
            navController.navigate(Screen.AddTransaction.createRoute())
        }
    }

    LaunchedEffect(triggerDynamicIsland) {
        if (triggerDynamicIsland) {
            dynamicIslandViewModel.expand()
            onDynamicIslandDismissed()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelRoute = Screen.bottomNavItems.any { it.route == currentRoute }

    val barOffset by animateDpAsState(
        targetValue = if (isTopLevelRoute) 0.dp else 140.dp,
        animationSpec = KnowSpendsMotion.expressive(),
        label = "barOffset"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = if (isTopLevelRoute) 0.dp else innerPadding.calculateBottomPadding()
                )
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    fadeIn(tween(150)) + slideInVertically(tween(210)) { it / 16 } +
                        scaleIn(initialScale = 0.98f, animationSpec = tween(210))
                },
                exitTransition = { fadeOut(tween(80)) }
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToTransactions = {
                            navController.navigateSingleTop(Screen.Transactions.route)
                        },
                        onTransactionClick = { transactionId ->
                            navController.navigate(Screen.AddTransaction.createRoute(transactionId))
                        },
                        onNavigateToInbox = {
                            navController.navigate(Screen.ReviewInbox.route)
                        }
                    )
                }
                composable(Screen.Transactions.route) {
                    TransactionsScreen(
                        onEditTransaction = { transactionId ->
                            navController.navigate(Screen.AddTransaction.createRoute(transactionId))
                        }
                    )
                }
                composable(Screen.Budgets.route) {
                    BudgetsScreen(onNavigateUp = { navController.navigateUp() })
                }
                composable(Screen.Reports.route) {
                    ReportsScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(onNavigateToBudgets = { navController.navigate(Screen.Budgets.route) })
                }
                composable(
                    route = Screen.ReviewInbox.route,
                    enterTransition = {
                        slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing)) { it } +
                            fadeIn(animationSpec = tween(160))
                    },
                    popExitTransition = {
                        slideOutHorizontally(animationSpec = tween(180, easing = FastOutSlowInEasing)) { it } +
                            fadeOut(animationSpec = tween(120))
                    }
                ) {
                    ReviewInboxScreen(
                        onNavigateUp = { navController.navigateUp() }
                    )
                }
                composable(
                    route = Screen.AddTransaction.routeWithArgs,
                    arguments = listOf(
                        navArgument("transactionId") {
                            type = NavType.LongType
                            defaultValue = -1L
                        },
                        navArgument("type") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    ),
                    enterTransition = {
                        slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing)) { it } +
                            fadeIn(animationSpec = tween(160))
                    },
                    popExitTransition = {
                        slideOutHorizontally(animationSpec = tween(180, easing = FastOutSlowInEasing)) { it } +
                            fadeOut(animationSpec = tween(120))
                    }
                ) {
                    AddTransactionScreen(
                        onNavigateUp = { navController.navigateUp() }
                    )
                }
            }

            // Stride-style Floating Navigation Bar & Direct + Button (Auto-Layout Row beside navigation)
            if (barOffset < 140.dp) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
                        .graphicsLayer {
                            translationY = barOffset.toPx()
                            alpha = ((140.dp - barOffset) / 140.dp).coerceIn(0f, 1f)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VyxelFloatingNavBar(
                        items = Screen.bottomNavItems,
                        currentRoute = currentRoute,
                        onItemClick = { screen ->
                            if (currentRoute != screen.route) {
                                navController.navigateSingleTop(screen.route)
                            }
                        }
                    )

                    // Dedicated + Button beside navigation bar (bouncy, solid color, no scrim overlay!)
                    Surface(
                        onClick = {
                            navController.navigate(Screen.AddTransaction.createRoute())
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .size(52.dp)
                            .bouncyClickable(scaleDown = 0.88f) {
                                navController.navigate(Screen.AddTransaction.createRoute())
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Transaction",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            // Dynamic Island Quick-Add Overlay (iOS Style, Framer Motion Springs)
            DynamicIslandQuickSpend(
                uiState = islandUiState,
                onExpand = { dynamicIslandViewModel.expand() },
                onCollapse = { dynamicIslandViewModel.collapse() },
                onDigit = { dynamicIslandViewModel.onDigit(it) },
                onBackspace = { dynamicIslandViewModel.onBackspace() },
                onClear = { dynamicIslandViewModel.onClear() },
                onCategorySelected = { dynamicIslandViewModel.onCategorySelected(it) },
                onAccountSelected = { dynamicIslandViewModel.onAccountSelected(it) },
                onSaveExpense = { dynamicIslandViewModel.saveExpense() },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
