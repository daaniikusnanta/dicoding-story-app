package com.daaniikusnanta.storyapp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingPreferences private constructor(private val dataStore: DataStore<Preferences>){
    private val KEY_THEME = booleanPreferencesKey("theme")
    private val KEY_TOKEN = stringPreferencesKey("token")

    fun getThemeSetting(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[KEY_THEME] ?: false
        }
    }

    suspend fun saveThemeSetting(isDarkModeActive: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME] = isDarkModeActive
        }
    }

    fun getTokenSetting(): Flow<String> {
        return dataStore.data.map {
            it[KEY_TOKEN] ?: ""
        }
    }

    suspend fun saveTokenSetting(token: String?) {
        dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token ?: ""
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>) = INSTANCE ?: synchronized(this) {
            SettingPreferences(dataStore)
        }.also { INSTANCE = it }
    }
}