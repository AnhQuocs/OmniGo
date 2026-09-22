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
import com.example.omnigo.features.customer.food.presentation.ui.FoodHomeScreen
import com.example.omnigo.features.customer.food.presentation.ui.RestaurantDetailScreen
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
                            // Will navigate to checkout screen
                        }
                    )
                }
            }
        }
    }
}
