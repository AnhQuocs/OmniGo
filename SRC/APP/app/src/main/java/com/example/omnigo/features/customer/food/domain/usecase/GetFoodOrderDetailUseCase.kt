package com.example.omnigo.features.customer.food.domain.usecase



import com.example.omnigo.features.customer.food.domain.error.FoodError

import com.example.omnigo.features.customer.food.domain.model.GetFoodOrderDetailResult

import com.example.omnigo.features.customer.food.domain.repository.FoodRepository

import javax.inject.Inject



class GetFoodOrderDetailUseCase @Inject constructor(

    private val repository: FoodRepository

) {

    suspend operator fun invoke(orderId: Long): GetFoodOrderDetailResult {

        if (orderId <= 0) {

            return GetFoodOrderDetailResult.Error(

                error = FoodError.UNKNOWN_ERROR,

                message = "Invalid order ID"

            )

        }

        return repository.getFoodOrderDetail(orderId)

    }

}

