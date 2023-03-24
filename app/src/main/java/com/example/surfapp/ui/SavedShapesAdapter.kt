package com.example.surfapp.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.MODE_READ_ONLY
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.surfapp.R
import com.itextpdf.text.pdf.PdfReader
import java.io.File


class SavedShapesAdapter(private val onClick: (File) -> Unit):RecyclerView.Adapter<SavedShapesAdapter.ViewHolder>() {
    var boardFiles: MutableList<File> = mutableListOf()

    override fun getItemCount() = this.boardFiles.size

    fun updateBoards(files: List<File>) {
        boardFiles = files.toMutableList() ?: mutableListOf()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_board_item, parent, false)
        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(this.boardFiles[position])
        holder.deleteButton.setOnClickListener {
            Log.d("DELETE", "delete")
            holder.currentFile.delete()
            boardFiles.removeAt(position)
            notifyItemRemoved(position)
        }
    }



    class ViewHolder(itemView: View, val onClick: (File) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val pictureIV: ImageView = itemView.findViewById(R.id.iv_board_picture)
        val deleteButton: Button = itemView.findViewById<Button>(R.id.delete_item)
        private val nameTV: TextView = itemView.findViewById(R.id.tv_board_name)
        lateinit var currentFile: File
        init {
            itemView.setOnClickListener{
                currentFile.let(onClick)
            }
        }
        fun bind(file: File) {
            currentFile = file
            nameTV.text = file.name.replace(".pdf", "")
//https://stackoverflow.com/questions/10698360/how-to-convert-a-pdf-page-to-an-image-in-android
            val fileDescriptor: ParcelFileDescriptor =
                ParcelFileDescriptor.open(File(file.path), MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)
            val page: PdfRenderer.Page = renderer.openPage(0)
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