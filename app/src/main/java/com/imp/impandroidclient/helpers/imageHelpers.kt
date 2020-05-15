package com.imp.impandroidclient.helpers

import android.graphics.*
import android.widget.ImageView
import com.imp.impandroidclient.app_state.web_client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request


fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Int): Bitmap {
    val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

    val canvas = Canvas(output);

    val color = 0xf424242;
    val paint = Paint()
    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    val rectf = RectF(rect)
    val roundPx = pixels.toFloat();

    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    canvas.drawRoundRect(rectf, roundPx, roundPx, paint)

    paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
    canvas.drawBitmap(bitmap, rect, rect, paint)
    return output
}

fun squareCropBitmap(bitmap: Bitmap): Bitmap {
    val max: Int = if (bitmap.height > bitmap.width)
        bitmap.width
    else
        bitmap.height

    val output = Bitmap.createBitmap(bitmap, 0, 0, max, max)

    return output;
}

fun fillViewBitmap(bitmap: Bitmap, imageView: ImageView): Bitmap {

    val widthScale = bitmap.width / imageView.width
    return Bitmap.createBitmap(bitmap, 0, 0, imageView.width * widthScale, bitmap.height)
}

suspend fun loadImage(url: String): Bitmap? = withContext(Dispatchers.IO){

    val request = Request.Builder()
        .url(HttpClient.SERVER_URL + url)
        .addHeader("Authorization", "Bearer " + HttpClient.accessKey)
        .get()
        .build()

    val response = HttpClient.webClient.newCall(request).execute()

    if(response.isSuccessful) {
        val bytes = response.body!!.bytes()
        val bitmap: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)!!

        return@withContext bitmap
    }

    return@withContext null
}