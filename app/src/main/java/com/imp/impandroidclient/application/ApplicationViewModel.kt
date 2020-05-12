package com.imp.impandroidclient.application

import android.graphics.Bitmap
import android.util.LruCache
import androidx.lifecycle.AndroidViewModel

class ApplicationViewModel(application: GlobalApplication) : AndroidViewModel(application)
{
    val root_path: String = "http://192.168.0.15:8000"
    val memCache: LruCache<Int, Bitmap> = LruCache(1024 * 1024 * 10)

}

