package com.daaniikusnanta.storyapp.views.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.daaniikusnanta.storyapp.R
import com.daaniikusnanta.storyapp.api.ListStoryItem
import com.daaniikusnanta.storyapp.data.SettingPreferences
import com.daaniikusnanta.storyapp.databinding.ActivityMapsBinding
import com.daaniikusnanta.storyapp.views.SharedViewModel
import com.daaniikusnanta.storyapp.views.dataStore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var stories: List<ListStoryItem>
    private val mainViewModel by viewModels<MainViewModel>()
    private val sharedViewModel by viewModels<SharedViewModel> {
        SharedViewModel.Factory(
            SettingPreferences.getInstance(dataStore)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_light) as SupportMapFragment
        mapFragment.getMapAsync(this)
        sharedViewModel.apply {
            getToken().observe(this@MapsActivity) {
                mainViewModel.token = it
                if (it.isEmpty()) {
                    finish()
                } else {
                    mainViewModel.getStories(it, true)
                }
            }
//            getThemeSettings().observe(this@MapsActivity) {
//                val mapId =
//                    if (!it) getString(R.string.map_id_light)
//                    else getString(R.string.map_id_dark)
//                val mapOptions = GoogleMapOptions().mapId(mapId)
//                val mapFragment = SupportMapFragment.newInstance(mapOptions)
//                mapFragment.getMapAsync(this@MapsActivity)
//            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        mMap.setOnMarkerClickListener { marker ->
            val storyIndex = marker.tag as Int
            val moveToDetail = Intent(this, StoryDetailActivity::class.java)
            moveToDetail.putExtra(StoryDetailActivity.EXTRA_STORY, stories[storyIndex])
            startActivity(moveToDetail)
            true
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getMyLastLocation()

        mainViewModel.apply {
//            isLoading.observe(this@MainActivity) {
//                binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
//                binding.rvStories.visibility = if (it) View.GONE else View.VISIBLE
//            }

            listStories.observe(this@MapsActivity) {
                if(it.isNotEmpty()) {
                    Toast.makeText(this@MapsActivity, "Stories: ${it.size}", Toast.LENGTH_SHORT).show()
                    stories = it
                    addStoryMarker(stories)
                }
            }
//            errorMessage.observe(this@MapsActivity) {
//                if (it != null) {
//                    Snackbar.make(this@MapsActivity, getString(R.string.fetch_stories_failed), Snackbar.LENGTH_LONG)
//                        .setAction(R.string.retry.toString()) { onResume() }
//                        .show()
//                }
//            }
        }
    }

    private fun addStoryMarker(stories: List<ListStoryItem>) {
        for (story in stories) {
            if (story.lat.isNullOrBlank() || story.lon.isNullOrBlank()) {
                continue
            }
            val storyLatLng = LatLng(story.lat.toDouble(), story.lon.toDouble())
            val options = MarkerOptions()
                .position(storyLatLng)
                .title(story.name)
                .snippet(story.description)

            val storyMarker = mMap.addMarker(options)
            storyMarker?.tag = stories.indexOf(story)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    // Precise location access granted.
                    getMyLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Only approximate location access granted.
                    getMyLastLocation()
                }
                else -> {
                    // No location access granted.
                }
            }
        }
    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ){
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    showStartMarker(location)
                } else {
                    Toast.makeText(
                        this@MapsActivity,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showStartMarker(location: Location) {
        val startLocation = LatLng(location.latitude, location.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 9f))
    }
}