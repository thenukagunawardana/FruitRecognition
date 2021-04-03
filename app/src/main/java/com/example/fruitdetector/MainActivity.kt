package com.example.fruitdetector

import android.os.Bundle
import android.widget.Toast
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import java.io.IOException
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity:AppCompatActivity()
{
    private lateinit var classifier: ClassifierClass
    private lateinit var bitmap: Bitmap

    private val cameraRequest=0
    private val galleryRequest=2

    private val maxInputSize=100
    private val modelPath="model-fruit.tflite"
    private val labelPath="labels.txt"
    private val sampleImagePath=""  //Optional

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)
        classifier = ClassifierClass(assets, modelPath,labelPath, maxInputSize)

        resources.assets.open(sampleImagePath).use {
            bitmap = BitmapFactory.decodeStream(it)
            bitmap = Bitmap.createScaledBitmap(bitmap, maxInputSize, maxInputSize, true)
            photoView.setImageBitmap(bitmap)
        }

        cameraButton.setOnClickListener {
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(callCameraIntent, cameraRequest)
        }

        imageButton.setOnClickListener {
            val callGalleryIntent = Intent(Intent.ACTION_PICK)
            callGalleryIntent.type = "image/*"
            startActivityForResult(callGalleryIntent, galleryRequest)
        }
        detectButton.setOnClickListener {
            val results = classifier.recognizeImage(bitmap).firstOrNull()
            resultView.text = results?.title + "\n Confidence:" + results?.confidence

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == cameraRequest)
        {
            if(resultCode == Activity.RESULT_OK && data != null) {
                bitmap = data.extras!!.get("data") as Bitmap
                bitmap = scaleImage(bitmap)
                val toast = Toast.makeText(this, ("Image crop to: w= ${bitmap.width} h= ${bitmap.height}"), Toast.LENGTH_LONG)
                toast.setGravity(Gravity.BOTTOM, 0, 20)
                toast.show()
                photoView.setImageBitmap(bitmap)
                resultView.text= "Your photo image set now."
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
        } else if(requestCode == galleryRequest) {
            if (data != null) {
                val uri = data.data

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                println("Success")
                bitmap = scaleImage(bitmap)
                photoView.setImageBitmap(bitmap)

            }
        } else {
            Toast.makeText(this, "Unauthorized", Toast.LENGTH_LONG).show()

        }
    }


    fun scaleImage(bitmap: Bitmap?): Bitmap {
        val orignalWidth = bitmap!!.width
        val originalHeight = bitmap.height
        val scaleWidth = maxInputSize.toFloat() / orignalWidth
        val scaleHeight = maxInputSize.toFloat() / originalHeight
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, orignalWidth, originalHeight, matrix, true)
    }




}