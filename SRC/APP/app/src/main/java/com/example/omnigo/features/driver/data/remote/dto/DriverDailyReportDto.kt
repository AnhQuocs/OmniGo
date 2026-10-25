package com.example.omnigo.features.driver.data.remote.dto

import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.google.gson.annotations.SerializedName

data class DriverDailyReportDto(
    @SerializedName(value = "totalTrips", alternate = ["completedTrips", "tripsCount", "totalBookings"])
    val totalTrips: Int? = null,
    @SerializedName(value = "totalRevenue", alternate = ["totalEarnings", "grossRevenue", "totalAmount"])
    val totalRevenue: Double? = null,
    @SerializedName(value = "driverEarnings", alternate = ["netEarnings", "netIncome", "actualEarnings"])
    val driverEarnings: Double? = null,
    @SerializedName(value = "platformFee", alternate = ["commissionFee", "serviceFee"])
    val platformFee: Double? = null,
    @SerializedName(value = "cashCollected", alternate = ["cashEarnings", "cashAmount"])
    val cashCollected: Double? = null,
    @SerializedName(value = "appPaid", alternate = ["appEarnings", "digitalAmount", "onlinePaid"])
    val appPaid: Double? = null
) {
    fun toDomain(): DriverDailyReport {
        val revenue = totalRevenue ?: 0.0
        val earnings = driverEarnings ?: (revenue * 0.80)
        val fee = platformFee ?: (revenue * 0.20)
        return DriverDailyReport(
            totalTrips = totalTrips ?: 0,
            totalRevenue = revenue,
            driverEarnings = earnings,
            platformFee = fee,
            cashCollected = cashCollected ?: 0.0,
            appPaid = appPaid ?: 0.0
        )
    }
}
