package com.example.surfapp.ui

import android.content.res.AssetManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surfapp.R
import java.io.File

class SavedShapesFragment : Fragment(R.layout.saved_shapes_fragment) {
    private lateinit var shapesListRV: RecyclerView
    private val savedShapesAdapter = SavedShapesAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)  {
        super.onViewCreated(view, savedInstanceState)

        shapesListRV = view.findViewById(R.id.rv_shapes_list)
        shapesListRV.layoutManager = LinearLayoutManager(requireContext())
        shapesListRV.setHasFixedSize(true)
        shapesListRV.adapter = savedShapesAdapter
        var filePath = requireContext().filesDir.path

        var directory = File(filePath)
        var files = directory.listFiles().toList()

        if (files != null) for (i in files.indices) {
//            Log.e("FILE:", path + "/" + list[i])
        }

        savedShapesAdapter.updateBoards(files)
    }
}