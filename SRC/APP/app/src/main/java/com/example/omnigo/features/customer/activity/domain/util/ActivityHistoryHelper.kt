package com.example.omnigo.features.customer.activity.domain.util

import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.FoodOrderItem
import com.example.omnigo.features.customer.food.domain.util.FoodOrderHistoryHelper
import com.example.omnigo.features.customer.food.domain.util.OrderHistoryStatusGroup
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MonthActivityGroup<T>(
    val year: Int,
    val month: Int,
    val items: List<T>
)

data class OngoingFoodDeliveryWindow(
    val startTimeText: String,
    val endTimeText: String,
    val maxMinutes: Int
)

object ActivityHistoryHelper {

    fun isOngoingFoodOrder(order: FoodOrder): Boolean {
        return FoodOrderHistoryHelper.resolveStatusGroup(order.status) == OrderHistoryStatusGroup.ONGOING
    }

    fun isHistoryFoodOrder(order: FoodOrder): Boolean {
        return FoodOrderHistoryHelper.resolveStatusGroup(order.status) != OrderHistoryStatusGroup.ONGOING
    }

    fun resolveFoodTimelineStep(status: String): Int {
        return when (status.trim().uppercase(Locale.ROOT)) {
            "AWAITING_PAYMENT",
            "PENDING_PAYMENT",
            "PENDING",
            "PENDING_MERCHANT",
            "ACCEPTED" -> 1
            "PREPARING",
            "DRIVER_SEARCHING",
            "READY_FOR_PICKUP",
            "NO_DRIVER_FOUND" -> 2
            "DRIVER_ASSIGNED",
            "PICKED_UP",
            "DELIVERING",
            "ARRIVED_CUSTOMER" -> 3
            "COMPLETED",
            "DELIVERED" -> 4
            else -> 1
        }
    }

    fun totalItemQuantity(items: List<FoodOrderItem>): Int {
        if (items.isEmpty()) return 1
        return items.sumOf { it.quantity.coerceAtLeast(1) }.coerceAtLeast(1)
    }

    fun summarizeFoodItemsText(items: List<FoodOrderItem>, maxNames: Int = 3): String {
        val names = items.mapNotNull { it.itemName.trim().takeIf(String::isNotBlank) }
        if (names.isEmpty()) return ""
        val safeMax = maxNames.coerceAtLeast(1)
        val visible = names.take(safeMax).joinToString(", ")
        return if (names.size > safeMax) "$visible, ..." else visible
    }

    fun formatConfirmedTime(
        rawDate: String?,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val zdt = parseZonedDateTime(rawDate, zoneId) ?: return ""
        return zdt.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
    }

    fun formatPreparingTime(
        rawDate: String?,
        currentStep: Int,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        if (currentStep < 2) return ""
        val zdt = parseZonedDateTime(rawDate, zoneId) ?: return ""
        return zdt.plusMinutes(5).format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
    }

    fun calculateDeliveryWindow(
        order: FoodOrder,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): OngoingFoodDeliveryWindow {
        val etaPair = FoodOrderHistoryHelper.estimateOngoingEtaRange(order, zoneId) ?: (15 to 25)
        val minMinutes = etaPair.first.coerceAtLeast(5)
        val maxMinutes = etaPair.second.coerceAtLeast(minMinutes + 5)
        val baseTime = parseZonedDateTime(order.createdAt, zoneId) ?: ZonedDateTime.now(zoneId)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

        return OngoingFoodDeliveryWindow(
            startTimeText = baseTime.plusMinutes(minMinutes.toLong()).format(timeFormatter),
            endTimeText = baseTime.plusMinutes(maxMinutes.toLong()).format(timeFormatter),
            maxMinutes = maxMinutes
        )
    }

    fun formatCompactDateAndTime(
        rawDate: String?,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val zdt = parseZonedDateTime(rawDate, zoneId) ?: return rawDate?.trim().orEmpty()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm", Locale.getDefault())
        return zdt.format(formatter)
    }

    fun groupFoodOrdersByMonth(
        orders: List<FoodOrder>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<MonthActivityGroup<FoodOrder>> {
        if (orders.isEmpty()) return emptyList()
        val fallbackDate = LocalDate.now(zoneId)
        return orders
            .groupBy { order ->
                val zdt = parseZonedDateTime(order.createdAt, zoneId)
                val year = zdt?.year ?: fallbackDate.year
                val month = zdt?.monthValue ?: fallbackDate.monthValue
                year to month
            }
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<Pair<Int, Int>, List<FoodOrder>>> { it.key.first }
                    .thenByDescending { it.key.second }
            )
            .map { (yearMonth, groupItems) ->
                MonthActivityGroup(
                    year = yearMonth.first,
                    month = yearMonth.second,
                    items = groupItems
                )
            }
    }

    internal fun parseZonedDateTime(
        rawDate: String?,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): ZonedDateTime? {
        if (rawDate.isNullOrBlank()) return null
        val trimmed = rawDate.trim()
        return try {
            Instant.parse(trimmed).atZone(zoneId)
        } catch (_: Exception) {
            try {
                LocalDateTime.parse(trimmed).atZone(zoneId)
            } catch (_: Exception) {
                null
            }
        }
    }
}

