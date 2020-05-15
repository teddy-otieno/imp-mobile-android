package com.imp.impandroidclient.app_state.web_client

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient

object HttpClient {
    var webClient: OkHttpClient = OkHttpClient()
    const val SERVER_URL = "http://192.168.0.15:8000"

    val JSON = "application/json; charset=utf-8".toMediaType()

    lateinit var accessKey: String
    lateinit var refreshKey: String

    fun getAuthHeader(): HashMap<String, String> {
        return hashMapOf("Authorization" to "Bearer $accessKey")
    }
}
