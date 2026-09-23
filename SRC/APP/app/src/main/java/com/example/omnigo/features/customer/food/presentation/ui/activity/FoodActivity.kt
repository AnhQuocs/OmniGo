package com.example.omnigo.features.customer.food.presentation.ui.activity

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import com.example.omnigo.BaseComponentActivity
import com.example.omnigo.features.customer.food.presentation.ui.FoodHomeScreen
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
            FoodHomeScreen(
                onBackClick = { finish() },
                onRestaurantClick = { restaurantId ->
                    // Navigate to Restaurant Detail
                }
            )
        }
    }
}
