package com.example.surfapp.data

import android.util.Log
import com.example.surfapp.api.ApiService
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher

class ForecastRepository (
    private val service: ApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
){
    private var currentLatitude: Float? = null
    private var currentLongitude: Float? = null
    private var currentStartDate: String? = null
    private var currentEndDate: String? = null
    private var cachedForecast: List<WaveForecastResponse>? = null

    suspend fun loadSurfForecast(
        latitude: Float,
        longitude: Float,
        startDate: String,
        endDate: String,
        hourly: Array<String>
    ) : Result<List<WaveForecastResponse>?> {
        /*
         * If we have a cached forecast for the same location, return the cached forecast
         * without making a network call.  Otherwise, make an API call to fetch the forecast and
         * cache it.
         */
        return if (
            latitude == currentLatitude &&
            longitude == currentLongitude &&
            startDate == currentStartDate &&
            endDate == currentEndDate &&
            cachedForecast!= null
        ) {
            Result.success(cachedForecast!!)
        } else {
            currentLatitude = latitude
            currentLongitude = longitude
            currentStartDate = startDate
            currentEndDate = endDate

            withContext(ioDispatcher) {
                try {
                    val response = service.getHourlyWaveForecasts(latitude, longitude, startDate, endDate, hourly)
                    if (response.isSuccessful) {
                        Log.d("Forecast Repo:", "response: $response")
                        cachedForecast = response.body()!!
                        Result.success(cachedForecast)
                    } else {
                        Log.d("Error:", "$response.errorBody()?.string()")
                        Result.failure(Exception(response.errorBody()?.string()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }
    }
}