package com.moxmose.moxmybike

import android.app.Application
import com.moxmose.moxmybike.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyBikeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyBikeApplication)
            modules(appModule)
        }
    }
}
