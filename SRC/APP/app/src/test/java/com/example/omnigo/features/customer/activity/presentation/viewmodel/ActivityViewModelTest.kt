package com.example.omnigo.features.customer.activity.presentation.viewmodel

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.GetCustomerBookingsResult
import com.example.omnigo.features.customer.ride.domain.usecase.GetCustomerBookingsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getCustomerBookingsUseCase: GetCustomerBookingsUseCase

    private val sampleBookings = listOf(
        Booking(
            bookingId = 1L,
            customerId = 5L,
            status = BookingStatus.PENDING,
            startLatitude = 21.0,
            startLongitude = 105.0,
            endLatitude = 21.1,
            endLongitude = 105.1
        ),
        Booking(
            bookingId = 2L,
            customerId = 5L,
            status = BookingStatus.IN_PROGRESS,
            startLatitude = 21.0,
            startLongitude = 105.0,
            endLatitude = 21.1,
            endLongitude = 105.1
        ),
        Booking(
            bookingId = 3L,
            customerId = 5L,
            status = BookingStatus.COMPLETED,
            startLatitude = 21.0,
            startLongitude = 105.0,
            endLatitude = 21.1,
            endLongitude = 105.1
        ),
        Booking(
            bookingId = 4L,
            customerId = 5L,
            status = BookingStatus.CANCELLED,
            startLatitude = 21.0,
            startLongitude = 105.0,
            endLatitude = 21.1,
            endLongitude = 105.1
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getCustomerBookingsUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads bookings and partitions into ongoing and history`() = runTest {
        coEvery {
            getCustomerBookingsUseCase()
        } returns GetCustomerBookingsResult.Success(sampleBookings)

        val viewModel = ActivityViewModel(getCustomerBookingsUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.ongoingBookings.size)
        assertEquals(1L, state.ongoingBookings[0].bookingId)
        assertEquals(2L, state.ongoingBookings[1].bookingId)

        assertEquals(2, state.historyBookings.size)
        assertEquals(3L, state.historyBookings[0].bookingId)
        assertEquals(4L, state.historyBookings[1].bookingId)
        assertTrue(state.hasOngoing)
        assertTrue(state.hasHistory)
        coVerify(atLeast = 1) { getCustomerBookingsUseCase() }
    }

    @Test
    fun `loadBookings updates state with error message when usecase fails`() = runTest {
        coEvery {
            getCustomerBookingsUseCase()
        } returns GetCustomerBookingsResult.Error(
            error = RideError.NETWORK_ERROR,
            message = "Unable to connect"
        )

        val viewModel = ActivityViewModel(getCustomerBookingsUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.ongoingBookings.isEmpty())
        assertTrue(state.historyBookings.isEmpty())
        assertEquals("Unable to connect", state.errorMessage)
    }
}
