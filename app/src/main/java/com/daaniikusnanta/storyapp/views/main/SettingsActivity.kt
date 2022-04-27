package com.daaniikusnanta.storyapp.views.main

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.daaniikusnanta.storyapp.data.SettingPreferences
import com.daaniikusnanta.storyapp.databinding.ActivitySettingsBinding
import com.daaniikusnanta.storyapp.views.SharedViewModel
import com.daaniikusnanta.storyapp.views.dataStore

class SettingsActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivitySettingsBinding
    private var isDarkModeActive: Boolean = false
    private val sharedViewModel by viewModels<SharedViewModel> {
        SharedViewModel.Factory(
            SettingPreferences.getInstance(dataStore)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnLogout.setOnClickListener(this)
        binding.btnChangeLanguage.setOnClickListener(this)
        binding.btnDarkMode.setOnClickListener(this)

        sharedViewModel.getThemeSettings().observe(this) { isDarkModeActive: Boolean ->
            this.isDarkModeActive = isDarkModeActive
            if (isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            binding.btnDarkMode.isChecked = isDarkModeActive
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            binding.btnLogout.id -> {
                logout()
                finish()
            }
            binding.btnChangeLanguage.id -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }
            binding.btnDarkMode.id -> {
                sharedViewModel.saveThemeSetting(!isDarkModeActive)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        sharedViewModel.saveToken("")
    }
}