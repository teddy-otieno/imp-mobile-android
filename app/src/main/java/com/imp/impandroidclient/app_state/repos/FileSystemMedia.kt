package com.imp.impandroidclient.app_state.repos

import android.app.Application
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import com.imp.impandroidclient.app_state.repos.data.LocalImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.joda.time.LocalDate

object FileSystemMedia {

    lateinit var application: Application
    private val scope = CoroutineScope(Dispatchers.IO)
    private val localImages : MutableLiveData<MutableList<LocalImage>> = MutableLiveData(
        mutableListOf()
    )
    private var imagesLoaded: Boolean = false

    val images get() = localImages
    val newImage: MutableLiveData<LocalImage> by lazy {
        MutableLiveData<LocalImage>()
    }

    fun loadImages() {
        newImage.postValue(null)
        if(!imagesLoaded) {
            scope.launch {
                getImageThumbNails()
                imagesLoaded = true
            }
        }
    }

    private fun getImageThumbNails()  {

        /**
         * TODO(teddy) Lazy load chunks instead of entire library
         */
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DISPLAY_NAME
        )

        val selection = "${MediaStore.Images.Media.DATE_ADDED} <= ?"
        val selectionArgs = arrayOf(
            LocalDate.now().toString()
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} ASC"

        val query = application.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        val images: MutableList<LocalImage> = mutableListOf()

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            var counter = 0
            while(cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                val imageDat = LocalImage(id, contentUri)
                localImages.value?.let { images ->

                    val tempItem = images.find { it.contentUri == contentUri }

                    if(tempItem == null) {
                        images += imageDat
                        localImages.postValue(images)
                    }

                }

                newImage.postValue(imageDat)
                counter++
            }
        } ?: throw IllegalStateException("Media Query cannot be null")
    }

    fun clean() {

        scope.cancel()
    }
}

fun resize(image: Bitmap, width: Int, height: Int): Bitmap {
    val background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val originalWidth = image.width
    val originalHeight = image.height

    val canvas = Canvas(background)

    val scale = (width / originalWidth).toFloat()
    val xTranslation = 0.0f
    val yTranslation = (height - originalHeight * scale) / 2.0f

    val transformation = Matrix()
    transformation.postTranslate(xTranslation, yTranslation)
    transformation.preScale(scale, scale)

    val paint = Paint()
    paint.isFilterBitmap = true

    canvas.drawBitmap(image, transformation, paint)

    return background
}