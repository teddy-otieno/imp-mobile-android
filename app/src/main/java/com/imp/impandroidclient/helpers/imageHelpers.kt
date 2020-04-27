package com.imp.impandroidclient.helpers

import android.graphics.*


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
