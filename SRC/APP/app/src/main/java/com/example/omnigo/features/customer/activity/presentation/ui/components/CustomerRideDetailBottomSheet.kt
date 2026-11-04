package com.example.omnigo.features.customer.activity.presentation.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingReview
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OutlineLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.RatingGold
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.formatVnd
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerRideDetailBottomSheet(
    booking: Booking,
    existingReview: BookingReview?,
    isLoading: Boolean,
    isSubmittingReview: Boolean,
    onSubmitReview: (Int, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXXL, topEnd = AppShape.ShapeXXL)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM)
                .padding(bottom = Dimen.PaddingXL),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.ride_detail_sheet_title, booking.bookingId),
                    style = MaterialTheme.typography.s16.bold(),
                    color = TextPrimary
                )
                Text(
                    text = booking.price.formatVnd(),
                    style = MaterialTheme.typography.s16.bold(),
                    color = PrimaryColor
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimen.PaddingL),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimen.SizeL),
                        color = PrimaryColor,
                        strokeWidth = Dimen.PaddingXXS
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationS),
                border = BorderStroke(Dimen.BorderHairline, CardBorderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimen.PaddingM),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.XS)
                ) {
                    Text(
                        text = booking.pickupAddress,
                        style = MaterialTheme.typography.s13.normal(),
                        color = TextSecondary
                    )
                    HorizontalDivider(color = CardBorderColor, thickness = Dimen.BorderHairline)
                    Text(
                        text = booking.dropoffAddress,
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary
                    )
                    if (!booking.driverName.isNullOrBlank() || !booking.driverVehiclePlate.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(AppSpacing.XS))
                        Text(
                            text = stringResource(id = R.string.ride_detail_driver_section),
                            style = MaterialTheme.typography.s12.bold(),
                            color = PrimaryColor
                        )
                        Text(
                            text = listOfNotNull(
                                booking.driverName,
                                booking.driverVehiclePlate,
                                booking.driverPhone
                            ).joinToString(" • "),
                            style = MaterialTheme.typography.s13.normal(),
                            color = TextPrimary
                        )
                    }
                }
            }

            if (existingReview != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationS),
                    border = BorderStroke(Dimen.BorderHairline, CardBorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimen.PaddingM),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.XS)
                    ) {
                        Text(
                            text = stringResource(id = R.string.ride_detail_review_section),
                            style = MaterialTheme.typography.s14.bold(),
                            color = TextPrimary
                        )
                        Row {
                            for (s in 1..5) {
                                Icon(
                                    imageVector = if (s <= existingReview.rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                    contentDescription = null,
                                    tint = if (s <= existingReview.rating) RatingGold else OutlineLight,
                                    modifier = Modifier.size(Dimen.SizeSM)
                                )
                            }
                        }
                        if (existingReview.comment.isNotBlank()) {
                            Text(
                                text = existingReview.comment,
                                style = MaterialTheme.typography.s13.normal(),
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else if (booking.status == BookingStatus.COMPLETED) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationS),
                    border = BorderStroke(Dimen.BorderHairline, CardBorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimen.PaddingM),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.ride_detail_review_section),
                            style = MaterialTheme.typography.s14.bold(),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.S))
                        Row {
                            for (star in 1..5) {
                                val filled = star <= rating
                                Icon(
                                    imageVector = if (filled) Icons.Default.Star else Icons.Outlined.StarOutline,
                                    contentDescription = null,
                                    tint = if (filled) RatingGold else OutlineLight,
                                    modifier = Modifier
                                        .size(Dimen.SizeL)
                                        .clip(CircleShape)
                                        .clickable { rating = star }
                                        .padding(Dimen.PaddingXXS)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(AppSpacing.S))
                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.ride_review_comment_hint),
                                    style = MaterialTheme.typography.s13.normal(),
                                    color = TextSecondary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppShape.ShapeM),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryColor,
                                unfocusedBorderColor = CardBorderColor,
                                focusedContainerColor = SurfaceLight,
                                unfocusedContainerColor = SurfaceLight
                            ),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.S))
                        Button(
                            onClick = { onSubmitReview(rating, comment.takeIf { it.isNotBlank() }) },
                            enabled = !isSubmittingReview,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimen.HeightDefault),
                            shape = RoundedCornerShape(AppShape.ShapeM),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Text(
                                text = stringResource(id = R.string.ride_detail_rate_now_btn),
                                style = MaterialTheme.typography.s14.bold(),
                                color = TextWhite
                            )
                        }
                    }
                }
            }
        }
    }
}
