package com.example.omnigo.features.customer.food.presentation.ui.activity

import android.content.Context
import android.content.Intent
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
import com.example.omnigo.features.customer.food.presentation.ui.history.FoodOrderHistoryScreen
import com.example.omnigo.features.customer.food.presentation.ui.home.FoodHomeScreen
import com.example.omnigo.features.customer.food.presentation.ui.order.FoodOrderDetailScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FoodActivity : BaseComponentActivity() {

    companion object {
        const val EXTRA_RESTAURANT_ID = "extra_restaurant_id"
        const val EXTRA_ORDER_ID = "extra_order_id"

        fun createOrderDetailIntent(context: Context, orderId: Long): Intent {
            return Intent(context, FoodActivity::class.java).apply {
                putExtra(EXTRA_ORDER_ID, orderId)
            }
        }
    }

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
            val initialOrderId = intent.getLongExtra(EXTRA_ORDER_ID, -1L)
            val initialRestaurantId = intent.getLongExtra(EXTRA_RESTAURANT_ID, -1L)
            val startDestination = when {
                initialOrderId > 0L -> "food_order_detail/$initialOrderId"
                initialRestaurantId > 0L -> "restaurant_detail/$initialRestaurantId"
                else -> "food_home"
            }

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable("food_home") {
                    FoodHomeScreen(
                        onBackClick = { finish() },
                        onRestaurantClick = { restaurantId ->
                            navController.navigate("restaurant_detail/$restaurantId")
                        },
                        onHistoryClick = {
                            navController.navigate("food_order_history")
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
                        onBackClick = {
                            if (!navController.popBackStack()) {
                                finish()
                            }
                        },
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

                composable("food_order_history") {
                    FoodOrderHistoryScreen(
                        onBackClick = {
                            if (!navController.popBackStack()) {
                                finish()
                            }
                        },
                        onOrderClick = { orderId ->
                            navController.navigate("food_order_detail/$orderId")
                        },
                        onReorderClick = { order ->
                            navController.navigate("restaurant_detail/${order.restaurantId}")
                        },
                        onReviewClick = { order ->
                            navController.navigate("food_order_review/${order.id}")
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
                            if (!navController.popBackStack()) {
                                finish()
                            }
                        },
                        onReorderClick = { order ->
                            navController.navigate("restaurant_detail/${order.restaurantId}")
                        },
                        onReviewClick = { order ->
                            navController.navigate("food_order_review/${order.id}")
                        }
                    )
                }

                composable(
                    route = "food_order_review/{orderId}",
                    arguments = listOf(
                        navArgument("orderId") { type = NavType.LongType }
                    )
                ) {
                    com.example.omnigo.features.customer.food.presentation.ui.review.FoodReviewScreen(
                        onBackClick = {
                            if (!navController.popBackStack()) {
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }
}
