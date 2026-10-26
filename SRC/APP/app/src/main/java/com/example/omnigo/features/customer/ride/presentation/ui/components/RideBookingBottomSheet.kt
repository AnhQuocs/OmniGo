package com.example.omnigo.features.customer.ride.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.customer.ride.presentation.state.RideBookingUiState
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import java.text.DecimalFormat

@Composable
fun RideBookingBottomSheet(
    uiState: RideBookingUiState,
    onSelectVehicleType: (VehicleType) -> Unit,
    onCreateBooking: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DecimalFormat("#,###")
    val selectedPrice = uiState.pricingEstimate?.totalPrice

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(topStart = AppShape.ShapeL, topEnd = AppShape.ShapeL),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = Dimen.PaddingM
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS)
        ) {
            // Minimal Drag Handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(Dimen.ShimmerTextWidthXXS)
                    .height(Dimen.PaddingXS)
                    .clip(CircleShape)
                    .background(OutlineLight)
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            // Vehicle Type Selector (BIKE, CAR_4, CAR_7)
            RideVehicleTypeSelector(
                selectedType = uiState.selectedVehicleType,
                vehicleEstimates = uiState.vehicleEstimates,
                isCalculating = uiState.isCalculatingPrice,
                onSelectType = onSelectVehicleType
            )

            // Pricing Error Banner (Explicit Backend Error)
            val pricingErrorText = uiState.pricingError?.asString()
            if (!pricingErrorText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.S))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.S))
                        Text(
                            text = pricingErrorText,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.s12.medium()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Book Now Button
            val isEnabled = uiState.pickupLocation != null &&
                    uiState.dropoffLocation != null &&
                    uiState.pricingEstimate != null &&
                    !uiState.isCreatingBooking &&
                    !uiState.isSearchingDriver &&
                    !uiState.isCalculatingPrice

            Button(
                onClick = onCreateBooking,
                enabled = isEnabled,
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    disabledContainerColor = PrimaryColor.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightDefault)
            ) {
                if (uiState.isCreatingBooking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimen.SizeM),
                        color = TextWhite,
                        strokeWidth = Dimen.PaddingXXS
                    )
                } else {
                    val bookText = stringResource(id = R.string.ride_book_now_button)
                    val priceText = if (selectedPrice != null && selectedPrice > 0.0) {
                        " • ${formatter.format(selectedPrice)} đ"
                    } else ""
                    Text(
                        text = "$bookText$priceText",
                        color = TextWhite,
                        style = MaterialTheme.typography.s14.bold()
                    )
                }
            }
        }
    }
}
