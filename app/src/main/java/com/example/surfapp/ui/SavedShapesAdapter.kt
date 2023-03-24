package com.example.surfapp.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.MODE_READ_ONLY
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.surfapp.R
import com.itextpdf.text.pdf.PdfReader
import java.io.File


class SavedShapesAdapter():RecyclerView.Adapter<SavedShapesAdapter.ViewHolder>() {
    var boardFiles: List<File> = listOf()

    override fun getItemCount() = this.boardFiles.size

    fun updateBoards(files: List<File>) {
        boardFiles = files ?: listOf()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_board_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(this.boardFiles[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pictureIV: ImageView = itemView.findViewById(R.id.iv_board_picture)
        private val nameTV: TextView = itemView.findViewById(R.id.tv_board_name)

        fun bind(file: File) {
            nameTV.text = file.name
//https://stackoverflow.com/questions/10698360/how-to-convert-a-pdf-page-to-an-image-in-android
            val fileDescriptor: ParcelFileDescriptor =
                ParcelFileDescriptor.open(File(file.path), MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)
            val page: PdfRenderer.Page = renderer.openPage(1)
            val bitmap =
                Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(bitmap, (0).toFloat(), (0).toFloat(), null)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            pictureIV.setImageBitmap(bitmap)
        }
    }
}