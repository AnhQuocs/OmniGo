package com.example.omnigo.features.customer.ride.presentation.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.customer.ride.domain.model.BookingDriverProfile
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16

internal fun vehicleImageRes(type: VehicleType): Int = when (type) {
    VehicleType.CAR_4_SEAT -> R.drawable.omnicar4
    VehicleType.CAR_7_SEAT -> R.drawable.omnicar7
    VehicleType.BIKE, VehicleType.EXPRESS -> R.drawable.omnibike
}

@Composable
fun TripDriverRow(
    driver: BookingDriverProfile?,
    vehicleType: VehicleType,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val plate = driver?.vehiclePlate.orEmpty()
    val vehicleModel = driver?.vehicleModel ?: driver?.vehicleType.orEmpty()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimen.SizeXXL)
                .clip(CircleShape)
                .background(PrimaryColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(Dimen.SizeL)
            )
        }
        Spacer(modifier = Modifier.width(AppSpacing.M))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = driver?.driverName ?: stringResource(id = R.string.driver_trip_title),
                    style = MaterialTheme.typography.s16.bold(),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(AppSpacing.XS))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = SecondaryColor,
                    modifier = Modifier.size(Dimen.SizeS)
                )
                Text(
                    text = stringResource(id = R.string.ride_trip_driver_rating_default),
                    style = MaterialTheme.typography.s12.medium(),
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(AppSpacing.XXS))
            if (plate.isNotBlank()) {
                Surface(shape = RoundedCornerShape(AppShape.ShapeXXS), color = SurfaceVariantLight) {
                    Text(
                        text = plate,
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                    )
                }
            }
            if (vehicleModel.isNotBlank()) {
                Text(
                    text = vehicleModel,
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Image(
            painter = painterResource(id = vehicleImageRes(vehicleType)),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(width = Dimen.SizeXXLPlus, height = Dimen.SizeXXL)
        )
        Spacer(modifier = Modifier.width(AppSpacing.S))

        IconButton(
            onClick = {
                val phone = driver?.driverPhone
                if (!phone.isNullOrBlank()) {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                }
            },
            modifier = Modifier
                .size(Dimen.SizeXLPlus)
                .clip(CircleShape)
                .background(SuccessColor)
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = stringResource(id = R.string.ride_btn_call_driver),
                tint = OnPrimaryColor,
                modifier = Modifier.size(Dimen.SizeM)
            )
        }
    }
}

