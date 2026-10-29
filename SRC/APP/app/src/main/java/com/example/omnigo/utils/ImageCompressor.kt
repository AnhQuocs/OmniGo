package com.example.omnigo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max

object ImageCompressor {

    private const val MAX_SIZE_BYTES = 900 * 1024 // 900KB (dưới 1MB của server)
    private const val MAX_DIMENSION = 1080

    fun compressAvatarToMultipart(
        context: Context,
        uri: Uri,
        partName: String = "file",
        fileName: String = "avatar_${System.currentTimeMillis()}.jpg"
    ): MultipartBody.Part? {
        return try {
            val bitmap = decodeSampledBitmapFromUri(context, uri, MAX_DIMENSION, MAX_DIMENSION) ?: return null
            val rotatedBitmap = rotateBitmapIfRequired(context, uri, bitmap)
            compressBitmapInternal(rotatedBitmap, partName, fileName)
        } catch (_: Exception) {
            null
        }
    }

    fun compressBitmapToMultipart(
        bitmap: Bitmap,
        partName: String = "file",
        fileName: String = "avatar_cam_${System.currentTimeMillis()}.jpg"
    ): MultipartBody.Part? {
        return try {
            val scaledBitmap = scaleBitmapIfRequired(bitmap, MAX_DIMENSION, MAX_DIMENSION)
            compressBitmapInternal(scaledBitmap, partName, fileName)
        } catch (_: Exception) {
            null
        }
    }

    private fun compressBitmapInternal(
        bitmap: Bitmap,
        partName: String,
        fileName: String
    ): MultipartBody.Part {
        var quality = 85
        var stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

        while (stream.size() > MAX_SIZE_BYTES && quality > 30) {
            stream.reset()
            quality -= 15
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        }

        val bytes = stream.toByteArray()
        val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, fileName, requestBody)
    }

    private fun scaleBitmapIfRequired(bitmap: Bitmap, maxW: Int, maxH: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxW && height <= maxH) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val targetW: Int
        val targetH: Int
        if (ratio > 1f) {
            targetW = maxW
            targetH = (maxW / ratio).toInt()
        } else {
            targetH = maxH
            targetW = (maxH * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    private fun decodeSampledBitmapFromUri(
        context: Context,
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return max(1, inSampleSize)
    }

    private fun rotateBitmapIfRequired(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        var input: InputStream? = null
        try {
            input = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(input)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }

            val rotated = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
            if (rotated != bitmap) {
                bitmap.recycle()
            }
            return rotated
        } catch (_: Exception) {
            return bitmap
        } finally {
            input?.close()
        }
    }
}
