package com.imp.impandroidclient.app_state.web_client

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient

object HttpClient {
    var webClient: OkHttpClient = OkHttpClient()
    const val SERVER_URL = "http://192.168.0.15:8000"

    val JSON = "application/json; charset=utf-8".toMediaType()
    val MEDIA_TYPE_PNG = "image/png".toMediaTypeOrNull()

    lateinit var accessKey: String
    lateinit var refreshKey: String

    fun getAuthHeader(): HashMap<String, String> =  hashMapOf("Authorization" to "Bearer $accessKey")
    fun isAccessKeyInititialized(): Boolean = this::accessKey.isInitialized
}
