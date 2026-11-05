package com.example.omnigo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.max

object ImageUploadUtils {

    private const val TAG = "ImageUploadUtils"
    private const val MAX_DIMENSION = 1280
    private const val MAX_BYTES = 800 * 1024 // 800 KB (guarantees staying well under 1MB server limit)

    fun uriToMultipart(
        context: Context,
        uri: Uri,
        partName: String = "file",
        fileName: String = "review_image.jpg"
    ): MultipartBody.Part? {
        return try {
            val compressedBytes = compressImageFromUri(context, uri)
            if (compressedBytes == null) {
                Log.e(TAG, "compressImageFromUri returned null for uri: $uri")
                return null
            }
            val requestBody = compressedBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, fileName, requestBody)
        } catch (e: Exception) {
            Log.e(TAG, "uriToMultipart failed for uri: $uri", e)
            null
        }
    }

    private fun compressImageFromUri(context: Context, uri: Uri): ByteArray? {
        val resolver = context.contentResolver

        // Read bytes once from stream so we avoid multiple open attempts on picker URIs
        val rawBytes = try {
            resolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read raw bytes from uri: $uri", e)
            null
        } ?: return null

        if (rawBytes.isEmpty()) {
            Log.e(TAG, "Raw bytes empty for uri: $uri")
            return null
        }

        // 1. Decode bounds
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, options)

        if (options.outWidth <= 0 || options.outHeight <= 0) {
            Log.e(TAG, "Invalid image bounds: ${options.outWidth}x${options.outHeight}")
            return null
        }

        // 2. Calculate sample size
        var sampleSize = 1
        val maxSide = max(options.outWidth, options.outHeight)
        while (maxSide / (sampleSize * 2) >= MAX_DIMENSION) {
            sampleSize *= 2
        }

        // 3. Decode scaled bitmap
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOptions)
        if (bitmap == null) {
            Log.e(TAG, "Failed to decode bitmap from raw bytes (sampleSize: $sampleSize)")
            return null
        }

        // 4. Handle EXIF rotation
        val rotatedBitmap = rotateBitmapIfRequired(rawBytes, bitmap)

        // 5. Scale to max dimension if still larger
        val finalBitmap = scaleBitmap(rotatedBitmap, MAX_DIMENSION)

        // 6. Compress to JPEG with decreasing quality until under 800KB
        var quality = 85
        var outputBytes: ByteArray
        do {
            val outputStream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputBytes = outputStream.toByteArray()
            quality -= 15
        } while (outputBytes.size > MAX_BYTES && quality >= 40)

        if (finalBitmap != bitmap && !bitmap.isRecycled) {
            bitmap.recycle()
        }
        if (rotatedBitmap != finalBitmap && !rotatedBitmap.isRecycled) {
            rotatedBitmap.recycle()
        }
        if (!finalBitmap.isRecycled) {
            finalBitmap.recycle()
        }

        return outputBytes
    }

    private fun rotateBitmapIfRequired(rawBytes: ByteArray, bitmap: Bitmap): Bitmap {
        return try {
            val exif = ExifInterface(ByteArrayInputStream(rawBytes))
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read EXIF orientation", e)
            bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) {
            bitmap.recycle()
        }
        return rotated
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDim: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val maxSide = max(width, height)
        if (maxSide <= maxDim) return bitmap

        val ratio = maxDim.toFloat() / maxSide
        val targetWidth = (width * ratio).toInt()
        val targetHeight = (height * ratio).toInt()
        val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        if (scaled != bitmap) {
            bitmap.recycle()
        }
        return scaled
    }
}
