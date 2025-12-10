package com.moxmose.moxmybike.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenance_logs",
    foreignKeys = [
        ForeignKey(
            entity = Bike::class,
            parentColumns = ["id"],
            childColumns = ["bikeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OperationType::class,
            parentColumns = ["id"],
            childColumns = ["operationTypeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index(value = ["bikeId"]),
        androidx.room.Index(value = ["operationTypeId"])
    ]
)
data class MaintenanceLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "bikeId")
    val bikeId: Int,
    @ColumnInfo(name = "operationTypeId")
    val operationTypeId: Int,
    val notes: String?,
    val kilometers: Int,
    val date: Long
)
