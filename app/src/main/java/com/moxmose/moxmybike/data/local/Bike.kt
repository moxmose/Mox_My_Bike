package com.moxmose.moxmybike.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bikes")
data class Bike(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String,
    val photoUri: String? = null,
    val displayOrder: Int = 0,
    @ColumnInfo(defaultValue = "false")
    val dismissed: Boolean = false,
    val color: String? = null
)
