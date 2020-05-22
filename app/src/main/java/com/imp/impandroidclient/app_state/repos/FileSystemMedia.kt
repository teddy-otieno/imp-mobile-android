package com.imp.impandroidclient.app_state.repos

import android.app.Application
import android.provider.MediaStore
import com.imp.impandroidclient.app_state.repos.data.LocalImage
import org.joda.time.LocalDate
import java.lang.IllegalStateException

class FileSystemMedia(private val application: Application)
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

    fun getImageThumbNails() : List<LocalImage>
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
                images += LocalImage(id)
            }
        } ?: throw IllegalStateException("Media Query cannot be null")

        return images
    }
}