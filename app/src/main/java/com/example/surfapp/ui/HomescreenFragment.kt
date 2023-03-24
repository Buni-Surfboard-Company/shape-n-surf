package com.example.surfapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.surfapp.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class HomescreenFragment : Fragment(R.layout.homescreen_fragment) {

    private lateinit var contextMenuView: View
    private lateinit var contextMenu: PopupWindow
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
//        setHasOptionsMenu(true)

        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment


        mapFragment.getMapAsync { googleMap: GoogleMap ->
            // Set an OnClickListener for the marker
            googleMap.setOnMarkerClickListener { marker ->
                // Show the context menu for the marker
                Log.d("Markers location:", "Latitude: ${marker.position.latitude}, Longitude: ${marker.position.longitude}")
                // open a dialog
                showMarkerInfo(marker.position.latitude.toString(), marker.position.longitude.toString())
                // Return true to indicate that we have handled the click event
                true
            }

            // Set an OnMapClickListener to add a marker when the user clicks on the map
            googleMap.setOnMapClickListener { latLng ->
                // Create a marker at the clicked location and add it to the map
                val markerOptions = MarkerOptions().position(latLng)
                val marker = googleMap.addMarker(markerOptions)

                // Set the title of the marker to the latitude and longitude
                marker?.title = "MySurfSpot"
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

        pictureBoardViewButton.setOnClickListener{
            val directions = HomescreenFragmentDirections.navigateToUploadBoardsScreen()
            findNavController().navigate(directions)
        }
    }

    private fun showMarkerInfo(lat : String, lon: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Do you want to see the wave forecast for the selected surf spot?")
            .setCancelable(true)
            .setPositiveButton("Yes") { dialog, which ->
                // Navigate to a new activity or perform other action
                val bundle = Bundle()
                bundle.putString("lat", lat)
                bundle.putString("lon", lon)
                val directions = HomescreenFragmentDirections.navigateToForecastScreen()
                findNavController().navigate(R.id.forecast_screen, bundle)
            }
            .setNegativeButton("No") { dialog, which ->
                // Do nothing
            }
        val dialog = builder.create()
        dialog.show()
    }
}