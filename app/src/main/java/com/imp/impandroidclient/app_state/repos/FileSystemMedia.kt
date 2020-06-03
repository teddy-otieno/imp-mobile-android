package com.imp.impandroidclient.app_state.repos

import android.app.Application
import android.content.ContentUris
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import com.imp.impandroidclient.app_state.repos.data.LocalImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.joda.time.LocalDate

class FileSystemMedia private  constructor(private val application: Application)
{
    companion object
    {
        private lateinit var instance: FileSystemMedia
        fun getInstance(): FileSystemMedia
        {
            return if(this::instance.isInitialized)
            {
                instance
            }
            else
            {
                throw IllegalStateException("FileSystemMedia was not initialized")
            }
        }

        fun initializeInstance(application: Application)
        {
            instance = FileSystemMedia(application)
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val localImages : MutableLiveData<List<LocalImage>> = MutableLiveData()

    val images get() = localImages
    init {
        scope.launch(Dispatchers.IO) {
            localImages.postValue(getImageThumbNails())
        }
    }

    private suspend fun getImageThumbNails() : List<LocalImage>
    {
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

            while(cursor.moveToNext())
            {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                images += LocalImage(id, contentUri)

            }
        } ?: throw IllegalStateException("Media Query cannot be null")

        return images
    }

    fun clean()
    {
        scope.cancel()
    }
}