package com.example.omnigo.features.customer.food.data.repository

import com.example.omnigo.features.customer.food.data.mapper.toDomain
import com.example.omnigo.features.customer.food.data.remote.api.FoodApi
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderRequest
import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.CreateFoodOrderResult
import com.example.omnigo.features.customer.food.domain.model.GetMenuItemsResult
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
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

    override suspend fun getRestaurantDetail(id: Long): GetRestaurantDetailResult {
        return try {
            val response = foodApi.getRestaurantDetail(id)
            if (response.success && response.data != null) {
                GetRestaurantDetailResult.Success(response.data.toDomain())
            } else {
                GetRestaurantDetailResult.Error(FoodError.SERVER_ERROR)
            }
        } catch (e: IOException) {
            GetRestaurantDetailResult.Error(FoodError.NETWORK_ERROR)
        } catch (e: Exception) {
            GetRestaurantDetailResult.Error(FoodError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getRestaurantItems(
        restaurantId: Long,
        availableOnly: Boolean
    ): GetMenuItemsResult {
        return try {
            val response = foodApi.getRestaurantItems(restaurantId, availableOnly)
            if (response.success && response.data != null) {
                GetMenuItemsResult.Success(response.data.map { it.toDomain() })
            } else {
                GetMenuItemsResult.Error(FoodError.SERVER_ERROR)
            }
        } catch (e: IOException) {
            GetMenuItemsResult.Error(FoodError.NETWORK_ERROR)
        } catch (e: Exception) {
            GetMenuItemsResult.Error(FoodError.UNKNOWN_ERROR)
        }
    }

    override suspend fun createFoodOrder(request: FoodOrderRequest): CreateFoodOrderResult {
        return try {
            val response = foodApi.createFoodOrder(request)
            if (response.success && response.data != null) {
                CreateFoodOrderResult.Success(response.data.toDomain())
            } else {
                CreateFoodOrderResult.Error(
                    error = FoodError.SERVER_ERROR,
                    message = response.message
                )
            }
        } catch (e: IOException) {
            CreateFoodOrderResult.Error(FoodError.NETWORK_ERROR, e.message)
        } catch (e: Exception) {
            CreateFoodOrderResult.Error(FoodError.UNKNOWN_ERROR, e.message)
        }
    }
}
