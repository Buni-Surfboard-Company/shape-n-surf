package com.example.surfapp.api

import com.example.surfapp.data.WaveForecastResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/v1/marine")
    fun getHourlyWaveForecasts(
        @Query("latitude") latitude: Float,
        @Query("longitude") longitude: Float,
        @Query("hourly") hourly: Array<String>,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Call<WaveForecastResponse>

    companion object {
        private const val BASE_URL = "https://marine-api.open-meteo.com/"
        fun create() : ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
