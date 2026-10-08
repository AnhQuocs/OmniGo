package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.CartItem
import com.example.omnigo.features.customer.food.domain.model.CreateFoodOrderResult
import com.example.omnigo.features.customer.food.domain.model.FoodOrderCreateCommand
import com.example.omnigo.features.customer.food.domain.model.FoodOrderCreateItemCommand
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import javax.inject.Inject

class CreateFoodOrderUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(
        restaurantId: Long,
        dropOffAddress: String,
        dropOffLatitude: Double,
        dropOffLongitude: Double,
        items: List<CartItem>,
        note: String? = null,
        paymentMethod: PaymentMethod = PaymentMethod.CASH
    ): CreateFoodOrderResult {
        if (items.isEmpty()) {
            return CreateFoodOrderResult.Error(
                error = FoodError.UNKNOWN_ERROR,
                message = "Cart is empty"
            )
        }
        if (dropOffAddress.isBlank()) {
            return CreateFoodOrderResult.Error(
                error = FoodError.UNKNOWN_ERROR,
                message = "Delivery address is required"
            )
        }

        val command = FoodOrderCreateCommand(
            restaurantId = restaurantId,
            dropOffAddress = dropOffAddress.trim(),
            dropOffLatitude = dropOffLatitude,
            dropOffLongitude = dropOffLongitude,
            items = items.map {
                FoodOrderCreateItemCommand(
                    menuItemId = it.menuItem.id,
                    quantity = it.quantity,
                    note = it.note.ifBlank { null }
                )
            },
            note = note?.trim()?.ifBlank { null },
            paymentMethod = paymentMethod
        )

        return repository.createFoodOrder(command)
    }
}
