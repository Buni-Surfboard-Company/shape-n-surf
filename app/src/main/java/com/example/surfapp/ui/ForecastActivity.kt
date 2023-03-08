package com.example.surfapp.ui


import com.example.surfapp.ui.WaveForecastAdapter
import com.example.surfapp.api.ApiService
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surfapp.R
import com.example.surfapp.data.WaveForecastResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForecastActivity : AppCompatActivity() {

    private lateinit var forecastAdapter: WaveForecastAdapter
    private lateinit var forecastListRV: RecyclerView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)
        forecastListRV = findViewById(R.id.recycler_view)
        forecastListRV.layoutManager = LinearLayoutManager(this)
        forecastListRV.setHasFixedSize(true)
        doWaveForecastApiCall(44.64.toFloat(), (-124.05).toFloat(), "2023-03-07", "2023-03-07")

    }

    private fun doWaveForecastApiCall(latitude: Float, longitude: Float, startDate: String, endDate: String) {
        ApiService.create().getHourlyWaveForecasts(latitude, longitude, arrayOf("wave_period", "wave_height", "wave_direction"), startDate, endDate)
            .enqueue(object : Callback<WaveForecastResponse> {
                override fun onResponse(call: Call<WaveForecastResponse>, response: Response<WaveForecastResponse>) {
                    Log.d("MainActivity", "Status code: ${response.code()}")
                    if (response.isSuccessful) {
                        val waveForecast = response.body() ?: return
                        forecastAdapter = WaveForecastAdapter(waveForecast)
                        forecastListRV.adapter = forecastAdapter
                        Log.d("MainActivity", "Response body: ${response.body()}")
                    } else {
                        Log.d("MainActivity", "Error: ${response.errorBody()?.string()}")
                    }
                    Log.d("MainActivity", "Response body: ${response.body()}")
                }

                override fun onFailure(call: Call<WaveForecastResponse>, t: Throwable) {
                    // Handle failure
                    Log.d("MainActivity", "Error making API call: ${t.message}")
                }
            })
    }
}
