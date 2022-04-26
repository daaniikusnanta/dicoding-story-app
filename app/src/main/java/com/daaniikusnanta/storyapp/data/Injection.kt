package com.daaniikusnanta.storyapp.data

import android.content.Context
import com.daaniikusnanta.storyapp.api.ApiConfig

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()
        return StoryRepository(database, apiService)
    }
}