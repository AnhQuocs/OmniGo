package com.example.omnigo.features.customer.common.domain.usecase

import com.example.omnigo.features.customer.common.domain.model.OrderAdmissionResult
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.GetMyFoodOrdersResult
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.GetCustomerBookingsResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckPendingUnpaidOrdersUseCaseTest {

    private val foodRepository: FoodRepository = mockk()
    private val rideBookingRepository: RideBookingRepository = mockk()
    private lateinit var useCase: CheckPendingUnpaidOrdersUseCase

    @Before
    fun setUp() {
        useCase = CheckPendingUnpaidOrdersUseCase(foodRepository, rideBookingRepository)
    }

    @Test
    fun `when unpaid online food order exists, returns BlockedUnpaidFood`() = runTest {
        val unpaidOrder = FoodOrder(
            id = 101L,
            restaurantId = 1L,
            restaurantName = "Phở Hà Nội",
            paymentMethod = PaymentMethod.VNPAY,
            isPaid = false,
            status = "PENDING",
            totalPrice = 115000.0
        )
        coEvery { foodRepository.getMyFoodOrders() } returns GetMyFoodOrdersResult.Success(listOf(unpaidOrder))
        coEvery { rideBookingRepository.getCustomerBookings() } returns GetCustomerBookingsResult.Success(emptyList())

        val result = useCase()

        assertTrue(result is OrderAdmissionResult.BlockedUnpaidFood)
        assertEquals(101L, (result as OrderAdmissionResult.BlockedUnpaidFood).orderId)
        assertEquals(115000.0, result.totalPrice, 0.01)
    }

    @Test
    fun `when online food order is already paid, returns Allowed`() = runTest {
        val paidOrder = FoodOrder(
            id = 102L,
            restaurantId = 1L,
            restaurantName = "Phở Hà Nội",
            paymentMethod = PaymentMethod.VNPAY,
            isPaid = true,
            status = "CONFIRMED",
            totalPrice = 115000.0
        )
        coEvery { foodRepository.getMyFoodOrders() } returns GetMyFoodOrdersResult.Success(listOf(paidOrder))
        coEvery { rideBookingRepository.getCustomerBookings() } returns GetCustomerBookingsResult.Success(emptyList())

        val result = useCase()

        assertEquals(OrderAdmissionResult.Allowed, result)
    }

    @Test
    fun `when food order uses cash, returns Allowed`() = runTest {
        val cashOrder = FoodOrder(
            id = 103L,
            restaurantId = 1L,
            restaurantName = "Bún Chả",
            paymentMethod = PaymentMethod.CASH,
            isPaid = false,
            status = "PENDING",
            totalPrice = 90000.0
        )
        coEvery { foodRepository.getMyFoodOrders() } returns GetMyFoodOrdersResult.Success(listOf(cashOrder))
        coEvery { rideBookingRepository.getCustomerBookings() } returns GetCustomerBookingsResult.Success(emptyList())

        val result = useCase()

        assertEquals(OrderAdmissionResult.Allowed, result)
    }

    @Test
    fun `when active ride exists, returns BlockedActiveRide`() = runTest {
        val activeRide = Booking(
            bookingId = 202L,
            status = BookingStatus.IN_PROGRESS
        )
        coEvery { foodRepository.getMyFoodOrders() } returns GetMyFoodOrdersResult.Success(emptyList())
        coEvery { rideBookingRepository.getCustomerBookings() } returns GetCustomerBookingsResult.Success(listOf(activeRide))

        val result = useCase()

        assertTrue(result is OrderAdmissionResult.BlockedActiveRide)
        assertEquals(202L, (result as OrderAdmissionResult.BlockedActiveRide).bookingId)
    }

    @Test
    fun `when no active or unpaid orders exist, returns Allowed`() = runTest {
        coEvery { foodRepository.getMyFoodOrders() } returns GetMyFoodOrdersResult.Success(emptyList())
        coEvery { rideBookingRepository.getCustomerBookings() } returns GetCustomerBookingsResult.Success(emptyList())

        val result = useCase()

        assertEquals(OrderAdmissionResult.Allowed, result)
    }
}

