package com.example.omnigo.features.driver.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class DriverDailyReportDtoTest {

    private val gson = Gson()

    @Test
    fun `toDomain maps explicit values correctly`() {
        val dto = DriverDailyReportDto(
            totalTrips = 5,
            totalRevenue = 200000.0,
            driverEarnings = 160000.0,
            platformFee = 40000.0,
            cashCollected = 120000.0,
            appPaid = 80000.0
        )

        val domain = dto.toDomain()

        assertEquals(5, domain.totalTrips)
        assertEquals(200000.0, domain.totalRevenue, 0.01)
        assertEquals(160000.0, domain.driverEarnings, 0.01)
        assertEquals(40000.0, domain.platformFee, 0.01)
        assertEquals(120000.0, domain.cashCollected, 0.01)
        assertEquals(80000.0, domain.appPaid, 0.01)
    }

    @Test
    fun `toDomain calculates 80 percent net and 20 percent fee fallback when null`() {
        val dto = DriverDailyReportDto(
            totalTrips = 10,
            totalRevenue = 500000.0,
            driverEarnings = null,
            platformFee = null,
            cashCollected = 300000.0,
            appPaid = 200000.0
        )

        val domain = dto.toDomain()

        assertEquals(10, domain.totalTrips)
        assertEquals(500000.0, domain.totalRevenue, 0.01)
        assertEquals(400000.0, domain.driverEarnings, 0.01) // 80% of 500,000
        assertEquals(100000.0, domain.platformFee, 0.01)    // 20% of 500,000
        assertEquals(300000.0, domain.cashCollected, 0.01)
        assertEquals(200000.0, domain.appPaid, 0.01)
    }

    @Test
    fun `toDomain handles completely null DTO gracefully`() {
        val dto = DriverDailyReportDto()

        val domain = dto.toDomain()

        assertEquals(0, domain.totalTrips)
        assertEquals(0.0, domain.totalRevenue, 0.01)
        assertEquals(0.0, domain.driverEarnings, 0.01)
        assertEquals(0.0, domain.platformFee, 0.01)
        assertEquals(0.0, domain.cashCollected, 0.01)
        assertEquals(0.0, domain.appPaid, 0.01)
    }

    @Test
    fun `Gson deserializes alternate keys properly`() {
        val json = """
            {
                "completedTrips": 7,
                "grossRevenue": 350000.0,
                "netIncome": 280000.0,
                "commissionFee": 70000.0,
                "cashAmount": 200000.0,
                "onlinePaid": 150000.0
            }
        """.trimIndent()

        val dto = gson.fromJson(json, DriverDailyReportDto::class.java)
        val domain = dto.toDomain()

        assertEquals(7, domain.totalTrips)
        assertEquals(350000.0, domain.totalRevenue, 0.01)
        assertEquals(280000.0, domain.driverEarnings, 0.01)
        assertEquals(70000.0, domain.platformFee, 0.01)
        assertEquals(200000.0, domain.cashCollected, 0.01)
        assertEquals(150000.0, domain.appPaid, 0.01)
    }
}
