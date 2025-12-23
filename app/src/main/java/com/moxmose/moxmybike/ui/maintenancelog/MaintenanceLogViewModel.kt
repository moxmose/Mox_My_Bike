package com.moxmose.moxmybike.ui.maintenancelog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moxmose.moxmybike.data.local.BikeDao
import com.moxmose.moxmybike.data.local.MaintenanceLog
import com.moxmose.moxmybike.data.local.MaintenanceLogDao
import com.moxmose.moxmybike.data.local.MaintenanceLogDetails
import com.moxmose.moxmybike.data.local.OperationTypeDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MaintenanceLogViewModel(
    private val maintenanceLogDao: MaintenanceLogDao,
    private val bikeDao: BikeDao,
    private val operationTypeDao: OperationTypeDao
) : ViewModel() {

    val activeLogsWithDetails: StateFlow<List<MaintenanceLogDetails>> = maintenanceLogDao.getActiveLogsWithDetails()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val allLogsWithDetails: StateFlow<List<MaintenanceLogDetails>> = maintenanceLogDao.getAllLogsWithDetails()
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

    fun addLog(bikeId: Int, operationTypeId: Int, notes: String?, kilometers: Int?, date: Long, color: String?) {
        viewModelScope.launch {
            val newLog = MaintenanceLog(
                bikeId = bikeId,
                operationTypeId = operationTypeId,
                notes = notes,
                kilometers = kilometers,
                date = date,
                color = color
            )
            maintenanceLogDao.insertLog(newLog)
        }
    }

    fun updateLog(log: MaintenanceLog) {
        viewModelScope.launch {
            maintenanceLogDao.updateLog(log)
        }
    }

    fun dismissLog(log: MaintenanceLog) {
        viewModelScope.launch {
            updateLog(log.copy(dismissed = true))
        }
    }

    fun restoreLog(log: MaintenanceLog) {
        viewModelScope.launch {
            updateLog(log.copy(dismissed = false))
        }
    }
}
