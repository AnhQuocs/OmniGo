package com.example.omnigo.features.driver.ride.presentation.state

import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.driver.common.config.PlatformFeeConfig

data class DriverActiveTripUiState(
    val bookingId: Long = 0L,
    val status: BookingStatus = BookingStatus.ACCEPTED,
    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    val endLatitude: Double? = null,
    val endLongitude: Double? = null,
    val pickupAddress: String? = null,
    val dropoffAddress: String? = null,
    val customerId: Long? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAvatarUrl: String? = null,
    val price: Double = 0.0,
    val paymentMethod: String = "CASH",
    val commissionRate: Double = PlatformFeeConfig.effectiveRate,
    val estimatedPlatformFee: Double? = null,
    val estimatedDriverEarnings: Double? = null,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val distanceToTargetMeters: Double? = null,
    val isWithinGeofence: Boolean = false,
    val isLoading: Boolean = false,
    val isUpdatingStatus: Boolean = false,
    val isTripCancelled: Boolean = false,
    val showSettlementDialog: Boolean = false,
    val showCancelConfirmDialog: Boolean = false,
    val showGeofenceOverrideDialog: Boolean = false,
    val errorMessage: String? = null,
    val actionMessage: String? = null
) {
    /**
     * Phí sàn ước tính dựa trên tỷ lệ cấu hình hoặc phí thực tế từ hệ thống.
     * Tuyệt đối không hardcode giá trị cố định price * 0.20.
     */
    val platformFee: Double
        get() = estimatedPlatformFee ?: PlatformFeeConfig.calculateEstimatedFee(price, commissionRate)

    /**
     * Thu nhập ước tính của tài xế dựa trên giá cước và tỷ lệ cấu hình.
     */
    val driverEarnings: Double
        get() = estimatedDriverEarnings ?: PlatformFeeConfig.calculateEstimatedEarnings(price, commissionRate)

    val isCashPayment: Boolean
        get() = paymentMethod.equals("CASH", ignoreCase = true)

    val amountToCollect: Double
        get() = if (isCashPayment) price else 0.0

    val isAccepted: Boolean
        get() = status == BookingStatus.ACCEPTED

    val isArrived: Boolean
        get() = status == BookingStatus.ARRIVED

    val isInProgress: Boolean
        get() = status == BookingStatus.IN_PROGRESS

    val isCompleted: Boolean
        get() = status == BookingStatus.COMPLETED

    val isFinished: Boolean
        get() = status == BookingStatus.COMPLETED || status == BookingStatus.CANCELLED
}
