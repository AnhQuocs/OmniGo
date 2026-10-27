package com.example.omnigo.features.customer.food.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
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
import com.example.omnigo.ui.theme.PrimaryColor
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
import com.example.omnigo.utils.s16

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onRestaurantClick: (Long) -> Unit,
    distanceKm: Double? = null,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = { onRestaurantClick(restaurant.id) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = AppSpacing.XXS)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightPromoBanner)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(restaurant.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(
                        id = R.string.food_cd_restaurant_image,
                        restaurant.name
                    ),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightPromoBanner)
                        .clip(RoundedCornerShape(topStart = AppShape.ShapeL, topEnd = AppShape.ShapeL))
                )

                val (statusColor, statusText) = when (restaurant.availability) {
                    RestaurantAvailability.LOCKED -> WarningColor to stringResource(id = R.string.food_status_locked)
                    RestaurantAvailability.CLOSED -> ErrorColor to stringResource(id = R.string.food_closed_status)
                    RestaurantAvailability.OPEN -> {
                        val text = if (restaurant.openTime.isNotBlank() && restaurant.closeTime.isNotBlank()) {
                            stringResource(id = R.string.food_status_open_hours, restaurant.openTime, restaurant.closeTime)
                        } else {
                            stringResource(id = R.string.food_open_status)
                        }
                        SuccessColor to text
                    }
                }

                Surface(
                    shape = RoundedCornerShape(AppShape.ShapeXS),
                    color = statusColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Dimen.PaddingS)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.s10.bold(),
                        color = TextWhite,
                        modifier = Modifier.padding(horizontal = AppSpacing.XSPlus, vertical = AppSpacing.XXS)
                    )
                }
            }

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
                        style = MaterialTheme.typography.s16.bold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(AppSpacing.S))

                    if (restaurant.hasReviews) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = WarningColor,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.XXS))
                            Text(
                                text = stringResource(
                                    id = R.string.food_rating_compact,
                                    restaurant.rating,
                                    restaurant.reviewCount
                                ),
                                style = MaterialTheme.typography.s12.bold(),
                                color = TextPrimary
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(id = R.string.food_no_reviews),
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.XS))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (distanceKm != null) {
                        Text(
                            text = stringResource(id = R.string.food_distance_km, distanceKm),
                            style = MaterialTheme.typography.s12.medium(),
                            color = PrimaryColor
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XS))
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.s12.medium(),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XS))
                    }

                    if (restaurant.address.isNotEmpty()) {
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

                if (restaurant.openTime.isNotEmpty() && restaurant.closeTime.isNotEmpty()) {
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
                            text = "${restaurant.openTime} - ${restaurant.closeTime}",
                            style = MaterialTheme.typography.s12.medium(),
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
