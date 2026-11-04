package com.example.omnigo.features.driver.food.domain.model

enum class DriverDeliveryStatus {
    ACCEPTED,
    DELIVERING,
    COMPLETED,
    CANCELLED,
    UNKNOWN;

    val isActive: Boolean
        get() = this == ACCEPTED || this == DELIVERING

    companion object {
        fun fromString(status: String?): DriverDeliveryStatus {
            return when (status?.uppercase()?.trim()) {
                "ACCEPTED", "CONFIRMED", "PREPARING", "READY_FOR_PICKUP" -> ACCEPTED
                "DELIVERING" -> DELIVERING
                "COMPLETED" -> COMPLETED
                "CANCELLED" -> CANCELLED
                else -> UNKNOWN
            }
        }
    }
}

data class DriverDeliveryOrderItem(
    val menuItemId: Long,
    val itemName: String,
    val quantity: Int,
    val itemPrice: Double,
    val subtotal: Double,
    val note: String? = null
)

data class DriverDeliveryOrder(
    val orderId: Long,
    val customerId: Long? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val restaurantId: Long? = null,
    val restaurantName: String = "",
    val restaurantAddress: String = "",
    val restaurantLatitude: Double? = null,
    val restaurantLongitude: Double? = null,
    val dropOffAddress: String = "",
    val dropOffLatitude: Double? = null,
    val dropOffLongitude: Double? = null,
    val status: DriverDeliveryStatus = DriverDeliveryStatus.ACCEPTED,
    val paymentMethod: String = "CASH",
    val isPaid: Boolean = false,
    val itemsPrice: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val totalPrice: Double = 0.0,
    val items: List<DriverDeliveryOrderItem> = emptyList(),
    val note: String? = null,
    val estimatedDeliveryMinutes: Int? = null,
    val createdAt: String? = null,
    val platformFee: Double? = null,
    val driverEarnings: Double? = null
) {
    /**
     * Cước giao hàng gộp (gross) của đơn đồ ăn.
     */
    val grossFee: Double
        get() = if (deliveryFee > 0.0) deliveryFee else totalPrice

    /**
     * Thu nhập thực tế của tài xế từ đơn đồ ăn.
     * Sử dụng phí thực tế của từng đơn, không tự tính lại bằng tỷ lệ 80%.
     */
    val actualEarnings: Double
        get() = driverEarnings ?: (platformFee?.let { (grossFee - it).coerceAtLeast(0.0) } ?: grossFee)

    /**
     * Phí sàn thực tế khấu trừ từ đơn đồ ăn.
     */
    val actualPlatformFee: Double
        get() = platformFee ?: (driverEarnings?.let { (grossFee - it).coerceAtLeast(0.0) } ?: 0.0)

    /**
     * Cash amount that driver must collect from customer upon delivery:
     * - If isPaid == true (Online/Wallet): 0.0 VND
     * - If isPaid == false (CASH): Full order totalPrice
     */
    val cashAmountToCollect: Double
        get() = if (isPaid) 0.0 else totalPrice
}
