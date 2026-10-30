package com.knowspends.app.di

import com.knowspends.app.data.repository.AccountRepositoryImpl
import com.knowspends.app.data.repository.CategoryRepositoryImpl
import com.knowspends.app.data.repository.TransactionRepositoryImpl
import com.knowspends.app.domain.repository.AccountRepository
import com.knowspends.app.domain.repository.CategoryRepository
import com.knowspends.app.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: com.knowspends.app.data.repository.BudgetRepositoryImpl): com.knowspends.app.domain.repository.BudgetRepository

    @Binds
    @Singleton
    abstract fun bindRecurringRuleRepository(impl: com.knowspends.app.data.repository.RecurringRuleRepositoryImpl): com.knowspends.app.domain.repository.RecurringRuleRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: com.knowspends.app.data.repository.BackupRepositoryImpl): com.knowspends.app.domain.repository.BackupRepository

    @Binds
    @Singleton
    abstract fun bindCsvRepository(impl: com.knowspends.app.data.repository.CsvRepositoryImpl): com.knowspends.app.domain.repository.CsvRepository

    @Binds
    @Singleton
    abstract fun bindSecurityRepository(impl: com.knowspends.app.data.repository.SecurityRepositoryImpl): com.knowspends.app.domain.repository.SecurityRepository

    @Binds
    @Singleton
    abstract fun bindPremiumRepository(impl: com.knowspends.app.data.repository.PremiumRepositoryImpl): com.knowspends.app.domain.repository.PremiumRepository

    @Binds
    @Singleton
    abstract fun bindPendingTransactionRepository(impl: com.knowspends.app.data.repository.PendingTransactionRepositoryImpl): com.knowspends.app.domain.repository.PendingTransactionRepository
}
