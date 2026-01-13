package com.moxmose.moxequiplog.ui.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moxmose.moxequiplog.data.AppSettingsManager
import com.moxmose.moxequiplog.data.MediaRepository
import com.moxmose.moxequiplog.data.local.AppColor
import com.moxmose.moxequiplog.data.local.Category
import com.moxmose.moxequiplog.data.local.EquipmentDao
import com.moxmose.moxequiplog.data.local.Media
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OptionsViewModel(
    private val appSettingsManager: AppSettingsManager,
    private val equipmentDao: EquipmentDao,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            mediaRepository.initializeAppData()
        }
    }

    val username: StateFlow<String> = appSettingsManager.username
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ""
        )

    val allMedia: StateFlow<List<Media>> = mediaRepository.allMedia
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val allCategories: StateFlow<List<Category>> = mediaRepository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val allColors: StateFlow<List<AppColor>> = mediaRepository.allColors
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun setUsername(newUsername: String) {
        viewModelScope.launch {
            appSettingsManager.setUsername(newUsername)
        }
    }

    fun setCategoryDefault(categoryId: String, iconId: String?, photoUri: String?) {
        viewModelScope.launch {
            mediaRepository.setCategoryDefault(categoryId, iconId, photoUri)
        }
    }

    fun toggleMediaVisibility(uri: String, category: String) {
        viewModelScope.launch {
            mediaRepository.toggleMediaVisibility(uri, category)
        }
    }

    fun addMedia(uri: String, category: String) {
        viewModelScope.launch {
            mediaRepository.addMedia(uri, category)
        }
    }

    fun removeMedia(uri: String, category: String) {
        viewModelScope.launch {
            mediaRepository.removeMedia(uri, category)
        }
    }

    fun updateMediaOrder(mediaList: List<Media>) {
        viewModelScope.launch {
            mediaRepository.updateMediaOrder(mediaList)
        }
    }

    fun updateCategoryColor(categoryId: String, colorHex: String) {
        viewModelScope.launch {
            mediaRepository.updateCategoryColor(categoryId, colorHex)
        }
    }

    fun addColor(hex: String, name: String) {
        viewModelScope.launch {
            mediaRepository.addColor(hex, name)
        }
    }

    fun updateColor(color: AppColor) {
        viewModelScope.launch {
            mediaRepository.updateColor(color)
        }
    }

    fun updateColorsOrder(colors: List<AppColor>) {
        viewModelScope.launch {
            mediaRepository.updateColorsOrder(colors)
        }
    }

    fun toggleColorVisibility(id: Long) {
        viewModelScope.launch {
            mediaRepository.toggleColorVisibility(id)
        }
    }

    fun deleteColor(color: AppColor) {
        viewModelScope.launch {
            mediaRepository.deleteColor(color)
        }
    }

    suspend fun isPhotoUsed(uri: String): Boolean {
        return equipmentDao.countEquipmentsUsingPhoto(uri) > 0
    }
}
