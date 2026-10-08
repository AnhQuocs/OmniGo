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

    val id: Long,

    val menuItemId: Long,

    val itemName: String,

    val itemPrice: Double,

    val quantity: Int,

    val subtotal: Double,

    val note: String

)



data class FoodOrder(

    val id: Long,

    val customerId: Long?,

    val driverId: Long?,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val driverVehiclePlate: String? = null,
    val driverAvatarUrl: String? = null,
    val restaurantId: Long,
    val restaurantName: String,
    val restaurantAddress: String? = null,
    val status: String,
    val paymentMethod: PaymentMethod?,
    val isPaid: Boolean?,
    val itemsPrice: Double,
    val deliveryFee: Double?,
    val totalPrice: Double?,
    val dropOffAddress: String,
    val dropOffLatitude: Double?,
    val dropOffLongitude: Double?,
    val note: String,
    val items: List<FoodOrderItem>,
    val estimatedDeliveryMinutes: Int?,

    val createdAt: String
)
