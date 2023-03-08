package com.example.surfapp.data
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WaveForecastResponse(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "generationtime_ms") val generationTimeMs: Double,
    @Json(name = "utc_offset_seconds") val utcOffsetSeconds: Int,
    @Json(name = "timezone") val timezone: String,
    @Json(name = "timezone_abbreviation") val timezoneAbbreviation: String,
    @Json(name = "hourly_units") val hourlyUnits: HourlyUnits,
    @Json(name = "hourly") val hourly: Hourly
)