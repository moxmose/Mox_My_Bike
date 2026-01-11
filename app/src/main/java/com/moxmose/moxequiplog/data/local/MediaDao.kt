package com.moxmose.moxequiplog.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMedia(media: Media)

    @Update
    suspend fun updateMedia(media: Media)

    @Update
    suspend fun updateAllMedia(mediaList: List<Media>)

    @Query("SELECT * FROM media_library WHERE category = :category ORDER BY displayOrder ASC, addedAt DESC")
    fun getMediaByCategory(category: String): Flow<List<Media>>

    @Query("SELECT * FROM media_library ORDER BY category ASC, displayOrder ASC, addedAt DESC")
    fun getAllMedia(): Flow<List<Media>>

    @Delete
    suspend fun deleteMedia(media: Media)

    @Query("SELECT * FROM media_library WHERE uri = :uri AND category = :category")
    suspend fun getMediaByUriAndCategory(uri: String, category: String): Media?

    @Query("SELECT MAX(displayOrder) FROM media_library WHERE category = :category")
    suspend fun getMaxOrder(category: String): Int?
}
