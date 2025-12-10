package com.moxmose.moxmybike.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BikeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBike(bike: Bike)

    @Query("SELECT * FROM bikes ORDER BY name ASC")
    fun getAllBikes(): Flow<List<Bike>>

    @Query("SELECT * FROM bikes WHERE id = :bikeId")
    fun getBikeById(bikeId: Int): Flow<Bike?>
}
