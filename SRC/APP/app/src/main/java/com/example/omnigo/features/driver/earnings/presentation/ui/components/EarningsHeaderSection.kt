package com.example.omnigo.features.driver.earnings.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s24

@Composable
fun EarningsHeaderSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingXS)
    ) {
        Text(
            text = stringResource(id = R.string.driver_nav_earnings),
            style = MaterialTheme.typography.s24.bold(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.XXS))

        Text(
            text = stringResource(id = R.string.driver_earnings_subtitle),
            style = MaterialTheme.typography.s14.normal(),
            color = TextSecondary
        )
    }
}

