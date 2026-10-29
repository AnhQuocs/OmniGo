package com.example.omnigo.features.driver.common.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.omnigo.ui.theme.ErrorContainer
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SecondaryVariant
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s20
import java.util.Locale

@Composable
fun DriverCashPaymentBanner(
    isCash: Boolean,
    amountToCollect: Double,
    modifier: Modifier = Modifier
) {
    val bgContainer = if (isCash) SecondaryContainer else PrimaryContainer
    val borderColor = if (isCash) SecondaryColor.copy(alpha = 0.5f) else SuccessColor.copy(alpha = 0.4f)
    val accentColor = if (isCash) SecondaryVariant else SuccessColor
    val badgeBg = if (isCash) ErrorContainer else SuccessColor.copy(alpha = 0.15f)

    val formattedAmount = if (isCash) {
        String.format(Locale.US, "%,.0f ₫", amountToCollect)
    } else {
        "0 ₫"
    }

    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = bgContainer),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = borderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeXLPlus)
                        .clip(CircleShape)
                        .background(SurfaceLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCash) Icons.Default.Payments else Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = if (isCash) SecondaryColor else PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.M))

                Column {
                    Text(
                        text = stringResource(
                            id = if (isCash) {
                                R.string.driver_payment_banner_cash_title
                            } else {
                                R.string.driver_payment_banner_online_title
                            }
                        ),
                        style = MaterialTheme.typography.s13.bold(),
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Text(
                        text = stringResource(
                            id = if (isCash) {
                                R.string.driver_payment_banner_cash_subtitle
                            } else {
                                R.string.driver_payment_banner_online_subtitle
                            }
                        ),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.S))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppShape.ShapeM))
                    .background(badgeBg)
                    .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingS),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.s20.bold(),
                    color = accentColor
                )
            }
        }
    }
}
