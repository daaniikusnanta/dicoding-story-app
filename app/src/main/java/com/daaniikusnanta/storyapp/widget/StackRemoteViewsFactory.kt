package com.daaniikusnanta.storyapp.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.daaniikusnanta.storyapp.R
import com.daaniikusnanta.storyapp.api.ApiConfig
import com.daaniikusnanta.storyapp.api.ListStoryItem
import com.daaniikusnanta.storyapp.data.SettingPreferences
import com.daaniikusnanta.storyapp.views.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

internal class StackRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    var stories = ArrayList<ListStoryItem>()

    override fun onDataSetChanged(): Unit = runBlocking {
        val pref = SettingPreferences.getInstance(context.dataStore)
        val token = pref.getTokenSetting().first()
        val auth = "Bearer $token"

        try {
            stories.addAll(
                ApiConfig.getApiService().getStories(auth, 0).listStory
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate() {

    }

    override fun onDestroy() {

    }

    override fun getCount(): Int = stories.size

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.stories_widget_item)
        val photo = Glide.with(context)
            .asBitmap()
            .load(stories[position].photoUrl)
            .submit()
            .get()
        rv.setImageViewBitmap(R.id.imageView, photo)

        val extras = bundleOf(
            StoriesWidget.EXTRA_ITEM to stories[position].name
        )

        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)

        rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(i: Int): Long = 0

    override fun hasStableIds(): Boolean = false
}