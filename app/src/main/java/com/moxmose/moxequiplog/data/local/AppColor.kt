package com.moxmose.moxequiplog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_colors")
data class AppColor(
    @PrimaryKey
    val hexValue: String, // e.g. "#FF0000"
    val name: String,
    val isSystem: Boolean = false,
    val displayOrder: Int = 0
)
