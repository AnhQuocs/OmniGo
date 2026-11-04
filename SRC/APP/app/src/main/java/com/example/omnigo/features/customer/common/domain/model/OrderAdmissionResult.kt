package com.example.omnigo.features.customer.common.domain.model

/**
 * Kết quả kiểm tra điều kiện tạo đơn mới (Order Admission Guard).
 * Chặn khách đặt đơn mới khi còn khoản thanh toán dở dang hoặc đơn chưa hoàn tất.
 */
sealed interface OrderAdmissionResult {
    /**
     * Không có đơn nợ hoặc đơn chưa hoàn tất -> Được phép đặt đơn mới.
     */
    data object Allowed : OrderAdmissionResult

    /**
     * Đang có đơn giao đồ ăn online chưa hoàn tất thanh toán.
     */
    data class BlockedUnpaidFood(
        val orderId: Long,
        val totalPrice: Double
    ) : OrderAdmissionResult

    /**
     * Đang có chuyến xe đang diễn ra hoặc chưa hoàn tất thanh toán.
     */
    data class BlockedActiveRide(
        val bookingId: Long,
        val status: String
    ) : OrderAdmissionResult
}

