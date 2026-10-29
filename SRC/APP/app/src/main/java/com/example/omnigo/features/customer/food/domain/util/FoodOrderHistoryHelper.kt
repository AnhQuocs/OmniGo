package com.example.omnigo.features.customer.food.domain.util

import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.FoodOrderItem
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class OrderHistoryStatusGroup {
    DELIVERED,
    ONGOING,
    CANCELLED
}

enum class OrderPaymentStatusType {
    CASH,
    PAID,
    REFUNDED
}

data class OrderThumbnailSummary(
    val visibleItems: List<FoodOrderItem>,
    val remainingQuantity: Int,
    val totalQuantity: Int
)

object FoodOrderHistoryHelper {

    const val MAX_VISIBLE_THUMBNAILS = 2
    private const val DEFAULT_ONGOING_MIN_MINUTES = 10
    private const val DEFAULT_ONGOING_MAX_MINUTES = 15

    fun resolveStatusGroup(status: String): OrderHistoryStatusGroup {
        return when (status.trim().uppercase(Locale.ROOT)) {
            "COMPLETED", "DELIVERED" -> OrderHistoryStatusGroup.DELIVERED
            "CANCELLED", "REJECTED" -> OrderHistoryStatusGroup.CANCELLED
            else -> OrderHistoryStatusGroup.ONGOING
        }
    }

    fun resolvePaymentStatusType(order: FoodOrder): OrderPaymentStatusType {
        val statusGroup = resolveStatusGroup(order.status)
        val isOnlineOrPaid = order.isPaid == true ||
            (order.paymentMethod != null && order.paymentMethod != PaymentMethod.CASH)

        return when {
            statusGroup == OrderHistoryStatusGroup.CANCELLED && isOnlineOrPaid ->
                OrderPaymentStatusType.REFUNDED
            isOnlineOrPaid ->
                OrderPaymentStatusType.PAID
            else ->
                OrderPaymentStatusType.CASH
        }
    }

    fun resolveThumbnails(
        items: List<FoodOrderItem>,
        maxVisible: Int = MAX_VISIBLE_THUMBNAILS
    ): OrderThumbnailSummary {
        if (items.isEmpty()) {
            return OrderThumbnailSummary(
                visibleItems = emptyList(),
                remainingQuantity = 0,
                totalQuantity = 1
            )
        }
        val safeMax = maxVisible.coerceAtLeast(1)
        val visibleItems = items.take(safeMax)
        val rawTotal = items.sumOf { it.quantity.coerceAtLeast(1) }
        val totalQuantity = rawTotal.coerceAtLeast(1)
        val remainingQuantity = (totalQuantity - visibleItems.size).coerceAtLeast(0)

        return OrderThumbnailSummary(
            visibleItems = visibleItems,
            remainingQuantity = remainingQuantity,
            totalQuantity = totalQuantity
        )
    }

    fun estimateOngoingEtaRange(
        order: FoodOrder,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Pair<Int, Int>? {
        if (resolveStatusGroup(order.status) != OrderHistoryStatusGroup.ONGOING) {
            return null
        }

        val explicitMinutes = order.estimatedDeliveryMinutes
        if (explicitMinutes != null && explicitMinutes > 0) {
            val maxMin = explicitMinutes.coerceAtLeast(DEFAULT_ONGOING_MIN_MINUTES)
            val minMin = (maxMin - 5).coerceAtLeast(5)
            return minMin to maxMin
        }

        if (order.restaurantLatitude != null &&
            order.restaurantLongitude != null &&
            order.dropOffLatitude != null &&
            order.dropOffLongitude != null
        ) {
            val estimate = RestaurantDeliveryEtaHelper.estimate(
                restaurantLat = order.restaurantLatitude,
                restaurantLon = order.restaurantLongitude,
                userLat = order.dropOffLatitude,
                userLon = order.dropOffLongitude
            )
            return estimate.minMinutes to estimate.maxMinutes
        }

        return DEFAULT_ONGOING_MIN_MINUTES to DEFAULT_ONGOING_MAX_MINUTES
    }

    fun formatTimeAndDate(
        rawDate: String?,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        if (rawDate.isNullOrBlank()) return ""
        val trimmed = rawDate.trim()
        val formatter = DateTimeFormatter.ofPattern("HH:mm • dd/MM/yyyy", Locale.getDefault())

        return try {
            val instant = Instant.parse(trimmed)
            instant.atZone(zoneId).format(formatter)
        } catch (e: Exception) {
            try {
                val ldt = LocalDateTime.parse(trimmed)
                ldt.format(formatter)
            } catch (e2: Exception) {
                trimmed
            }
        }
    }

    fun enrichOrder(
        order: FoodOrder,
        restaurant: Restaurant?,
        catalogItems: List<MenuItem> = restaurant?.menuItems.orEmpty()
    ): FoodOrder {
        val effectiveCatalog = if (catalogItems.isNotEmpty()) {
            catalogItems
        } else {
            restaurant?.menuItems.orEmpty()
        }

        val enrichedItems = order.items.map { item ->
            val matchedMenu = effectiveCatalog.firstOrNull { menu ->
                menu.id == item.menuItemId ||
                    menu.name.trim().equals(item.itemName.trim(), ignoreCase = true)
            }
            val resolvedImage = item.imageUrl?.takeIf { it.isNotBlank() }
                ?: matchedMenu?.imageUrl?.takeIf { it.isNotBlank() }

            if (!resolvedImage.isNullOrBlank() && resolvedImage != item.imageUrl) {
                item.copy(imageUrl = resolvedImage)
            } else {
                item
            }
        }

        val finalRestaurantImage = order.restaurantImageUrl?.takeIf { it.isNotBlank() }
            ?: restaurant?.imageUrl?.takeIf { it.isNotBlank() }
            ?: enrichedItems.firstOrNull { !it.imageUrl.isNullOrBlank() }?.imageUrl

        val finalRestaurantAddress = order.restaurantAddress?.takeIf { it.isNotBlank() }
            ?: restaurant?.address?.takeIf { it.isNotBlank() }

        val finalRestaurantLat = order.restaurantLatitude ?: restaurant?.latitude
        val finalRestaurantLon = order.restaurantLongitude ?: restaurant?.longitude

        return order.copy(
            restaurantImageUrl = finalRestaurantImage,
            restaurantAddress = finalRestaurantAddress,
            restaurantLatitude = finalRestaurantLat,
            restaurantLongitude = finalRestaurantLon,
            items = enrichedItems
        )
    }
}

