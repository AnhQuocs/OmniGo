package com.example.omnigo.features.customer.promos.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VoucherDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("discountType") val discountType: String? = null,
    @SerializedName("discountValue") val discountValue: Double? = null,
    @SerializedName("maxDiscountAmount") val maxDiscountAmount: Double? = null,
    @SerializedName("minOrderAmount") val minOrderAmount: Double? = null,
    @SerializedName("applicableService") val applicableService: String? = null,
    @SerializedName("usageLimit") val usageLimit: Int? = null,
    @SerializedName("usedCount") val usedCount: Int? = null,
    @SerializedName("perUserLimit") val perUserLimit: Int? = null,
    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("endDate") val endDate: String? = null,
    @SerializedName("active") val active: Boolean? = null
)

data class ApplyVoucherRequestDto(
    @SerializedName("code") val code: String,
    @SerializedName("orderAmount") val orderAmount: Double,
    @SerializedName("serviceType") val serviceType: String,
    @SerializedName("userId") val userId: String? = null
)

data class CommitVoucherRequestDto(
    @SerializedName("code") val code: String,
    @SerializedName("orderAmount") val orderAmount: Double,
    @SerializedName("serviceType") val serviceType: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("orderId") val orderId: String? = null,
    @SerializedName("originalAmount") val originalAmount: Double = 0.0,
    @SerializedName("discountAmount") val discountAmount: Double = 0.0,
    @SerializedName("finalAmount") val finalAmount: Double = 0.0
)

data class ApplyVoucherResponseDto(
    @SerializedName("voucherCode") val voucherCode: String? = null,
    @SerializedName("voucherName") val voucherName: String? = null,
    @SerializedName("originalAmount") val originalAmount: Double? = null,
    @SerializedName("discountAmount") val discountAmount: Double? = null,
    @SerializedName("finalAmount") val finalAmount: Double? = null,
    @SerializedName("message") val message: String? = null
)

data class VoucherUsageDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("voucherId") val voucherId: Long? = null,
    @SerializedName("voucherCode") val voucherCode: String? = null,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("orderId") val orderId: String? = null,
    @SerializedName("serviceType") val serviceType: String? = null,
    @SerializedName("discountAmount") val discountAmount: Double? = null,
    @SerializedName("usedAt") val usedAt: String? = null
)

data class DeliveryFeeRequestDto(
    @SerializedName(value = "restaurantLatitude", alternate = ["pickupLat", "startLatitude"])
    val restaurantLatitude: Double,
    @SerializedName(value = "restaurantLongitude", alternate = ["pickupLng", "startLongitude"])
    val restaurantLongitude: Double,
    @SerializedName(value = "customerLatitude", alternate = ["dropoffLat", "endLatitude"])
    val customerLatitude: Double,
    @SerializedName(value = "customerLongitude", alternate = ["dropoffLng", "endLongitude"])
    val customerLongitude: Double
)

data class DeliveryFeeResponseDto(
    @SerializedName("distanceKm") val distanceKm: Double? = null,
    @SerializedName("deliveryFee") val deliveryFee: Double? = null,
    @SerializedName(value = "durationMinutes", alternate = ["estimatedMinutes"]) val durationMinutes: Int? = null,
    @SerializedName("calculationSource") val calculationSource: String? = null
)
