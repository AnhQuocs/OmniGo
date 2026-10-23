package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.repository.FoodReviewRepository
import okhttp3.MultipartBody
import javax.inject.Inject

class UploadReviewImageUseCase @Inject constructor(
    private val foodReviewRepository: FoodReviewRepository
) {
    suspend operator fun invoke(file: MultipartBody.Part): Result<String> {
        return foodReviewRepository.uploadReviewImage(file)
    }
}
