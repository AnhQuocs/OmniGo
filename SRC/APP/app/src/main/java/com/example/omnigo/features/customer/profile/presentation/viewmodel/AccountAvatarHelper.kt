package com.example.omnigo.features.customer.profile.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.omnigo.features.auth.domain.usecase.UpdateAvatarUseCase
import com.example.omnigo.utils.ImageCompressor
import javax.inject.Inject

class AccountAvatarHelper @Inject constructor(
    private val updateAvatarUseCase: UpdateAvatarUseCase
) {
    suspend fun uploadFromUri(context: Context, uri: Uri): Result<String> {
        val part = ImageCompressor.compressAvatarToMultipart(context, uri)
            ?: return Result.failure(Exception("Cannot compress avatar"))
        return updateAvatarUseCase(part)
    }

    suspend fun uploadFromBitmap(bitmap: Bitmap): Result<String> {
        val part = ImageCompressor.compressBitmapToMultipart(bitmap)
            ?: return Result.failure(Exception("Cannot compress bitmap"))
        return updateAvatarUseCase(part)
    }
}

