package com.example.surfapp.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.surfapp.R
import com.example.surfapp.data.Hourly
import com.example.surfapp.data.WaveForecastResponse
import java.text.SimpleDateFormat
import java.util.*

class WaveForecastAdapter() :
    RecyclerView.Adapter<WaveForecastAdapter.ViewHolder>() {

    var waveForecasts: WaveForecastResponse? = null
    fun updateForecast(forecastsObj: WaveForecastResponse?) {
        waveForecasts = forecastsObj
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_forecast_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(waveForecasts!!.hourly[position])
    }

    override fun getItemCount(): Int {
//        Log.d("Forecast adapter", "waveForecasts: $waveForecasts")
        return waveForecasts?.hourly?.time?.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.tv_time_value)
        private val waveHeightTextView: TextView = itemView.findViewById(R.id.tv_wave_height_value)
        private val waveDirectionTextView: TextView = itemView.findViewById(R.id.tv_wave_direction_value)
        private val wavePeriodTextView: TextView = itemView.findViewById(R.id.tv_wave_period_value)

        fun bind(hourly: Hourly) {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("h a", Locale.getDefault())
            val date = inputFormat.parse(hourly.time[0])
            if (hourly.waveHeight[0] != null && hourly.waveDirection[0] != null && hourly.wavePeriod[0] != null ) {
                timeTextView.text = outputFormat.format(date)
                waveHeightTextView.text = hourly.waveHeight[0].toString() + " ft"
                waveDirectionTextView.text = hourly.waveDirection[0].toString() + " °"
                wavePeriodTextView.text = hourly.wavePeriod[0].toString() + " s"
            }
        }
    }
}
