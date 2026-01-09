package com.moxmose.moxmaintenancelogs.ui.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moxmose.moxmaintenancelogs.data.AppSettingsManager
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

    fun setUsername(newUsername: String) {
        viewModelScope.launch {
            appSettingsManager.setUsername(newUsername)
        }
    }
}
