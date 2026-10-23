package com.example.omnigo.features.auth.domain.usecase

import com.example.omnigo.features.auth.domain.repository.AuthRepository
import okhttp3.MultipartBody
import javax.inject.Inject

class UploadDriverDocumentUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(file: MultipartBody.Part): Result<String> {
        return authRepository.uploadDriverDocument(file)
    }
}
