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
    private val registerViewModel by viewModels<RegisterViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener(this)
        binding.tvLoginButton.setOnClickListener(this)

        registerViewModel.apply {
            isLoading.observe(this@RegisterActivity) {
                binding.btnRegister.isEnabled = it != true
            }
            isSuccess.observe(this@RegisterActivity) {
                if (it == true) {
                    Toast.makeText(this@RegisterActivity, "Register Success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                }
            }
            errorMessage.observe(this@RegisterActivity) {
                Toast.makeText(this@RegisterActivity, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_register -> {
                registerViewModel.register(
                    binding.edtName.text.toString(),
                    binding.edtEmail.text.toString(),
                    binding.edtPassword.text.toString(),
                )
            }
            R.id.tv_login_button -> {
                val moveToLogin = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(moveToLogin)
            }
        }
    }
}