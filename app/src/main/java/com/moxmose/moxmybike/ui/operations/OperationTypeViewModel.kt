package com.moxmose.moxmybike.ui.operations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moxmose.moxmybike.data.local.OperationType
import com.moxmose.moxmybike.data.local.OperationTypeDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OperationTypeViewModel(private val operationTypeDao: OperationTypeDao) : ViewModel() {

    val activeOperationTypes: StateFlow<List<OperationType>> = operationTypeDao.getActiveOperationTypes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val allOperationTypes: StateFlow<List<OperationType>> = operationTypeDao.getAllOperationTypes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun addOperationType(description: String, iconIdentifier: String?, color: String?, photoUri: String?) {
        viewModelScope.launch {
            val maxDisplayOrder = allOperationTypes.first().maxOfOrNull { it.displayOrder } ?: -1
            operationTypeDao.insertOperationType(
                OperationType(
                    description = description,
                    iconIdentifier = iconIdentifier,
                    color = color,
                    photoUri = photoUri,
                    displayOrder = maxDisplayOrder + 1
                )
            )
        }
    }

    fun updateOperationType(operationType: OperationType) {
        viewModelScope.launch {
            operationTypeDao.updateOperationType(operationType)
        }
    }

    fun updateOperationTypes(operationTypes: List<OperationType>) {
        viewModelScope.launch {
            operationTypeDao.updateOperationTypes(operationTypes)
        }
    }

    fun dismissOperationType(operationType: OperationType) {
        viewModelScope.launch {
            updateOperationType(operationType.copy(dismissed = true))
        }
    }

    fun restoreOperationType(operationType: OperationType) {
        viewModelScope.launch {
            updateOperationType(operationType.copy(dismissed = false))
        }
    }
}
