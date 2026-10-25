package com.example.omnigo.features.driver.ride.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.WarningColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s18

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverTripHeaderBar(
    bookingId: Long,
    status: BookingStatus,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(R.string.driver_trip_title),
                    style = MaterialTheme.typography.s18.bold(),
                    color = TextPrimary
                )
                if (bookingId > 0L) {
                    Text(
                        text = stringResource(R.string.driver_trip_id_format, bookingId),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextPrimary.copy(alpha = 0.7f)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cancel),
                    tint = TextPrimary
                )
            }
        },
        actions = {
            DriverTripStatusBadge(status = status)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SurfaceLight
        ),
        modifier = modifier
    )
}

@Composable
fun DriverTripStatusBadge(
    status: BookingStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, textRes) = when (status) {
        BookingStatus.ACCEPTED -> Triple(
            PrimaryColor.copy(alpha = 0.15f),
            PrimaryColor,
            R.string.driver_trip_status_accepted
        )
        BookingStatus.ARRIVED -> Triple(
            WarningColor.copy(alpha = 0.15f),
            WarningColor,
            R.string.driver_trip_status_arrived
        )
        BookingStatus.IN_PROGRESS -> Triple(
            SuccessColor.copy(alpha = 0.15f),
            SuccessColor,
            R.string.driver_trip_status_in_progress
        )
        BookingStatus.COMPLETED -> Triple(
            SuccessColor.copy(alpha = 0.15f),
            SuccessColor,
            R.string.driver_trip_status_completed
        )
        else -> Triple(
            PrimaryColor.copy(alpha = 0.15f),
            PrimaryColor,
            R.string.driver_trip_status_accepted
        )
    }

    Box(
        modifier = modifier
            .padding(end = Dimen.PaddingM)
            .background(
                color = bgColor,
                shape = RoundedCornerShape(AppShape.ShapeM)
            )
            .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXXS)
    ) {
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.s12.bold(),
            color = textColor
        )
    }
}
