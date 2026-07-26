package com.harissabil.fisch.core.common.util

import android.graphics.Bitmap
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

/**
 * Downscales the bitmap so its longest side is at most [maxDimension], then JPEG-compresses it,
 * lowering quality further if needed until the result is at or under [maxSizeBytes].
 *
 * Camera photos come in at full sensor resolution (e.g. 3000x4000px); compressing that at a fixed
 * JPEG quality alone barely reduces file size. Downscaling first is what actually keeps uploads
 * small.
 */
fun Bitmap.toCompressedJpeg(
    maxDimension: Int = 1024,
    startQuality: Int = 80,
    minQuality: Int = 40,
    maxSizeBytes: Int = 300 * 1024,
): ByteArray {
    val scale = maxDimension.toFloat() / maxOf(width, height)
    val scaledBitmap =
        if (scale < 1f) {
            this.scale((width * scale).roundToInt(), (height * scale).roundToInt())
        } else {
            this
        }

    var quality = startQuality
    var bytes: ByteArray
    do {
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        bytes = outputStream.toByteArray()
        quality -= 10
    } while (bytes.size > maxSizeBytes && quality >= minQuality)

    return bytes
}
