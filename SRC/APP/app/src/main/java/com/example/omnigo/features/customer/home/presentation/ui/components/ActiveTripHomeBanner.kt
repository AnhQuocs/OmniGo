package com.example.omnigo.features.customer.home.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.data.local.RideTrackingSessionManager
import com.example.omnigo.features.customer.ride.domain.model.ActiveRideState
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold

@Composable
fun ActiveTripHomeBanner(
    activeRideState: ActiveRideState?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visible = activeRideState != null && (activeRideState.isTripActive || activeRideState.isPending)

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (activeRideState != null) {
            ActiveTripHomeBannerContent(
                activeRideState = activeRideState,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun ActiveTripHomeBannerContent(
    activeRideState: ActiveRideState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS)
            .shadow(
                elevation = Dimen.PaddingXS,
                shape = RoundedCornerShape(AppShape.PillShape)
            )
            .clip(RoundedCornerShape(AppShape.PillShape))
            .clickable(onClick = onClick),
        color = BackgroundLight,
        tonalElevation = Dimen.PaddingXXS
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Status Icon
            Box(
                modifier = Modifier
                    .size(Dimen.SizeXLPlus)
                    .clip(CircleShape)
                    .background(PrimaryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (activeRideState.status == BookingStatus.PENDING || activeRideState.isSearchingDriver) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimen.SizeSM),
                        color = PrimaryColor,
                        strokeWidth = Dimen.PaddingXXS
                    )
                } else {
                    Icon(
                        imageVector = if (activeRideState.status == BookingStatus.IN_PROGRESS) {
                            Icons.Default.NearMe
                        } else {
                            Icons.Default.DirectionsCar
                        },
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.M))

            // Center Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                val bannerTitle = resolveBannerTitle(activeRideState)
                Text(
                    text = bannerTitle,
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val bannerSubtitle = resolveBannerSubtitle(activeRideState)
                if (bannerSubtitle.isNotBlank()) {
                    Text(
                        text = bannerSubtitle,
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.S))

            // Right Action Hint
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.active_trip_sheet_title),
                    style = MaterialTheme.typography.s12.medium(),
                    color = PrimaryColor
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeS)
                )
            }
        }
    }
}

@Composable
private fun resolveBannerTitle(activeRideState: ActiveRideState): String {
    return when {
        activeRideState.statusMessage == RideTrackingSessionManager.STATUS_DRIVER_CANCELLED_MSG -> {
            stringResource(id = R.string.active_trip_banner_driver_cancelled)
        }
        activeRideState.status == BookingStatus.PENDING || activeRideState.isSearchingDriver -> {
            stringResource(id = R.string.active_trip_banner_pending)
        }
        activeRideState.status == BookingStatus.ACCEPTED -> {
            val eta = activeRideState.liveEtaMinutes ?: 5
            val dist = activeRideState.liveDistanceKm ?: 1.5
            stringResource(id = R.string.active_trip_banner_eta_format, eta, dist)
        }
        activeRideState.status == BookingStatus.ARRIVED -> {
            stringResource(id = R.string.active_trip_banner_arrived)
        }
        activeRideState.status == BookingStatus.IN_PROGRESS -> {
            stringResource(id = R.string.active_trip_banner_in_progress)
        }
        else -> {
            stringResource(id = R.string.active_trip_sheet_title)
        }
    }
}

@Composable
private fun resolveBannerSubtitle(activeRideState: ActiveRideState): String {
    val driver = activeRideState.driverProfile
    return if (driver != null && !driver.vehiclePlate.isNullOrBlank()) {
        val name = driver.driverName ?: stringResource(id = R.string.ride_driver_name_fallback)
        "$name • ${driver.vehiclePlate}"
    } else if (activeRideState.dropoffAddress.isNotBlank()) {
        activeRideState.dropoffAddress
    } else {
        ""
    }
}
