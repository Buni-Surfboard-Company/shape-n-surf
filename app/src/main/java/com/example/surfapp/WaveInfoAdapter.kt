package com.example.surfapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class WaveInfoAdapter : Adapter<WaveInfoAdapter.WaveInfoViewHolder>() {
    val waveInfos: MutableList<WaveInfo> = mutableListOf()

    override fun getItemCount() = waveInfos.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WaveInfoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.wave_list_item, parent, false)
        return WaveInfoViewHolder(view)
    }

    override fun onBindViewHolder(holder: WaveInfoAdapter.WaveInfoViewHolder, position: Int) {
        holder.bind(waveInfos[position])
    }

    fun addWaveInfo(wave: WaveInfo) {
        waveInfos.add(0, wave)
        notifyItemInserted(0)
    }

    class WaveInfoViewHolder(view: View): ViewHolder(view) {
        private val waveInfoTV: TextView = view.findViewById(R.id.tv_wave_text)

        fun bind(waveInfo: WaveInfo) {
            waveInfoTV.text = waveInfo.text
        }
    }
}