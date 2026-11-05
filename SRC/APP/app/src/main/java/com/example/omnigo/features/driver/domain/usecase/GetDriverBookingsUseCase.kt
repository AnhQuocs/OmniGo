package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.customer.ride.domain.model.GetDriverBookingsResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import com.example.omnigo.features.driver.earnings.data.remote.api.DriverPaymentApi
import javax.inject.Inject

class GetDriverBookingsUseCase @Inject constructor(
    private val rideBookingRepository: RideBookingRepository,
    private val paymentApi: DriverPaymentApi
) {
    suspend operator fun invoke(): GetDriverBookingsResult {
        // 1. Gọi API lấy danh sách cuốc xe trực tiếp của tài xế
        val directResult = rideBookingRepository.getDriverBookings()
        if (directResult !is GetDriverBookingsResult.Success) {
            return directResult
        }

        val bookings = directResult.bookings
        if (bookings.isEmpty()) {
            return directResult
        }

        // 2. Bổ sung phí sàn và thu nhập thực tế từ giao dịch ví (COMMISSION_FEE)
        // Tuyệt đối KHÔNG tự suy ngược giá cuốc từ phí bằng phép tính "/ 0.20"
        return try {
            val transactions = paymentApi.getTransactions()
            val commissionMap = transactions
                .filter { it.transactionType?.uppercase() == "COMMISSION_FEE" && it.bookingId != null }
                .filter { it.orderId?.startsWith("FOOD_") != true && (it.status == null || it.status.equals("SUCCESS", ignoreCase = true)) }
                .associate { it.bookingId!! to Math.abs(it.amount ?: 0.0) }

            val enrichedBookings = bookings.map { booking ->
                val actualFee = booking.platformFee ?: commissionMap[booking.bookingId]
                val actualEarnings = booking.driverEarnings ?: actualFee?.let { fee ->
                    (booking.grossPrice - fee).coerceAtLeast(0.0)
                }

                booking.copy(
                    platformFee = actualFee,
                    driverEarnings = actualEarnings
                )
            }
            GetDriverBookingsResult.Success(enrichedBookings)
        } catch (_: Exception) {
            directResult
        }
    }
}
