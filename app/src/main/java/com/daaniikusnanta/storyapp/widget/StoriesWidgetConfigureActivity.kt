package com.daaniikusnanta.storyapp.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.daaniikusnanta.storyapp.R
import com.daaniikusnanta.storyapp.data.SettingPreferences
import com.daaniikusnanta.storyapp.databinding.ActivityStoriesWidgetConfigureBinding
import com.daaniikusnanta.storyapp.views.SharedViewModel
import com.daaniikusnanta.storyapp.views.auth.LoginViewModel
import com.daaniikusnanta.storyapp.views.auth.RegisterActivity
import com.daaniikusnanta.storyapp.views.dataStore

class StoriesWidgetConfigureActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityStoriesWidgetConfigureBinding
    private val loginViewModel by viewModels<LoginViewModel>()
    private val sharedViewModel by viewModels<SharedViewModel> {
        SharedViewModel.Factory(
            SettingPreferences.getInstance(dataStore)
        )
    }
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoriesWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setResult(RESULT_CANCELED)

        binding.btnLogin.setOnClickListener(this)
        binding.tvRegisterButton.setOnClickListener(this)

        loginViewModel.apply {
            isLoading.observe(this@StoriesWidgetConfigureActivity) {
                binding.btnLogin.isEnabled = it != true
                binding.btnLogin.text = if (it == true) getString(R.string.logging_in) else getString(R.string.login)
            }
            token.observe(this@StoriesWidgetConfigureActivity) {
                sharedViewModel.saveToken(it)
            }
            errorMessage.observe(this@StoriesWidgetConfigureActivity) {
                Toast.makeText(this@StoriesWidgetConfigureActivity, it, Toast.LENGTH_SHORT).show()
            }
        }

        sharedViewModel.apply {
            getToken().observe(this@StoriesWidgetConfigureActivity) {
                if (it.isNotEmpty()) {
                    showWidget()
                }
            }
        }

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnLogin.id -> {
                loginViewModel.login(
                    binding.edtEmail.text.toString(),
                    binding.edtPassword.text.toString()
                )
            }
            binding.tvRegisterButton.id -> {
                val moveToRegister = Intent(this, RegisterActivity::class.java)
                startActivity(moveToRegister)
            }
        }
    }

    private fun showWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        StoriesWidget.updateAppWidget(this, appWidgetManager, appWidgetId)

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }
}