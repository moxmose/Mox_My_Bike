package com.moxmose.moxequiplog.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppSettingsManager(private val context: Context) {

    private val usernameKey = stringPreferencesKey("username")
    private val favoriteIconKey = stringPreferencesKey("favorite_icon")
    private val favoritePhotoUriKey = stringPreferencesKey("favorite_photo_uri")

    val username: Flow<String> = context.dataStore.data
        .map { it[usernameKey] ?: "" }

    val favoriteIcon: Flow<String?> = context.dataStore.data
        .map { it[favoriteIconKey] }

    val favoritePhotoUri: Flow<String?> = context.dataStore.data
        .map { it[favoritePhotoUriKey] }

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
            }
        }
    }
}