package com.moxmose.moxequiplog.data.local

import androidx.room.Entity

@Entity(tableName = "media_library", primaryKeys = ["uri", "category"])
data class Media(
    val uri: String,
    val category: String = "EQUIPMENT", // "EQUIPMENT" o "OPERATION"
    val mediaType: String = "IMAGE",    // "IMAGE" o "ICON"
    val displayOrder: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)
