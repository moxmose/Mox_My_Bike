package com.moxmose.moxequiplog.ui.equipments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moxmose.moxequiplog.data.AppSettingsManager
import com.moxmose.moxequiplog.data.local.Equipment
import com.moxmose.moxequiplog.data.local.EquipmentDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EquipmentsViewModel(
    private val equipmentDao: EquipmentDao,
    private val appSettingsManager: AppSettingsManager
) : ViewModel() {

    val activeEquipments: StateFlow<List<Equipment>> = equipmentDao.getActiveEquipments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val allEquipments: StateFlow<List<Equipment>> = equipmentDao.getAllEquipments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun addEquipment(description: String, photoUri: String?, iconIdentifier: String? = null) {
        viewModelScope.launch {
            val currentEquipments = activeEquipments.first()
            val defaultIcon = appSettingsManager.favoriteIcon.first()
            
            val newEquipment = Equipment(
                description = description,
                photoUri = photoUri,
                iconIdentifier = iconIdentifier ?: if (photoUri == null) defaultIcon else null,
                displayOrder = currentEquipments.size
            )
            equipmentDao.insertEquipment(newEquipment)
        }
    }

    fun updateEquipment(equipment: Equipment) {
        viewModelScope.launch {
            equipmentDao.updateEquipment(equipment)
        }
    }

    fun updateEquipments(equipments: List<Equipment>) {
        viewModelScope.launch {
            equipmentDao.updateEquipments(equipments)
        }
    }

    fun dismissEquipment(equipment: Equipment) {
        viewModelScope.launch {
            updateEquipment(equipment.copy(dismissed = true))
        }
    }

    fun restoreEquipment(equipment: Equipment) {
        viewModelScope.launch {
            updateEquipment(equipment.copy(dismissed = false))
        }
    }
}
