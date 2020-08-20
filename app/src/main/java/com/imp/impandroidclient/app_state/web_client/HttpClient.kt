package com.imp.impandroidclient.app_state.web_client

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import java.text.DateFormat
import java.util.concurrent.TimeUnit

object HttpClient {

    val webClient: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    val gson = GsonBuilder().apply {
        setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SX")
    }.create()

    const val SERVER_URL = "http://192.168.0.29:8000"
    const val SERVER_WEBSOCKET_URL = "ws://192.168.0.29:8000"

    val JSON = "application/json; charset=utf-8".toMediaType()
    val MEDIA_TYPE_PNG = "image/png".toMediaTypeOrNull()
    val MEDIA_TYPE_JPEG = "image/jpeg".toMediaTypeOrNull()

    lateinit var accessKey: String
    lateinit var refreshKey: String

    fun getAuthHeader(): HashMap<String, String> =  hashMapOf("Authorization" to "Bearer $accessKey")
    fun isAccessKeyInititialized(): Boolean = this::accessKey.isInitialized
}