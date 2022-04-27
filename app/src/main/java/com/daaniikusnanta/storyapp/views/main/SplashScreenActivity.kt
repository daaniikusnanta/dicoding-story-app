package com.daaniikusnanta.storyapp.views.main

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import com.daaniikusnanta.storyapp.data.SettingPreferences
import com.daaniikusnanta.storyapp.databinding.ActivitySplashScreenBinding
import com.daaniikusnanta.storyapp.views.SharedViewModel
import com.daaniikusnanta.storyapp.views.auth.LoginActivity
import com.daaniikusnanta.storyapp.views.dataStore

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private val sharedViewModel by viewModels<SharedViewModel> {
        SharedViewModel.Factory(
            SettingPreferences.getInstance(dataStore)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        sharedViewModel.apply {
            getToken().observe(this@SplashScreenActivity) { token ->

                if (token.isEmpty()) {
                    val moveToLogin = Intent(this@SplashScreenActivity, LoginActivity::class.java)
                    startActivity(moveToLogin)
                    finish()
                } else {
                    val moveToMain = Intent(this@SplashScreenActivity, MainActivity::class.java)
                    moveToMain.putExtra("token", token)
                    startActivity(moveToMain)
                    finish()
                }
            }
            getThemeSettings().observe(this@SplashScreenActivity) { isDarkModeActive: Boolean ->
                if (isDarkModeActive) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
    }
}