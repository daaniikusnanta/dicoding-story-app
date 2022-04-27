package com.daaniikusnanta.storyapp.views.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.daaniikusnanta.storyapp.data.SettingPreferences
import com.daaniikusnanta.storyapp.databinding.ActivityAddStoryBinding
import com.daaniikusnanta.storyapp.views.SharedViewModel
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.daaniikusnanta.storyapp.R
import com.daaniikusnanta.storyapp.misc.rotateBitmap
import com.daaniikusnanta.storyapp.misc.uriToFile
import com.daaniikusnanta.storyapp.views.dataStore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.*

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private var token = ""
    private var photoFile: File? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var location: Location? = null
    private val addStoryViewModel by viewModels<AddStoryViewModel> {
        AddStoryViewModel.ViewModelFactory(this)
    }
    private val sharedViewModel by viewModels<SharedViewModel> {
        SharedViewModel.Factory (
            SettingPreferences.getInstance(dataStore)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        sharedViewModel.getToken().observe(this) {
            token = it
        }

        addStoryViewModel.apply {
            isLoading.observe(this@AddStoryActivity) {
                binding.btnUploadStory.isEnabled = it != true
                binding.btnUploadStory.text = if (it == true) getString(R.string.uploading) else getString(R.string.upload)
                binding.btnAddFromGallery.isEnabled = it != true
                binding.btnAddFromCamera.isEnabled = it != true
                binding.btnLocation.isEnabled = it != true
            }
            isSuccess.observe(this@AddStoryActivity) {
                if (it == true) {
                    finish()
                }
            }
            location.observe(this@AddStoryActivity) { loc ->
                this@AddStoryActivity.location = loc
                if (loc == null) {
                    binding.tvLocation.visibility = View.GONE
                    binding.btnLocation.text = getString(R.string.btn_location_text)
                    binding.btnLocation.setBackgroundColor(ContextCompat.getColor(this@AddStoryActivity, com.google.android.material.R.color.design_default_color_primary))
                } else {
                    binding.tvLocation.text =
                        try {
                            Geocoder.isPresent()
                                .let {
                                    if (it) {
                                        Geocoder(this@AddStoryActivity).getFromLocation(
                                            loc.latitude,
                                            loc.longitude,
                                            1
                                        )[0].locality
                                    } else {
                                        "${loc.latitude}, ${loc.longitude}"
                                    }
                                }
                        } catch (e: Exception) {
                            "${loc.latitude}, ${loc.longitude}"
                        }
                    binding.tvLocation.visibility = View.VISIBLE
                    binding.btnLocation.text = getString(R.string.remove)
                    binding.btnLocation.setBackgroundColor(ContextCompat.getColor(this@AddStoryActivity, com.google.android.material.R.color.design_default_color_error))
                }
            }
            errorMessage.observe(this@AddStoryActivity) {
                Toast.makeText(this@AddStoryActivity, getString(R.string.upload_failed), Toast.LENGTH_SHORT).show()
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.apply {
            btnAddFromCamera.setOnClickListener { startCameraX() }
            btnAddFromGallery.setOnClickListener { startGallery() }
            btnUploadStory.setOnClickListener { uploadStory() }
            btnLocation.setOnClickListener {
                if (location != null) {
                    addStoryViewModel.setLocation(null)
                } else {
                    getMyLastLocation()
                }
            }
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val resultFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            val result = rotateBitmap(
                BitmapFactory.decodeFile(resultFile.path),
                isBackCamera
            )
            photoFile = resultFile

            binding.imgPhotoPreview.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            photoFile = uriToFile(selectedImg, this@AddStoryActivity)
            binding.imgPhotoPreview.setImageURI(selectedImg)
        }
    }

    private fun startCameraX() {
        val intent = Intent(this@AddStoryActivity, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_a_picture))
        launcherIntentGallery.launch(chooser)
    }

    private fun uploadStory() {
        with(binding) {
            if(edtDescriptionInput.text.isEmpty() || photoFile == null) {
                return
            } else {
                addStoryViewModel.uploadStory(
                    photoFile!!,
                    edtDescriptionInput.text.toString(),
                    location,
                    token,
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    getString(R.string.permission_not_granted),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getMyLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                addStoryViewModel.setLocation(location)
            } else {
                Toast.makeText(
                    this@AddStoryActivity,
                    "Location is not found. Try Again",
                    Toast.LENGTH_SHORT
                ).show()
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

    companion object {
        const val CAMERA_X_RESULT = 200

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}