package com.daaniikusnanta.storyapp.views

import androidx.lifecycle.*
import com.daaniikusnanta.storyapp.data.SettingPreferences
import kotlinx.coroutines.launch

class SharedViewModel(private val pref: SettingPreferences) : ViewModel() {
    init {
        getThemeSettings()
    }

    fun getToken(): LiveData<String> {
        return pref.getTokenSetting().asLiveData()
    }

    fun saveToken(token: String) {
        viewModelScope.launch {
            pref.saveTokenSetting(token)
        }
    }

    fun getThemeSettings(): LiveData<Boolean> {
        return pref.getThemeSetting().asLiveData()
    }

    fun saveThemeSetting(isDarkModeActive: Boolean) {
        viewModelScope.launch {
            pref.saveThemeSetting(isDarkModeActive)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val pref: SettingPreferences) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SharedViewModel(pref) as T
        }
    }
}