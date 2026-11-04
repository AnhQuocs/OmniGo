package com.example.omnigo.features.driver.data.remote.dto

import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.google.gson.annotations.SerializedName

data class DriverDailyReportDto(
    @SerializedName(value = "totalTrips", alternate = ["completedTrips", "tripsCount", "totalBookings"])
    val totalTrips: Int? = null,
    @SerializedName(value = "totalRevenue", alternate = ["totalEarnings", "grossRevenue", "totalAmount"])
    val totalRevenue: Double? = null,
    @SerializedName(value = "driverEarnings", alternate = ["netEarnings", "netIncome", "actualEarnings", "income"])
    val driverEarnings: Double? = null,
    @SerializedName(value = "platformFee", alternate = ["commissionFee", "serviceFee", "commission"])
    val platformFee: Double? = null,
    @SerializedName(value = "cashCollected", alternate = ["cashEarnings", "cashAmount"])
    val cashCollected: Double? = null,
    @SerializedName(value = "appPaid", alternate = ["appEarnings", "digitalAmount", "onlinePaid"])
    val appPaid: Double? = null
) {
    fun toDomain(): DriverDailyReport {
        val revenue = totalRevenue ?: 0.0
        // Lấy commissionFee và driverEarnings thực tế từ API, không tự ý nhân 20% hay 80%
        val fee = platformFee ?: if (driverEarnings != null) (revenue - driverEarnings).coerceAtLeast(0.0) else 0.0
        val earnings = driverEarnings ?: if (platformFee != null) (revenue - platformFee).coerceAtLeast(0.0) else revenue
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
