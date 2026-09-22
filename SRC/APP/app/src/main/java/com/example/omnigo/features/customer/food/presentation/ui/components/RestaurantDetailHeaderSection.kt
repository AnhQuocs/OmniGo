package com.example.omnigo.features.customer.food.presentation.ui.components

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
import androidx.compose.material.icons.filled.BookmarkBorder
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
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
import com.example.omnigo.utils.s20

@Composable
fun RestaurantDetailHeaderSection(
    restaurant: Restaurant,
    distanceKm: Double?,
    estimatedMinutes: Int?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceLight)
    ) {
        // Hero Image with top action buttons overlay
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

            // Gradient scrim for top bar visibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )

            // Top action buttons (Back & Bookmark)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(Dimen.SizeXLPlus)
                        .clip(CircleShape)
                        .background(SurfaceLight.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                }

                IconButton(
                    onClick = { /* Bookmark action */ },
                    modifier = Modifier
                        .size(Dimen.SizeXLPlus)
                        .clip(CircleShape)
                        .background(SurfaceLight.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.BookmarkBorder,
                        contentDescription = "Bookmark",
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
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.s20.bold(),
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                val statusColor = if (restaurant.isOpen) SuccessColor else ErrorColor
                val statusText = if (restaurant.isOpen) {
                    stringResource(id = R.string.restaurant_detail_status_open)
                } else {
                    stringResource(id = R.string.restaurant_detail_status_closed)
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

                if (distanceKm != null && estimatedMinutes != null) {
                    Spacer(modifier = Modifier.width(AppSpacing.M))
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.s12.bold(),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.M))
                    Text(
                        text = stringResource(
                            id = R.string.restaurant_detail_distance,
                            distanceKm,
                            estimatedMinutes
                        ),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextSecondary
                    )
                }
            }

            if (restaurant.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.XSPlus))
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
                            id = R.string.restaurant_detail_open_hours,
                            restaurant.openTime,
                            restaurant.closeTime
                        ),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
