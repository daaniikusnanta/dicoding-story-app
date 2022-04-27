package com.daaniikusnanta.storyapp.views.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.daaniikusnanta.storyapp.R
import com.daaniikusnanta.storyapp.data.SettingPreferences
import com.daaniikusnanta.storyapp.databinding.ActivityLoginBinding
import com.daaniikusnanta.storyapp.views.SharedViewModel
import com.daaniikusnanta.storyapp.views.dataStore
import com.daaniikusnanta.storyapp.views.main.MainActivity

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel by viewModels<LoginViewModel> {
        LoginViewModel.ViewModelFactory()
    }
    private val sharedViewModel by viewModels<SharedViewModel> {
        SharedViewModel.Factory(
            SettingPreferences.getInstance(dataStore)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener(this)
        binding.tvRegisterButton.setOnClickListener(this)

        loginViewModel.apply {
            isLoading.observe(this@LoginActivity) {
                binding.btnLogin.isEnabled = it != true
                binding.btnLogin.text = if (it == true) getString(R.string.logging_in) else getString(R.string.login)
            }
            token.observe(this@LoginActivity) {
                sharedViewModel.saveToken(it)
            }
            errorMessage.observe(this@LoginActivity) {
                Toast.makeText(this@LoginActivity, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
            }
        }

        sharedViewModel.apply {
            getToken().observe(this@LoginActivity) {
                if (it.isNotEmpty()) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
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
}