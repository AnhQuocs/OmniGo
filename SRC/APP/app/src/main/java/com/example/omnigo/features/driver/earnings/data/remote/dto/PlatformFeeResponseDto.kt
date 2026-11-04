package com.example.omnigo.features.driver.earnings.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response từ GET /api/v1/payments/platform-fee
 * {"percentage": 20}
 * Tỷ lệ phí sàn chỉ dùng để hiển thị trên giao diện, không hardcode hay dùng để tự tính tiền quyết toán.
 */
data class PlatformFeeResponseDto(
    @SerializedName("percentage")
    val percentage: Int? = null
)

