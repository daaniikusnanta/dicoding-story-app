package com.daaniikusnanta.storyapp.data

import android.content.Context
import com.daaniikusnanta.storyapp.api.ApiConfig

object Injection {
    fun provideStoryRepository(context: Context, auth: String): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()
        return StoryRepository(database, apiService, auth)
    }

    fun provideAuthRepository() : AuthRepository {
        val apiService = ApiConfig.getApiService()
        return AuthRepository(apiService)
    }
}