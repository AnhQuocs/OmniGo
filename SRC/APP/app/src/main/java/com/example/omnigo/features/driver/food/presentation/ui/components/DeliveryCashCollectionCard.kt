package com.example.omnigo.features.driver.food.presentation.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s20
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun DeliveryCashCollectionCard(
    isPaid: Boolean,
    cashAmountToCollect: Double,
    modifier: Modifier = Modifier
) {
    val containerBg = if (isPaid) {
        SuccessColor.copy(alpha = 0.10f)
    } else {
        ErrorColor.copy(alpha = 0.10f)
    }

    val accentColor = if (isPaid) SuccessColor else ErrorColor

    Card(
        shape = RoundedCornerShape(AppShape.ShapeM),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeM)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPaid) {
                            stringResource(id = R.string.driver_delivery_paid_online_title)
                        } else {
                            stringResource(id = R.string.driver_delivery_cash_title)
                        },
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Text(
                        text = if (isPaid) {
                            stringResource(id = R.string.driver_delivery_paid_online_note)
                        } else {
                            stringResource(id = R.string.driver_delivery_cash_note)
                        },
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.ShapeS))
                        .background(containerBg)
                        .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPaid) {
                            "0 ₫"
                        } else {
                            String.format(Locale.US, "%,.0f ₫", cashAmountToCollect)
                        },
                        style = MaterialTheme.typography.s20.bold(),
                        color = accentColor
                    )
                }
            }
        }
    }
}
