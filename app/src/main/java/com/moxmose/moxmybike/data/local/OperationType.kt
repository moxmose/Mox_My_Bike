package com.moxmose.moxmybike.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operation_types")
data class OperationType(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String
)
