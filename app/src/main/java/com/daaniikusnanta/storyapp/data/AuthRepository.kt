package com.daaniikusnanta.storyapp.data

import com.daaniikusnanta.storyapp.api.ApiService

class AuthRepository (
    private val apiService: ApiService,
) {

    suspend fun login(email: String, password: String) =
        apiService.login(email, password)

    suspend fun register(name: String, email: String, password: String) =
        apiService.register(name, email, password)
}