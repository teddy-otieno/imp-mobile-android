package com.imp.impandroidclient.app_state.web_client

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

object HttpClient {
    private lateinit var requestQueue: RequestQueue
    const val SERVER_URL = "http://192.168.0.15:8000"

    lateinit var accessKey: String
    lateinit var refreshKey: String

    fun initializeRequestQueue(context: Context) {
        requestQueue = Volley.newRequestQueue(context)
    }

    fun<T> sendRequest(request: Request<T>) {
        requestQueue.add(request)
    }

    fun getAuthHeader(): HashMap<String, String> {
        return hashMapOf("Authorization" to "Bearer $accessKey")
    }
}
