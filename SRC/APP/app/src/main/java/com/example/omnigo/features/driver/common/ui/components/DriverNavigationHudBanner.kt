package com.example.omnigo.features.driver.common.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.driver.common.presentation.DriverNavigationUiState
import com.example.omnigo.features.driver.domain.util.TurnManeuver
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.ErrorContainer
import com.example.omnigo.ui.theme.OnBackgroundLight
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s22
import com.example.omnigo.utils.semiBold

@Composable
fun DriverNavigationHudBanner(
    codeNumber: Long,
    stageLabel: String,
    navigationState: DriverNavigationUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCancelClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val maneuverIcon = resolveManeuverIcon(navigationState.nextTurnManeuver)
    val maneuverText = resolveManeuverText(navigationState.nextTurnManeuver)
    val turnDistanceFormatted = formatDistanceLabel(navigationState.distanceToNextTurnMeters)
    val totalDistanceFormatted = formatDistanceLabel(navigationState.remainingDistanceMeters)
    val etaLabel = if (navigationState.etaMinutes <= 0) {
        stringResource(id = R.string.driver_nav_eta_arriving_now)
    } else {
        stringResource(id = R.string.driver_nav_eta_minutes_format, navigationState.etaMinutes)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.S)
    ) {
        // Top compact cockpit bar: Back, #ID + Stage, Cancel button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.S),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    onClick = onBackClick,
                    shape = CircleShape,
                    color = SurfaceLight,
                    shadowElevation = Dimen.PaddingXS,
                    modifier = Modifier.size(Dimen.SizeXLPlus)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.cancel),
                            tint = TextPrimary,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(AppShape.PillShape),
                    color = SurfaceLight,
                    shadowElevation = Dimen.PaddingXS
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = Dimen.PaddingSM,
                            vertical = Dimen.PaddingXSPlus
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                    ) {
                        if (codeNumber > 0L) {
                            Text(
                                text = "#$codeNumber",
                                style = MaterialTheme.typography.s13.bold(),
                                color = PrimaryColor
                            )
                        }
                        Text(
                            text = stageLabel,
                            style = MaterialTheme.typography.s12.semiBold(),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (onCancelClick != null) {
                Spacer(modifier = Modifier.width(AppSpacing.S))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.PillShape))
                        .background(ErrorContainer)
                        .clickable(onClick = onCancelClick)
                        .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXSPlus),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.driver_trip_btn_cancel),
                        tint = ErrorColor,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XXS))
                    Text(
                        text = stringResource(id = R.string.driver_trip_btn_cancel),
                        style = MaterialTheme.typography.s12.bold(),
                        color = ErrorColor
                    )
                }
            }
        }

        // Turn-by-Turn Navigation HUD Card
        Card(
            shape = RoundedCornerShape(AppShape.ShapeXL),
            colors = CardDefaults.cardColors(containerColor = OnBackgroundLight),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXS),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Maneuver Icon Box
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeXXL)
                        .clip(RoundedCornerShape(AppShape.ShapeL))
                        .background(
                            if (navigationState.nextTurnManeuver == TurnManeuver.ARRIVE) {
                                SuccessColor
                            } else {
                                PrimaryColor
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = maneuverIcon,
                        contentDescription = maneuverText,
                        tint = OnPrimaryColor,
                        modifier = Modifier.size(Dimen.SizeL)
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.M))

                // Turn Distance + Maneuver + Destination Address
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                    ) {
                        Text(
                            text = turnDistanceFormatted,
                            style = MaterialTheme.typography.s22.bold(),
                            color = TextWhite
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppShape.ShapeS))
                                .background(SuccessColor.copy(alpha = 0.22f))
                                .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                        ) {
                            Text(
                                text = "$etaLabel • $totalDistanceFormatted",
                                style = MaterialTheme.typography.s12.bold(),
                                color = SuccessColor
                            )
                        }
                    }

                    Text(
                        text = if (navigationState.isRouteLoading) {
                            stringResource(id = R.string.driver_nav_loading_route)
                        } else {
                            maneuverText
                        },
                        style = MaterialTheme.typography.s16.bold(),
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (navigationState.targetAddress.isNotBlank()) {
                        Spacer(modifier = Modifier.height(AppSpacing.XXS))
                        Text(
                            text = navigationState.targetAddress,
                            style = MaterialTheme.typography.s12.medium(),
                            color = TextTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacing.S))

                // Compact button to open external Google Maps if needed
                IconButton(
                    onClick = {
                        openExternalGoogleMaps(
                            context = context,
                            targetLat = navigationState.targetLat,
                            targetLng = navigationState.targetLng,
                            targetAddress = navigationState.targetAddress
                        )
                    },
                    modifier = Modifier
                        .size(Dimen.SizeXLPlus)
                        .clip(CircleShape)
                        .background(PrimaryColor.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = stringResource(id = R.string.driver_open_google_maps),
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                }
            }
        }
    }
}

@Composable
private fun resolveManeuverIcon(maneuver: TurnManeuver): ImageVector {
    return when (maneuver) {
        TurnManeuver.STRAIGHT -> Icons.Default.KeyboardArrowUp
        TurnManeuver.TURN_LEFT -> Icons.AutoMirrored.Filled.ArrowBack
        TurnManeuver.TURN_RIGHT -> Icons.AutoMirrored.Filled.ArrowForward
        TurnManeuver.U_TURN -> Icons.Default.Refresh
        TurnManeuver.ARRIVE -> Icons.Default.LocationOn
    }
}

@Composable
private fun resolveManeuverText(maneuver: TurnManeuver): String {
    return when (maneuver) {
        TurnManeuver.STRAIGHT -> stringResource(id = R.string.driver_nav_maneuver_straight)
        TurnManeuver.TURN_LEFT -> stringResource(id = R.string.driver_nav_maneuver_turn_left)
        TurnManeuver.TURN_RIGHT -> stringResource(id = R.string.driver_nav_maneuver_turn_right)
        TurnManeuver.U_TURN -> stringResource(id = R.string.driver_nav_maneuver_u_turn)
        TurnManeuver.ARRIVE -> stringResource(id = R.string.driver_nav_maneuver_arrive)
    }
}

@Composable
private fun formatDistanceLabel(distanceMeters: Double): String {
    return if (distanceMeters >= 1000.0) {
        stringResource(id = R.string.driver_nav_distance_km_format, distanceMeters / 1000.0)
    } else {
        stringResource(id = R.string.driver_nav_distance_meters_format, distanceMeters.coerceAtLeast(0.0))
    }
}

private fun openExternalGoogleMaps(
    context: Context,
    targetLat: Double?,
    targetLng: Double?,
    targetAddress: String
) {
    val query = if (targetLat != null && targetLng != null) {
        "$targetLat,$targetLng"
    } else {
        Uri.encode(targetAddress)
    }
    val navIntent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$query"))
    try {
        context.startActivity(navIntent)
    } catch (_: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$query")
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, webUri)) }
    }
}
