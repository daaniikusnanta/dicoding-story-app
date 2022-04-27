package com.daaniikusnanta.storyapp.data

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.daaniikusnanta.storyapp.api.ApiService
import com.daaniikusnanta.storyapp.api.ListStoryItem
import okhttp3.MultipartBody
import okhttp3.RequestBody

@OptIn(ExperimentalPagingApi::class)
class StoryRepository(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
    ) {

    fun getStory(auth: String, isLocation: Int = 0): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, auth, isLocation),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }

    suspend fun getStoriesWithLocation(auth: String) =
        apiService.getStories(auth, 1).listStory

    suspend fun addStory(
        auth: String,
        multipart: MultipartBody.Part,
        params: HashMap<String, RequestBody>
    ) =
        !apiService.addStory(multipart, params, auth).error
}