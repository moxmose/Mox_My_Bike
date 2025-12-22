package com.moxmose.moxmybike.ui.bikes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moxmose.moxmybike.data.local.Bike
import com.moxmose.moxmybike.data.local.BikeDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BikesViewModel(private val bikeDao: BikeDao) : ViewModel() {

    val activeBikes: StateFlow<List<Bike>> = bikeDao.getActiveBikes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val allBikes: StateFlow<List<Bike>> = bikeDao.getAllBikes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun addBike(description: String, photoUri: String?) {
        viewModelScope.launch {
            val currentBikes = activeBikes.first()
            val newBike = Bike(
                description = description,
                photoUri = photoUri,
                displayOrder = currentBikes.size
            )
            bikeDao.insertBike(newBike)
        }
    }

    fun updateBike(bike: Bike) {
        viewModelScope.launch {
            bikeDao.updateBike(bike)
        }
    }

    fun updateBikes(bikes: List<Bike>) {
        viewModelScope.launch {
            bikeDao.updateBikes(bikes)
        }
    }

    fun dismissBike(bike: Bike) {
        viewModelScope.launch {
            updateBike(bike.copy(dismissed = true))
        }
    }

    fun restoreBike(bike: Bike) {
        viewModelScope.launch {
            updateBike(bike.copy(dismissed = false))
        }
    }
}
