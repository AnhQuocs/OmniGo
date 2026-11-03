package com.example.omnigo.features.customer.promos.presentation.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.HeroGradientEnd
import com.example.omnigo.ui.theme.HeroGradientMiddle
import com.example.omnigo.ui.theme.HeroGradientStart
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
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
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@Composable
fun PromosTopBar(
    totalVouchersCount: Int,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimen.HeightTopBar)
            .padding(horizontal = Dimen.PaddingM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.promos_title),
                style = MaterialTheme.typography.s18.bold(),
                color = TextPrimary,
                maxLines = 1
            )
            if (totalVouchersCount > 0) {
                Spacer(modifier = Modifier.width(AppSpacing.S))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.PillShape))
                        .background(PrimaryContainer)
                        .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                ) {
                    Text(
                        text = stringResource(id = R.string.promos_badge_total, totalVouchersCount),
                        style = MaterialTheme.typography.s12.bold(),
                        color = PrimaryColor,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(AppSpacing.S))

        OutlinedButton(
            onClick = onOpenHistory,
            shape = RoundedCornerShape(AppShape.PillShape),
            border = BorderStroke(Dimen.BorderHairline, CardBorderColor),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceLight),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = Dimen.PaddingSM,
                vertical = Dimen.PaddingXS
            )
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(Dimen.SizeS)
            )
            Spacer(modifier = Modifier.width(AppSpacing.XS))
            Text(
                text = stringResource(id = R.string.promos_history_btn),
                style = MaterialTheme.typography.s12.semiBold(),
                color = PrimaryColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun PromosHeroBanner(
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.linearGradient(
        listOf(HeroGradientStart, HeroGradientMiddle, Color(0xFF0284C7))
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeXL),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationM)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(Dimen.PaddingM)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppShape.PillShape))
                            .background(Color.White.copy(alpha = 0.22f))
                            .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                    ) {
                        Text(
                            text = stringResource(id = R.string.promos_tag_hot),
                            style = MaterialTheme.typography.s10.bold(),
                            color = Color.White
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(Dimen.SizeL)
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                Text(
                    text = stringResource(id = R.string.promos_hero_title),
                    style = MaterialTheme.typography.s18.bold(),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(AppSpacing.XXS))

                Text(
                    text = stringResource(id = R.string.promos_hero_subtitle),
                    style = MaterialTheme.typography.s12.normal(),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun PromosInputBar(
    code: String,
    isLoading: Boolean,
    onCodeChange: (String) -> Unit,
    onPasteCode: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationS),
        border = BorderStroke(Dimen.BorderHairline, CardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = code,
                onValueChange = onCodeChange,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.promos_input_hint),
                        style = MaterialTheme.typography.s13.normal(),
                        color = TextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                },
                trailingIcon = {
                    if (code.isBlank()) {
                        IconButton(onClick = onPasteCode) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = stringResource(id = R.string.promos_paste_btn),
                                tint = PrimaryColor,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(Dimen.HeightInputField),
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(AppSpacing.S))

            Button(
                onClick = onApply,
                enabled = code.isNotBlank() && !isLoading,
                modifier = Modifier.height(Dimen.HeightDefault),
                shape = RoundedCornerShape(AppShape.PillShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    disabledContainerColor = CardBorderColor
                )
            ) {
                Text(
                    text = stringResource(id = R.string.promos_apply_btn),
                    style = MaterialTheme.typography.s13.bold(),
                    color = if (code.isNotBlank() && !isLoading) TextWhite else TextSecondary
                )
            }
        }
    }
}

@Composable
fun PromosFilterPills(
    selectedIndex: Int,
    allCount: Int,
    rideCount: Int,
    foodCount: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val filterItems = listOf(
        Pair(0, stringResource(id = R.string.promos_filter_all_label, allCount)),
        Pair(1, stringResource(id = R.string.promos_filter_ride_label, rideCount)),
        Pair(2, stringResource(id = R.string.promos_filter_food_label, foodCount))
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
    ) {
        filterItems.forEach { (index, title) ->
            val isSelected = selectedIndex == index
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AppShape.PillShape))
                    .clickable { onSelectIndex(index) },
                shape = RoundedCornerShape(AppShape.PillShape),
                color = if (isSelected) PrimaryColor else SurfaceLight,
                border = BorderStroke(
                    width = if (isSelected) Dimen.BorderThin else Dimen.BorderHairline,
                    color = if (isSelected) PrimaryColor else CardBorderColor
                ),
                shadowElevation = if (isSelected) Dimen.ElevationS else Dimen.ElevationNone
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimen.PaddingS),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = if (isSelected) MaterialTheme.typography.s12.bold() else MaterialTheme.typography.s12.medium(),
                        color = if (isSelected) TextWhite else TextSecondary
                    )
                }
            }
        }
    }
}
