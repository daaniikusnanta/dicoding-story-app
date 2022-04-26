package com.daaniikusnanta.storyapp.views.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.daaniikusnanta.storyapp.api.ApiConfig
import com.daaniikusnanta.storyapp.api.ListStoryItem
import com.daaniikusnanta.storyapp.api.StoryResponse
import com.daaniikusnanta.storyapp.data.Injection
import com.daaniikusnanta.storyapp.data.StoryRepository
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _listStories = MutableLiveData<List<ListStoryItem>>()
    val listStories: MutableLiveData<List<ListStoryItem>> = _listStories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: MutableLiveData<String?> = _errorMessage

    var token = ""

    fun getStoriesWithLocation(authToken: String) {
        _isLoading.value = true
        val auth = "Bearer $authToken"

        viewModelScope.launch {
            _listStories.value = ApiConfig.getApiService().getStories(auth, 1).listStory
            _isLoading.value = false
        }
    }

    fun getStories(authToken: String, isLocation: Boolean = false) : LiveData<PagingData<ListStoryItem>> {
        _isLoading.value = true
        val auth = "Bearer $authToken"

        val storyData = storyRepository.getStory(auth, if (isLocation) 1 else 0).cachedIn(viewModelScope)
        _isLoading.value = false

        return storyData
    }

    class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(Injection.provideRepository(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }
}