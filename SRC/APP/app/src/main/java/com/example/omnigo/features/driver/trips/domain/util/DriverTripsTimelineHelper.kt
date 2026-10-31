package com.example.omnigo.features.driver.trips.domain.util

import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import com.example.omnigo.features.driver.trips.domain.model.DriverHistoryDayGroup
import com.example.omnigo.features.driver.trips.domain.model.DriverHistoryItem
import com.example.omnigo.features.driver.trips.domain.model.DriverTripFilter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DriverTripsTimelineHelper {

    private val dtfDisplay: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    fun parseDate(timestampStr: String?): LocalDate {
        if (timestampStr.isNullOrBlank()) return LocalDate.now()
        return try {
            val clean = timestampStr.trim()
            if (clean.contains("T")) {
                val iso = clean.substringBefore("+").substringBefore("Z").take(19)
                LocalDateTime.parse(iso).toLocalDate()
            } else if (clean.contains(" ")) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                LocalDateTime.parse(clean.take(19), formatter).toLocalDate()
            } else {
                LocalDate.parse(clean.take(10))
            }
        } catch (_: Exception) {
            LocalDate.now()
        }
    }

    fun parseTime(timestampStr: String?): String {
        if (timestampStr.isNullOrBlank()) return "--:--"
        return try {
            val clean = timestampStr.trim()
            if (clean.contains("T")) {
                val iso = clean.substringBefore("+").substringBefore("Z").take(19)
                val ldt = LocalDateTime.parse(iso)
                String.format(Locale.getDefault(), "%02d:%02d", ldt.hour, ldt.minute)
            } else if (clean.contains(" ")) {
                val timePart = clean.substringAfter(" ")
                timePart.take(5)
            } else {
                "--:--"
            }
        } catch (_: Exception) {
            "--:--"
        }
    }

    fun groupTrips(
        rides: List<Booking>,
        foods: List<DriverDeliveryOrder>,
        filter: DriverTripFilter,
        todayLabel: String,
        yesterdayLabel: String
    ): List<DriverHistoryDayGroup> {
        val items = mutableListOf<DriverHistoryItem>()

        if (filter == DriverTripFilter.ALL || filter == DriverTripFilter.RIDE) {
            val historyRides = rides.filter {
                it.status.name == "COMPLETED" || it.status.name == "CANCELLED" || it.status.name == "REJECTED"
            }
            items.addAll(historyRides.map { DriverHistoryItem.Ride(it) })
        }

        if (filter == DriverTripFilter.ALL || filter == DriverTripFilter.FOOD) {
            val historyFoods = foods.filter { !it.status.isActive }
            items.addAll(historyFoods.map { DriverHistoryItem.Food(it) })
        }

        if (items.isEmpty()) return emptyList()

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val groupedByDate = items.groupBy { parseDate(it.timestampStr) }
        val sortedDates = groupedByDate.keys.sortedDescending()

        return sortedDates.map { date ->
            val dayItems = groupedByDate[date].orEmpty()
            val title = when (date) {
                today -> todayLabel
                yesterday -> yesterdayLabel
                else -> date.format(dtfDisplay)
            }
            val totalEarnings = dayItems.filter { it.isSuccess }.sumOf { it.netEarnings }

            DriverHistoryDayGroup(
                title = title,
                date = date,
                totalTrips = dayItems.size,
                totalEarnings = totalEarnings,
                items = dayItems
            )
        }
    }
}
