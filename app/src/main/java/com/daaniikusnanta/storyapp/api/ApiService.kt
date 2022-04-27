package com.daaniikusnanta.storyapp.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field ("name") name: String,
        @Field ("email") email: String,
        @Field ("password") password: String,
    ): ApiResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field ("email") email: String,
        @Field ("password") password: String,
    ): LoginResponse

    @Multipart
    @POST("stories")
    suspend fun addStory(
        @Part file: MultipartBody.Part,
        @PartMap params: HashMap<String, RequestBody>,
        @Header("Authorization") token: String,
    ): ApiResponse

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") token: String,
        @Query("location") location: Int = 0,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 15,
    ): StoryResponse
}