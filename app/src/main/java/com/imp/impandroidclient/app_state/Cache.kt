package com.imp.impandroidclient.app_state

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.LruCache
import android.util.Size
import com.imp.impandroidclient.app_state.repos.data.LocalImage
import com.imp.impandroidclient.app_state.web_client.HttpClient
import kotlinx.coroutines.*
import okhttp3.Request
import java.io.IOException
import java.lang.IllegalStateException

/**
 * TODO(teddy) Implement resource cache/manager to handle images \
 * prevent network loading of already existing images
 */

private class ImageCache {
    private val imageCache: LruCache<String, Bitmap> = LruCache(1024 * 1024 * 10)

    fun addImageToMemCache(key: String, value: Bitmap) {
        if(getImageFromMemCache(key) == null) {
            imageCache.put(key, value)
        }

    }

    fun getImageFromMemCache(key: String) : Bitmap?{
        return imageCache.get(key)
    }

    fun clearCaches() {
        imageCache.evictAll()
    }
}

private class ThumbnailCache {
    private val cache: LruCache<String, Bitmap> = LruCache(1024 * 1024 * 10)

    fun addImage(key: LocalImage, value: Bitmap) {
        if(getImage(key.toString()) == null) {
            cache.put(key.toString(), value)
        }
    }

    fun clearCache() {
        cache.evictAll()
    }

    fun getImage(key: String) : Bitmap? = cache.get(key)
}

private data class ImageFuture(val url: String, val image: Deferred<Bitmap?>)

object ResourceManager {

    private val imageCache: ImageCache = ImageCache()
    private val thumbnailCache: ThumbnailCache = ThumbnailCache()

    private val mainThreadScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    private val ioNetworkScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val loadingImages: MutableList<ImageFuture> = mutableListOf()


    fun onLoadImage(url: String, callback: (image: Bitmap?) -> Unit) {
        val image = imageCache.getImageFromMemCache(url)

        if(image != null) {
            callback(image)

        } else {
            val future = loadingImages.find { it.url == url }

            if(future != null) {

               mainThreadScope.launch {
                   val loadedImage = future.image.await()
                   callback(loadedImage)
               }

            } else {

                val loadingCoroutine = ioNetworkScope.async {
                    getImageFromServer(url)
                }

                loadingImages.add(ImageFuture(url, loadingCoroutine))

                mainThreadScope.launch {
                    callback(loadingCoroutine.await())
                }
            }
        }
    }

    fun getLocalImage(
        contentResolver: ContentResolver,
        image: LocalImage,
        size: Size? = null,
        callback: (image: Bitmap?) -> Unit
    ) {

        val cachedThumbnail = thumbnailCache.getImage(image.contentUri.toString())

        if(cachedThumbnail == null) {

            mainThreadScope.launch {

                if (Build.VERSION.SDK_INT >= 29) {

                    size?.let {
                        val bitmap = contentResolver.loadThumbnail(image.contentUri, size, null)
                        thumbnailCache.addImage(image, bitmap)
                        callback(bitmap)
                    } ?: throw IllegalStateException("Size parameter was not passed")

                } else {

                    val bitmapFactory = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                        inMutable = false
                        inSampleSize = 2
                    }

                    val bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                        contentResolver,
                        image.imageID,
                        MediaStore.Images.Thumbnails.MINI_KIND,
                        bitmapFactory
                    )

                    if (bitmap != null) {

                        thumbnailCache.addImage(image, bitmap)
                        callback(bitmap)

                    }

                }
            }

        } else {
            callback(cachedThumbnail)
        }

    }

    fun getThumbnailBitmap(uri: Uri): Bitmap? {
        return thumbnailCache.getImage(uri.toString())
    }

    private suspend fun getImageFromServer(url: String) : Bitmap? {

        val image = loadImage(url);

        image?.let {
            imageCache.addImageToMemCache(url, it)
        }

        return image
    }

    fun getImageFromCache(url: String) : Bitmap? = imageCache.getImageFromMemCache(url)

    fun clear() {}

    fun addImage(urlOrUri: String, image: Bitmap) {
        imageCache.addImageToMemCache(urlOrUri, image)
    }
}

private suspend fun loadImage(url: String): Bitmap? = withContext(Dispatchers.IO){

    val request = Request.Builder()
        .url(HttpClient.SERVER_URL + url)
        .addHeader("Authorization", "Bearer " + HttpClient.accessKey)
        .get()
        .build()

    try {
        val response = HttpClient.webClient.newCall(request).execute()

        if(response.isSuccessful) {
            val bytes = response.body!!.bytes()

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)!!
            return@withContext bitmap

        } else {
            Log.w("NETWORK", "Failed with Status Code ${response.code} ")
            return@withContext null
        }

    } catch (e: IOException) {

        Log.w("NETWORK",e.message ?: "$url : Unable to load image")
        return@withContext null
    }
}
