package com.example.omnigo.features.driver.earnings.data.mapper

import com.example.omnigo.features.driver.earnings.data.remote.dto.TransactionResponseDto
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import org.junit.Assert.*
import org.junit.Test

class VoucherSettlementDisplayTest {
    @Test fun foodIncomeAndVoucherReimbursementAreCredits() {
        for (reference in listOf("FOOD_INC_99_SETTLED", "FOOD_VOUCHER_99")) {
            val transaction = DriverPaymentMapper.mapToDomain(TransactionResponseDto(
                transactionType = "FOOD_DELIVERY_INCOME", orderId = reference, bookingId = 99, amount = 11520.0))
            assertTrue(transaction.isIncome)
            assertTrue(transaction.amountFormatted.startsWith("+"))
            if (reference.contains("VOUCHER")) assertTrue(transaction.description.contains("bù voucher"))
        }
    }
    @Test fun zeroDeliveryFeeNeverTurnsFoodAmountIntoDriverIncome() {
        val order = DriverDeliveryOrder(orderId = 99, deliveryFee = 0.0, totalPrice = 65000.0)
        assertEquals(0.0, order.grossFee, 0.001)
        assertEquals(0.0, order.actualEarnings, 0.001)
        assertEquals(65000.0, order.cashAmountToCollect, 0.001)
    }
    @Test fun backendNetTotalIsNotDiscountedAgainIncludingFreeOrders() {
        val order = FoodOrder(id = 99, restaurantId = 1, restaurantName = "Test", totalPrice = 132480.0,
            originalAmount = 144000.0, voucherDiscount = 11520.0)
        assertEquals(132480.0, order.payableTotal, 0.001)
        assertEquals(0.0, order.copy(totalPrice = 0.0, voucherDiscount = 144000.0).payableTotal, 0.001)
    }
}
