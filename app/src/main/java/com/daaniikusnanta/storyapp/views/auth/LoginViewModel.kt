package com.daaniikusnanta.storyapp.views.auth

import androidx.lifecycle.*
import com.daaniikusnanta.storyapp.data.AuthRepository
import com.daaniikusnanta.storyapp.data.Injection
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _token = MutableLiveData<String>()
    val token: MutableLiveData<String> = _token

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: MutableLiveData<String?> = _errorMessage

    fun login(email: String, password: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                _token.value = authRepository.login(email, password)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    class ViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(Injection.provideAuthRepository()) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}