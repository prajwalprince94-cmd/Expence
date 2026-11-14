package com.knowspends.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
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

    companion object {
        @Volatile
        var isAppInForeground: Boolean = false
            private set
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var resumedCount = 0

            override fun onActivityResumed(activity: Activity) {
                resumedCount++
                isAppInForeground = resumedCount > 0
            }

            override fun onActivityPaused(activity: Activity) {
                resumedCount = maxOf(0, resumedCount - 1)
                isAppInForeground = resumedCount > 0
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })

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
