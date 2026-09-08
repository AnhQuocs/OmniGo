package com.example.omnigo.features.home.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
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

@Composable
fun HomeHeaderSection(
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.weight(1f, fill = false),
            shape = RoundedCornerShape(AppShape.ShapeXL2),
            color = SurfaceLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXSPlus),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeM)
                )

                Spacer(modifier = Modifier.width(AppSpacing.XSPlus))

                Column {
                    Text(
                        text = stringResource(id = R.string.home_location_label),
                        style = MaterialTheme.typography.s10.normal(),
                        color = TextSecondary
                    )
                    Text(
                        text = stringResource(id = R.string.home_location_default),
                        style = MaterialTheme.typography.s13.bold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.XS))

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(Dimen.SizeS)
                )
            }
        }

        Spacer(modifier = Modifier.width(AppSpacing.SPlus))

        Box {
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(Dimen.SizeXLPlus)
                    .clip(CircleShape)
                    .background(SurfaceLight)
                    .border(1.dp, CardBorderColor, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = TextPrimary,
                    modifier = Modifier.size(Dimen.SizeM)
                )
            }

            Box(
                modifier = Modifier
                    .size(9.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 2.dp)
                    .clip(CircleShape)
                    .background(ErrorColor)
            )
        }
    }
}

@Composable
fun HomeQuickWalletSection(
    onTopUpClick: () -> Unit = {},
    onScanQrClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingM),
        shape = RoundedCornerShape(AppShape.ShapeL),
        color = SurfaceLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.SPlus))

                Column {
                    Text(
                        text = stringResource(id = R.string.home_wallet_balance),
                        style = MaterialTheme.typography.s10.medium(),
                        color = TextSecondary
                    )
                    Text(
                        text = stringResource(id = R.string.home_wallet_balance_mock),
                        style = MaterialTheme.typography.s16.bold(),
                        color = TextPrimary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.clickable { onTopUpClick() },
                    shape = RoundedCornerShape(AppShape.ShapeXL2),
                    color = PrimaryColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XXS))
                        Text(
                            text = stringResource(id = R.string.home_wallet_topup),
                            style = MaterialTheme.typography.s12.bold(),
                            color = TextWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacing.S))

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SecondaryContainer)
                        .clickable { onScanQrClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = stringResource(id = R.string.home_wallet_qr),
                        tint = SecondaryColor,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeSearchSection(
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingM)
            .clickable { onSearchClick() },
        shape = RoundedCornerShape(AppShape.ShapeXL2),
        color = SurfaceLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeM)
                )

                Spacer(modifier = Modifier.width(AppSpacing.SPlus))

                Text(
                    text = stringResource(id = R.string.home_search_placeholder),
                    style = MaterialTheme.typography.s14.normal(),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Filled.Tune,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(Dimen.SizeM)
            )
        }
    }
}

@Composable
fun HomeFilterChipsSection(
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf(0) }
    val filters = listOf(
        R.string.home_filter_all,
        R.string.home_filter_nearby,
        R.string.home_filter_promos,
        R.string.home_filter_top_rated
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Dimen.PaddingM)
    ) {
        items(filters.size) { index ->
            val isSelected = selectedIndex == index
            FilterChip(
                selected = isSelected,
                onClick = { selectedIndex = index },
                label = {
                    Text(
                        text = stringResource(id = filters[index]),
                        style = if (isSelected) MaterialTheme.typography.s12.bold() else MaterialTheme.typography.s12.normal()
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryContainer,
                    selectedLabelColor = PrimaryColor,
                    containerColor = SurfaceLight,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) PrimaryColor else CardBorderColor,
                    borderWidth = 1.dp
                ),
                shape = RoundedCornerShape(AppShape.ShapeXL2)
            )
        }
    }
}