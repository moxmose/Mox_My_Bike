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

    // Default per l'equipaggiamento (ora presi dalla tabella Category)
    val favoriteIcon: StateFlow<String?> = mediaRepository.allCategories
        .map { cats -> cats.find { it.id == "EQUIPMENT" }?.defaultIconIdentifier }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    val favoritePhotoUri: StateFlow<String?> = mediaRepository.allCategories
        .map { cats -> cats.find { it.id == "EQUIPMENT" }?.defaultPhotoUri }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    val hiddenIcons: StateFlow<Set<String>> = appSettingsManager.hiddenIcons
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptySet()
        )

    val hiddenImages: StateFlow<Set<String>> = appSettingsManager.hiddenImages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptySet()
        )

    val equipmentMedia: StateFlow<List<Media>> = mediaRepository.getMediaByCategory("EQUIPMENT")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
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

    // Imposta il default per una categoria specifica
    fun setCategoryDefault(categoryId: String, iconId: String?, photoUri: String?) {
        viewModelScope.launch {
            mediaRepository.setCategoryDefault(categoryId, iconId, photoUri)
        }
    }

    fun toggleIconVisibility(iconId: String) {
        viewModelScope.launch {
            appSettingsManager.toggleIconVisibility(iconId)
        }
    }

    fun toggleImageVisibility(uri: String) {
        viewModelScope.launch {
            appSettingsManager.toggleImageVisibility(uri)
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

    suspend fun isPhotoUsed(uri: String): Boolean {
        return equipmentDao.countEquipmentsUsingPhoto(uri) > 0
    }
}
