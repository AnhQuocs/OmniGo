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
    val createdAt: String? = null
) {
    /**
     * Cash amount that driver must collect from customer upon delivery:
     * - If isPaid == true (Online/Wallet): 0.0 VND
     * - If isPaid == false (CASH): Full order totalPrice
     */
    val cashAmountToCollect: Double
        get() = if (isPaid) 0.0 else totalPrice
}
