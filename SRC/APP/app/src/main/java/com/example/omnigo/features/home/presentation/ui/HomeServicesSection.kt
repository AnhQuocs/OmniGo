package com.example.omnigo.features.home.presentation.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalShipping
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
import androidx.compose.ui.unit.dp
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.ui.theme.WarningColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold

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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ServiceItem(
                title = stringResource(id = R.string.home_service_ride),
                badge = stringResource(id = R.string.home_badge_50off),
                icon = Icons.Filled.DirectionsCar,
                containerColor = PrimaryContainer,
                iconColor = PrimaryColor,
                badgeColor = WarningColor,
                onClick = { onServiceClick("ride") }
            )

            ServiceItem(
                title = stringResource(id = R.string.home_service_food),
                badge = stringResource(id = R.string.home_badge_freeship),
                icon = Icons.Filled.Restaurant,
                containerColor = SecondaryContainer,
                iconColor = SecondaryColor,
                badgeColor = SecondaryColor,
                onClick = { onServiceClick("food") }
            )

            ServiceItem(
                title = stringResource(id = R.string.home_service_mart),
                badge = stringResource(id = R.string.home_badge_fast),
                icon = Icons.Filled.LocalMall,
                containerColor = Color(0xFFDCFCE7),
                iconColor = SuccessColor,
                badgeColor = SuccessColor,
                onClick = { onServiceClick("mart") }
            )

            ServiceItem(
                title = stringResource(id = R.string.home_service_express),
                badge = stringResource(id = R.string.home_badge_express),
                icon = Icons.Filled.LocalShipping,
                containerColor = Color(0xFFF3E8FF),
                iconColor = Color(0xFF9333EA),
                badgeColor = Color(0xFF9333EA),
                onClick = { onServiceClick("express") }
            )
        }
    }
}

@Composable
private fun ServiceItem(
    title: String,
    badge: String,
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(AppShape.ShapeL))
                    .background(containerColor)
                    .border(1.dp, containerColor.copy(alpha = 0.5f), RoundedCornerShape(AppShape.ShapeL)),
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
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.XSPlus))

        Text(
            text = title,
            style = MaterialTheme.typography.s12.semiBold(),
            color = TextPrimary
        )
    }
}
