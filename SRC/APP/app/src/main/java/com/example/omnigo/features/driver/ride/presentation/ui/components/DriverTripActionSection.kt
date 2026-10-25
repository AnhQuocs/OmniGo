package com.example.omnigo.features.driver.ride.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16

@Composable
fun DriverTripActionSection(
    status: BookingStatus,
    isWithinGeofence: Boolean,
    isUpdatingStatus: Boolean,
    onArrivePickup: () -> Unit,
    onStartTrip: () -> Unit,
    onCompleteTrip: () -> Unit,
    onRequestCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isUpdatingStatus) {
            CircularProgressIndicator(
                color = PrimaryColor,
                modifier = Modifier.size(Dimen.SizeL)
            )
            Spacer(modifier = Modifier.height(AppSpacing.M))
        }

        when (status) {
            BookingStatus.ACCEPTED -> {
                Button(
                    onClick = onArrivePickup,
                    enabled = !isUpdatingStatus,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isWithinGeofence) SuccessColor else PrimaryColor,
                        contentColor = OnPrimaryColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault)
                ) {
                    Text(
                        text = stringResource(R.string.driver_trip_btn_arrived),
                        style = MaterialTheme.typography.s16.bold()
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                OutlinedButton(
                    onClick = onRequestCancel,
                    enabled = !isUpdatingStatus,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ErrorColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault)
                ) {
                    Text(
                        text = stringResource(R.string.driver_trip_btn_cancel),
                        style = MaterialTheme.typography.s14.bold()
                    )
                }
            }

            BookingStatus.ARRIVED -> {
                Button(
                    onClick = onStartTrip,
                    enabled = !isUpdatingStatus,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor,
                        contentColor = OnPrimaryColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault)
                ) {
                    Text(
                        text = stringResource(R.string.driver_trip_btn_start),
                        style = MaterialTheme.typography.s16.bold()
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                OutlinedButton(
                    onClick = onRequestCancel,
                    enabled = !isUpdatingStatus,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ErrorColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault)
                ) {
                    Text(
                        text = stringResource(R.string.driver_trip_btn_cancel),
                        style = MaterialTheme.typography.s14.bold()
                    )
                }
            }

            BookingStatus.IN_PROGRESS -> {
                Button(
                    onClick = onCompleteTrip,
                    enabled = !isUpdatingStatus,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isWithinGeofence) SuccessColor else PrimaryColor,
                        contentColor = OnPrimaryColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault)
                ) {
                    Text(
                        text = stringResource(R.string.driver_trip_btn_complete),
                        style = MaterialTheme.typography.s16.bold()
                    )
                }
            }

            else -> { /* No actions needed */ }
        }
    }
}
