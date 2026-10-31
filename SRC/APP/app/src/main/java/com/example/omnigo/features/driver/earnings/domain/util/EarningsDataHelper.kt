package com.example.omnigo.features.driver.earnings.domain.util

import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.example.omnigo.features.driver.earnings.domain.model.EarningsPeriod
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import com.example.omnigo.features.driver.trips.domain.util.DriverTripsTimelineHelper

data class EarningsChartPoint(
    val label: String,
    val amount: Double,
    val ratio: Float
)

data class EarningsTripItem(
    val id: Long,
    val title: String,
    val time: String,
    val grossAmount: Double,
    val netAmount: Double,
    val isCash: Boolean
)

object EarningsDataHelper {

    fun generateChartPoints(
        period: EarningsPeriod,
        report: DriverDailyReport?,
        orders: List<DriverDeliveryOrder>
    ): List<EarningsChartPoint> {
        val totalRevenue = report?.totalRevenue ?: 0.0
        val totalTrips = report?.totalTrips ?: 0

        // If no revenue and no trips, return points with 0 to show empty grid
        if (totalRevenue <= 0.0 && totalTrips <= 0) {
            return when (period) {
                EarningsPeriod.TODAY -> listOf(
                    EarningsChartPoint("0h", 0.0, 0f),
                    EarningsChartPoint("4h", 0.0, 0f),
                    EarningsChartPoint("8h", 0.0, 0f),
                    EarningsChartPoint("12h", 0.0, 0f),
                    EarningsChartPoint("16h", 0.0, 0f),
                    EarningsChartPoint("20h", 0.0, 0f),
                    EarningsChartPoint("24h", 0.0, 0f)
                )
                EarningsPeriod.THIS_WEEK -> listOf(
                    EarningsChartPoint("T2", 0.0, 0f),
                    EarningsChartPoint("T3", 0.0, 0f),
                    EarningsChartPoint("T4", 0.0, 0f),
                    EarningsChartPoint("T5", 0.0, 0f),
                    EarningsChartPoint("T6", 0.0, 0f),
                    EarningsChartPoint("T7", 0.0, 0f),
                    EarningsChartPoint("CN", 0.0, 0f)
                )
                EarningsPeriod.THIS_MONTH -> listOf(
                    EarningsChartPoint("T1", 0.0, 0f),
                    EarningsChartPoint("T2", 0.0, 0f),
                    EarningsChartPoint("T3", 0.0, 0f),
                    EarningsChartPoint("T4", 0.0, 0f)
                )
            }
        }

        // When revenue exists, calculate distribution
        val maxScale = 300_000.0.coerceAtLeast(totalRevenue)
        return when (period) {
            EarningsPeriod.TODAY -> {
                val p8h = totalRevenue * 0.25
                val p12h = totalRevenue * 0.40
                val p16h = totalRevenue * 0.20
                val p20h = totalRevenue * 0.15
                listOf(
                    EarningsChartPoint("0h", 0.0, 0f),
                    EarningsChartPoint("4h", 0.0, 0f),
                    EarningsChartPoint("8h", p8h, (p8h / maxScale).toFloat().coerceIn(0.05f, 1f)),
                    EarningsChartPoint("12h", p12h, (p12h / maxScale).toFloat().coerceIn(0.05f, 1f)),
                    EarningsChartPoint("16h", p16h, (p16h / maxScale).toFloat().coerceIn(0.05f, 1f)),
                    EarningsChartPoint("20h", p20h, (p20h / maxScale).toFloat().coerceIn(0.05f, 1f)),
                    EarningsChartPoint("24h", 0.0, 0f)
                )
            }
            EarningsPeriod.THIS_WEEK -> {
                val dailyAvg = totalRevenue / 7.0
                listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").mapIndexed { index, day ->
                    val weight = when (index) {
                        4 -> 1.4
                        5 -> 1.6
                        6 -> 1.3
                        else -> 0.8
                    }
                    val amount = (dailyAvg * weight).coerceAtLeast(0.0)
                    EarningsChartPoint(day, amount, (amount / maxScale).toFloat().coerceIn(0.05f, 1f))
                }
            }
            EarningsPeriod.THIS_MONTH -> {
                val weeklyAvg = totalRevenue / 4.0
                listOf("Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4").map { week ->
                    EarningsChartPoint(week, weeklyAvg, (weeklyAvg / maxScale).toFloat().coerceIn(0.05f, 1f))
                }
            }
        }
    }

    fun extractTripItems(
        report: DriverDailyReport?,
        orders: List<DriverDeliveryOrder>,
        bookings: List<Booking> = emptyList()
    ): List<EarningsTripItem> {
        val items = mutableListOf<EarningsTripItem>()

        // 1. Chuyến xe chở khách thực tế từ cơ sở dữ liệu
        if (bookings.isNotEmpty()) {
            val rideItems = bookings.map { b ->
                val timeStr = DriverTripsTimelineHelper.parseTime(b.completedAt ?: b.createdAt)
                EarningsTripItem(
                    id = b.bookingId,
                    title = "Chuyến xe #${b.bookingId}",
                    time = timeStr,
                    grossAmount = b.price,
                    netAmount = b.price * 0.80,
                    isCash = b.paymentMethod?.uppercase() == "CASH"
                )
            }
            items.addAll(rideItems)
        }

        // 2. Đơn giao món thực tế từ cơ sở dữ liệu
        val completedOrders = orders.filter { !it.status.isActive }
        if (completedOrders.isNotEmpty()) {
            val foodItems = completedOrders.map { order ->
                val gross = if (order.deliveryFee > 0) order.deliveryFee else order.totalPrice
                val timeStr = DriverTripsTimelineHelper.parseTime(order.createdAt)
                EarningsTripItem(
                    id = order.orderId,
                    title = "Giao món #${order.orderId}",
                    time = timeStr,
                    grossAmount = gross,
                    netAmount = gross * 0.80,
                    isCash = order.paymentMethod.uppercase() == "CASH"
                )
            }
            items.addAll(foodItems)
        }

        return items
    }
}
