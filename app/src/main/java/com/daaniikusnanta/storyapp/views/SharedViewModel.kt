package com.daaniikusnanta.storyapp.views

import androidx.lifecycle.*
import com.daaniikusnanta.storyapp.data.SettingPreferences
import kotlinx.coroutines.launch

class SharedViewModel(private val pref: SettingPreferences) : ViewModel() {
    fun getToken(): LiveData<String> {
        return pref.getTokenSetting().asLiveData()
    }

    fun saveToken(token: String) {
        viewModelScope.launch {
            pref.saveTokenSetting(token)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val pref: SettingPreferences) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SharedViewModel(pref) as T
        }
    }
}