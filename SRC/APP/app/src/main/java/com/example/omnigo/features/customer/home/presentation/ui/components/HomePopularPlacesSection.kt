package com.example.omnigo.features.customer.home.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.omnigo.R
import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.util.RestaurantDeliveryEtaHelper
import com.example.omnigo.features.customer.food.domain.util.RestaurantPriceRangeHelper
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s16
import java.util.Locale

@Composable
fun HomePopularPlacesSection(
    popularRestaurants: List<Restaurant>,
    onPlaceClick: (String) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier,
    userLocation: UserLocation? = null
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
                text = stringResource(id = R.string.home_section_popular),
                style = MaterialTheme.typography.s16.bold(),
                color = TextPrimary
            )

            Text(
                text = stringResource(id = R.string.home_view_all),
                style = MaterialTheme.typography.s12.bold(),
                color = PrimaryColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(AppShape.ShapeXS))
                    .clickable { onViewAllClick() }
                    .padding(horizontal = AppSpacing.XS, vertical = AppSpacing.XXS)
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.M))

        popularRestaurants.forEach { restaurant ->
            PopularPlaceItem(
                restaurant = restaurant,
                userLocation = userLocation,
                onClick = { onPlaceClick(restaurant.id.toString()) }
            )
            Spacer(modifier = Modifier.height(AppSpacing.SPlus))
        }
    }
}

@Composable
private fun PopularPlaceItem(
    restaurant: Restaurant,
    userLocation: UserLocation?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val desc = restaurant.menuItems.firstOrNull()?.name ?: restaurant.address

    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXXS),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.ShapeL))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingSM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizePopularPlaceImage)
                    .clip(RoundedCornerShape(AppShape.ShapeM))
            ) {
                AsyncImage(
                    model = restaurant.imageUrl,
                    contentDescription = restaurant.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Dimen.PaddingXSPlus)
                        .size(Dimen.SizeML)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = SurfaceLight,
                    shadowElevation = Dimen.PaddingXXS,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(Dimen.PaddingXSPlus)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = Dimen.PaddingS,
                            vertical = Dimen.PaddingXXS
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(Dimen.PaddingXSPlus)
                                .background(SuccessColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XS))
                        Text(
                            text = stringResource(id = R.string.home_popular_status_open),
                            style = MaterialTheme.typography.s10.bold(),
                            color = SuccessColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.MediumLarge))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.s15.bold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(AppSpacing.XSPlus))

                    Row(
                        modifier = Modifier
                            .background(
                                color = SecondaryContainer,
                                shape = RoundedCornerShape(AppShape.ShapeXXS)
                            )
                            .padding(
                                horizontal = Dimen.PaddingXSPlus,
                                vertical = Dimen.PaddingXXS
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = SecondaryColor,
                            modifier = Modifier.size(Dimen.PaddingSM)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XXS))
                        Text(
                            text = String.format(Locale.US, "%.1f", restaurant.rating),
                            style = MaterialTheme.typography.s12.bold(),
                            color = SecondaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.XXS))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(AppSpacing.XSPlus))

                    Text(
                        text = stringResource(
                            id = R.string.rating_count,
                            formatReviewCount(restaurant.reviewCount)
                        ),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.XS)
                    ) {
                        PopularPlaceInfo(
                            icon = Icons.Default.Place,
                            value = restaurant.address
                        )
                        PopularPlaceInfo(
                            icon = Icons.Default.TwoWheeler,
                            value = RestaurantDeliveryEtaHelper.formatEta(
                                restaurantLat = restaurant.latitude,
                                restaurantLon = restaurant.longitude,
                                userLat = userLocation?.latitude,
                                userLon = userLocation?.longitude
                            )
                        )
                        PopularPlaceInfo(
                            icon = Icons.Default.LocalOffer,
                            value = RestaurantPriceRangeHelper.formatPriceRange(restaurant.menuItems)
                        )
                    }

                    Spacer(modifier = Modifier.width(AppSpacing.XSPlus))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                }
            }
        }
    }
}

@Composable
private fun PopularPlaceInfo(
    icon: ImageVector,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimen.SizeSM)
                .background(color = SurfaceVariantLight, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(Dimen.PaddingSM)
            )
        }

        Spacer(modifier = Modifier.width(AppSpacing.S))

        Text(
            text = value,
            style = MaterialTheme.typography.s12.normal(),
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatReviewCount(count: Int): String {
    return if (count >= 1000) {
        String.format(Locale.US, "%.1fK", count / 1000.0)
    } else {
        count.toString()
    }
}
