package com.example.surfapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.surfapp.R
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SavedShapesFragment : Fragment(R.layout.saved_shapes_fragment) {
    private lateinit var shapesAdapter: WaveForecastAdapter
    private lateinit var shapesListRV: RecyclerView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)  {
        super.onViewCreated(view, savedInstanceState)

        shapesListRV = view.findViewById(R.id.rv_shapes_list)
        shapesListRV.layoutManager = LinearLayoutManager(requireContext())
        shapesListRV.setHasFixedSize(true)
    }
}