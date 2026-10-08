package com.example.omnigo.features.customer.food.data.repository



import com.example.omnigo.features.customer.food.data.mapper.toDomain
import com.example.omnigo.features.customer.food.data.mapper.toRequest
import com.example.omnigo.features.customer.food.data.remote.api.FoodApi
import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.CreateFoodOrderResult
import com.example.omnigo.features.customer.food.domain.model.FoodOrderCreateCommand
import com.example.omnigo.features.customer.food.domain.model.GetDriverProfileResult

import com.example.omnigo.features.customer.food.domain.model.GetFoodOrderDetailResult

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



    override suspend fun createFoodOrder(command: FoodOrderCreateCommand): CreateFoodOrderResult {
        return try {
            val response = foodApi.createFoodOrder(command.toRequest())

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



    override suspend fun getFoodOrderDetail(orderId: Long): GetFoodOrderDetailResult {

        return try {

            val response = foodApi.getFoodOrderDetail(orderId)

            if (response.success && response.data != null) {

                GetFoodOrderDetailResult.Success(response.data.toDomain())

            } else {

                GetFoodOrderDetailResult.Error(

                    error = FoodError.SERVER_ERROR,

                    message = response.message

                )

            }

        } catch (e: IOException) {

            GetFoodOrderDetailResult.Error(FoodError.NETWORK_ERROR, e.message)

        } catch (e: Exception) {
            GetFoodOrderDetailResult.Error(FoodError.UNKNOWN_ERROR, e.message)
        }
    }

    override suspend fun getDriverProfile(driverId: Long): GetDriverProfileResult {
        return try {
            val response = foodApi.getDriverProfile(driverId)
            if (response.success && response.data != null) {
                GetDriverProfileResult.Success(response.data.toDomain(driverId))
            } else {
                GetDriverProfileResult.Error(FoodError.SERVER_ERROR, response.message)
            }
        } catch (e: IOException) {
            GetDriverProfileResult.Error(FoodError.NETWORK_ERROR, e.message)
        } catch (e: Exception) {
            GetDriverProfileResult.Error(FoodError.UNKNOWN_ERROR, e.message)
        }
    }
}
