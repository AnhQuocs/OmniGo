package com.example.omnigo.features.main.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.omnigo.features.main.presentation.navigation.BottomNavDestination
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12

@Composable
fun OmniBottomNavBar(
    currentRoute: String?,
    onNavigateToDestination: (BottomNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(
                    topStart = AppShape.ShapeL,
                    topEnd = AppShape.ShapeL
                ),
                spotColor = CardBorderColor
            ),
        color = SurfaceLight,
        shape = RoundedCornerShape(
            topStart = AppShape.ShapeL,
            topEnd = AppShape.ShapeL
        )
    ) {
        CompositionLocalProvider(LocalRippleConfiguration provides null) {
            NavigationBar(
                containerColor = SurfaceLight,
                tonalElevation = 0.dp,
                modifier = Modifier.height(68.dp)
            ) {
                BottomNavDestination.items.forEach { destination ->
                    val isSelected = currentRoute == destination.route

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) PrimaryColor else TextSecondary,
                        animationSpec = tween(durationMillis = 200),
                        label = "BottomNavIconColor"
                    )

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                onNavigateToDestination(destination)
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = destination.iconRes),
                                contentDescription = stringResource(id = destination.titleRes),
                                tint = iconColor,
                                modifier = Modifier.size(Dimen.SizeM)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(id = destination.titleRes),
                                style = if (isSelected) {
                                    MaterialTheme.typography.s12.bold()
                                } else {
                                    MaterialTheme.typography.s12.normal()
                                },
                                color = if (isSelected) PrimaryColor else TextSecondary,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryColor,
                            selectedTextColor = PrimaryColor,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}
