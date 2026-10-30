package com.knowspends.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.knowspends.app.data.local.dao.AccountDao
import com.knowspends.app.data.local.dao.CategoryDao
import com.knowspends.app.data.local.dao.TransactionDao
import com.knowspends.app.data.local.db.KnowSpendsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        databaseProvider: Provider<KnowSpendsDatabase>
    ): KnowSpendsDatabase {
        return Room.databaseBuilder(
            context,
            KnowSpendsDatabase::class.java,
            KnowSpendsDatabase.DATABASE_NAME
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial accounts and categories
                CoroutineScope(Dispatchers.IO).launch {
                    val database = databaseProvider.get()
                    seedInitialData(database)
                }
            }
        }).build()
    }

    private suspend fun seedInitialData(database: KnowSpendsDatabase) {
        val accountDao = database.accountDao()
        val categoryDao = database.categoryDao()

        // Initial accounts
        accountDao.insertAccounts(
            listOf(
                com.knowspends.app.data.local.entity.AccountEntity(
                    name = "Cash",
                    type = "CASH",
                    openingBalanceMinor = 0L,
                    sortOrder = 0
                ),
                com.knowspends.app.data.local.entity.AccountEntity(
                    name = "Bank Account",
                    type = "BANK",
                    openingBalanceMinor = 0L,
                    sortOrder = 1
                ),
                com.knowspends.app.data.local.entity.AccountEntity(
                    name = "UPI / Wallet",
                    type = "WALLET",
                    openingBalanceMinor = 0L,
                    sortOrder = 2
                )
            )
        )

        // Initial categories
        categoryDao.insertCategories(
            listOf(
                // Expenses
                com.knowspends.app.data.local.entity.CategoryEntity(
                    name = "Food & Dining",
                    kind = "EXPENSE",
                    iconKey = "restaurant",
                    colour = 0xFFF57C00
                ),
                com.knowspends.app.data.local.entity.CategoryEntity(
                    name = "Groceries",
                    kind = "EXPENSE",
                    iconKey = "shopping_cart",
                    colour = 0xFF388E3C
                ),
                com.knowspends.app.data.local.entity.CategoryEntity(
                    name = "Transportation",
                    kind = "EXPENSE",
                    iconKey = "directions_car",
                    colour = 0xFF1976D2
                ),
                com.knowspends.app.data.local.entity.CategoryEntity(
                    name = "Bills & Utilities",
                    kind = "EXPENSE",
                    iconKey = "receipt_long",
                    colour = 0xFFD32F2F
                ),
                com.knowspends.app.data.local.entity.CategoryEntity(
                    name = "Shopping",
                    kind = "EXPENSE",
                    iconKey = "shopping_bag",
                    colour = 0xFF7B1FA2
                ),
                com.knowspends.app.data.local.entity.CategoryEntity(
                    name = "Entertainment",
                    kind = "EXPENSE",
                    iconKey = "movie",
                    colour = 0xFFC2185B
                ),
                com.knowspends.app.data.local.entity.CategoryEntity(
                    name = "Health",
                    kind = "EXPENSE",
                    iconKey = "medical_services",
                    colour = 0xFF0097A7
                ),
                // Income
                com.knowspends.app.data.local.entity.CategoryEntity(
                    name = "Salary",
                    kind = "INCOME",
                    iconKey = "payments",
                    colour = 0xFF2E7D32
                ),
                com.knowspends.app.data.local.entity.CategoryEntity(
                    name = "Freelance / Extra",
                    kind = "INCOME",
                    iconKey = "work",
                    colour = 0xFF0288D1
                ),
                com.knowspends.app.data.local.entity.CategoryEntity(
                    name = "Investments",
                    kind = "INCOME",
                    iconKey = "trending_up",
                    colour = 0xFF4527A0
                )
            )
        )
    }

    @Provides
    fun provideAccountDao(db: KnowSpendsDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideCategoryDao(db: KnowSpendsDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideTransactionDao(db: KnowSpendsDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideBudgetDao(db: KnowSpendsDatabase): com.knowspends.app.data.local.dao.BudgetDao = db.budgetDao()

    @Provides
    fun provideRecurringRuleDao(db: KnowSpendsDatabase): com.knowspends.app.data.local.dao.RecurringRuleDao = db.recurringRuleDao()

    @Provides
    fun providePendingTransactionDao(db: KnowSpendsDatabase): com.knowspends.app.data.local.dao.PendingTransactionDao = db.pendingTransactionDao()
}
