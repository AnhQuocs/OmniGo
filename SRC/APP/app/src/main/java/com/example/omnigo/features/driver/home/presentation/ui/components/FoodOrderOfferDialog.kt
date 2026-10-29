package com.example.omnigo.features.driver.home.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.omnigo.features.driver.domain.model.DriverFoodOffer
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.SurfaceLight

@Composable
fun FoodOrderOfferDialog(
    offer: DriverFoodOffer,
    countdownSeconds: Int,
    countdownProgress: Float,
    isAccepting: Boolean,
    isRejecting: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingM),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(AppShape.ShapeXL2),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingS),
                modifier = modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimen.PaddingML, vertical = Dimen.PaddingL)
                ) {
                    FoodOfferHeader(
                        countdownSeconds = countdownSeconds,
                        countdownProgress = countdownProgress
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.M))

                    FoodOfferSummaryCard(offer = offer)

                    Spacer(modifier = Modifier.height(AppSpacing.SPlus))

                    FoodOfferCustomerInfoRow(offer = offer)

                    Spacer(modifier = Modifier.height(AppSpacing.SPlus))

                    FoodOfferRouteCard(offer = offer)

                    Spacer(modifier = Modifier.height(AppSpacing.L))

                    FoodOfferActionButtons(
                        countdownSeconds = countdownSeconds,
                        isAccepting = isAccepting,
                        isRejecting = isRejecting,
                        onAccept = onAccept,
                        onReject = onReject
                    )
                }
            }
        }
    }
}
