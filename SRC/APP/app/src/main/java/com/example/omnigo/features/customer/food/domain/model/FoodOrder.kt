package com.example.omnigo.features.customer.food.domain.model

enum class PaymentMethod(val code: String) {
    CASH("CASH"),
    WALLET("WALLET"),
    MOMO("MOMO"),
    VNPAY("VNPAY");

    companion object {
        fun fromCode(code: String?): PaymentMethod? =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
    }
}

data class FoodOrderItem(
    val id: Long = 0L,
    val menuItemId: Long,
    val itemName: String,
    val itemPrice: Double,
    val quantity: Int,
    val subtotal: Double = itemPrice * quantity,
    val note: String = "",
    val imageUrl: String? = null
)

data class FoodOrder(
    val id: Long,
    val customerId: Long? = null,
    val driverId: Long? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val driverVehiclePlate: String? = null,
    val driverAvatarUrl: String? = null,
    val restaurantId: Long,
    val restaurantName: String,
    val restaurantAddress: String? = null,
    val restaurantLatitude: Double? = null,
    val restaurantLongitude: Double? = null,
    val restaurantImageUrl: String? = null,
    val status: String = "PENDING",
    val paymentMethod: PaymentMethod? = PaymentMethod.CASH,
    val isPaid: Boolean? = false,
    val itemsPrice: Double = 0.0,
    val deliveryFee: Double? = 0.0,
    val totalPrice: Double? = 0.0,
    val dropOffAddress: String = "",
    val dropOffLatitude: Double? = null,
    val dropOffLongitude: Double? = null,
    val note: String = "",
    val items: List<FoodOrderItem> = emptyList(),
    val estimatedDeliveryMinutes: Int? = null,
    val createdAt: String = "",
    val voucherCode: String? = null,
    val voucherDiscount: Double = 0.0
) {
    /** Amount the customer actually pays: backend total minus locally-tracked voucher discount. */
    val payableTotal: Double
        get() = ((totalPrice ?: (itemsPrice + (deliveryFee ?: 0.0))) - voucherDiscount).coerceAtLeast(0.0)
}
