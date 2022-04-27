package com.daaniikusnanta.storyapp.views.main

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.daaniikusnanta.storyapp.data.Injection
import com.daaniikusnanta.storyapp.data.StoryRepository
import com.daaniikusnanta.storyapp.misc.reduceFileImage
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AddStoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean> = _isLoading

    private val _location = MutableLiveData<Location?>()
    val location: MutableLiveData<Location?> = _location

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: MutableLiveData<Boolean> = _isSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: MutableLiveData<String?> = _errorMessage

    fun setLocation(location: Location?) {
        _location.value = location
    }

    fun uploadStory(photo: File, description: String, location: Location? = null, authToken: String) {
        val auth = "Bearer $authToken"
        _isLoading.value = true

        val reducedPhoto = reduceFileImage(photo)
        val descriptionMultiPart = description.toRequestBody("text/plain".toMediaType())
        val photoMultiPart = MultipartBody.Part.createFormData(
            "photo",
            reducedPhoto.name,
            reducedPhoto.asRequestBody("image/*".toMediaType())
        )

        val params = mutableMapOf(
            "description" to descriptionMultiPart,
        )
        location?.let {
            val latMultiPart = it.latitude.toString().toRequestBody("text/plain".toMediaType())
            val lonMultiPart = it.longitude.toString().toRequestBody("text/plain".toMediaType())

            params["lat"] = latMultiPart
            params["lon"] = lonMultiPart
        }

        viewModelScope.launch {
            try {
                _isSuccess.value = storyRepository.addStory(auth, photoMultiPart, HashMap(params))
            } catch (e: Exception) {
                _errorMessage.value = e.message
                Log.e("AddStoryViewModel", e.message.toString())
            } finally {
                _isLoading.value = false
            }
        }
    }

    class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddStoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AddStoryViewModel(Injection.provideStoryRepository(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}