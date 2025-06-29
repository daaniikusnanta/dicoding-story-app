package com.daaniikusnanta.storyapp.views.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.TextView
import com.daaniikusnanta.storyapp.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MapsInfoWindowAdapter(context: Context) : GoogleMap.InfoWindowAdapter {

    @SuppressLint("InflateParams")
    private var mWindow: View = (context as Activity).layoutInflater.inflate(R.layout.maps_info_window, null)

    override fun getInfoContents(marker: Marker): View {
        setWindowData(marker)
        return mWindow
    }

    override fun getInfoWindow(marker: Marker): View {
        setWindowData(marker)
        return mWindow
    }

    private fun setWindowData(marker: Marker) {
        val tvUser = mWindow.findViewById<TextView>(R.id.tv_username)
        val tvTime = mWindow.findViewById<TextView>(R.id.tv_time)

        tvUser.text = marker.title
        tvTime.text = marker.snippet
    }
}