package com.imp.impandroidclient.media

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.ResourceManager
import kotlinx.android.synthetic.main.activity_image_selector.*

class ImageSelector : AppCompatActivity()
{

    lateinit var selectedImage: Bitmap
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_selector)

        val imageUri: Uri? = intent.extras?.getParcelable("IMAGE")

        imageUri?.run {
            setUp(this);
            listeners(imageUri)
        } ?: throw IllegalStateException("Image Uri was not passed into the bundle")

    }

    private fun setUp(imageUri: Uri)
    {
        if(Build.VERSION.SDK_INT > 29)
        {
            val source = ImageDecoder.createSource(contentResolver, imageUri)
            val bitmap = ImageDecoder.decodeBitmap(source)
            selectedImage = bitmap
            image.setImageBitmap(selectedImage)
        }
        else
        {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            selectedImage = bitmap
            image.setImageBitmap(selectedImage)
        }
    }

    private fun listeners(imageUri: Uri)
    {
        back.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        submit.setOnClickListener {

            ResourceManager.addImage(imageUri.toString(), selectedImage)

            val intent = Intent().apply {
                putExtra("IMAGE", imageUri)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}