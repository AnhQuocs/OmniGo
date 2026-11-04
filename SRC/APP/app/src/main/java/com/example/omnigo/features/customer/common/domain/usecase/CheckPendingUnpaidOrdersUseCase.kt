package com.example.omnigo.features.customer.common.domain.usecase

import com.example.omnigo.features.customer.common.domain.model.OrderAdmissionResult
import com.example.omnigo.features.customer.food.domain.model.GetMyFoodOrdersResult
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.GetCustomerBookingsResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import javax.inject.Inject

/**
 * UseCase kiểm tra trạng thái các đơn hiện tại của khách hàng.
 * Thực thi cơ chế Order Admission Guard:
 * Chặn khách đặt đơn mới nếu:
 * 1. Còn đơn thức ăn online chưa thanh toán (isPaid == false & method != CASH & status != CANCELLED)
 * 2. Còn chuyến xe đang diễn ra (PENDING, ACCEPTED, ARRIVED, IN_PROGRESS)
 */
class CheckPendingUnpaidOrdersUseCase @Inject constructor(
    private val foodRepository: FoodRepository,
    private val rideBookingRepository: RideBookingRepository
) {
    suspend operator fun invoke(): OrderAdmissionResult {
        // 1. Kiểm tra đơn đồ ăn
        val foodOrdersResult = foodRepository.getMyFoodOrders()
        if (foodOrdersResult is GetMyFoodOrdersResult.Success) {
            val unpaidFood = foodOrdersResult.orders.firstOrNull { order ->
                val isOnline = order.paymentMethod != null && order.paymentMethod != PaymentMethod.CASH
                val isNotPaid = order.isPaid != true
                val isNotCancelled = !order.status.equals("CANCELLED", ignoreCase = true) &&
                    !order.status.equals("REJECTED", ignoreCase = true) &&
                    !order.status.equals("EXPIRED", ignoreCase = true)
                isOnline && isNotPaid && isNotCancelled
            }
            if (unpaidFood != null) {
                return OrderAdmissionResult.BlockedUnpaidFood(
                    orderId = unpaidFood.id,
                    totalPrice = unpaidFood.totalPrice ?: 0.0
                )
            }
        }

        // 2. Kiểm tra chuyến xe
        val bookingsResult = rideBookingRepository.getCustomerBookings()
        if (bookingsResult is GetCustomerBookingsResult.Success) {
            val activeRide = bookingsResult.bookings.firstOrNull { booking ->
                booking.status in listOf(
                    BookingStatus.PENDING,
                    BookingStatus.ACCEPTED,
                    BookingStatus.ARRIVED,
                    BookingStatus.IN_PROGRESS
                )
            }
            if (activeRide != null) {
                return OrderAdmissionResult.BlockedActiveRide(
                    bookingId = activeRide.bookingId,
                    status = activeRide.status.name
                )
            }
        }

        return OrderAdmissionResult.Allowed
    }
}

