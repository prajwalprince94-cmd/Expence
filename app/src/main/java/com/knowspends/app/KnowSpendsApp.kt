package com.knowspends.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.knowspends.app.domain.usecase.ProcessRecurringTransactionsUseCase
import com.knowspends.app.work.RecurringTransactionWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class KnowSpendsApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var processRecurringTransactionsUseCase: ProcessRecurringTransactionsUseCase

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Schedule periodic background recurrence worker
        RecurringTransactionWorker.schedule(this)

        // Catch-up missed occurrences immediately on app launch
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                processRecurringTransactionsUseCase()
            }
        }
    }
}
