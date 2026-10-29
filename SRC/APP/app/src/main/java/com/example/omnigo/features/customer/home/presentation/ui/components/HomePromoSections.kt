package com.example.omnigo.features.customer.home.presentation.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryVariant
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryVariant
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.ui.theme.WarningColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s16

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
        onClick = onClick,
        modifier = Modifier.width(Dimen.WidthPromoBanner),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXXS)
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
                            modifier = Modifier.padding(horizontal = Dimen.PaddingXSPlus, vertical = Dimen.PaddingXXS)
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
                        modifier = Modifier.padding(
                            horizontal = Dimen.PaddingSM,
                            vertical = Dimen.PaddingXXS
                        ),
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
