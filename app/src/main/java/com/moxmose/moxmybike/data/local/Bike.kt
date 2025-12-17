package com.moxmose.moxmybike.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bikes")
data class Bike(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String,
    val photoUri: String? = null,
    val displayOrder: Int = 0
)
