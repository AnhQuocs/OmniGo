package com.example.omnigo.features.customer.home.presentation.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16

@Composable
fun CustomerQrScanDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(AppShape.ShapeXL),
            color = SurfaceLight,
            shadowElevation = 8.dp,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingS)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingL),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.home_qr_dialog_title),
                        style = MaterialTheme.typography.s16.bold(),
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(Dimen.SizeL)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(Dimen.SizeM)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Simulated QR presentation box
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(AppShape.ShapeL))
                        .background(PrimaryContainer)
                        .padding(Dimen.PaddingM),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCode2,
                        contentDescription = stringResource(id = R.string.home_qr_my_code),
                        tint = PrimaryColor,
                        modifier = Modifier.size(150.dp)
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.M))

                Text(
                    text = stringResource(id = R.string.home_qr_my_code),
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(AppSpacing.XS))

                Text(
                    text = stringResource(id = R.string.home_qr_dialog_desc),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = Dimen.PaddingM)
                )

                Spacer(modifier = Modifier.height(AppSpacing.L))

                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(AppShape.ShapeL),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault)
                ) {
                    Text(
                        text = stringResource(id = R.string.promos_detail_close),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextWhite
                    )
                }
            }
        }
    }
}

