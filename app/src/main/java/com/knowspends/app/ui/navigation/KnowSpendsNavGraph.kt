package com.knowspends.app.ui.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.graphics.graphicsLayer
import com.knowspends.app.ui.theme.KnowSpendsMotion
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.knowspends.app.ui.components.VyxelFloatingNavBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.knowspends.app.ui.home.HomeScreen
import com.knowspends.app.ui.inbox.ReviewInboxScreen
import com.knowspends.app.ui.reports.ReportsScreen
import com.knowspends.app.ui.settings.SettingsScreen
import com.knowspends.app.ui.transactions.TransactionsScreen

@Composable
fun KnowSpendsAppRoot(
    navController: NavHostController = rememberNavController(),
    initialRoute: String? = null
) {
    androidx.compose.runtime.LaunchedEffect(initialRoute) {
        if (initialRoute == "add_transaction") {
            navController.navigate(Screen.AddTransaction.createRoute())
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var isFabExpanded by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(currentRoute) {
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
        bottomBar = {
            if (barOffset < 140.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .graphicsLayer {
                            translationY = barOffset.toPx()
                            alpha = ((140.dp - barOffset) / 140.dp).coerceIn(0f, 1f)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    VyxelFloatingNavBar(
                        items = Screen.bottomNavItems,
                        currentRoute = currentRoute,
                        onItemClick = { screen ->
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (fabOffset < 140.dp) {
                Box(
                    modifier = Modifier.graphicsLayer {
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
        }
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
                    fadeIn(animationSpec = androidx.compose.animation.core.tween(140, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                },
                exitTransition = {
                    fadeOut(animationSpec = androidx.compose.animation.core.tween(90, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = androidx.compose.animation.core.tween(140, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                },
                popExitTransition = {
                    fadeOut(animationSpec = androidx.compose.animation.core.tween(90, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                }
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToTransactions = {
                            navController.navigate(Screen.Transactions.route)
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
                        slideInHorizontally(animationSpec = androidx.compose.animation.core.tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it } +
                            fadeIn(animationSpec = androidx.compose.animation.core.tween(160))
                    },
                    popExitTransition = {
                        slideOutHorizontally(animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it } +
                            fadeOut(animationSpec = androidx.compose.animation.core.tween(120))
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
                        slideInHorizontally(animationSpec = androidx.compose.animation.core.tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it } +
                            fadeIn(animationSpec = androidx.compose.animation.core.tween(160))
                    },
                    popExitTransition = {
                        slideOutHorizontally(animationSpec = androidx.compose.animation.core.tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it } +
                            fadeOut(animationSpec = androidx.compose.animation.core.tween(120))
                    }
                ) {
                    AddTransactionScreen(
                        onNavigateUp = { navController.navigateUp() }
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
