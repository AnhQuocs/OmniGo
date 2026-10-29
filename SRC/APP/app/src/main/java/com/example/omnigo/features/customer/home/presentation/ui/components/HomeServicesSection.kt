package com.example.omnigo.features.customer.home.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.ui.theme.WarningColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s16

@Composable
fun HomeServicesGridSection(
    onServiceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingM)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.home_section_services),
                style = MaterialTheme.typography.s16.bold(),
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.M))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.M)
        ) {
            HeroServiceCard(
                title = stringResource(id = R.string.home_service_ride),
                subtitle = stringResource(id = R.string.home_service_ride_desc),
                badge = stringResource(id = R.string.home_badge_50off),
                icon = Icons.Filled.DirectionsCar,
                containerColor = PrimaryContainer,
                iconColor = PrimaryColor,
                badgeColor = WarningColor,
                onClick = { onServiceClick("ride") },
                modifier = Modifier.weight(1f)
            )

            HeroServiceCard(
                title = stringResource(id = R.string.home_service_food),
                subtitle = stringResource(id = R.string.home_service_food_desc),
                badge = stringResource(id = R.string.home_badge_freeship),
                icon = Icons.Filled.Restaurant,
                containerColor = SecondaryContainer,
                iconColor = SecondaryColor,
                badgeColor = SecondaryColor,
                onClick = { onServiceClick("food") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HeroServiceCard(
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(AppShape.ShapeL))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppShape.ShapeL),
        color = SurfaceLight,
        border = androidx.compose.foundation.BorderStroke(Dimen.PaddingXXS, CardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeXXL)
                        .clip(CircleShape)
                        .background(containerColor)
                        .border(Dimen.PaddingXXS, containerColor.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(Dimen.SizeL)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(AppShape.ShapeXXS),
                    color = badgeColor
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.s10.bold(),
                        color = TextWhite,
                        modifier = Modifier.padding(horizontal = Dimen.PaddingXSPlus, vertical = Dimen.PaddingXXS)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Text(
                text = title,
                style = MaterialTheme.typography.s16.bold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.XXS))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(Dimen.SizeS2)
                )
            }
        }
    }
}
