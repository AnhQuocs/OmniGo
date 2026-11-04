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
import com.example.omnigo.features.customer.food.domain.manager.CartManager
import com.example.omnigo.features.customer.food.presentation.ui.checkout.FoodCheckoutScreen
import com.example.omnigo.features.customer.food.presentation.ui.detail.RestaurantDetailScreen
import com.example.omnigo.features.customer.food.presentation.ui.history.FoodOrderHistoryScreen
import com.example.omnigo.features.customer.food.presentation.ui.home.FoodHomeScreen
import com.example.omnigo.features.customer.food.presentation.ui.order.FoodOrderDetailScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FoodActivity : BaseComponentActivity() {

    @Inject
    lateinit var cartManager: CartManager

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
                            navController.navigate("restaurant_detail/$restaurantId") {
                                launchSingleTop = true
                            }
                        },
                        onCheckoutClick = {
                            navController.navigate("food_checkout") {
                                launchSingleTop = true
                            }
                        },
                        onHistoryClick = {
                            navController.navigate("food_order_history") {
                                launchSingleTop = true
                            }
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
                            navController.navigate("food_checkout") {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("food_checkout") {
                    FoodCheckoutScreen(
                        onBackClick = { navController.popBackStack() },
                        onOrderCreated = { orderId ->
                            navController.navigate("food_order_detail/$orderId") {
                                val hasHome = runCatching { navController.getBackStackEntry("food_home") }.isSuccess
                                if (hasHome) {
                                    popUpTo("food_home") { inclusive = false }
                                } else {
                                    val hasRestaurant = runCatching { navController.getBackStackEntry("restaurant_detail/{restaurantId}") }.isSuccess
                                    if (hasRestaurant) {
                                        popUpTo("restaurant_detail/{restaurantId}") { inclusive = true }
                                    } else {
                                        popUpTo("food_checkout") { inclusive = true }
                                    }
                                }
                                launchSingleTop = true
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
                            navController.navigate("food_order_detail/$orderId") {
                                launchSingleTop = true
                            }
                        },
                        onReorderClick = { order ->
                            cartManager.restoreCartFromOrder(order)
                            navController.navigate("restaurant_detail/${order.restaurantId}") {
                                launchSingleTop = true
                            }
                        },
                        onReviewClick = { order ->
                            navController.navigate("food_order_review/${order.id}") {
                                launchSingleTop = true
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
                            if (!navController.popBackStack()) {
                                finish()
                            }
                        },
                        onReorderClick = { order ->
                            cartManager.restoreCartFromOrder(order)
                            navController.navigate("restaurant_detail/${order.restaurantId}") {
                                popUpTo("food_order_detail/{orderId}") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onReviewClick = { order ->
                            navController.navigate("food_order_review/${order.id}") {
                                launchSingleTop = true
                            }
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
