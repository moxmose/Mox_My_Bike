package com.moxmose.moxmybike.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OperationTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperationType(operationType: OperationType)

    @Update
    suspend fun updateOperationType(operationType: OperationType)

    @Query("SELECT * FROM operation_types WHERE dismissed = 0 ORDER BY description ASC")
    fun getActiveOperationTypes(): Flow<List<OperationType>>

    @Query("SELECT * FROM operation_types ORDER BY description ASC")
    fun getAllOperationTypes(): Flow<List<OperationType>>
}
