package com.moxmose.moxmybike.data

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

    val username: Flow<String> = context.dataStore.data
        .map {
            it[usernameKey] ?: ""
        }

    suspend fun setUsername(username: String) {
        context.dataStore.edit {
            it[usernameKey] = username
        }
    }
}