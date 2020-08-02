package com.imp.impandroidclient.app_state.web_client

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object HttpClient {
    /**
     * @Warning(teddy) Implement thread safety
     *
     * Not sure if what i'm implementing is pure evil
     */
    val webClient: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    val gson = Gson()

    const val SERVER_URL = "http://192.168.0.15:8000"

    val JSON = "application/json; charset=utf-8".toMediaType()
    val MEDIA_TYPE_PNG = "image/png".toMediaTypeOrNull()
    val MEDIA_TYPE_JPEG = "image/jpeg".toMediaTypeOrNull()

    lateinit var accessKey: String
    lateinit var refreshKey: String

    fun getAuthHeader(): HashMap<String, String> =  hashMapOf("Authorization" to "Bearer $accessKey")
    fun isAccessKeyInititialized(): Boolean = this::accessKey.isInitialized
}
