package com.imp.impandroidclient.app_state

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import com.imp.impandroidclient.app_state.web_client.HttpClient
import kotlinx.coroutines.*
import okhttp3.Request
import java.io.IOException

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

private data class ImageFuture(val url: String, val image: Deferred<Bitmap?>)

object ResourceManager {

    private val imageCache: ImageCache = ImageCache()

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

    private suspend fun getImageFromServer(url: String) : Bitmap? {

        val image = loadImage(url);

        image?.let {
            imageCache.addImageToMemCache(url, it)
        }

        return image
    }

    fun getImage(url: String) : Bitmap? = imageCache.getImageFromMemCache(url)

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

            return@withContext BitmapFactory.decodeByteArray(bytes, 0, bytes.size)!!

        } else {
            Log.w("NETWORK", "Failed with Status Code ${response.code} ")
            return@withContext null
        }

    } catch (e: IOException) {

        Log.w("NETWORK",e.message ?: "$url : Unable to load image")
        return@withContext null
    }
}
