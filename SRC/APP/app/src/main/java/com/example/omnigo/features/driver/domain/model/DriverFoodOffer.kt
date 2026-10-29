package com.example.omnigo.features.driver.domain.model

data class DriverFoodOffer(
    val orderId: Long,
    val customerId: Long? = null,
    val restaurantId: Long? = null,
    val restaurantName: String,
    val restaurantAddress: String,
    val restaurantLatitude: Double? = null,
    val restaurantLongitude: Double? = null,
    val dropOffAddress: String,
    val dropOffLatitude: Double? = null,
    val dropOffLongitude: Double? = null,
    val deliveryFee: Double,
    val totalPrice: Double,
    val isPaid: Boolean = false,
    val paymentMethod: String? = null,
    val timeoutSeconds: Int = DEFAULT_TIMEOUT_SECONDS,
    val receivedAtMillis: Long = System.currentTimeMillis(),
    val customerName: String? = null,
    val customerPhone: String? = null
) {
    companion object {
        const val DEFAULT_TIMEOUT_SECONDS = 20
    }
}
