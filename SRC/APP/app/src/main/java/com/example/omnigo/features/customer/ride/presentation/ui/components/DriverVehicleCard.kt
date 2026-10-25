package com.example.omnigo.features.customer.ride.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.BookingDriverProfile
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold

@Composable
fun DriverVehicleCard(
    driver: BookingDriverProfile?,
    isLoading: Boolean,
    onCallDriver: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        if (isLoading && driver == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingL),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimen.SizeL),
                    color = PrimaryColor,
                    strokeWidth = Dimen.PaddingXXS
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingL),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.M),
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeMega)
                            .clip(CircleShape)
                            .background(PrimaryContainer)
                            .border(Dimen.PaddingXXS, PrimaryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeL)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                        ) {
                            Text(
                                text = driver?.driverName ?: stringResource(id = R.string.ride_driver_name_fallback),
                                style = MaterialTheme.typography.s16.bold(),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(id = R.string.ride_driver_rating_default),
                                style = MaterialTheme.typography.s12.semiBold(),
                                color = PrimaryColor
                            )
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.XXS))

                        // Vehicle plate badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppShape.ShapeXS))
                                .background(OutlineVariantLight.copy(alpha = 0.3f))
                                .border(Dimen.PaddingXXS, CardBorderColor, RoundedCornerShape(AppShape.ShapeXS))
                                .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                        ) {
                            Text(
                                text = driver?.vehiclePlate ?: stringResource(id = R.string.ride_vehicle_plate_fallback),
                                style = MaterialTheme.typography.s13.bold(),
                                color = TextPrimary
                            )
                        }

                        val vehicleDesc = listOfNotNull(driver?.vehicleType, driver?.vehicleModel).joinToString(" • ")
                        if (vehicleDesc.isNotBlank()) {
                            Spacer(modifier = Modifier.height(AppSpacing.XXS))
                            Text(
                                text = vehicleDesc,
                                style = MaterialTheme.typography.s12.normal(),
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Call Action Button
                Button(
                    onClick = { onCallDriver(driver?.driverPhone) },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                    modifier = Modifier.size(Dimen.SizeMega)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = stringResource(id = R.string.ride_call_driver_button),
                        tint = OnPrimaryColor,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                }
            }
        }
    }
}
