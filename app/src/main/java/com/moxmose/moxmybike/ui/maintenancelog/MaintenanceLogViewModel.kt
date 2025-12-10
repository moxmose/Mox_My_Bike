package com.moxmose.moxmybike.ui.maintenancelog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moxmose.moxmybike.data.local.BikeDao
import com.moxmose.moxmybike.data.local.MaintenanceLog
import com.moxmose.moxmybike.data.local.MaintenanceLogDao
import com.moxmose.moxmybike.data.local.OperationTypeDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MaintenanceLogViewModel(
    private val maintenanceLogDao: MaintenanceLogDao,
    private val bikeDao: BikeDao,
    private val operationTypeDao: OperationTypeDao
) : ViewModel() {

    val allLogsWithDetails = maintenanceLogDao.getAllLogsWithDetails()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val allBikes = bikeDao.getAllBikes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val allOperationTypes = operationTypeDao.getAllOperationTypes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun addLog(bikeId: Int, operationTypeId: Int, notes: String?, kilometers: Int, date: Long) {
        viewModelScope.launch {
            val newLog = MaintenanceLog(
                bikeId = bikeId,
                operationTypeId = operationTypeId,
                notes = notes,
                kilometers = kilometers,
                date = date
            )
            maintenanceLogDao.insertLog(newLog)
        }
    }
}
