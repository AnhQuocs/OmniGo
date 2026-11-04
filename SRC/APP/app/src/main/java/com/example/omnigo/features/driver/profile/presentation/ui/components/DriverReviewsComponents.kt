package com.example.omnigo.features.driver.profile.presentation.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.FoodReview
import com.example.omnigo.features.customer.ride.domain.model.BookingReview
import com.example.omnigo.features.driver.profile.presentation.state.DriverReviewsUiState
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.RatingGold
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.italic
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s32
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun HeroRatingSummaryCard(
    uiState: DriverReviewsUiState,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationS),
        modifier = modifier
            .fillMaxWidth()
            .border(Dimen.BorderHairline, CardBorderColor, RoundedCornerShape(AppShape.ShapeL))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = Dimen.PaddingM)
            ) {
                Text(
                    text = String.format(Locale.US, "%.1f", uiState.currentAverageRating),
                    style = MaterialTheme.typography.s32.bold(),
                    color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { idx ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (idx < uiState.currentAverageRating.toInt()) RatingGold else OutlineVariantLight,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = stringResource(id = R.string.driver_reviews_total_count, uiState.currentTotalReviews),
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.XS)
            ) {
                val total = uiState.currentTotalReviews.coerceAtLeast(1)
                for (star in 5 downTo 1) {
                    val count = uiState.starCountForCurrentTab(star)
                    val fraction = (count.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$star★",
                            style = MaterialTheme.typography.s12.medium(),
                            color = TextSecondary,
                            modifier = Modifier.width(Dimen.SizeML)
                        )
                        LinearProgressIndicator(
                            progress = { fraction },
                            color = RatingGold,
                            trackColor = OutlineVariantLight,
                            modifier = Modifier
                                .weight(1f)
                                .height(Dimen.PaddingXSPlus)
                                .clip(RoundedCornerShape(AppShape.PillShape))
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XS))
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StarFilterRow(
    selectedStar: Int,
    onSelectStar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val stars = listOf(0, 5, 4, 3, 2, 1)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
    ) {
        stars.forEach { star ->
            val isSelected = selectedStar == star
            val label = if (star == 0) {
                stringResource(id = R.string.driver_reviews_filter_all)
            } else {
                "$star ★"
            }
            Card(
                onClick = { onSelectStar(star) },
                shape = RoundedCornerShape(AppShape.PillShape),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) PrimaryColor else SurfaceLight
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationNone),
                modifier = Modifier
                    .border(
                        width = Dimen.BorderHairline,
                        color = if (isSelected) PrimaryColor else CardBorderColor,
                        shape = RoundedCornerShape(AppShape.PillShape)
                    )
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.s13.semiBold(),
                    color = if (isSelected) OnPrimaryColor else TextPrimary,
                    modifier = Modifier.padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingXS)
                )
            }
        }
    }
}

@Composable
fun RideReviewItemCard(
    review: BookingReview,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationXS),
        modifier = modifier
            .fillMaxWidth()
            .border(Dimen.BorderHairline, CardBorderColor, RoundedCornerShape(AppShape.ShapeL))
    ) {
        Column(modifier = Modifier.padding(Dimen.PaddingM)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.driver_reviews_trip_ref, review.bookingId),
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(review.rating.coerceIn(1, 5)) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = RatingGold,
                            modifier = Modifier.size(Dimen.SizeS2)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(AppSpacing.XS))
            if (review.comment.isNotBlank()) {
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.s14.normal(),
                    color = TextPrimary
                )
            } else {
                Text(
                    text = stringResource(id = R.string.driver_reviews_no_comment),
                    style = MaterialTheme.typography.s13.italic(),
                    color = TextSecondary
                )
            }
            if (review.createdAt.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.XS))
                Text(
                    text = review.createdAt.take(16).replace("T", " "),
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun FoodDeliveryReviewItemCard(
    review: FoodReview,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationXS),
        modifier = modifier
            .fillMaxWidth()
            .border(Dimen.BorderHairline, CardBorderColor, RoundedCornerShape(AppShape.ShapeL))
    ) {
        Column(modifier = Modifier.padding(Dimen.PaddingM)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.driver_reviews_order_ref, review.orderId),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary
                    )
                    if (!review.customerName.isNullOrBlank()) {
                        Text(
                            text = review.customerName,
                            style = MaterialTheme.typography.s12.medium(),
                            color = PrimaryColor
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(review.driverRating.coerceIn(1, 5)) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = RatingGold,
                            modifier = Modifier.size(Dimen.SizeS2)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(AppSpacing.XS))
            val commentText = review.driverComment ?: review.driverTags.joinToString(", ")
            if (commentText.isNotBlank()) {
                Text(
                    text = commentText,
                    style = MaterialTheme.typography.s14.normal(),
                    color = TextPrimary
                )
            } else {
                Text(
                    text = stringResource(id = R.string.driver_reviews_no_comment),
                    style = MaterialTheme.typography.s13.italic(),
                    color = TextSecondary
                )
            }
            if (!review.createdAt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.XS))
                Text(
                    text = review.createdAt.take(16).replace("T", " "),
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun EmptyReviewsBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimen.PaddingXXL),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.driver_reviews_empty),
            style = MaterialTheme.typography.s14.normal(),
            color = TextSecondary
        )
    }
}

