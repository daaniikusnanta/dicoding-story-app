package com.daaniikusnanta.storyapp.views.auth

import androidx.lifecycle.*
import com.daaniikusnanta.storyapp.data.AuthRepository
import com.daaniikusnanta.storyapp.data.Injection
import kotlinx.coroutines.launch

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData<Boolean?>()
    val isSuccess: MutableLiveData<Boolean?> = _isSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: MutableLiveData<String?> = _errorMessage

    fun register(name: String, email: String, password: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                _isSuccess.value = authRepository.register(name, email, password)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    class ViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RegisterViewModel(Injection.provideAuthRepository()) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}