package com.example.omnigo.features.customer.ride.domain.model

data class Booking(
    val bookingId: Long = 0L,
    val customerId: Long = 0L,
    val driverId: Long? = null,
    val customerAvatarUrl: String? = null,
    val driverAvatarUrl: String? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val driverVehiclePlate: String? = null,
    val driverRating: Double? = null,
    val startLongitude: Double = 0.0,
    val startLatitude: Double = 0.0,
    val endLongitude: Double = 0.0,
    val endLatitude: Double = 0.0,
    val status: BookingStatus = BookingStatus.PENDING,
    val distanceInKm: Double = 0.0,
    val price: Double = 0.0,
    val paymentMethod: String? = null,
    val pickupAddress: String = "",
    val dropoffAddress: String = "",
    val vehicleType: String? = null,
    val createdAt: String? = null,
    val completedAt: String? = null,
    val platformFee: Double? = null,
    val driverEarnings: Double? = null,
    val commissionRate: Double? = null,
    val discountAmount: Double = 0.0,
    val voucherCode: String? = null,
    val originalPrice: Double? = null
) {
    /**
     * Giá cước thực tế khách hàng cần thanh toán sau khi trừ chiết khấu voucher/khuyến mãi.
     */
    val finalPrice: Double
        get() = price.coerceAtLeast(0.0)

    /**
     * Thu nhập thực tế của tài xế nhận được từ cuốc xe.
     * Sử dụng trực tiếp từ API hoặc trừ phí sàn thực tế đã thu.
     * Tuyệt đối không dùng tỷ lệ cố định của hiện tại để tính lại đơn cũ.
     */
    val actualEarnings: Double
        get() = driverEarnings ?: (platformFee?.let { ((originalPrice ?: (price + discountAmount)) - it).coerceAtLeast(0.0) } ?: (originalPrice ?: (price + discountAmount)))

    /**
     * Phí nền tảng (chiết khấu sàn) thực tế của cuốc xe.
     */
    val actualPlatformFee: Double
        get() = platformFee ?: (driverEarnings?.let { ((originalPrice ?: (price + discountAmount)) - it).coerceAtLeast(0.0) } ?: 0.0)
}
