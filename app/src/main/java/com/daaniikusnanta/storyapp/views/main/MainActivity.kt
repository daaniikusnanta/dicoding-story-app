package com.daaniikusnanta.storyapp.views.main

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.daaniikusnanta.storyapp.adapter.ListStoryAdapter
import com.daaniikusnanta.storyapp.api.ListStoryItem
import com.daaniikusnanta.storyapp.data.SettingPreferences
import com.daaniikusnanta.storyapp.databinding.ActivityMainBinding
import com.daaniikusnanta.storyapp.views.SharedViewModel
import com.daaniikusnanta.storyapp.views.auth.LoginActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel by viewModels<MainViewModel>()
    private val sharedViewModel by viewModels<SharedViewModel> {
        SharedViewModel.Factory(
            SettingPreferences.getInstance(dataStore)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (applicationContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.rvStories.layoutManager = GridLayoutManager(this, 2)
        } else {
            binding.rvStories.layoutManager = LinearLayoutManager(this)
        }

        sharedViewModel.getToken().observe(this) {
            mainViewModel.token = it
            if (it.isEmpty()) {
                val moveToLogin = Intent(this, LoginActivity::class.java)
                startActivity(moveToLogin)
                finish()
            } else {
                mainViewModel.getStories(it)
            }
        }

        mainViewModel.apply {
            isLoading.observe(this@MainActivity) {
                binding.progressBar.visibility = if (it) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
            }
            listStories.observe(this@MainActivity) {
                if(it.isNotEmpty()) {
                    setStories(it)
                }
            }
        }
    }

    private fun setStories(listStory: List<ListStoryItem>) {
        val listStoryAdapter = ListStoryAdapter(listStory)
        binding.rvStories.adapter = listStoryAdapter

        listStoryAdapter.setOnItemClickCallback(object : ListStoryAdapter.OnItemClickCallback {
            override fun onItemClicked(data: ListStoryItem) {
                TODO()
            }
        })
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}