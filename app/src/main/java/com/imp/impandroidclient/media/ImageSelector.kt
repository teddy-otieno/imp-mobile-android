package com.imp.impandroidclient.media

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.imp.impandroidclient.R
import kotlinx.android.synthetic.main.activity_image_selector.*

class ImageSelector : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_selector)

        val imageUri: Uri? = intent.extras?.getParcelable("IMAGE")

        imageUri?.run {
            setUp(this);
        } ?: throw IllegalStateException("Image Uri was not passed into the bundle")
    }

    private fun setUp(imageUri: Uri)
    {
        if(Build.VERSION.SDK_INT > 29)
        {
            val source = ImageDecoder.createSource(contentResolver, imageUri)
            val bitmap = ImageDecoder.decodeBitmap(source)
            image.setImageBitmap(bitmap)
        }
        else
        {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            image.setImageBitmap(bitmap)
        }
    }
}