package com.moxmose.moxmybike.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MaintenanceLog)

    @Query("SELECT * FROM maintenance_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<MaintenanceLog>>
}
