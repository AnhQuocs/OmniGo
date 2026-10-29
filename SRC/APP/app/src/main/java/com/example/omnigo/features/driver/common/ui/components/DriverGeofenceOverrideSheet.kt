package com.example.omnigo.features.driver.common.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.WarningColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun DriverGeofenceOverrideSheet(
    distanceMeters: Double?,
    onConfirmOverride: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(AppShape.ShapeXL),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingL),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeXXLPlus)
                        .clip(CircleShape)
                        .background(SecondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = WarningColor,
                        modifier = Modifier.size(Dimen.SizeL)
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

                Text(
                    text = stringResource(id = R.string.driver_geofence_override_title),
                    style = MaterialTheme.typography.s18.bold(),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppSpacing.S))

                val descriptionText = if (distanceMeters != null && distanceMeters > 0.0) {
                    val formattedDist = if (distanceMeters >= 1000.0) {
                        String.format(Locale.US, "%.1f km", distanceMeters / 1000.0)
                    } else {
                        String.format(Locale.US, "%.0f m", distanceMeters)
                    }
                    stringResource(id = R.string.driver_geofence_override_desc, formattedDist)
                } else {
                    stringResource(id = R.string.driver_geofence_override_desc_no_gps)
                }

                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.s14.normal(),
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppSpacing.L))

                Button(
                    onClick = onConfirmOverride,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarningColor,
                        contentColor = OnPrimaryColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault)
                ) {
                    Text(
                        text = stringResource(id = R.string.driver_geofence_override_confirm),
                        style = MaterialTheme.typography.s14.bold()
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault)
                ) {
                    Text(
                        text = stringResource(id = R.string.driver_geofence_override_cancel),
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
