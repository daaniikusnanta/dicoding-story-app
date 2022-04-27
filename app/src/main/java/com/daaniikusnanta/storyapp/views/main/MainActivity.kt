package com.daaniikusnanta.storyapp.views.main

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.daaniikusnanta.storyapp.R
import com.daaniikusnanta.storyapp.adapter.ListStoryAdapter
import com.daaniikusnanta.storyapp.adapter.LoadingStateAdapter
import com.daaniikusnanta.storyapp.api.ListStoryItem
import com.daaniikusnanta.storyapp.data.SettingPreferences
import com.daaniikusnanta.storyapp.databinding.ActivityMainBinding
import com.daaniikusnanta.storyapp.databinding.ItemStoryBinding
import com.daaniikusnanta.storyapp.views.SharedViewModel
import com.daaniikusnanta.storyapp.views.auth.LoginActivity
import com.daaniikusnanta.storyapp.views.dataStore
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private var token = ""
    private var listStoryAdapter = ListStoryAdapter()
    private val mainViewModel by viewModels<MainViewModel> {
        MainViewModel.ViewModelFactory(this, token)
    }
    private val sharedViewModel by viewModels<SharedViewModel> {
        SharedViewModel.Factory(
            SettingPreferences.getInstance(dataStore)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = intent.getStringExtra("token")?.let { "Bearer $it" } ?: ""

        binding.rvStories.setHasFixedSize(true)
        if (applicationContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.rvStories.layoutManager = GridLayoutManager(this, 2)
        } else {
            binding.rvStories.layoutManager = LinearLayoutManager(this)
        }

        sharedViewModel.apply {
            getToken().observe(this@MainActivity) { token ->
                this@MainActivity.token = "Bearer $token"

                if (token.isEmpty()) {
                    val moveToLogin = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(moveToLogin)
                    finish()
                }
            }
            getThemeSettings().observe(this@MainActivity) { isDarkModeActive: Boolean ->
                if (isDarkModeActive) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }

        binding.fabAddStory.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        Log.i("token", mainViewModel.auth)
        mainViewModel.stories.observe(this@MainActivity) {
            if (it != null) {
                setStories(it)
            }
        }

        mainViewModel.apply {
            isLoading.observe(this@MainActivity) {
                binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
                binding.rvStories.visibility = if (it) View.GONE else View.VISIBLE
            }

            errorMessage.observe(this@MainActivity) {
                if (it != null) {
                    Snackbar.make(binding.coordinatorLayout, getString(R.string.fetch_stories_failed), Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry.toString()) { listStoryAdapter.refresh() }
                        .show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        listStoryAdapter.refresh()
    }

    private fun setStories(data: PagingData<ListStoryItem>) {
        binding.rvStories.adapter = listStoryAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                listStoryAdapter.retry()
            }
        )
        listStoryAdapter.submitData(lifecycle, data)

        listStoryAdapter.setOnItemClickCallback(object : ListStoryAdapter.OnItemClickCallback {
            override fun onItemClicked(data: ListStoryItem?, binding: ItemStoryBinding) {
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@MainActivity,
                        Pair.create(binding.imgStory, "photo"),
                        Pair.create(binding.tvUsername, "username"),
                        Pair.create(binding.tvTime, "time"),
                    )

                val moveToDetail = Intent(this@MainActivity, StoryDetailActivity::class.java)
                moveToDetail.putExtra(StoryDetailActivity.EXTRA_STORY, data)
                startActivity(moveToDetail, optionsCompat.toBundle())
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_top_app_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                val moveToSetting = Intent(this, SettingsActivity::class.java)
                startActivity(moveToSetting)
                true
            }
            R.id.menu_refresh -> {
                listStoryAdapter.refresh()
                Toast.makeText(this, getString(R.string.refreshing), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_maps -> {
                val moveToMaps = Intent(this, MapsActivity::class.java)
                startActivity(moveToMaps)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            binding.fabAddStory.id -> {
                val moveToAddStory = Intent(this, AddStoryActivity::class.java)
                startActivity(moveToAddStory)
            }
        }
    }
}