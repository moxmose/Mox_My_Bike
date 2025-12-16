package com.moxmose.moxmybike.ui.bikes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moxmose.moxmybike.data.local.Bike
import com.moxmose.moxmybike.data.local.BikeDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BikesViewModel(private val bikeDao: BikeDao) : ViewModel() {

    val allBikes: StateFlow<List<Bike>> = bikeDao.getAllBikes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun addBike(description: String) {
        viewModelScope.launch {
            bikeDao.insertBike(Bike(description = description))
        }
    }

    fun updateBike(bike: Bike) {
        viewModelScope.launch {
            bikeDao.updateBike(bike)
        }
    }
}
