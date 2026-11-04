package com.example.omnigo.features.customer.ride.presentation.ui.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppSpacing

@Composable
internal fun RidePaymentSelector(selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)) {
        FilterChip(selected = selected == "CASH", onClick = { onSelect("CASH") },
            label = { Text(stringResource(R.string.ride_payment_cash_label)) })
        FilterChip(selected = selected == "WALLET", onClick = { onSelect("WALLET") },
            label = { Text(stringResource(R.string.ride_payment_wallet_prepaid)) })
    }
}
