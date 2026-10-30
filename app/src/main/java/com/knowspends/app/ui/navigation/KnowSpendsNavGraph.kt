package com.knowspends.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.knowspends.app.ui.add.AddTransactionScreen
import com.knowspends.app.ui.budgets.BudgetsScreen
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

    val isTopLevelRoute = Screen.bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = isTopLevelRoute,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar {
                    Screen.bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                screen.icon?.let {
                                    Icon(imageVector = it, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isTopLevelRoute,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.AddTransaction.createRoute())
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction"
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
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
            composable(Screen.ReviewInbox.route) {
                ReviewInboxScreen(
                    onNavigateUp = { navController.navigateUp() }
                )
            }
            composable(
                route = Screen.AddTransaction.routeWithArgs,
                arguments = listOf(
                    androidx.navigation.navArgument("transactionId") {
                        type = androidx.navigation.NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) {
                AddTransactionScreen(
                    onNavigateUp = { navController.navigateUp() }
                )
            }
        }
    }
}
