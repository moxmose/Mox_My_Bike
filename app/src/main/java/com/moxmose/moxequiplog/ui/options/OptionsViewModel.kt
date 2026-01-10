package com.moxmose.moxequiplog.ui.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moxmose.moxequiplog.data.AppSettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OptionsViewModel(private val appSettingsManager: AppSettingsManager) : ViewModel() {

    val username: StateFlow<String> = appSettingsManager.username
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ""
        )

    val favoriteIcon: StateFlow<String?> = appSettingsManager.favoriteIcon
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    val favoritePhotoUri: StateFlow<String?> = appSettingsManager.favoritePhotoUri
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    fun setUsername(newUsername: String) {
        viewModelScope.launch {
            appSettingsManager.setUsername(newUsername)
        }
    }

    fun setFavoriteResource(iconId: String?, photoUri: String?) {
        viewModelScope.launch {
            appSettingsManager.setFavoriteResource(iconId, photoUri)
        }
    }
}
