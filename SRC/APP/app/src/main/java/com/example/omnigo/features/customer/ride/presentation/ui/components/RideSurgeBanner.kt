package com.example.omnigo.features.customer.ride.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.semiBold

@Composable
fun RideSurgeBanner(
    surgeMultiplier: Double,
    surgeLevel: String?,
    modifier: Modifier = Modifier
) {
    if (surgeMultiplier <= 1.0) return

    val formattedMultiplier = String.format(java.util.Locale.US, "%.1f", surgeMultiplier)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = SecondaryContainer,
                shape = RoundedCornerShape(AppShape.ShapeM)
            )
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = SecondaryColor,
            modifier = Modifier.size(Dimen.SizeM)
        )
        Spacer(modifier = Modifier.width(AppSpacing.S))
        Text(
            text = stringResource(id = R.string.ride_surge_banner_text, formattedMultiplier),
            style = MaterialTheme.typography.s12.semiBold(),
            color = SecondaryColor
        )
    }
}
