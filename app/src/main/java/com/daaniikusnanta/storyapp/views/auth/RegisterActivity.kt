package com.daaniikusnanta.storyapp.views.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.daaniikusnanta.storyapp.R
import com.daaniikusnanta.storyapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityRegisterBinding
    private val registerViewModel by viewModels<RegisterViewModel> {
        RegisterViewModel.ViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener(this)
        binding.tvLoginButton.setOnClickListener(this)

        registerViewModel.apply {
            isLoading.observe(this@RegisterActivity) {
                binding.btnRegister.isEnabled = it != true
                binding.btnRegister.text = if (it == true) getString(R.string.registering) else getString(R.string.register)
            }
            isSuccess.observe(this@RegisterActivity) {
                if (it == true) {
                    Toast.makeText(this@RegisterActivity, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            errorMessage.observe(this@RegisterActivity) {
                Toast.makeText(this@RegisterActivity, getString(R.string.register_failed, it!!), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnRegister.id -> {
                registerViewModel.register(
                    binding.edtName.text.toString(),
                    binding.edtEmail.text.toString(),
                    binding.edtPassword.text.toString(),
                )
            }
            binding.tvLoginButton.id -> {
                val moveToLogin = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(moveToLogin)
            }
        }
    }
}