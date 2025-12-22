package com.moxmose.moxmybike.data.local

import androidx.room.Embedded

data class MaintenanceLogDetails(
    @Embedded val log: MaintenanceLog,
    val bikeDescription: String,
    val operationTypeDescription: String,
    val bikePhotoUri: String?,
    val operationTypePhotoUri: String?,
    val operationTypeIconIdentifier: String?
)
