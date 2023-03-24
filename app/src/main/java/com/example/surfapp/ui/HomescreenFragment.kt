package com.example.surfapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.surfapp.R

class HomescreenFragment : Fragment(R.layout.homescreen_fragment) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

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
}