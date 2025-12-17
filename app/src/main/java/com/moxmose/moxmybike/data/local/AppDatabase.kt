package com.moxmose.moxmybike.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Bike::class, OperationType::class, MaintenanceLog::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bikeDao(): BikeDao
    abstract fun operationTypeDao(): OperationTypeDao
    abstract fun maintenanceLogDao(): MaintenanceLogDao
}
