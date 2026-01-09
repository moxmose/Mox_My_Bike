package com.moxmose.moxequiplog.di

import androidx.room.Room
import com.moxmose.moxequiplog.data.AppSettingsManager
import com.moxmose.moxequiplog.data.local.AppDatabase
import com.moxmose.moxequiplog.ui.equipments.EquipmentsViewModel
import com.moxmose.moxequiplog.ui.maintenancelog.MaintenanceLogViewModel
import com.moxmose.moxequiplog.ui.operations.OperationTypeViewModel
import com.moxmose.moxequiplog.ui.options.OptionsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Database & DAOs
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "mox-maintenance-logs-db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    single { get<AppDatabase>().equipmentDao() }
    single { get<AppDatabase>().operationTypeDao() }
    single { get<AppDatabase>().maintenanceLogDao() }

    // DataStore
    single { AppSettingsManager(androidContext()) }

    // ViewModels
    viewModel { EquipmentsViewModel(get()) }
    viewModel { OperationTypeViewModel(get()) }
    viewModel { MaintenanceLogViewModel(get(), get(), get()) }
    viewModel { OptionsViewModel(get()) }

}
