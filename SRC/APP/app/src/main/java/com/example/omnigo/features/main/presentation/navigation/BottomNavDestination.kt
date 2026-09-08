package com.example.omnigo.features.main.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.omnigo.R

sealed class BottomNavDestination(
    val route: String,
    @param:StringRes val titleRes: Int,
    @param:DrawableRes val iconRes: Int
) {
    data object Home : BottomNavDestination(
        route = "nav_home",
        titleRes = R.string.nav_home,
        iconRes = R.drawable.ic_home
    )

    data object Activity : BottomNavDestination(
        route = "nav_activity",
        titleRes = R.string.nav_activity,
        iconRes = R.drawable.ic_activity
    )

    data object Promos : BottomNavDestination(
        route = "nav_promos",
        titleRes = R.string.nav_promos,
        iconRes = R.drawable.ic_discount
    )

    data object Account : BottomNavDestination(
        route = "nav_account",
        titleRes = R.string.nav_account,
        iconRes = R.drawable.ic_account
    )

    companion object {
        val items: List<BottomNavDestination>
            get() = listOf(Home, Activity, Promos, Account)
    }
}
