package com.example.omnigo.features.customer.ride.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.s20
import com.example.omnigo.utils.semiBold

@Composable
fun RideSearchingRadarDialog(
    countdownSeconds: Int,
    isCancelling: Boolean,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmCancelDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { /* Prevent accidental dismiss without explicit cancel */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingL),
            shape = RoundedCornerShape(AppShape.ShapeXL),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = Dimen.PaddingS
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingL),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.ride_radar_title),
                    style = MaterialTheme.typography.s20.bold(),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppSpacing.XS))

                Text(
                    text = stringResource(id = R.string.ride_radar_subtitle),
                    style = MaterialTheme.typography.s12.medium(),
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Radar Pulsing Animation
                RadarPulseAnimation()

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Countdown Label
                Text(
                    text = stringResource(id = R.string.ride_radar_countdown_label, countdownSeconds),
                    style = MaterialTheme.typography.s16.semiBold(),
                    color = PrimaryColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Cancel Button
                Button(
                    onClick = { showConfirmCancelDialog = true },
                    enabled = !isCancelling,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorColor.copy(alpha = 0.1f),
                        contentColor = ErrorColor
                    ),
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimen.SizeM),
                            color = ErrorColor,
                            strokeWidth = Dimen.PaddingXXS
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.ride_cancel_searching_button),
                            style = MaterialTheme.typography.s14.bold()
                        )
                    }
                }
            }
        }
    }

    if (showConfirmCancelDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmCancelDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.ride_cancel_confirm_title),
                    style = MaterialTheme.typography.s18.bold()
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.ride_cancel_confirm_message),
                    style = MaterialTheme.typography.s14.medium()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmCancelDialog = false
                        onCancelClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text(
                        text = stringResource(id = R.string.ride_cancel_confirm_yes),
                        color = TextWhite,
                        style = MaterialTheme.typography.s14.bold()
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmCancelDialog = false }) {
                    Text(
                        text = stringResource(id = R.string.ride_cancel_confirm_no),
                        style = MaterialTheme.typography.s14.medium()
                    )
                }
            }
        )
    }
}

@Composable
private fun RadarPulseAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")

    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse1"
    )

    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Alpha1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse2"
    )

    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Alpha2"
    )

    Box(
        modifier = modifier.size(Dimen.SizeUltra),
        contentAlignment = Alignment.Center
    ) {
        // Outer wave 1
        Box(
            modifier = Modifier
                .size(Dimen.SizeXL)
                .scale(pulse1)
                .clip(CircleShape)
                .background(PrimaryColor.copy(alpha = alpha1))
        )

        // Outer wave 2
        Box(
            modifier = Modifier
                .size(Dimen.SizeXL)
                .scale(pulse2)
                .clip(CircleShape)
                .background(PrimaryColor.copy(alpha = alpha2))
        )

        // Center car beacon
        Box(
            modifier = Modifier
                .size(Dimen.SizeXL)
                .clip(CircleShape)
                .background(PrimaryColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(Dimen.SizeM)
            )
        }
    }
}
