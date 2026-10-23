package com.example.omnigo.features.customer.ride.presentation.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineLight
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold

@Composable
fun RideLocationPickerSection(
    pickupLocation: LocationPoint?,
    dropoffLocation: LocationPoint?,
    isLocating: Boolean,
    savedAddresses: List<SavedAddress>,
    onDetectCurrentLocation: () -> Unit,
    onSelectPickup: (LocationPoint) -> Unit,
    onSelectDropoff: (LocationPoint) -> Unit,
    onSwapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeL),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimen.PaddingXXS
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Route Visual Indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = Dimen.PaddingS)
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeS)
                            .clip(CircleShape)
                            .background(PrimaryColor)
                    )
                    Box(
                        modifier = Modifier
                            .width(Dimen.PaddingXXS)
                            .height(Dimen.SizeXL)
                            .background(OutlineLight)
                    )
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeS)
                            .clip(RoundedCornerShape(AppShape.ShapeXXS))
                            .background(SecondaryColor)
                    )
                }

                // Pickup & Dropoff Address Fields
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Pickup Field
                    LocationRow(
                        label = stringResource(id = R.string.ride_pickup_label),
                        point = pickupLocation,
                        placeholder = stringResource(id = R.string.ride_pickup_hint),
                        isPickup = true
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.S))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimen.PaddingXXS)
                            .background(OutlineVariantLight)
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.S))

                    // Dropoff Field
                    LocationRow(
                        label = stringResource(id = R.string.ride_dropoff_label),
                        point = dropoffLocation,
                        placeholder = stringResource(id = R.string.ride_dropoff_hint),
                        isPickup = false
                    )
                }

                // Swap Button
                IconButton(
                    onClick = onSwapClick,
                    modifier = Modifier
                        .padding(start = Dimen.PaddingXS)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = stringResource(id = R.string.ride_swap_locations),
                        tint = PrimaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Quick actions (Current Location & Saved Places chips)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
            ) {
                // Current Location Button
                Surface(
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    color = PrimaryColor.copy(alpha = 0.08f),
                    modifier = Modifier
                        .clickable(enabled = !isLocating) { onDetectCurrentLocation() }
                        .border(
                            width = Dimen.PaddingXXS,
                            color = PrimaryColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(AppShape.ShapeM)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = Dimen.PaddingS,
                            vertical = Dimen.PaddingXS
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLocating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimen.SizeS),
                                strokeWidth = Dimen.PaddingXXS,
                                color = PrimaryColor
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = PrimaryColor,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.XS))
                        Text(
                            text = stringResource(id = R.string.ride_my_location),
                            style = MaterialTheme.typography.s12.semiBold(),
                            color = PrimaryColor
                        )
                    }
                }

                // Quick Saved Locations chips
                savedAddresses.take(2).forEach { saved ->
                    Surface(
                        shape = RoundedCornerShape(AppShape.ShapeM),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clickable {
                            val point = LocationPoint.fromSavedAddress(saved)
                            if (pickupLocation == null) {
                                onSelectPickup(point)
                            } else {
                                onSelectDropoff(point)
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = Dimen.PaddingS,
                                vertical = Dimen.PaddingXS
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.XS))
                            Text(
                                text = saved.addressName,
                                style = MaterialTheme.typography.s12.medium(),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationRow(
    label: String,
    point: LocationPoint?,
    placeholder: String,
    isPickup: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.s10.medium(),
            color = if (isPickup) PrimaryColor else SecondaryColor
        )
        Text(
            text = point?.name ?: placeholder,
            style = MaterialTheme.typography.s14.bold(),
            color = if (point != null) TextPrimary else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (point != null && point.address.isNotBlank()) {
            Text(
                text = point.address,
                style = MaterialTheme.typography.s12.medium(),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
