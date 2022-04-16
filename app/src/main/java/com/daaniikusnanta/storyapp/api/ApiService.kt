package com.daaniikusnanta.storyapp.api

import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field ("name") name: String,
        @Field ("email") email: String,
        @Field ("password") password: String,
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field ("email") email: String,
        @Field ("password") password: String,
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("stories")
    fun addStory(
        @Path("login") id: String,
        @Header("Authorization") token: String,
    ): Call<ApiResponse>

    @GET("stories")
    fun getStories(
        @Header("Authorization") token: String,
    ): Call<StoryResponse>
}