package com.example.surfapp.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.surfapp.R
import com.example.surfapp.ml.Model
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfDocument
import com.itextpdf.text.pdf.PdfWriter
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.CvType.CV_8UC3
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil


class ScanBoardActivity : AppCompatActivity() {

    var pickedPhoto: Uri? = null
    var pickedBitMap: Bitmap? = null
    private val PAPER_HEIGHT = 8
    private val PPI = 96
    private val TAG = "ScanBoardActivity"

    fun resizeImage(image: Mat): Mat {
        val height = image.height()
        val width = image.width()
        val newHeight = (PAPER_HEIGHT * PPI).toDouble()
        val newWidth = ((newHeight / height) * width)
        val resizedImage = Mat()
        Imgproc.resize(image, resizedImage, Size(newWidth, newHeight))
        return resizedImage
    }

    fun createPdf(images: List<Mat>) {
        val document = Document()
        val fileName = "board.pdf"
        PdfWriter.getInstance(document, FileOutputStream(File(filesDir, fileName)))
        document.open()

        for (image in images){
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

    private val startGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d(TAG, "startGalleryLauncher")
        if (result.resultCode == RESULT_OK && result.data != null) {
            Log.d(TAG, result.resultCode.toString())
            pickedPhoto = result.data?.data
            //TODO: maybe check build version
            val source = ImageDecoder.createSource(this.contentResolver, pickedPhoto!!)
            pickedBitMap = ImageDecoder.decodeBitmap(source)
            val tempImageView: ImageView = findViewById(R.id.tempImage)
            tempImageView.setImageBitmap(pickedBitMap)

            val model = Model.newInstance(this)

            var bmp: Bitmap = pickedBitMap!!.copy(Bitmap.Config.ARGB_8888,true)
            bmp = Bitmap.createScaledBitmap(bmp, 360, 360, true)// 180*180*3 2160 180 720*540*3

            Log.d(TAG, "fromBitmap")
            val tfImage = TensorImage.fromBitmap(bmp)
            Log.d(TAG, "process")
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 180, 180, 3), DataType.FLOAT32) //180*180*4
            Log.d("shape", tfImage.buffer.toString())
            Log.d("shape", inputFeature0.buffer.toString())
            inputFeature0.loadBuffer(tfImage.buffer)
            Log.d(TAG, "process")

            val outputs = model.process(inputFeature0)

            Log.d(TAG, "outputs")
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray[0]
            model.close()

            val img = Mat()

            val newSrc = ImageDecoder.createSource(this.contentResolver, pickedPhoto!!)
            val freshBm = ImageDecoder.decodeBitmap(newSrc) {decoder, _, _ ->
                decoder.isMutableRequired = true
            }

            Utils.bitmapToMat(freshBm, img)
            Log.d(TAG, "bitmapToMat")
            val gray = Mat()
            Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY)
//            val width: Int = gray.rows()
//            val height: Int = gray.cols()
//            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//            Utils.matToBitmap(gray, bitmap)
//            tempImageView.setImageBitmap(bitmap)

            val blur = Mat()
            Imgproc.GaussianBlur(gray, blur, Size(5.0, 5.0), 0.0)

            // apply Canny edge detection
            val edges = Mat()
            Imgproc.Canny(blur, edges, 50.0, 150.0)

            // perform morphological operations to remove noise
            val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(3.0, 3.0))
            val dilated = Mat()
            Imgproc.dilate(edges, dilated, kernel)
            val eroded = Mat()
            Imgproc.erode(dilated, eroded, kernel)

            // find contours in the edges
            val contours = mutableListOf<MatOfPoint>()
            Imgproc.findContours(eroded, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

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

//            val width: Int = mask.rows()
//            val height: Int = mask.cols()
//            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//            Utils.matToBitmap(mask, bitmap)
//            tempImageView.setImageBitmap(bitmap)

            val halfMask = mask.clone()
            halfMask.colRange(mask.cols() / 2, mask.cols())!!.setTo(Scalar(0.0))

            // find contours in the half mask
            val halfContours = mutableListOf<MatOfPoint>()
            Imgproc.findContours(halfMask, halfContours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

            // find best contour in the half mask
            bestContour = halfContours.maxByOrNull { Imgproc.contourArea(it) }

            // create a new blank mask for the halved outline
            val halvedMask = Mat.zeros(halfMask.size(), CvType.CV_8UC1)

            // draw the halved outline in white
            Imgproc.drawContours(halvedMask, listOf(bestContour), 0, white, 4)


            val whiteImg = Mat(halvedMask.rows(), halfMask.cols(), CV_8UC3, Scalar(255.0,255.0,255.0));

            Core.subtract(whiteImg,Scalar(255.0), halvedMask)

            Imgproc.drawContours(whiteImg, listOf(bestContour), 0, Scalar(0.0), 4)

            /********************************/

            val rect = Imgproc.boundingRect(bestContour)

            val finalMask = Mat.zeros(whiteImg.size(), CvType.CV_8UC1)

            Imgproc.rectangle(finalMask, Point(rect.x.toDouble(), rect.y.toDouble()),
                Point((rect.x + rect.width).toDouble(), (rect.y + rect.height).toDouble()), white, -1)

            Core.bitwise_and(whiteImg, whiteImg, finalMask)

            // Crop the image to the bounding box
            val finalImg = Mat(finalMask, Rect(rect.x, rect.y, rect.width, rect.height))

            val width: Int = finalImg.rows()
            val height: Int = finalImg.cols()
            val bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(finalImg, bitmap)
            tempImageView.setImageBitmap(bitmap)

            /******************************** pdfer */

            val boardHeight = 72 // user param

            val numSections = ceil(boardHeight.toDouble() / PAPER_HEIGHT).toInt()
            val sectionHeight = bitmap.height / numSections
            val croppedImages = mutableListOf<Bitmap>()

            for (i in 0 until numSections) {
                val startY = i * sectionHeight
                val endY = (i + 1) * sectionHeight
                val croppedImage = Bitmap.createBitmap(bitmap, 0, startY, bitmap.width, endY - startY)
                croppedImages.add(croppedImage)
            }

            val images = mutableListOf<Mat>()
            for (i in 0 until numSections) {
                val image = Mat()
                Utils.bitmapToMat(croppedImages[i], image)
                val resizedImage = resizeImage(image)
                images.add(resizedImage)
            }

            createPdf(images)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        val uploadPhotoButton: Button = findViewById(R.id.uploadButton)
        uploadPhotoButton.setOnClickListener {
            pickImage()
        }

        val takePictureButton: Button = findViewById(R.id.cameraButton)
        takePictureButton.setOnClickListener {
            takePicture()
        }
    }

    private fun pickImage(){
        val galleryIntent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startGalleryLauncher.launch(galleryIntent)
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result?.data != null) {
                    var bitmap = result.data?.extras?.get("data") as Bitmap
                    val tempImageView: ImageView = findViewById(R.id.tempImage)
                    tempImageView.setImageBitmap(bitmap)
                }
            }
        }

    private fun takePicture(){
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultLauncher.launch(takePictureIntent)
    }

}