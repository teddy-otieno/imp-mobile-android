package com.imp.impandroidclient.helpers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser

class ImageRequest(
    url: String,
    private val headers: MutableMap<String, String>,
    private val listener: Response.Listener<Bitmap>,
    private val onErrorListener: Response.ErrorListener
) : Request<Bitmap>(Method.GET, url, onErrorListener) {
    override fun parseNetworkResponse(response: NetworkResponse?): Response<Bitmap> {
        if (response == null) {
            return Response.error(ParseError(Exception("Image not loaded")))
        }
        val response_data: ByteArray = response.data

        val bitmap = BitmapFactory.decodeByteArray(response_data, 0, response_data.size)
        return Response.success(bitmap, HttpHeaderParser.parseCacheHeaders(response))
    }

    override fun getHeaders(): MutableMap<String, String> = headers
    override fun deliverResponse(response: Bitmap?) = listener.onResponse(response)
}