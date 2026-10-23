package com.example.omnigo.features.driver.food.data.mapper

import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderItemResponse
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderResponse
import com.example.omnigo.features.driver.domain.model.DriverFoodOffer
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DriverDeliveryOrderMapperTest {

    private val sampleItemDto = FoodOrderItemResponse(
        id = 1L,
        menuItemId = 101L,
        itemName = "Com Tam Suon Bi Cha",
        itemPrice = 45000.0,
        quantity = 2,
        subtotal = 90000.0,
        note = "Khong hanh"
    )

    private val sampleOrderDto = FoodOrderResponse(
        id = 888L,
        customerId = 12L,
        customerName = "Tran Thi Khach",
        customerPhone = "0987654321",
        restaurantId = 5L,
        restaurantName = "Com Tam Ba Ghien",
        restaurantAddress = "84 Dang Van Ngu",
        restaurantLatitude = 10.7928,
        restaurantLongitude = 106.6710,
        status = "CONFIRMED",
        paymentMethod = "CASH",
        isPaid = false,
        itemsPrice = 90000.0,
        deliveryFee = 20000.0,
        totalPrice = 110000.0,
        dropOffAddress = "123 Le Duan, Q1",
        dropOffLatitude = 10.7800,
        dropOffLongitude = 106.6990,
        note = "Giao tan cua",
        items = listOf(sampleItemDto),
        estimatedDeliveryMinutes = 25,
        createdAt = "2026-10-22T10:00:00Z"
    )

    @Test
    fun `mapToDomain correctly maps all fields and calculates cashToCollect when isPaid false`() {
        val domain = DriverDeliveryOrderMapper.mapToDomain(sampleOrderDto)
        assertNotNull(domain)
        assertEquals(888L, domain?.orderId)
        assertEquals("Tran Thi Khach", domain?.customerName)
        assertEquals("0987654321", domain?.customerPhone)
        assertEquals("Com Tam Ba Ghien", domain?.restaurantName)
        assertEquals(10.7928, domain?.restaurantLatitude)
        assertEquals(106.6710, domain?.restaurantLongitude)
        assertEquals("123 Le Duan, Q1", domain?.dropOffAddress)
        assertEquals(DriverDeliveryStatus.ACCEPTED, domain?.status)
        assertFalse(domain?.isPaid ?: true)
        assertEquals(110000.0, domain?.totalPrice ?: 0.0, 0.001)
        // Cash to collect must equal full totalPrice when isPaid is false
        assertEquals(110000.0, domain?.cashAmountToCollect ?: 0.0, 0.001)
        assertEquals(1, domain?.items?.size)
        assertEquals("Com Tam Suon Bi Cha", domain?.items?.first()?.itemName)
    }

    @Test
    fun `mapToDomain returns cashToCollect 0 when isPaid true`() {
        val paidDto = sampleOrderDto.copy(isPaid = true, paymentMethod = "WALLET")
        val domain = DriverDeliveryOrderMapper.mapToDomain(paidDto)
        assertNotNull(domain)
        assertTrue(domain?.isPaid ?: false)
        assertEquals(110000.0, domain?.totalPrice ?: 0.0, 0.001)
        // Must be 0 when paid online
        assertEquals(0.0, domain?.cashAmountToCollect ?: 1.0, 0.001)
    }

    @Test
    fun `mapToDomain returns null when orderId is missing or non-positive`() {
        assertNull(DriverDeliveryOrderMapper.mapToDomain(sampleOrderDto.copy(id = null)))
        assertNull(DriverDeliveryOrderMapper.mapToDomain(sampleOrderDto.copy(id = 0L)))
        assertNull(DriverDeliveryOrderMapper.mapToDomain(sampleOrderDto.copy(id = -5L)))
    }

    @Test
    fun `mapFromOffer initializes delivery order from offer event`() {
        val offer = DriverFoodOffer(
            orderId = 999L,
            customerId = 44L,
            restaurantId = 7L,
            restaurantName = "Banh Mi Huynh Hoa",
            restaurantAddress = "26 Le Thi Rieng",
            restaurantLatitude = 10.7711,
            restaurantLongitude = 106.6922,
            dropOffAddress = "88 Hai Ba Trung",
            dropOffLatitude = 10.7788,
            dropOffLongitude = 106.7001,
            deliveryFee = 25000.0,
            totalPrice = 125000.0,
            isPaid = false,
            paymentMethod = "CASH"
        )

        val domain = DriverDeliveryOrderMapper.mapFromOffer(offer)
        assertEquals(999L, domain.orderId)
        assertEquals("Banh Mi Huynh Hoa", domain.restaurantName)
        assertEquals(DriverDeliveryStatus.ACCEPTED, domain.status)
        assertEquals(125000.0, domain.cashAmountToCollect, 0.001)
        assertTrue(domain.items.isEmpty())
    }
}
