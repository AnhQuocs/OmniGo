package com.example.omnigo.features.customer.food.data.repository

import com.example.omnigo.features.customer.food.data.mapper.toDomain
import com.example.omnigo.features.customer.food.data.remote.api.FoodApi
import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantsResult
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import java.io.IOException
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val foodApi: FoodApi
) : FoodRepository {

    override suspend fun getRestaurants(search: String?): GetRestaurantsResult {
        return try {
            val response = foodApi.getRestaurants(search = search)
            if (response.success && response.data != null) {
                GetRestaurantsResult.Success(response.data.map { it.toDomain() })
            } else {
                GetRestaurantsResult.Error(FoodError.SERVER_ERROR)
            }
        } catch (e: IOException) {
            GetRestaurantsResult.Error(FoodError.NETWORK_ERROR)
        } catch (e: Exception) {
            GetRestaurantsResult.Error(FoodError.UNKNOWN_ERROR)
        }
    }
}
