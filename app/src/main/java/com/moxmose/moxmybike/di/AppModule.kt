package com.moxmose.moxmybike.di

import androidx.room.Room
import com.moxmose.moxmybike.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "mox-my-bike-db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    single { get<AppDatabase>().bikeDao() }
    single { get<AppDatabase>().operationTypeDao() }
    single { get<AppDatabase>().maintenanceLogDao() }

}
