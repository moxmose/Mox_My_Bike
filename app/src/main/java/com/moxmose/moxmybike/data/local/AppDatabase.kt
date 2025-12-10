package com.moxmose.moxmybike.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Bike::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bikeDao(): BikeDao
}
