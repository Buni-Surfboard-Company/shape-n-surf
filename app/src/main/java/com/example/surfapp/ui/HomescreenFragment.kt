package com.example.surfapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.surfapp.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class HomescreenFragment : Fragment(R.layout.homescreen_fragment) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync { googleMap: GoogleMap ->
            // Set an OnClickListener for the marker
            googleMap.setOnMarkerClickListener { marker ->
                // Navigate to a new fragment
                // ...
                val position = marker.position
                Log.d("Markers location:", "Latitude: ${position.latitude}, Longitude: ${position.longitude}")

                true // return true to indicate that we have handled the click event
            }

//            googleMap.setOnMarkerClickListener { marker ->
//                registerForContextMenu(marker)
//                openContextMenu(marker)
//                true
//            }

            // Set an OnMapClickListener to add a marker when the user clicks on the map
            googleMap.setOnMapClickListener { latLng ->
                // Create a marker at the clicked location and add it to the map
                val markerOptions = MarkerOptions().position(latLng)
                val marker = googleMap.addMarker(markerOptions)

                // Save the marker information to your app's data store
                // we could use a Room database to store the latitude and longitude

                // Set the title of the marker to the latitude and longitude
                 marker?.title = "Latitude: ${latLng.latitude}, Longitude: ${latLng.longitude}"
            }

            // Enable zoom controls
            googleMap.uiSettings.isZoomControlsEnabled = true
        }

        // Disable swipe back so that the users won't be able go back to the log in page by accident
        val callback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                // do nothing
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        val surfForecastViewButton: LinearLayout = view.findViewById(R.id.surfForecastViewButton)
        val savedShapesViewButton: LinearLayout = view.findViewById(R.id.savedShapesViewButton)
        val pictureBoardViewButton: LinearLayout = view.findViewById(R.id.pictureBoardViewButton)

        surfForecastViewButton.setOnClickListener {
            val directions = HomescreenFragmentDirections.navigateToForecastScreen()
            findNavController().navigate(directions)
        }

        savedShapesViewButton.setOnClickListener {
            val directions = HomescreenFragmentDirections.navigateToSavedShapesScreen()
            findNavController().navigate(directions)
        }
    }
}