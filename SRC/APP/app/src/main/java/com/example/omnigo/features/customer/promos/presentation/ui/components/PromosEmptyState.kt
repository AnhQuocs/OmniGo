package com.example.omnigo.features.customer.promos.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
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
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold

@Composable
fun PromosEmptyState(
    onResetFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeXL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationS),
        border = BorderStroke(Dimen.BorderHairline, CardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingXL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeUltra)
                    .clip(RoundedCornerShape(AppShape.PillShape))
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeXXL)
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Text(
                text = stringResource(id = R.string.promos_empty),
                style = MaterialTheme.typography.s16.bold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.XS))

            OutlinedButton(
                onClick = onResetFilter,
                shape = RoundedCornerShape(AppShape.PillShape),
                border = BorderStroke(Dimen.BorderHairline, PrimaryColor)
            ) {
                Text(
                    text = stringResource(id = R.string.promos_empty_filter_btn),
                    style = MaterialTheme.typography.s12.semiBold(),
                    color = PrimaryColor
                )
            }
        }
    }
}

