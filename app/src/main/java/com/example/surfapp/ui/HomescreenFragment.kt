package com.example.surfapp.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.surfapp.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.example.surfapp.data.StoredData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds

class HomescreenFragment : Fragment(R.layout.homescreen_fragment) {

    private lateinit var contextMenuView: View
    private lateinit var contextMenu: PopupWindow
    private val dbViewModel : SavedCoordinatesViewModel by viewModels()
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
//        setHasOptionsMenu(true)

        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment


        mapFragment.getMapAsync { googleMap: GoogleMap ->
            // Retrieve the list of saved coordinates from the SavedCoordinatesViewModel
            dbViewModel.savedCoordinates.observe(viewLifecycleOwner) { savedCoordinates ->
                googleMap.clear()
                // Loop through the list of saved coordinates to create and add markers to the map
                savedCoordinates.forEach { storedData ->
                    val latLng = storedData.coordinate.split(",") // Split the latLngString to get the latitude and longitude
                    val latitude = latLng[0].toDouble()
                    val longitude = latLng[1].toDouble()
                    val markerOptions = MarkerOptions().position(LatLng(latitude, longitude)).title("MySurfSpot")
                    val marker = googleMap.addMarker(markerOptions)
                    marker?.tag = storedData.timeStamp // Set the id of the StoredData object as the unique tag of the marker
                }
                dbViewModel.lastSavedCoordinate.observe(viewLifecycleOwner) { lastSavedCoordinate ->
                    if (lastSavedCoordinate != null) {
                        val latLng = lastSavedCoordinate.coordinate.split(",")
                        val latitude = latLng[0].toDouble()
                        val longitude = latLng[1].toDouble()
                        val lastSavedSurfSpot = LatLng(latitude, longitude)

                        // Use a LatLngBounds.Builder to create a bounding box that includes all the saved surf spots on the map
                        val boundsBuilder = LatLngBounds.builder()
                        savedCoordinates.forEach { storedData ->
                            val latLng = storedData.coordinate.split(",")
                            val latitude = latLng[0].toDouble()
                            val longitude = latLng[1].toDouble()
                            boundsBuilder.include(LatLng(latitude, longitude))
                        }
                        boundsBuilder.include(lastSavedSurfSpot)

                        // Zoom the map to the bounding box that includes all the saved surf spots on the map
                        val bounds = boundsBuilder.build()
                        val padding = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            50f,
                            resources.displayMetrics
                        ).toInt() // Add 50dp of padding around the bounding box
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                    }
                }
            }

            // Set an OnClickListener for the marker
            googleMap.setOnMarkerClickListener { marker ->
                // Show the context menu for the marker
                Log.d("Markers location:", "Latitude: ${marker.position.latitude}, Longitude: ${marker.position.longitude}")
                // open a dialog
                showMarkerDialog(marker)
                // Return true to indicate that we have handled the click event
                true
            }

            // Set an OnMapClickListener to add a marker when the user clicks on the map
            googleMap.setOnMapClickListener { latLng ->
                // Create a marker at the clicked location and add it to the map
                val markerOptions = MarkerOptions().position(latLng)
                val marker = googleMap.addMarker(markerOptions)

                // Get the latitude and longitude of the clicked location
                val latitude = latLng.latitude
                val longitude = latLng.longitude

                // Combine the latitude and longitude into a string
                val latLngString = "$latitude,$longitude"

                dbViewModel.addSavedSurfSpot(StoredData(latLngString, System.currentTimeMillis()))

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
            dbViewModel.lastSavedCoordinate.observe(viewLifecycleOwner) { lastSavedCoordinate ->
                if (lastSavedCoordinate != null) {
                    // open the last saved location
                    val bundle = Bundle()
                    val latLng = lastSavedCoordinate.coordinate.split(",") // Split the latLngString to get the latitude and longitude
                    bundle.putString("lat", latLng[0])
                    bundle.putString("lon", latLng[1])
                    findNavController().navigate(R.id.forecast_screen, bundle)
                } else {
                    // open default, Newport
                    val directions = HomescreenFragmentDirections.navigateToForecastScreen()
                    findNavController().navigate(directions)
                }
            }
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

    private fun showMarkerDialog(marker: Marker) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Please select an action for the selected surf spot:")
            .setCancelable(true)
            .setPositiveButton("Delete saved spot") { dialog, which ->
                Log.d("showMarkerDialog", "attempting to delete ${marker.position.latitude},${marker.position.longitude}")
                //delete mark from the db
                dbViewModel.deleteSavedSurfSpot("${marker.position.latitude},${marker.position.longitude}")
                //delete mark from the map
//                marker.remove()
            }
            .setNeutralButton("View wave forecast") { dialog, which ->
                // Navigate to a new activity or perform other action
                val bundle = Bundle()
                bundle.putString("lat", marker.position.latitude.toString())
                bundle.putString("lon", marker.position.longitude.toString())
                findNavController().navigate(R.id.forecast_screen, bundle)
            }
        val dialog = builder.create()
        dialog.setOnShowListener {
            val btnNeutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            val btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val layoutParams = btnNeutral.layoutParams as LinearLayout.LayoutParams

            layoutParams.gravity = Gravity.CENTER
            btnNeutral.layoutParams = layoutParams
            btnPositive.layoutParams = layoutParams
            dialog.getButton(DialogInterface.BUTTON_NEUTRAL)?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        }
        dialog.show()
    }
}