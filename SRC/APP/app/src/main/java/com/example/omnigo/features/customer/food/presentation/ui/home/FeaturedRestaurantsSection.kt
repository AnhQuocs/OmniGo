package com.example.omnigo.features.customer.food.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.WarningColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16

@Composable
fun FeaturedRestaurantsSection(
    featuredRestaurants: List<Restaurant>,
    distancesKm: Map<Long, Double>,
    onRestaurantClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (featuredRestaurants.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.food_section_featured),
                style = MaterialTheme.typography.s16.bold(),
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.S))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = Dimen.PaddingM),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.M)
        ) {
            items(featuredRestaurants, key = { it.id }) { restaurant ->
                FeaturedRestaurantMiniCard(
                    restaurant = restaurant,
                    distanceKm = distancesKm[restaurant.id],
                    onRestaurantClick = onRestaurantClick
                )
            }
        }
    }
}

@Composable
private fun FeaturedRestaurantMiniCard(
    restaurant: Restaurant,
    distanceKm: Double?,
    onRestaurantClick: (Long) -> Unit
) {
    ElevatedCard(
        onClick = { onRestaurantClick(restaurant.id) },
        modifier = Modifier.width(Dimen.WidthPromoBanner),
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
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingM)
            ) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(AppSpacing.XXS))

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

                    if (distanceKm != null) {
                        Spacer(modifier = Modifier.width(AppSpacing.XS))
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XS))
                        Text(
                            text = stringResource(id = R.string.food_distance_km, distanceKm),
                            style = MaterialTheme.typography.s12.normal(),
                            color = PrimaryColor
                        )
                    }
                }
            }
        }
    }
}
