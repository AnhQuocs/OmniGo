package com.example.omnigo.features.customer.activity.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen

@Composable
fun ActivityListSection(
    isHistory: Boolean,
    modifier: Modifier = Modifier
) {
    val address1 = stringResource(id = R.string.activity_mock_ride_address)
    val address2 = stringResource(id = R.string.activity_mock_food_name)
    
    val status1 = if (isHistory) {
        stringResource(id = R.string.activity_mock_status_completed)
    } else {
        stringResource(id = R.string.activity_mock_status_ongoing)
    }

    val price1 = stringResource(id = R.string.activity_mock_price_1)
    val price2 = stringResource(id = R.string.activity_mock_price_2)

    val date1 = stringResource(id = R.string.activity_mock_date_1)
    val date2 = stringResource(id = R.string.activity_mock_date_2)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimen.PaddingM),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
    ) {
        ActivityItemCard(
            type = ActivityType.RIDE,
            title = address1,
            date = date1,
            price = price1,
            status = status1,
            isCompleted = isHistory
        )

        if (isHistory) {
            ActivityItemCard(
                type = ActivityType.FOOD,
                title = address2,
                date = date2,
                price = price2,
                status = stringResource(id = R.string.activity_mock_status_completed),
                isCompleted = true
            )
        }
    }
}
