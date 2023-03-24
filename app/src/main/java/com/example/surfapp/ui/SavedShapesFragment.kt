package com.example.surfapp.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surfapp.R
import java.io.File


class SavedShapesFragment : Fragment(R.layout.saved_shapes_fragment) {
    private lateinit var shapesListRV: RecyclerView
    private val savedShapesAdapter = SavedShapesAdapter(::onShapeClicked)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)  {
        super.onViewCreated(view, savedInstanceState)
        val backButton = view.findViewById<Button>(R.id.backButton)

        // Set an OnClickListener on the back button
        backButton.setOnClickListener {
            val directions = ForecastFragmentDirections.navigateToHomeScreen()
            findNavController().navigate(directions)
        }
        shapesListRV = view.findViewById(R.id.rv_shapes_list)
        shapesListRV.layoutManager = LinearLayoutManager(requireContext())
        shapesListRV.setHasFixedSize(true)
        shapesListRV.adapter = savedShapesAdapter
        var filePath = requireContext().filesDir.path

        var directory = File(filePath)
        var files = directory.listFiles().toList().filter { it.extension == "pdf" }

        if (files != null) for (i in files.indices) {
//            Log.e("FILE:", path + "/" + list[i])
        }

        savedShapesAdapter.updateBoards(files)
    }

    private fun onShapeClicked(file: File){
        val uriPdfPath = FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".provider",
            file
        )

       // https://trendoceans.com/how-to-open-pdf-programmatically-using-intent-in-android/
        // Start Intent to View PDF from the Installed Applications.
        val pdfOpenIntent = Intent(Intent.ACTION_VIEW)
        pdfOpenIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        pdfOpenIntent.clipData = ClipData.newRawUri("", uriPdfPath)
        pdfOpenIntent.setDataAndType(uriPdfPath, "application/pdf")
        pdfOpenIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        try {
            startActivity(pdfOpenIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "There is no app to load corresponding PDF", Toast.LENGTH_LONG)
                .show()
        }
    }
}