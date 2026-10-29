package com.example.omnigo.features.driver.food.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s20
import com.example.omnigo.utils.semiBold

@Composable
fun DeliveryCompactHeaderAndStepper(
    orderId: Long,
    isPickupStep: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
            ) {
                Text(
                    text = stringResource(id = R.string.driver_delivery_order_code_label),
                    style = MaterialTheme.typography.s12.semiBold(),
                    color = TextSecondary
                )
                Text(
                    text = "#$orderId",
                    style = MaterialTheme.typography.s20.bold(),
                    color = PrimaryColor
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppShape.PillShape))
                    .background(PrimaryContainer)
                    .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        id = if (isExpanded) {
                            R.string.driver_nav_sheet_collapse
                        } else {
                            R.string.driver_nav_sheet_expand
                        }
                    ),
                    style = MaterialTheme.typography.s12.bold(),
                    color = PrimaryColor
                )
                Spacer(modifier = Modifier.width(AppSpacing.XXS))
                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Default.KeyboardArrowDown
                    } else {
                        Icons.Default.KeyboardArrowUp
                    },
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeS2)
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.S))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AppShape.ShapeM))
                    .background(if (isPickupStep) PrimaryColor else SuccessColor.copy(alpha = 0.14f))
                    .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXSPlus),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.driver_delivery_stage_1_label),
                    style = MaterialTheme.typography.s12.bold(),
                    color = if (isPickupStep) OnPrimaryColor else SuccessColor
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AppShape.ShapeM))
                    .background(if (!isPickupStep) SuccessColor else SurfaceVariantLight)
                    .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXSPlus),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.driver_delivery_stage_2_label),
                    style = MaterialTheme.typography.s12.bold(),
                    color = if (!isPickupStep) OnPrimaryColor else TextSecondary
                )
            }
        }
    }
}

