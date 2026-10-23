package com.example.omnigo.features.customer.food.presentation.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.FoodReview
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestaurantReviewsSection(
    reviews: List<FoodReview>,
    isLoading: Boolean,
    selectedRating: Int?,
    onRatingFilterSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.restaurant_reviews_title),
            style = MaterialTheme.typography.s16.bold(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.S))

        // Rating Filter Chips (All, 5, 4, 3, 2, 1)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.S),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.XXS)
        ) {
            FilterChip(
                selected = selectedRating == null,
                onClick = { onRatingFilterSelected(null) },
                label = {
                    Text(
                        text = stringResource(id = R.string.restaurant_reviews_filter_all),
                        style = MaterialTheme.typography.s12.normal()
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryColor,
                    selectedLabelColor = SurfaceLight
                )
            )

            for (star in 5 downTo 1) {
                FilterChip(
                    selected = selectedRating == star,
                    onClick = { onRatingFilterSelected(if (selectedRating == star) null else star) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$star",
                                style = MaterialTheme.typography.s12.normal()
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.XXS))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = SecondaryColor,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryColor,
                        selectedLabelColor = SurfaceLight
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.M))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingL),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryColor, strokeWidth = Dimen.PaddingXXS)
            }
        } else if (reviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingL),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.restaurant_reviews_empty),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.M)) {
                reviews.forEach { review ->
                    RestaurantReviewCard(review = review)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RestaurantReviewCard(
    review: FoodReview,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeM),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXXS)
    ) {
        Column(modifier = Modifier.padding(Dimen.PaddingM)) {
            // Header: Stars + Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i <= review.restaurantRating) SecondaryColor else CardBorderColor,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                    }
                }
                review.createdAt?.let { date ->
                    Text(
                        text = date.take(10),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }
            }

            // Comment
            review.restaurantComment?.takeIf { it.isNotBlank() }?.let { comment ->
                Spacer(modifier = Modifier.height(AppSpacing.S))
                Text(
                    text = comment,
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextPrimary
                )
            }

            // Photo thumbnails (up to 5)
            if (review.reviewPhotos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AppSpacing.S))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.XS),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.XS)
                ) {
                    review.reviewPhotos.forEach { photoUrl ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(Dimen.SizeXXL)
                                .clip(RoundedCornerShape(AppShape.ShapeS))
                                .border(Dimen.PaddingXXS, CardBorderColor, RoundedCornerShape(AppShape.ShapeS))
                        )
                    }
                }
            }

            // Merchant Reply if available
            review.merchantReply?.takeIf { it.isNotBlank() }?.let { reply ->
                Spacer(modifier = Modifier.height(AppSpacing.S))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryColor.copy(alpha = 0.06f), RoundedCornerShape(AppShape.ShapeS))
                        .padding(Dimen.PaddingS)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = PrimaryColor,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.XXS))
                            Text(
                                text = stringResource(id = R.string.food_review_merchant_reply_title),
                                style = MaterialTheme.typography.s12.bold(),
                                color = PrimaryColor
                            )
                        }
                        Spacer(modifier = Modifier.height(AppSpacing.XXS))
                        Text(
                            text = reply,
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}
