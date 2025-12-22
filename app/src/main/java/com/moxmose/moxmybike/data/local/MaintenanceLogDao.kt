package com.moxmose.moxmybike.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MaintenanceLog)

    @Update
    suspend fun updateLog(log: MaintenanceLog)

    @Query("""
        SELECT
            l.*,
            b.description as bikeDescription,
            ot.description as operationTypeDescription,
            b.photoUri as bikePhotoUri,
            ot.photoUri as operationTypePhotoUri,
            ot.iconIdentifier as operationTypeIconIdentifier
        FROM maintenance_logs as l
        JOIN bikes as b ON l.bikeId = b.id
        JOIN operation_types as ot ON l.operationTypeId = ot.id
        WHERE l.dismissed = 0
        ORDER BY l.date DESC
    """)
    fun getActiveLogsWithDetails(): Flow<List<MaintenanceLogDetails>>

    @Query("""
        SELECT
            l.*,
            b.description as bikeDescription,
            ot.description as operationTypeDescription,
            b.photoUri as bikePhotoUri,
            ot.photoUri as operationTypePhotoUri,
            ot.iconIdentifier as operationTypeIconIdentifier
        FROM maintenance_logs as l
        JOIN bikes as b ON l.bikeId = b.id
        JOIN operation_types as ot ON l.operationTypeId = ot.id
        ORDER BY l.date DESC
    """)
    fun getAllLogsWithDetails(): Flow<List<MaintenanceLogDetails>>
}
