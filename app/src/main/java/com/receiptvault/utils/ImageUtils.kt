package com.receiptvault.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    /**
     * Compress and rotate image from a file path to a cache file.
     * Returns the path to the compressed file, or null on failure.
     */
    fun compressImage(context: Context, sourcePath: String, maxDimension: Int = 1200): String? {
        return runCatching {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(sourcePath, options)

            val scale = calculateInSampleSize(options, maxDimension, maxDimension)
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }

            var bitmap = BitmapFactory.decodeFile(sourcePath, decodeOptions) ?: return null

            // Correct rotation from EXIF
            bitmap = correctRotation(bitmap, sourcePath)

            val outputFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            bitmap.recycle()
            outputFile.absolutePath
        }.getOrNull()
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun correctRotation(bitmap: Bitmap, imagePath: String): Bitmap {
        val exif = runCatching { ExifInterface(imagePath) }.getOrNull() ?: return bitmap
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val rotation = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }

        val matrix = Matrix().apply { postRotate(rotation) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            .also { if (it != bitmap) bitmap.recycle() }
    }

    fun deleteFile(path: String) {
        runCatching { File(path).delete() }
    }

    fun fileExists(path: String) = File(path).exists()
}
