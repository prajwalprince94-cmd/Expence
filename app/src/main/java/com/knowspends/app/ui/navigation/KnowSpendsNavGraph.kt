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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.knowspends.app.ui.add.AddTransactionScreen
import com.knowspends.app.ui.budgets.BudgetsScreen
import com.knowspends.app.ui.components.ExpressiveFabMenu
import com.knowspends.app.ui.components.VyxelFloatingNavBar
import com.knowspends.app.ui.home.HomeScreen
import com.knowspends.app.ui.inbox.ReviewInboxScreen
import com.knowspends.app.ui.reports.ReportsScreen
import com.knowspends.app.ui.settings.SettingsScreen
import com.knowspends.app.ui.theme.KnowSpendsMotion
import com.knowspends.app.ui.transactions.TransactionsScreen

@Composable
fun KnowSpendsAppRoot(
    navController: NavHostController = rememberNavController(),
    initialRoute: String? = null
) {
    LaunchedEffect(initialRoute) {
        if (initialRoute == "add_transaction") {
            navController.navigate(Screen.AddTransaction.createRoute())
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var isFabExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        isFabExpanded = false
    }

    val isTopLevelRoute = Screen.bottomNavItems.any { it.route == currentRoute }

    val barOffset by animateDpAsState(
        targetValue = if (isTopLevelRoute) 0.dp else 140.dp,
        animationSpec = KnowSpendsMotion.expressive(),
        label = "barOffset"
    )
    val fabOffset by animateDpAsState(
        targetValue = if (isTopLevelRoute) 0.dp else 140.dp,
        animationSpec = KnowSpendsMotion.expressive(),
        label = "fabOffset"
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
                    BudgetsScreen()
                }
                composable(Screen.Reports.route) {
                    ReportsScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen()
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

            // Stride-style Floating Navigation Bar Overlay (eliminates scaffold bottomBar re-measurement)
            if (barOffset < 140.dp) {
                VyxelFloatingNavBar(
                    items = Screen.bottomNavItems,
                    currentRoute = currentRoute,
                    onItemClick = { screen ->
                        if (currentRoute != screen.route) {
                            navController.navigateSingleTop(screen.route)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 18.dp)
                        .graphicsLayer {
                            translationY = barOffset.toPx()
                            alpha = ((140.dp - barOffset) / 140.dp).coerceIn(0f, 1f)
                        }
                )
            }

            // Floating Action Button Overlay
            if (fabOffset < 140.dp) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(bottom = 86.dp, end = 16.dp)
                        .graphicsLayer {
                            translationY = fabOffset.toPx()
                            alpha = ((140.dp - fabOffset) / 140.dp).coerceIn(0f, 1f)
                        }
                ) {
                    ExpressiveFabMenu(
                        expanded = isFabExpanded,
                        onExpandChanged = { isFabExpanded = it },
                        onAddExpense = {
                            isFabExpanded = false
                            navController.navigate(Screen.AddTransaction.createRoute(type = TransactionType.EXPENSE))
                        },
                        onAddIncome = {
                            isFabExpanded = false
                            navController.navigate(Screen.AddTransaction.createRoute(type = TransactionType.INCOME))
                        },
                        onAddTransfer = {
                            isFabExpanded = false
                            navController.navigate(Screen.AddTransaction.createRoute(type = TransactionType.TRANSFER))
                        }
                    )
                }
            }

            if (isFabExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.32f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isFabExpanded = false }
                )
            }
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
