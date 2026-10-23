package com.example.omnigo.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object ImageUploadUtils {
    fun uriToMultipart(
        context: Context,
        uri: Uri,
        partName: String = "file",
        fileName: String = "document.jpg"
    ): MultipartBody.Part? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.use { it.readBytes() }
            val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, fileName, requestBody)
        } catch (_: Exception) {
            null
        }
    }
}
