package com.example.omnigo.features.customer.activity.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingReview
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.PrimaryColor

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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundLight,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXXL, topEnd = AppShape.ShapeXXL)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimen.PaddingL)
                .padding(bottom = Dimen.PaddingL)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.MediumLarge)
        ) {
            RideDetailHeader(booking = booking)

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimen.PaddingS),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimen.SizeM),
                        color = PrimaryColor,
                        strokeWidth = Dimen.PaddingXXS
                    )
                }
            }

            RideDetailRouteCard(booking = booking)
            RideDetailInfoCard(booking = booking)

            if (!booking.driverName.isNullOrBlank() || !booking.driverVehiclePlate.isNullOrBlank()) {
                RideDetailDriverCard(booking = booking)
            }

            RideDetailFareCard(booking = booking)

            if (existingReview != null) {
                RideDetailReviewDisplay(review = existingReview)
            } else if (booking.status == BookingStatus.COMPLETED) {
                RideDetailReviewForm(
                    isSubmitting = isSubmittingReview,
                    onSubmit = onSubmitReview
                )
            }
        }
    }
}
