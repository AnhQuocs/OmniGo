package com.example.omnigo.features.customer.food.presentation.ui.activity

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.omnigo.BaseComponentActivity
import com.example.omnigo.features.customer.food.presentation.ui.checkout.FoodCheckoutScreen
import com.example.omnigo.features.customer.food.presentation.ui.detail.RestaurantDetailScreen
import com.example.omnigo.features.customer.food.presentation.ui.home.FoodHomeScreen
import com.example.omnigo.features.customer.food.presentation.ui.order.FoodOrderDetailScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FoodActivity : BaseComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)

        setOmniGoContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "food_home"
            ) {
                composable("food_home") {
                    FoodHomeScreen(
                        onBackClick = { finish() },
                        onRestaurantClick = { restaurantId ->
                            navController.navigate("restaurant_detail/$restaurantId")
                        }
                    )
                }

                composable(
                    route = "restaurant_detail/{restaurantId}",
                    arguments = listOf(
                        navArgument("restaurantId") { type = NavType.LongType }
                    )
                ) { backStackEntry ->
                    val restaurantId = backStackEntry.arguments?.getLong("restaurantId") ?: 0L
                    RestaurantDetailScreen(
                        restaurantId = restaurantId,
                        onBackClick = { navController.popBackStack() },
                        onCheckoutClick = {
                            navController.navigate("food_checkout")
                        }
                    )
                }

                composable("food_checkout") {
                    FoodCheckoutScreen(
                        onBackClick = { navController.popBackStack() },
                        onOrderCreated = { orderId ->
                            navController.navigate("food_order_detail/$orderId") {
                                popUpTo("food_home") { inclusive = false }
                            }
                        }
                    )
                }

                composable(
                    route = "food_order_detail/{orderId}",
                    arguments = listOf(
                        navArgument("orderId") { type = NavType.LongType }
                    )
                ) {
                    FoodOrderDetailScreen(
                        onBackClick = {
                            navController.popBackStack("food_home", inclusive = false)
                        }
                    )
                }
            }
        }
    }
}
