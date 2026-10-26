package com.example.omnigo.features.customer.ride.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineLight
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold

@Composable
fun RideFloatingHeader(
    pickupLocation: LocationPoint?,
    dropoffLocation: LocationPoint?,
    isLocatingPickup: Boolean,
    onPickupClick: () -> Unit,
    onDropoffClick: () -> Unit,
    onSwapClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = Dimen.PaddingS,
            modifier = Modifier.size(Dimen.SizeXLPlus)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.width(AppSpacing.S))

        // Dual-Point Selection Card
        Surface(
            shape = RoundedCornerShape(AppShape.ShapeL),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = Dimen.PaddingS,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Route indicator dots
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(end = Dimen.PaddingS)
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimen.PaddingS)
                            .clip(CircleShape)
                            .background(SuccessColor)
                    )
                    Box(
                        modifier = Modifier
                            .width(Dimen.PaddingXXS)
                            .height(AppSpacing.XLPlus)
                            .background(OutlineLight)
                    )
                    Box(
                        modifier = Modifier
                            .size(Dimen.PaddingS)
                            .clip(RoundedCornerShape(AppShape.ShapeXXS))
                            .background(SecondaryColor)
                    )
                }

                // Pickup & Dropoff Text Fields
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    HeaderLocationItem(
                        location = pickupLocation,
                        placeholderRes = R.string.ride_pickup_hint,
                        isLocating = isLocatingPickup,
                        onClick = onPickupClick
                    )

                    HorizontalDivider(
                        color = OutlineVariantLight,
                        thickness = Dimen.PaddingXXS
                    )

                    HeaderLocationItem(
                        location = dropoffLocation,
                        placeholderRes = R.string.ride_search_destination_hint,
                        isLocating = false,
                        onClick = onDropoffClick
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.XS))

                // Swap Button
                IconButton(
                    onClick = onSwapClick,
                    modifier = Modifier.size(Dimen.SizeL)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = stringResource(id = R.string.ride_swap_locations),
                        tint = TextTertiary,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderLocationItem(
    location: LocationPoint?,
    placeholderRes: Int,
    isLocating: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimen.HeightDefault)
            .clip(RoundedCornerShape(AppShape.ShapeXS))
            .clickable(onClick = onClick)
            .padding(vertical = Dimen.PaddingXSPlus, horizontal = Dimen.PaddingXS),
        contentAlignment = Alignment.CenterStart
    ) {
        if (isLocating) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimen.SizeS),
                    strokeWidth = Dimen.PaddingXXS,
                    color = SuccessColor
                )
                Spacer(modifier = Modifier.width(AppSpacing.XS))
                Text(
                    text = stringResource(id = R.string.ride_pin_picker_detecting),
                    style = MaterialTheme.typography.s14.normal(),
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else if (location == null) {
            Text(
                text = stringResource(id = placeholderRes),
                style = MaterialTheme.typography.s14.normal(),
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            val title = if (location.isCurrentLocation) {
                stringResource(id = R.string.ride_my_location)
            } else {
                location.name.ifBlank { location.address }
            }

            val subtitle = if (location.isCurrentLocation) {
                location.address.takeIf { it.isNotBlank() && it != title }
            } else {
                extractRemainingAddress(location.name, location.address).takeIf { it.isNotBlank() }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun extractRemainingAddress(name: String, fullAddress: String): String {
    if (fullAddress.isBlank()) return ""
    val trimmedName = name.trim()
    val trimmedFull = fullAddress.trim()
    if (trimmedName.isEmpty() || trimmedName.equals(trimmedFull, ignoreCase = true)) return ""

    return if (trimmedFull.startsWith(trimmedName, ignoreCase = true)) {
        trimmedFull.removePrefix(trimmedName)
            .trimStart(',', ' ', '-', '•')
            .trim()
    } else {
        trimmedFull
    }
}

