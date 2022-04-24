package com.daaniikusnanta.storyapp.views.main

import android.util.Log
import androidx.lifecycle.*
import com.daaniikusnanta.storyapp.api.ApiConfig
import com.daaniikusnanta.storyapp.api.ListStoryItem
import com.daaniikusnanta.storyapp.api.StoryResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel() : ViewModel() {
    private val _listStories = MutableLiveData<List<ListStoryItem>>()
    val listStories: MutableLiveData<List<ListStoryItem>> = _listStories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: MutableLiveData<String?> = _errorMessage

    var token = ""

    fun getStories(authToken: String, isLocation: Boolean = false) {
        _isLoading.value = true
        val auth = "Bearer $authToken"

        val client = ApiConfig.getApiService().getStories(
            auth,
            if (isLocation) 1 else 0,
        )
        client.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(
                call: Call<StoryResponse>,
                response: Response<StoryResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _listStories.value = response.body()?.listStory
                } else {
                    _errorMessage.value = response.body()?.message
                    Log.e(TAG, "onResponse onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }
}