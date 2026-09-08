package com.example.omnigo.features.promos.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@Composable
fun PromosScreen(
    modifier: Modifier = Modifier
) {
    var promoCode by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filters = listOf(
        stringResource(id = R.string.promos_filter_all),
        stringResource(id = R.string.promos_filter_ride),
        stringResource(id = R.string.promos_filter_food)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .padding(horizontal = Dimen.PaddingM)
    ) {
        Text(
            text = stringResource(id = R.string.promos_title),
            style = MaterialTheme.typography.s18.bold(),
            color = TextPrimary,
            modifier = Modifier.padding(vertical = Dimen.PaddingM)
        )

        // Input promo code
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promoCode,
                onValueChange = { promoCode = it.uppercase() },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.promos_input_hint),
                        style = MaterialTheme.typography.s14.normal(),
                        color = TextSecondary
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = CardBorderColor,
                    focusedContainerColor = SurfaceLight,
                    unfocusedContainerColor = SurfaceLight
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(AppSpacing.S))

            Button(
                onClick = { /* Apply promo code */ },
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                modifier = Modifier.height(54.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.promos_apply_btn),
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.M))

        // Filter chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
        ) {
            filters.forEachIndexed { index, filter ->
                FilterChip(
                    selected = selectedFilterIndex == index,
                    onClick = { selectedFilterIndex = index },
                    label = {
                        Text(
                            text = filter,
                            style = if (selectedFilterIndex == index) {
                                MaterialTheme.typography.s12.bold()
                            } else {
                                MaterialTheme.typography.s12.normal()
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryColor,
                        selectedLabelColor = TextWhite,
                        containerColor = SurfaceLight,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilterIndex == index,
                        borderColor = CardBorderColor,
                        selectedBorderColor = PrimaryColor
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.M))

        // Voucher List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
        ) {
            item {
                VoucherCard(
                    title = stringResource(id = R.string.promos_voucher_title_1),
                    desc = stringResource(id = R.string.promos_voucher_desc_1),
                    badge = "OMNIRIDE",
                    iconColor = PrimaryColor,
                    containerColor = PrimaryContainer
                )
            }
            item {
                VoucherCard(
                    title = stringResource(id = R.string.promos_voucher_title_2),
                    desc = stringResource(id = R.string.promos_voucher_desc_2),
                    badge = "OMNIFOOD",
                    iconColor = SecondaryColor,
                    containerColor = SecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun VoucherCard(
    title: String,
    desc: String,
    badge: String,
    iconColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeM),
        color = SurfaceLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(AppShape.ShapeM))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalOffer,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(Dimen.SizeM)
                )
            }

            Spacer(modifier = Modifier.width(AppSpacing.M))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(AppShape.ShapeXXS),
                    color = iconColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.s10.bold(),
                        color = iconColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.XXS))

                Text(
                    text = title,
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextPrimary
                )

                Text(
                    text = desc,
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
            }

            Button(
                onClick = { /* Use voucher */ },
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.promos_use_now),
                    style = MaterialTheme.typography.s12.semiBold(),
                    color = TextWhite
                )
            }
        }
    }
}
