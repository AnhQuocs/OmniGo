package com.example.omnigo.features.customer.food.domain.model



import com.example.omnigo.features.customer.food.domain.error.FoodError



sealed interface GetRestaurantsResult {

    data class Success(

        val restaurants: List<Restaurant>

    ) : GetRestaurantsResult



    data class Error(

        val error: FoodError

    ) : GetRestaurantsResult

}



sealed interface GetRestaurantDetailResult {

    data class Success(

        val restaurant: Restaurant

    ) : GetRestaurantDetailResult



    data class Error(

        val error: FoodError

    ) : GetRestaurantDetailResult

}



sealed interface GetMenuItemsResult {

    data class Success(

        val items: List<MenuItem>

    ) : GetMenuItemsResult



    data class Error(

        val error: FoodError

    ) : GetMenuItemsResult

}



sealed interface CreateFoodOrderResult {

    data class Success(

        val order: FoodOrder

    ) : CreateFoodOrderResult



    data class Error(

        val error: FoodError,

        val message: String? = null

    ) : CreateFoodOrderResult

}



sealed interface GetFoodOrderDetailResult {

    data class Success(

        val order: FoodOrder

    ) : GetFoodOrderDetailResult



    data class Error(

        val error: FoodError,

        val message: String? = null

    ) : GetFoodOrderDetailResult

}

sealed interface GetDriverProfileResult {
    data class Success(
        val profile: DriverProfile
    ) : GetDriverProfileResult

    data class Error(
        val error: FoodError,
        val message: String? = null
    ) : GetDriverProfileResult
}

sealed interface GetMyFoodOrdersResult {
    data class Success(
        val orders: List<FoodOrder>
    ) : GetMyFoodOrdersResult

    data class Error(
        val error: FoodError,
        val message: String? = null
    ) : GetMyFoodOrdersResult
}

sealed interface CancelFoodOrderResult {
    data class Success(
        val order: FoodOrder
    ) : CancelFoodOrderResult

    data class Error(
        val error: FoodError,
        val message: String? = null
    ) : CancelFoodOrderResult
}

sealed interface SwitchToCashResult {
    data class Success(
        val order: FoodOrder
    ) : SwitchToCashResult

    data class Error(
        val error: FoodError,
        val message: String? = null
    ) : SwitchToCashResult
}

sealed interface RetryDriverResult {
    data class Success(
        val order: FoodOrder
    ) : RetryDriverResult

    data class Error(
        val error: FoodError,
        val message: String? = null
    ) : RetryDriverResult
}
