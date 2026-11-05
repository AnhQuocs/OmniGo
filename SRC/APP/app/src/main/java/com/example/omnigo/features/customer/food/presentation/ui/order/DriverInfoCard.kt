package com.example.omnigo.features.customer.food.presentation.ui.order

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import java.util.Locale

@Composable
fun DriverInfoCard(
    driverId: Long?,
    driverName: String?,
    driverPhone: String?,
    driverVehiclePlate: String?,
    driverAvatarUrl: String?,
    status: String,
    isProfileLoading: Boolean,
    profileErrorMessage: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val normalizedStatus = status.uppercase(Locale.ROOT)
    val hasAssignedDriver = driverId != null ||
        !driverName.isNullOrBlank() ||
        normalizedStatus in setOf("DRIVER_ASSIGNED", "PICKED_UP", "DELIVERING", "ARRIVED_CUSTOMER", "DELIVERED", "COMPLETED")
    val isSearching = normalizedStatus in setOf(
        "PENDING",
        "PENDING_MERCHANT",
        "ACCEPTED",
        "PREPARING",
        "DRIVER_SEARCHING",
        "READY_FOR_PICKUP"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXXS)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingML),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Avatar / Bike Icon
            Box(
                modifier = Modifier
                    .size(Dimen.SizeXXL)
                    .clip(CircleShape)
                    .background(PrimaryColor.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                if (hasAssignedDriver && !driverAvatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(driverAvatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = driverName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.M))

            if (hasAssignedDriver) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = driverName?.takeIf { it.isNotBlank() }
                            ?: stringResource(id = R.string.order_detail_driver_title),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.XXS))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (!driverName.isNullOrBlank()) {
                                stringResource(id = R.string.order_detail_driver_title)
                            } else {
                                stringResource(id = R.string.order_detail_driver_assigned)
                            },
                            style = MaterialTheme.typography.s13.normal(),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(AppSpacing.XS))

                        Text(
                            text = "• 5.0",
                            style = MaterialTheme.typography.s13.normal(),
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.width(AppSpacing.XXS))

                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = SecondaryColor,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                    }

                    if (!driverVehiclePlate.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(AppSpacing.XXS))
                        Text(
                            text = driverVehiclePlate,
                            style = MaterialTheme.typography.s13.medium(),
                            color = TextSecondary
                        )
                    }
                }

                // Call action button only
                IconButton(
                    onClick = {
                        if (!driverPhone.isNullOrBlank()) {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = android.net.Uri.parse("tel:$driverPhone")
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier
                        .size(Dimen.SizeXLPlus)
                        .background(PrimaryColor.copy(alpha = 0.10f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = stringResource(id = R.string.order_detail_action_call_driver),
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeS2)
                    )
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.order_detail_driver_title),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.XXS))

                    Text(
                        text = if (isSearching) {
                            stringResource(id = R.string.order_detail_driver_searching)
                        } else {
                            stringResource(id = R.string.order_detail_driver_not_assigned)
                        },
                        style = MaterialTheme.typography.s13.normal(),
                        color = TextSecondary
                    )
                }

                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimen.SizeM),
                        color = PrimaryColor,
                        strokeWidth = Dimen.PaddingXXS
                    )
                }
            }
        }
    }
}
