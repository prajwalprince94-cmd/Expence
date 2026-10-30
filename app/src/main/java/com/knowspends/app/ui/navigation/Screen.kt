package com.knowspends.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Transactions : Screen("transactions", "Transactions", Icons.Default.ReceiptLong)
    object Budgets : Screen("budgets", "Budgets", Icons.Default.AccountBalanceWallet)
    object Reports : Screen("reports", "Reports", Icons.Default.PieChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object AddTransaction : Screen("add_transaction", "Add Transaction") {
        const val routeWithArgs = "add_transaction?transactionId={transactionId}&type={type}"
        fun createRoute(
            transactionId: Long? = null,
            type: com.knowspends.app.domain.model.TransactionType? = null
        ): String {
            val params = mutableListOf<String>()
            if (transactionId != null && transactionId > 0) params.add("transactionId=$transactionId")
            if (type != null) params.add("type=${type.name}")
            return if (params.isEmpty()) "add_transaction" else "add_transaction?${params.joinToString("&")}"
        }
    }
    object ReviewInbox : Screen("review_inbox", "Review Inbox")

    companion object {
        val bottomNavItems = listOf(
            Home,
            Transactions,
            Reports,
            Settings
        )
    }
}
