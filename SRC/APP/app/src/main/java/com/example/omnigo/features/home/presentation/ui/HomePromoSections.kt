package com.example.omnigo.features.home.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryVariant
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryVariant
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.ui.theme.WarningColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold

@Composable
fun HomePromoBannersSection(
    onBannerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.home_section_promos),
                style = MaterialTheme.typography.s16.bold(),
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.M))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.MediumLarge),
            contentPadding = PaddingValues(horizontal = Dimen.PaddingM)
        ) {
            item {
                PromoBannerCard(
                    title = stringResource(id = R.string.home_banner_title_1),
                    desc = stringResource(id = R.string.home_banner_desc_1),
                    badge = "HOT DEAL",
                    cta = stringResource(id = R.string.home_banner_cta),
                    gradientColors = listOf(PrimaryColor, PrimaryVariant),
                    icon = Icons.Filled.LocalOffer,
                    onClick = { onBannerClick("OMNIRIDE50") }
                )
            }

            item {
                PromoBannerCard(
                    title = stringResource(id = R.string.home_banner_title_2),
                    desc = stringResource(id = R.string.home_banner_desc_2),
                    badge = "FREESHIP",
                    cta = stringResource(id = R.string.home_banner_food_cta),
                    gradientColors = listOf(SecondaryColor, SecondaryVariant),
                    icon = Icons.Filled.Fastfood,
                    onClick = { onBannerClick("FREESHIP50") }
                )
            }
        }
    }
}

@Composable
private fun PromoBannerCard(
    title: String,
    desc: String,
    badge: String,
    cta: String,
    gradientColors: List<Color>,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(Dimen.PaddingM)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(AppShape.ShapeXXS),
                        color = WarningColor
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.s10.bold(),
                            color = TextWhite,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TextWhite.copy(alpha = 0.8f),
                        modifier = Modifier.size(Dimen.SizeL)
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                Text(
                    text = title,
                    style = MaterialTheme.typography.s16.bold(),
                    color = TextWhite,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(AppSpacing.XXS))

                Text(
                    text = desc,
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextWhite.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(AppSpacing.M))

                Surface(
                    shape = RoundedCornerShape(AppShape.ShapeXL2),
                    color = TextWhite.copy(alpha = 0.2f),
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXXS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cta,
                            style = MaterialTheme.typography.s12.bold(),
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XXS))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomePopularPlacesSection(
    onPlaceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingM)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.home_section_popular),
                style = MaterialTheme.typography.s16.bold(),
                color = TextPrimary
            )

            Text(
                text = stringResource(id = R.string.home_view_all),
                style = MaterialTheme.typography.s12.bold(),
                color = PrimaryColor,
                modifier = Modifier.clickable { onPlaceClick("all") }
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.M))

        PopularPlaceItem(
            name = stringResource(id = R.string.home_suggest_pho),
            desc = stringResource(id = R.string.home_suggest_pho_desc),
            icon = Icons.Filled.RamenDining,
            iconTint = SecondaryColor,
            rating = "4.9",
            onClick = { onPlaceClick("pho_bo") }
        )

        Spacer(modifier = Modifier.height(AppSpacing.SPlus))

        PopularPlaceItem(
            name = stringResource(id = R.string.home_suggest_coffee),
            desc = stringResource(id = R.string.home_suggest_coffee_desc),
            icon = Icons.Filled.LocalCafe,
            iconTint = PrimaryColor,
            rating = "4.8",
            onClick = { onPlaceClick("highlands") }
        )

        Spacer(modifier = Modifier.height(AppSpacing.SPlus))

        PopularPlaceItem(
            name = stringResource(id = R.string.home_suggest_bmt),
            desc = stringResource(id = R.string.home_suggest_bmt_desc),
            icon = Icons.Filled.Fastfood,
            iconTint = SecondaryColor,
            rating = "4.7",
            onClick = { onPlaceClick("banh_mi") }
        )
    }
}

@Composable
private fun PopularPlaceItem(
    name: String,
    desc: String,
    icon: ImageVector,
    iconTint: Color,
    rating: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    .size(50.dp)
                    .clip(RoundedCornerShape(AppShape.ShapeM))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = iconTint,
                    modifier = Modifier.size(Dimen.SizeL)
                )
            }

            Spacer(modifier = Modifier.width(AppSpacing.MediumLarge))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(AppShape.ShapeXXS),
                        color = WarningColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = WarningColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = rating,
                                style = MaterialTheme.typography.s10.bold(),
                                color = WarningColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.XXS))

                Text(
                    text = desc,
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
            }
        }
    }
}
