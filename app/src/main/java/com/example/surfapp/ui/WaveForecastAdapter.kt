package com.example.surfapp.ui

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

class WaveForecastAdapter(private val waveForecast: WaveForecastResponse) :
    RecyclerView.Adapter<WaveForecastAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_forecast_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(waveForecast.hourly[position])
    }

    override fun getItemCount(): Int {
        return waveForecast.hourly.time.size
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
            timeTextView.text = outputFormat.format(date)
            waveHeightTextView.text = hourly.waveHeight[0].toString() + " ft"
            waveDirectionTextView.text = hourly.waveDirection[0].toString() + " ft"
            wavePeriodTextView.text = hourly.wavePeriod[0].toString() + " ft"
        }
    }
}
