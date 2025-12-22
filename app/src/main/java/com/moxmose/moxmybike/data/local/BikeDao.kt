package com.moxmose.moxmybike.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BikeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBike(bike: Bike)

    @Update
    suspend fun updateBike(bike: Bike)

    @Update
    suspend fun updateBikes(bikes: List<Bike>)

    @Query("SELECT * FROM bikes WHERE dismissed = 0 ORDER BY displayOrder ASC")
    fun getActiveBikes(): Flow<List<Bike>>

    @Query("SELECT * FROM bikes ORDER BY displayOrder ASC")
    fun getAllBikes(): Flow<List<Bike>>

    @Query("SELECT * FROM bikes WHERE id = :bikeId")
    fun getBikeById(bikeId: Int): Flow<Bike?>
}
