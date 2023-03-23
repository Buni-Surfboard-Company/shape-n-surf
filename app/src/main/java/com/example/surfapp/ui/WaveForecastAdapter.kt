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
        Log.d("Forecast adapter", "waveForecasts: $waveForecasts")
        if (waveForecasts != null) {
            holder.bind(waveForecasts!!.hourly)
        }
    }

    override fun getItemCount(): Int {
        Log.d("Forecast adapter", "waveForecasts: $waveForecasts")
        return waveForecasts?.hourly?.time?.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.tv_time_value)
        private val waveHeightTextView: TextView = itemView.findViewById(R.id.tv_wave_height_value)
        private val waveDirectionTextView: TextView = itemView.findViewById(R.id.tv_wave_direction_value)
        private val wavePeriodTextView: TextView = itemView.findViewById(R.id.tv_wave_period_value)

        fun bind(hourly: Hourly) {
            val ctx = itemView.context
            timeTextView.text = hourly.time[0]
            waveHeightTextView.text = hourly.waveHeight[0].toString()
            waveDirectionTextView.text = hourly.waveDirection[0].toString()
            wavePeriodTextView.text = hourly.wavePeriod[0].toString()
        }
    }
}
