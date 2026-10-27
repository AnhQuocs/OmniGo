package com.example.omnigo.features.customer.food.presentation.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.model.RestaurantAvailability
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.ErrorContainer
import com.example.omnigo.ui.theme.OnErrorContainer
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.ui.theme.WarningColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s18

@Composable
fun RestaurantDetailHeaderSection(
    restaurant: Restaurant,
    distanceKm: Double?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceLight)
    ) {
        // Hero Image with top back button overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimen.HeightHeroBanner)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(restaurant.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = restaurant.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(Dimen.SizeXLPlus)
                        .clip(CircleShape)
                        .background(SurfaceLight.copy(alpha = 0.9f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.food_cd_back),
                        tint = TextPrimary,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                }
            }
        }

        // Restaurant Information Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.s18.bold(),
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(AppSpacing.S))

                val (statusColor, statusText) = when (restaurant.availability) {
                    RestaurantAvailability.LOCKED -> WarningColor to stringResource(id = R.string.food_status_locked)
                    RestaurantAvailability.CLOSED -> ErrorColor to stringResource(id = R.string.restaurant_detail_status_closed)
                    RestaurantAvailability.OPEN -> SuccessColor to stringResource(id = R.string.restaurant_detail_status_open)
                }

                Surface(
                    shape = RoundedCornerShape(AppShape.ShapeXS),
                    color = statusColor
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.s10.bold(),
                        color = TextWhite,
                        modifier = Modifier.padding(horizontal = AppSpacing.S, vertical = AppSpacing.XXS)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.S))

            // Rating & Distance Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (restaurant.hasReviews) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = WarningColor,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XXS))
                    Text(
                        text = stringResource(
                            id = R.string.restaurant_detail_rating,
                            restaurant.rating,
                            restaurant.reviewCount
                        ),
                        style = MaterialTheme.typography.s12.bold(),
                        color = TextPrimary
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.food_no_reviews),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }

                if (distanceKm != null) {
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.s12.bold(),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    Text(
                        text = stringResource(id = R.string.restaurant_menu_distance, distanceKm),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextSecondary
                    )
                }
            }

            if (restaurant.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.XS))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XXS))
                    Text(
                        text = restaurant.address,
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (restaurant.openTime.isNotBlank() && restaurant.closeTime.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XXS))
                    Text(
                        text = stringResource(
                            id = R.string.restaurant_menu_hours,
                            restaurant.openTime,
                            restaurant.closeTime
                        ),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }
            }

            // Notice Banner when not open
            if (!restaurant.canOrder) {
                Spacer(modifier = Modifier.height(AppSpacing.M))
                val isLocked = restaurant.availability == RestaurantAvailability.LOCKED
                val bannerColor = if (isLocked) SecondaryContainer else ErrorContainer
                val textColor = if (isLocked) WarningColor else OnErrorContainer
                val bannerText = when {
                    isLocked && restaurant.lockedReason.isNotBlank() ->
                        stringResource(id = R.string.restaurant_menu_banner_locked_reason, restaurant.lockedReason)
                    isLocked ->
                        stringResource(id = R.string.restaurant_menu_banner_locked)
                    else ->
                        stringResource(id = R.string.restaurant_menu_banner_closed)
                }

                Surface(
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    color = bannerColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Dimen.PaddingM),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(Dimen.SizeM)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.S))
                        Text(
                            text = bannerText,
                            style = MaterialTheme.typography.s13.normal(),
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
