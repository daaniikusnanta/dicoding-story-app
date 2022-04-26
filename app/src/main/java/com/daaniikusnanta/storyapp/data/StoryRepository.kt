package com.daaniikusnanta.storyapp.data

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.daaniikusnanta.storyapp.api.ApiService
import com.daaniikusnanta.storyapp.api.ListStoryItem

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
}