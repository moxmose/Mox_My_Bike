package com.moxmose.moxequiplog.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppSettingsManager(private val context: Context) {

    private val usernameKey = stringPreferencesKey("username")
    private val favoriteIconKey = stringPreferencesKey("favorite_icon")
    private val favoritePhotoUriKey = stringPreferencesKey("favorite_photo_uri")
    private val hiddenIconsKey = stringSetPreferencesKey("hidden_icons")
    private val hiddenImagesKey = stringSetPreferencesKey("hidden_images")

    val username: Flow<String> = context.dataStore.data
        .map { it[usernameKey] ?: "" }

    val favoriteIcon: Flow<String?> = context.dataStore.data
        .map { it[favoriteIconKey] }

    val favoritePhotoUri: Flow<String?> = context.dataStore.data
        .map { it[favoritePhotoUriKey] }

    val hiddenIcons: Flow<Set<String>> = context.dataStore.data
        .map { it[hiddenIconsKey] ?: emptySet() }

    val hiddenImages: Flow<Set<String>> = context.dataStore.data
        .map { it[hiddenImagesKey] ?: emptySet() }

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

    suspend fun toggleIconVisibility(iconId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[hiddenIconsKey] ?: emptySet()
            if (current.contains(iconId)) {
                preferences[hiddenIconsKey] = current - iconId
            } else {
                preferences[hiddenIconsKey] = current + iconId
            }
        }
    }

    suspend fun toggleImageVisibility(uri: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[hiddenImagesKey] ?: emptySet()
            if (current.contains(uri)) {
                preferences[hiddenImagesKey] = current - uri
            } else {
                preferences[hiddenImagesKey] = current + uri
            }
        }
    }
}