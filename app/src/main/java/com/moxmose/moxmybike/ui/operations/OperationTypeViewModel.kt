package com.moxmose.moxmybike.ui.operations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moxmose.moxmybike.data.local.OperationType
import com.moxmose.moxmybike.data.local.OperationTypeDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OperationTypeViewModel(private val operationTypeDao: OperationTypeDao) : ViewModel() {

    val allOperationTypes: StateFlow<List<OperationType>> = operationTypeDao.getAllOperationTypes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun addOperationType(description: String) {
        viewModelScope.launch {
            operationTypeDao.insertOperationType(OperationType(description = description))
        }
    }
}
