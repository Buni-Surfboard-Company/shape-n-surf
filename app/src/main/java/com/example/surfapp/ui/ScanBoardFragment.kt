package com.example.surfapp.ui

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.surfapp.R
import com.example.surfapp.ml.Model
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.CvType.CV_8UC3
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit
import kotlin.math.ceil


class ScanBoardFragment : Fragment(R.layout.upload_boards_fragment) {

    var pickedPhoto: Uri? = null
    var pickedBitMap: Bitmap? = null

    private val PAPER_HEIGHT = 8
    private val PPI = 96
    private val TAG = "ScanBoardActivity"

    private lateinit var globalView: View

    fun resizeImage(image: Mat): Mat {
        val height = image.height()
        val width = image.width()
        val newHeight = (PAPER_HEIGHT * PPI).toDouble()
        val newWidth = ((newHeight / height) * width)
        val resizedImage = Mat()
        Imgproc.resize(image, resizedImage, Size(newWidth, newHeight))
        return resizedImage
    }

    fun createPdf(images: List<Mat>, fileName: String) {
        val document = Document()
        //TODO if name is taken

        PdfWriter.getInstance(document, FileOutputStream(File(requireContext().filesDir,
            "$fileName.pdf"
        )))
        document.open()

        for (image in images) {
            val stream = ByteArrayOutputStream()
            val width: Int = image.rows()
            val height: Int = image.cols()
            val bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(image, bitmap)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            val pdfImg = Image.getInstance(byteArray) //TODO try catch
            document.add(pdfImg)
        }


        document.close()

    }

    fun saveBoard(images: List<Mat>) {
        val builder = AlertDialog.Builder(context)

        builder.setTitle("Save As")
        builder.setMessage("Please enter a file name:")

        val input = EditText(context)
        input.hint = "File name"
        builder.setView(input)

        builder.setPositiveButton("Save") { dialog, which ->
            val fileName = input.text.toString()
            dialog.dismiss()
            createPdf(images, fileName)
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }
        builder.show()
    }

    fun chooseSize(bitmap: Bitmap): Int{
        var input = 0
        val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            if (source.toString().matches(Regex("\\d+"))) {
                null // accept the input
            } else {
                "" // reject the input
            }
        }

        val editText = EditText(context)
        editText.filters = arrayOf(inputFilter)
        editText.inputType = InputType.TYPE_CLASS_NUMBER

        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setView(editText)
        alertDialogBuilder.setTitle("Enter Height")
        alertDialogBuilder.setMessage("Please enter height in inches:")
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            input = editText.text.toString().toInt()
            dialog.dismiss()
            processImage(bitmap, input)
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        alertDialogBuilder.show()

        return input
    }

    private fun matToBitmapLocal(mat: Mat): Bitmap{
        val lastwidth: Int = mat.rows()
        val lastheight: Int = mat.cols()
        val lastbitmap = Bitmap.createBitmap(lastwidth, lastheight, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, lastbitmap)
        return lastbitmap
    }

    private fun isBoard(bitmap: Bitmap): Float{
        val model = Model.newInstance(requireContext())

        var bmp: Bitmap = bitmap!!.copy(Bitmap.Config.ARGB_8888, true)
        //  https://stackoverflow.com/questions/72478531/nan-in-tflite-model
        val resizeBitmap: Bitmap = Bitmap.createScaledBitmap(bmp, 180, 180, true)

        val bytebuffer = ByteBuffer.allocateDirect(4*180*180*3)
        bytebuffer.order(ByteOrder.nativeOrder())
        val intValues  = IntArray(180*180)
        bmp.getPixels(intValues,0,resizeBitmap.width,0,0,resizeBitmap.width,resizeBitmap.height)
        var pixel = 0
        for( i in 0 .. 179){
            for(j in 0..179){
                val tmpVal = intValues[pixel++]
                bytebuffer.putFloat(((tmpVal shr 16) and  0xFF)*(1.0f/1))
                bytebuffer.putFloat(((tmpVal shr 8) and  0xFF)*(1.0f/1))
                bytebuffer.putFloat((tmpVal and  0xFF)*(1.0f/1))
            }
        }
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 180, 180, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(bytebuffer)
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        val tab = outputFeature0.floatArray
        model.close()

        return tab[0]
    }

    private fun showBoard(mats: List<Mat>){
        val tempImageView: ImageView = requireView().findViewById(R.id.tempImage)

        for (mat in mats){
            val bm = matToBitmapLocal(mat)
            tempImageView.postDelayed({
                tempImageView.setImageBitmap(bm)
            }, 3000)
        }
    }

    private fun processImage(bitmap: Bitmap, scaleSize: Int): Boolean{
        val tempImageView: ImageView = globalView.findViewById(R.id.tempImage)
        val img = Mat()


        Utils.bitmapToMat(bitmap, img)
//        showBoard(img)
        Log.d(TAG, "bitmapToMat")
        val gray = Mat()
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY)

        val blur = Mat()
        Imgproc.GaussianBlur(gray, blur, Size(5.0, 5.0), 0.0)

        // apply Canny edge detection
        val edges = Mat()
        Imgproc.Canny(blur, edges, 50.0, 150.0)

//        showBoard(edges)

        // perform morphological operations to remove noise
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(3.0, 3.0))
        val dilated = Mat()
        Imgproc.dilate(edges, dilated, kernel)
        val eroded = Mat()
        Imgproc.erode(dilated, eroded, kernel)

//        showBoard(eroded)

        // find contours in the edges
        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(
            eroded,
            contours,
            Mat(),
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        // find contour with largest area
        var maxArea = 0.0
        var bestContour: MatOfPoint? = null
        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area > maxArea) {
                maxArea = area
                bestContour = contour
            }
        }

        // create blank mask image and fill in best contour with white
        val mask = Mat.zeros(gray.size(), CvType.CV_8UC1)
        val white = Scalar(255.0)
        Imgproc.drawContours(mask, listOf(bestContour), 0, white, -1)

        val halfMask = mask.clone()
        halfMask.colRange(mask.cols() / 2, mask.cols())!!.setTo(Scalar(0.0))

        // find contours in the half mask
        val halfContours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(
            halfMask,
            halfContours,
            Mat(),
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        // find best contour in the half mask
        bestContour = halfContours.maxByOrNull { Imgproc.contourArea(it) }

        // create a new blank mask for the halved outline
        val halvedMask = Mat.zeros(halfMask.size(), CvType.CV_8UC1)

        // draw the halved outline in white
        Imgproc.drawContours(halvedMask, listOf(bestContour), 0, white, 4)


        val whiteImg =
            Mat(halvedMask.rows(), halfMask.cols(), CV_8UC3, Scalar(255.0, 255.0, 255.0));

        Core.subtract(whiteImg, Scalar(255.0), halvedMask)

        Imgproc.drawContours(whiteImg, listOf(bestContour), 0, Scalar(0.0), 4)

        /********************************/

        val rect = Imgproc.boundingRect(bestContour)

        val finalMask = Mat.zeros(whiteImg.size(), CvType.CV_8UC1)

        Imgproc.rectangle(
            finalMask,
            Point(rect.x.toDouble(), rect.y.toDouble()),
            Point((rect.x + rect.width).toDouble(), (rect.y + rect.height).toDouble()),
            white,
            -1
        )

        Core.bitwise_and(whiteImg, whiteImg, finalMask)

        // Crop the image to the bounding box
        val displayImg = finalMask
        val finalImg = Mat(finalMask, Rect(rect.x, rect.y, rect.width, rect.height))

        val width: Int = finalImg.rows()
        val height: Int = finalImg.cols()
        val bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(finalImg, bitmap)
        tempImageView.setImageBitmap(bitmap)

        val boardHeight = scaleSize

        val numSections = ceil(boardHeight.toDouble() / PAPER_HEIGHT).toInt()
        val sectionHeight = bitmap.height / numSections
        val croppedImages = mutableListOf<Bitmap>()

        for (i in 0 until numSections) {
            val startY = i * sectionHeight
            val endY = (i + 1) * sectionHeight
            val croppedImage =
                Bitmap.createBitmap(bitmap, 0, startY, bitmap.width, endY - startY)
            croppedImages.add(croppedImage)
        }

        val images = mutableListOf<Mat>()
        for (i in 0 until numSections) {
            val image = Mat()
            Utils.bitmapToMat(croppedImages[i], image)
            val resizedImage = resizeImage(image)
            images.add(resizedImage)
        }
        val lastwidth: Int = img.rows()
        val lastheight: Int = img.cols()
        val lastbitmap = Bitmap.createBitmap(lastwidth, lastheight, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(img, lastbitmap)
        images.add(0, resizeImage(img))
        tempImageView.setImageBitmap(lastbitmap)
        saveBoard(images)

        val matList = mutableListOf<Mat>()
        matList.add(img)
        matList.add(edges)
        matList.add(eroded)
        matList.add(img)
        showBoard(matList)

        return true
    }

    private fun funTime(bitmap: Bitmap){

        val prob = isBoard(bitmap)
        Log.d(TAG, prob.toString())
        if (prob < 1.5){
            val welcomeTV = requireView().findViewById<TextView>(R.id.welcome)
            val infoTV = requireView().findViewById<TextView>(R.id.info)
            welcomeTV.visibility = View.GONE
            infoTV.visibility = View.GONE
            chooseSize(bitmap)
        }
        else {
            val toast = Toast.makeText(requireContext(), "Not a Board, fool!", Toast.LENGTH_LONG)
            toast.show()
        }
    }

    private val startGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "startGalleryLauncher")
            if (result.resultCode == RESULT_OK && result.data != null) {
                Log.d(TAG, result.resultCode.toString())
                pickedPhoto = result.data?.data
                val source = ImageDecoder.createSource(requireContext().contentResolver, pickedPhoto!!)
                val bitmap = ImageDecoder.decodeBitmap(source){ decoder, _, _ ->
                    decoder.isMutableRequired = true
                }
                val resizeBitmap: Bitmap = Bitmap.createScaledBitmap(bitmap, 180, 180, true)
                if (pickedPhoto != null ) funTime(resizeBitmap!!)
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalView = view
        val uploadPhotoButton: Button = view.findViewById(R.id.uploadButton)
        uploadPhotoButton.setOnClickListener {
            pickImage()
        }

        val takePictureButton: Button = view.findViewById(R.id.cameraButton)
        takePictureButton.setOnClickListener {
            takePicture()
        }

        val backButton = view.findViewById<Button>(R.id.backButton)

        // Set an OnClickListener on the back button
        backButton.setOnClickListener {
            val directions = ForecastFragmentDirections.navigateToHomeScreen()
            findNavController().navigate(directions)
        }
    }

    private fun pickImage() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        startGalleryLauncher.launch(galleryIntent)
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result?.data != null) {
                    var bitmap = result.data?.extras?.get("data") as Bitmap
                    val resizeBitmap: Bitmap = Bitmap.createScaledBitmap(bitmap, 180, 180, true)
                    funTime(resizeBitmap)
                }
            }
        }

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultLauncher.launch(takePictureIntent)
    }
}