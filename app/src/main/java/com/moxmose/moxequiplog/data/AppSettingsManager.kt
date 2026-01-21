package com.moxmose.moxequiplog.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppSettingsManager(private val context: Context) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val usernameKey = stringPreferencesKey("username")
    private val favoriteIconKey = stringPreferencesKey("favorite_icon")
    private val favoritePhotoUriKey = stringPreferencesKey("favorite_photo_uri")

    val username: StateFlow<String> = context.dataStore.data
        .map { it[usernameKey] ?: "" }
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), "")

    val favoriteIcon: StateFlow<String?> = context.dataStore.data
        .map { it[favoriteIconKey] }
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), null)

    val favoritePhotoUri: StateFlow<String?> = context.dataStore.data
        .map { it[favoritePhotoUriKey] }
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), null)

    suspend fun setUsername(username: String) {
        context.dataStore.edit { it[usernameKey] = username }
    }

    suspend fun setFavoriteResource(iconId: String?, photoUri: String?) {
        context.dataStore.edit {
            if (iconId != null) {
                it[favoriteIconKey] = iconId
                it.remove(favoritePhotoUriKey)
            } else if (photoUri != null) {
                it[favoritePhotoUriKey] = photoUri
                it.remove(favoriteIconKey)
            } else {
                it.remove(favoriteIconKey)
                it.remove(favoritePhotoUriKey)
            }
        }
    }
}
