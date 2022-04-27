package com.daaniikusnanta.storyapp.views.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.daaniikusnanta.storyapp.R
import com.daaniikusnanta.storyapp.api.ListStoryItem
import com.daaniikusnanta.storyapp.data.SettingPreferences
import com.daaniikusnanta.storyapp.databinding.ActivityMapsBinding
import com.daaniikusnanta.storyapp.misc.getElapsedTimeString
import com.daaniikusnanta.storyapp.views.SharedViewModel
import com.daaniikusnanta.storyapp.views.dataStore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mapStyleResources: Int = R.raw.map_style_light
    private val mainViewModel by viewModels<MainViewModel> {
        MainViewModel.ViewModelFactory(this)
    }
    private val sharedViewModel by viewModels<SharedViewModel> {
        SharedViewModel.Factory(
            SettingPreferences.getInstance(dataStore)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_light) as SupportMapFragment
        mapFragment.getMapAsync(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getStoryData()

        sharedViewModel.apply {
            getThemeSettings().observe(this@MapsActivity) {
                mapStyleResources =
                    if (!it) R.raw.map_style_light
                    else R.raw.map_style_dark
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        mMap.setOnInfoWindowClickListener {marker ->
            val story = marker.tag as ListStoryItem
            val moveToDetail = Intent(this, StoryDetailActivity::class.java)
            moveToDetail.putExtra(StoryDetailActivity.EXTRA_STORY, story)
            startActivity(moveToDetail)
        }

        mMap.setInfoWindowAdapter(MapsInfoWindowAdapter(this))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getMyLastLocation()
        setMapStyle()

        mainViewModel.apply {
            isLoading.observe(this@MapsActivity) {
                binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
            }

            listStories.observe(this@MapsActivity) {
                if(it.isNotEmpty()) {
                    Toast.makeText(this@MapsActivity, "Stories: ${it.size}", Toast.LENGTH_SHORT).show()
                    addStoryMarker(it)
                }
            }
            errorMessage.observe(this@MapsActivity) {
                if (it != null) {
                    Snackbar.make(binding.root, getString(R.string.fetch_stories_failed), Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry.toString()) { getStoryData() }
                        .show()
                }
            }
        }
    }

    private fun getStoryData() {
        sharedViewModel.getToken().observe(this@MapsActivity) {
            mainViewModel.token = it
            if (it.isEmpty()) {
                finish()
            } else {
                mainViewModel.getStoriesWithLocation(it)
            }
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, mapStyleResources))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.maps_top_app_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.menu_settings -> {
                val moveToSetting = Intent(this, SettingsActivity::class.java)
                startActivity(moveToSetting)
                true
            }
            R.id.menu_refresh -> {
                getStoryData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addStoryMarker(stories: List<ListStoryItem>) {
        for (story in stories) {
            if (story.lat.isNullOrBlank() || story.lon.isNullOrBlank()) {
                continue
            }

            val storyLatLng = LatLng(story.lat.toDouble(), story.lon.toDouble())
            Glide.with(this@MapsActivity)
                .asBitmap()
                .load(story.photoUrl)
                .apply(RequestOptions.overrideOf(100,100))
                .circleCrop()
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val options = MarkerOptions()
                            .position(storyLatLng)
                            .title(story.name)
                            .snippet(getElapsedTimeString(story.createdAt, this@MapsActivity))
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromBitmap(resource))

                        val storyMarker = mMap.addMarker(options)
                        storyMarker?.tag = story
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
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

    @SuppressLint("MissingPermission")
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

    companion object {
        private const val TAG = "MapsActivity"
    }
}