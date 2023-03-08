package com.example.surfapp.data
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HourlyUnits(
    @Json(name = "time") val time: String,
    @Json(name = "wave_height") val waveHeight: String,
    @Json(name = "wave_direction") val waveDirection: String,
    @Json(name = "wave_period") val wavePeriod: String
)

@JsonClass(generateAdapter = true)
data class Hourly(
    @Json(name = "time") val time: List<String>,
    @Json(name = "wave_height") val waveHeight: List<Double>,
    @Json(name = "wave_direction") val waveDirection: List<Double>,
    @Json(name = "wave_period") val wavePeriod: List<Double>
) {
    operator fun get(position: Int): Hourly {
        return Hourly(listOf(time[position]), listOf(waveHeight[position]), listOf(waveDirection[position]), listOf(wavePeriod[position]))
    }
}
