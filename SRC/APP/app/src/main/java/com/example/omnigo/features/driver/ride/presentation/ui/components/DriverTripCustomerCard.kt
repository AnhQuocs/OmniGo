package com.example.omnigo.features.driver.ride.presentation.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold

@Composable
fun DriverTripCustomerCard(
    customerId: Long?,
    customerName: String,
    customerPhone: String,
    customerAvatarUrl: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val effectiveName = customerName.ifBlank {
        if (customerId != null && customerId > 0L) {
            "${stringResource(R.string.driver_trip_customer_name_default)} #$customerId"
        } else {
            stringResource(R.string.driver_trip_customer_name_default)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            ),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingML),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeMega)
                    .clip(CircleShape)
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!customerAvatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(customerAvatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = effectiveName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(Dimen.SizeMega)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = effectiveName,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeL)
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.MediumLarge))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.driver_trip_customer_title),
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
                Text(
                    text = effectiveName,
                    style = MaterialTheme.typography.s16.bold(),
                    color = TextPrimary
                )
                if (customerPhone.isNotBlank()) {
                    Text(
                        text = customerPhone,
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary
                    )
                }
            }

            if (customerPhone.isNotBlank()) {
                Spacer(modifier = Modifier.width(AppSpacing.S))
                Button(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$customerPhone")
                        }
                        context.startActivity(dialIntent)
                    },
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor,
                        contentColor = OnPrimaryColor
                    ),
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = stringResource(R.string.driver_trip_call_customer),
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XS))
                    Text(
                        text = stringResource(R.string.driver_trip_call_customer),
                        style = MaterialTheme.typography.s12.bold()
                    )
                }
            }
        }
    }
}
