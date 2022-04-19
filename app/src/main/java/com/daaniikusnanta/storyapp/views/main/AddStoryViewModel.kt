package com.daaniikusnanta.storyapp.views.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.daaniikusnanta.storyapp.api.ApiConfig
import com.daaniikusnanta.storyapp.api.ApiResponse
import com.daaniikusnanta.storyapp.api.StoryResponse
import com.daaniikusnanta.storyapp.misc.reduceFileImage
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddStoryViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: MutableLiveData<Boolean> = _isSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: MutableLiveData<String?> = _errorMessage

    fun uploadStory(photo: File, description: String, authToken: String) {
        val auth = "Bearer $authToken"
        _isLoading.value = true

        val reducedPhoto = reduceFileImage(photo)
        val descriptionMultiPart = description.toRequestBody("text/plain".toMediaType())
        val photoMultiPart = MultipartBody.Part.createFormData(
            "photo",
            reducedPhoto.name,
            reducedPhoto.asRequestBody("image/*".toMediaType())
        )

        val client = ApiConfig.getApiService().addStory(
            photoMultiPart,
            descriptionMultiPart,
            auth
        )
        client.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(
                call: Call<ApiResponse>,
                response: Response<ApiResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _isSuccess.value = true
                } else {
                    _errorMessage.value = response.body()?.message
                    Log.e(TAG, "onResponse onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    companion object {
        private val TAG = AddStoryViewModel::class.java.simpleName
    }
}