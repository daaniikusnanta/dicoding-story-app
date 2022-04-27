package com.daaniikusnanta.storyapp.views.main

import android.content.Context
import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.daaniikusnanta.storyapp.api.ListStoryItem
import com.daaniikusnanta.storyapp.data.Injection
import com.daaniikusnanta.storyapp.data.StoryRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainViewModel(
    private val storyRepository: StoryRepository,
) : ViewModel() {
    private val _listStories = MutableLiveData<List<ListStoryItem>>()
    val listStories: MutableLiveData<List<ListStoryItem>> = _listStories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: MutableLiveData<String?> = _errorMessage

    var auth = ""

    fun getStoriesWithLocation(authToken: String) {
        _isLoading.value = true
        val auth = "Bearer $authToken"

        viewModelScope.launch {
            try {
                _listStories.value = storyRepository.getStoriesWithLocation(auth).listStory
            } catch (e: HttpException) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    var stories = storyRepository.getStory(1).cachedIn(viewModelScope)

    class ViewModelFactory(private val context: Context, private val authToken: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(Injection.provideStoryRepository(context, authToken)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}