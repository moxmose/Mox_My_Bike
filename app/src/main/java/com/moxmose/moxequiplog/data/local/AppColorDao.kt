package com.moxmose.moxequiplog.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppColorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColor(color: AppColor)

    @Update
    suspend fun updateColor(color: AppColor)

    @Update
    suspend fun updateAllColors(colors: List<AppColor>)

    @Delete
    suspend fun deleteColor(color: AppColor)

    @Query("SELECT * FROM app_colors ORDER BY displayOrder ASC")
    fun getAllColors(): Flow<List<AppColor>>

    @Query("SELECT * FROM app_colors WHERE hexValue = :hex")
    suspend fun getColorByHex(hex: String): AppColor?

    @Query("SELECT MAX(displayOrder) FROM app_colors")
    suspend fun getMaxOrder(): Int?
}
