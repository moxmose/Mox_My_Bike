package com.moxmose.moxmybike.di

import androidx.room.Room
import com.moxmose.moxmybike.data.AppSettingsManager
import com.moxmose.moxmybike.data.local.AppDatabase
import com.moxmose.moxmybike.ui.bikes.BikesViewModel
import com.moxmose.moxmybike.ui.maintenancelog.MaintenanceLogViewModel
import com.moxmose.moxmybike.ui.operations.OperationTypeViewModel
import com.moxmose.moxmybike.ui.options.OptionsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Database & DAOs
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

    // DataStore
    single { AppSettingsManager(androidContext()) }

    // ViewModels
    viewModel { BikesViewModel(get()) }
    viewModel { OperationTypeViewModel(get()) }
    viewModel { MaintenanceLogViewModel(get(), get(), get()) }
    viewModel { OptionsViewModel(get()) }

}
