package com.example.surfapp.ui


import com.example.surfapp.api.ApiService
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.text.SimpleDateFormat
import java.util.*

class ForecastFragment : Fragment(R.layout.forecast_fragment) {

    private val forecastAdapter = WaveForecastAdapter()
    private lateinit var forecastListRV: RecyclerView

    private lateinit var loadingErrorTV: TextView
    private lateinit var loadingIndicator: CircularProgressIndicator

    private val viewModel: ForecastViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                loadingErrorTV.text = (error.message).toString()
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
    }

    override fun onResume() {
        super.onResume()

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModel.loadForecast(44.64.toFloat(), (-124.05).toFloat(), currentDate, currentDate, arrayOf("wave_period", "wave_height", "wave_direction"))
    }


//    private fun doWaveForecastApiCall(latitude: Float, longitude: Float, startDate: String, endDate: String) {
//        ApiService.create().getHourlyWaveForecasts(latitude, longitude, arrayOf("wave_period", "wave_height", "wave_direction"), startDate, endDate)
//            .enqueue(object : Callback<WaveForecastResponse> {
//                override fun onResponse(call: Call<WaveForecastResponse>, response: Response<WaveForecastResponse>) {
//                    Log.d("MainActivity", "Status code: ${response.code()}")
//                    if (response.isSuccessful) {
//                        val waveForecast = response.body() ?: return
//                        forecastAdapter = WaveForecastAdapter(waveForecast)
//                        forecastListRV.adapter = forecastAdapter
//                        Log.d("MainActivity", "Response body: ${response.body()}")
//                    } else {
//                        Log.d("MainActivity", "Error: ${response.errorBody()?.string()}")
//                    }
//                    Log.d("MainActivity", "Response body: ${response.body()}")
//                }
//
//                override fun onFailure(call: Call<WaveForecastResponse>, t: Throwable) {
//                    // Handle failure
//                    Log.d("MainActivity", "Error making API call: ${t.message}")
//                }
//            })
//    }
}
