package com.example.omnigo.features.customer.food.domain.model

data class Restaurant(
    val id: Long,
    val name: String,
    val phone: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String,
    val status: String,
    val openTime: String,
    val closeTime: String,
    val rating: Double,
    val reviewCount: Int,
    val isOpen: Boolean,
    val menuItems: List<MenuItem>,
    val isLocked: Boolean = false,
    val lockedReason: String = ""
) {
    /** Trạng thái nhận đơn thực tế: khóa bởi hệ thống > đóng cửa > mở cửa. */
    val availability: RestaurantAvailability
        get() = when {
            isLocked -> RestaurantAvailability.LOCKED
            isOpen -> RestaurantAvailability.OPEN
            else -> RestaurantAvailability.CLOSED
        }

    val canOrder: Boolean get() = availability == RestaurantAvailability.OPEN

    val hasCoordinates: Boolean get() = !(latitude == 0.0 && longitude == 0.0)

    val hasReviews: Boolean get() = reviewCount > 0 && rating > 0.0
}

enum class RestaurantAvailability { OPEN, CLOSED, LOCKED }
