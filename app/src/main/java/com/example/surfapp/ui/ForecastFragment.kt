package com.example.surfapp.ui


import android.app.DatePickerDialog
import android.content.Context
import com.example.surfapp.api.ApiService
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surfapp.R
import com.example.surfapp.data.WaveForecastResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.fragment.app.viewModels
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.google.android.material.progressindicator.CircularProgressIndicator
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ForecastFragment : Fragment(R.layout.forecast_fragment) {

    private val forecastAdapter = WaveForecastAdapter()
    private lateinit var forecastListRV: RecyclerView

    private lateinit var loadingErrorTV: TextView
    private lateinit var loadingIndicator: CircularProgressIndicator

    private val viewModel: ForecastViewModel by viewModels()

    // Declare the UI element that will trigger the date picker
    private lateinit var button: Button

    // Declare the initial date for the date picker
    private var initialYear: Int = 0
    private var initialMonth: Int = 0
    private var initialDay: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val backButton = view.findViewById<Button>(R.id.backButton)

        // Set an OnClickListener on the back button
        backButton.setOnClickListener {
//            val directions = ForecastFragmentDirections.navigateToHomeScreen()
//            findNavController().navigate(directions)
            handleOnBackPressed()
        }

        loadingErrorTV = view.findViewById(R.id.tv_loading_error)
        loadingIndicator = view.findViewById(R.id.loading_indicator)

        /*
         * Set up RecyclerView.
         */
        forecastListRV = view.findViewById(R.id.rv_forecast_list)
        forecastListRV.layoutManager = LinearLayoutManager(requireContext())
        forecastListRV.setHasFixedSize(true)
        forecastListRV.adapter = forecastAdapter

        /*
         * Set up an observer on the current forecast data.  If the forecast is not null, display
         * it in the UI.
         */
        viewModel.forecast.observe(viewLifecycleOwner) { forecast ->
            forecastAdapter.updateForecast(forecast)
            forecastListRV.visibility = View.VISIBLE
            forecastListRV.scrollToPosition(0)
        }

        /*
         * Set up an observer on the error associated with the current API call.  If the error is
         * not null, display the error that occurred in the UI.
         */
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                val errorMessage = JSONObject((error.message).toString())
                loadingErrorTV.text = errorMessage.getString("reason")
                loadingErrorTV.visibility = View.VISIBLE
                Log.e(tag, "Error fetching forecast: ${error.message}")
            }
        }


        /*
         * Set up an observer on the loading status of the API query.  Display the correct UI
         * elements based on the current loading status.
         */
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                loadingIndicator.visibility = View.VISIBLE
                loadingErrorTV.visibility = View.INVISIBLE
                forecastListRV.visibility = View.INVISIBLE
            } else {
                loadingIndicator.visibility = View.INVISIBLE
            }
        }

        button = view.findViewById(R.id.datePicker)

        button.setOnClickListener {
            showDatePickerDialog()
        }

        // Set the initial date for the date picker (e.g. the current date)
        val calendar = Calendar.getInstance()
        initialYear = calendar.get(Calendar.YEAR)
        initialMonth = calendar.get(Calendar.MONTH)
        initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Set the initial text of the button
        updateDateButton()
    }

    override fun onResume() {
        super.onResume()
        // Get the SharedPreferences instance
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Get the current date
        val calendar = Calendar.getInstance()
        val defaultYear = calendar.get(Calendar.YEAR)
        val defaultMonth = calendar.get(Calendar.MONTH)
        val defaultDay = calendar.get(Calendar.DAY_OF_MONTH)
        val defaultLat = 44.642274.toFloat()
        val defaultLon = (-124.062642).toFloat()

        // Set default values for year, month, and day if they don't exist in SharedPreferences
        if (!sharedPreferences.contains("year")) {
            sharedPreferences.edit().putInt("year", defaultYear).apply()
        }
        if (!sharedPreferences.contains("month")) {
            sharedPreferences.edit().putInt("month", defaultMonth).apply()
        }

        if (!sharedPreferences.contains("day")) {
            sharedPreferences.edit().putInt("day", defaultDay).apply()
        }

        val lat = arguments?.getString("lat")?.toFloat()
        if (lat != null){
            sharedPreferences.edit().putFloat("lat", lat).apply()
        } else if (!sharedPreferences.contains("lat")) {
            sharedPreferences.edit().putFloat("lat", defaultLat).apply()
        }

        val lon = arguments?.getString("lon")?.toFloat()
        if (lon != null) {
            sharedPreferences.edit().putFloat("lon", lon).apply()
        } else if (!sharedPreferences.contains("lon")){
            sharedPreferences.edit().putFloat("lon", defaultLon).apply()
        }

        // Update the text of the button with the default date
        updateDateButton()
        loadForecastData()
    }

    private fun showDatePickerDialog() {
        // Get the saved date data from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val year = sharedPreferences.getInt("year", 0)
        val month = sharedPreferences.getInt("month", 0)
        val day = sharedPreferences.getInt("day", 0)

        // Create a new DatePickerDialog instance with the saved date data as the initial date
        val datePickerDialog = DatePickerDialog(
            requireContext(), // Context
            { _: DatePicker, year: Int, month: Int, day: Int ->
                // Handle selected date here
                // year, month, and day are the selected values
                val editor = sharedPreferences.edit()
                editor.putInt("year", year)
                editor.putInt("month", month)
                editor.putInt("day", day)
                editor.apply()

                // Update the text of the button
                updateDateButton()
                // Call the method to load the forecast data using the new date
                loadForecastData()
            },
            year, // Initial year
            month, // Initial month (0-11)
            day // Initial day
        )
        // Set the minimum and maximum date for the DatePickerDialog
        val minDate = Calendar.getInstance()
        minDate.set(2022, 6, 29) // 2022-07-29
        datePickerDialog.datePicker.minDate = minDate.timeInMillis

        val maxDate = Calendar.getInstance()
        maxDate.add(Calendar.DATE,+6) // 2023-04-03
        datePickerDialog.datePicker.maxDate = maxDate.timeInMillis

        // Show the date picker dialog
        datePickerDialog.show()

        // Show the date picker dialog
        datePickerDialog.show()
    }

    private fun updateDateButton() {
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val year = sharedPreferences.getInt("year", 0)
        val month = sharedPreferences.getInt("month", 0)
        val day = sharedPreferences.getInt("day", 0)

        // Format the date as a string (e.g. "March 24, 2023")
        val dateString = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(year - 1900, month, day))

        // Update the text of the button
        button.text = dateString
    }
    private fun loadForecastData() {
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val year = sharedPreferences.getInt("year", 0)
        val month = sharedPreferences.getInt("month", 0)
        val day = sharedPreferences.getInt("day", 0)

        val lat = arguments?.getString("lat")?.toFloat() ?: sharedPreferences.getFloat("lat", 0f)
        val lon = arguments?.getString("lon")?.toFloat() ?: sharedPreferences.getFloat("lon", 0f)

        // Format the date as a string (e.g. "2023-03-21")
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(year - 1900, month, day))

        viewModel.loadForecast(lat, lon, dateString, dateString, arrayOf("wave_period", "wave_height", "wave_direction"))
    }
    private fun handleOnBackPressed() {
        requireActivity().onBackPressed()
    }
}
