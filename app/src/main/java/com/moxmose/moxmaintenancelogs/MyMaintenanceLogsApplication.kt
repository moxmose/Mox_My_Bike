package com.moxmose.moxmaintenancelogs

import android.app.Application
import com.moxmose.moxmaintenancelogs.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyMaintenanceLogsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyMaintenanceLogsApplication)
            modules(appModule)
        }
    }
}
