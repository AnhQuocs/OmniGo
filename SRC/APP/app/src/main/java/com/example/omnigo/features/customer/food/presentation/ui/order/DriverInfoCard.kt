package com.example.omnigo.features.customer.food.presentation.ui.order

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import coil.compose.AsyncImage
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
    val noDriverFound = normalizedStatus == "NO_DRIVER_FOUND"

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeM),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = AppSpacing.XXS)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            Text(
                text = stringResource(id = R.string.order_detail_driver_title),
                style = MaterialTheme.typography.s15.bold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            if (hasAssignedDriver) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(Dimen.SizeXXL)
                                .background(PrimaryColor.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (driverAvatarUrl.isNullOrBlank()) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(Dimen.SizeML)
                                )
                            } else {
                                AsyncImage(
                                    model = driverAvatarUrl,
                                    contentDescription = stringResource(id = R.string.order_detail_driver_avatar),
                                    placeholder = rememberVectorPainter(Icons.Default.Person),
                                    error = rememberVectorPainter(Icons.Default.Person),
                                    modifier = Modifier
                                        .size(Dimen.SizeXXL)
                                        .clip(CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(AppSpacing.M))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = driverName?.takeIf { it.isNotBlank() }
                                    ?: stringResource(id = R.string.order_detail_driver_assigned),
                                style = MaterialTheme.typography.s15.bold(),
                                color = TextPrimary
                            )

                            if (driverName.isNullOrBlank() &&
                                driverPhone.isNullOrBlank() &&
                                driverVehiclePlate.isNullOrBlank()
                            ) {
                                Text(
                                    text = stringResource(id = R.string.order_detail_driver_info_unavailable),
                                    style = MaterialTheme.typography.s12.normal(),
                                    color = TextSecondary
                                )
                            }

                            if (!driverVehiclePlate.isNullOrBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = AppSpacing.XXS)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(Dimen.SizeS)
                                    )
                                    Spacer(modifier = Modifier.width(AppSpacing.XXS))
                                    Text(
                                        text = driverVehiclePlate,
                                        style = MaterialTheme.typography.s13.medium(),
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (!driverPhone.isNullOrBlank()) {
                                Text(
                                    text = driverPhone,
                                    style = MaterialTheme.typography.s12.normal(),
                                    color = TextSecondary
                                )
                            }

                            if (isProfileLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(top = AppSpacing.XXS)
                                        .size(Dimen.SizeS),
                                    color = PrimaryColor,
                                    strokeWidth = Dimen.PaddingXXS
                                )
                            }

                            if (!profileErrorMessage.isNullOrBlank()) {
                                Text(
                                    text = profileErrorMessage,
                                    style = MaterialTheme.typography.s12.normal(),
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    if (!driverPhone.isNullOrBlank()) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:$driverPhone")
                                }
                                context.startActivity(intent)
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            modifier = Modifier.size(Dimen.SizeXLPlus)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = stringResource(id = R.string.order_detail_action_call_driver),
                                tint = TextWhite,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimen.SizeL),
                            color = PrimaryColor,
                            strokeWidth = Dimen.PaddingXXS
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(Dimen.SizeL)
                                .background(MaterialTheme.colorScheme.outlineVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonSearch,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(AppSpacing.M))

                    Text(
                        text = when {
                            isSearching -> stringResource(id = R.string.order_detail_driver_searching)
                            noDriverFound -> stringResource(id = R.string.order_detail_driver_not_found)
                            else -> stringResource(id = R.string.order_detail_driver_not_assigned)
                        },
                        style = MaterialTheme.typography.s14.normal(),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
