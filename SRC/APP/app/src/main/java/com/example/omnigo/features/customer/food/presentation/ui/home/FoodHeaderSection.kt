package com.example.omnigo.features.customer.food.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18

@Composable
fun FoodHeaderSection(
    onBackClick: () -> Unit,
    userAddressLabel: String? = null,
    onHistoryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceLight)
            .statusBarsPadding()
            .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingS),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.food_cd_back),
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.XS))

                if (!userAddressLabel.isNullOrBlank()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = PrimaryColor,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.XXS))
                            Text(
                                text = stringResource(id = R.string.food_tracking_destination_default),
                                style = MaterialTheme.typography.s12.medium(),
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = userAddressLabel,
                            style = MaterialTheme.typography.s16.bold(),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        text = stringResource(id = R.string.food_title),
                        style = MaterialTheme.typography.s18.bold(),
                        color = TextPrimary
                    )
                }
            }

            if (onHistoryClick != null) {
                IconButton(onClick = onHistoryClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = stringResource(id = R.string.food_history_title),
                        tint = PrimaryColor
                    )
                }
            }
        }
    }
}
