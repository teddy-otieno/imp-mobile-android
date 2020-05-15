package com.imp.impandroidclient.app_state

import android.graphics.Bitmap
import android.util.LruCache

object Cache {
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