package com.knowspends.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.knowspends.app.data.local.dao.AccountDao
import com.knowspends.app.data.local.dao.CategoryDao
import com.knowspends.app.data.local.dao.TransactionDao
import com.knowspends.app.data.local.entity.AccountEntity
import com.knowspends.app.data.local.entity.CategoryEntity
import com.knowspends.app.data.local.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        com.knowspends.app.data.local.entity.BudgetEntity::class,
        com.knowspends.app.data.local.entity.RecurringRuleEntity::class,
        com.knowspends.app.data.local.entity.RecurringExecutionEntity::class,
        com.knowspends.app.data.local.entity.PendingTransactionEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class KnowSpendsDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): com.knowspends.app.data.local.dao.BudgetDao
    abstract fun recurringRuleDao(): com.knowspends.app.data.local.dao.RecurringRuleDao
    abstract fun pendingTransactionDao(): com.knowspends.app.data.local.dao.PendingTransactionDao

    companion object {
        const val DATABASE_NAME = "knowspends.db"
    }
}
