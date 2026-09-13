package com.example.omnigo.features.main.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.omnigo.R

sealed class DriverBottomNavDestination(
    val route: String,
    @get:StringRes val titleRes: Int,
    @get:DrawableRes val iconRes: Int
) {
    data object Home : DriverBottomNavDestination(
        route = "driver_nav_home",
        titleRes = R.string.driver_nav_home,
        iconRes = R.drawable.ic_home
    )

    data object Trips : DriverBottomNavDestination(
        route = "driver_nav_trips",
        titleRes = R.string.driver_nav_trips,
        iconRes = R.drawable.ic_activity
    )

    data object Earnings : DriverBottomNavDestination(
        route = "driver_nav_earnings",
        titleRes = R.string.driver_nav_earnings,
        iconRes = R.drawable.ic_wallet
    )

    data object Account : DriverBottomNavDestination(
        route = "driver_nav_account",
        titleRes = R.string.driver_nav_account,
        iconRes = R.drawable.ic_account
    )

    companion object {
        val items: List<DriverBottomNavDestination>
            get() = listOf(Home, Trips, Earnings, Account)
    }
}
