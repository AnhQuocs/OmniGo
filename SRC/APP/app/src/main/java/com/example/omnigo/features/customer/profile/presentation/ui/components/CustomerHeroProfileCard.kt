package com.example.omnigo.features.customer.profile.presentation.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.RatingGold
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@Composable
fun CustomerHeroProfileCard(
    userName: String,
    userPhone: String,
    avatarUrl: String?,
    isUploadingAvatar: Boolean,
    onProfileClick: () -> Unit,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
    totalRides: Int = 12,
    totalFoodOrders: Int = 8,
    availablePromos: Int = 5
) {
    val context = LocalContext.current

    Card(
        onClick = onProfileClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationM),
        border = BorderStroke(Dimen.BorderHairline, CardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with camera badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(Dimen.SizeXXLPlus)
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(Dimen.PaddingXXS, PrimaryColor, CircleShape)
                                .clickable(onClick = onAvatarClick)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(PrimaryContainer)
                                .clickable(onClick = onAvatarClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.firstOrNull()?.uppercase() ?: "O",
                                style = MaterialTheme.typography.s18.bold(),
                                color = PrimaryColor
                            )
                        }
                    }

                    if (isUploadingAvatar) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = TextWhite,
                                modifier = Modifier.size(Dimen.SizeM)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(Dimen.SizeM)
                            .clip(CircleShape)
                            .background(PrimaryColor)
                            .border(Dimen.PaddingXXS, SurfaceLight, CircleShape)
                            .clickable(onClick = onAvatarClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Avatar",
                            tint = TextWhite,
                            modifier = Modifier.size(Dimen.SizeIconXS)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacing.MediumLarge))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.s16.bold(),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.XXS))

                    Text(
                        text = userPhone,
                        style = MaterialTheme.typography.s14.normal(),
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.XS))

                    // Member tier capsule pill
                    Surface(
                        shape = RoundedCornerShape(AppShape.ShapeXS),
                        color = PrimaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = RatingGold,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.XS))
                            Text(
                                text = stringResource(id = R.string.profile_tier_member),
                                style = MaterialTheme.typography.s12.semiBold(),
                                color = PrimaryColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

            // 3-Metric Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryContainer.copy(alpha = 0.35f), RoundedCornerShape(AppShape.ShapeM))
                    .padding(vertical = Dimen.PaddingS, horizontal = Dimen.PaddingM),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomerMetricItem(
                    icon = Icons.Default.DirectionsCar,
                    count = totalRides.toString(),
                    label = stringResource(id = R.string.profile_metric_trips)
                )
                Box(
                    modifier = Modifier
                        .height(Dimen.SizeM)
                        .width(Dimen.BorderHairline)
                        .background(CardBorderColor)
                )
                CustomerMetricItem(
                    icon = Icons.Default.Fastfood,
                    count = totalFoodOrders.toString(),
                    label = stringResource(id = R.string.profile_metric_orders)
                )
                Box(
                    modifier = Modifier
                        .height(Dimen.SizeM)
                        .width(Dimen.BorderHairline)
                        .background(CardBorderColor)
                )
                CustomerMetricItem(
                    icon = Icons.Default.LocalOffer,
                    count = availablePromos.toString(),
                    label = stringResource(id = R.string.profile_metric_promos)
                )
            }
        }
    }
}

@Composable
private fun CustomerMetricItem(
    icon: ImageVector,
    count: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(Dimen.SizeS)
            )
            Spacer(modifier = Modifier.width(AppSpacing.XXS))
            Text(
                text = count,
                style = MaterialTheme.typography.s14.bold(),
                color = TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.XXS))
        Text(
            text = label,
            style = MaterialTheme.typography.s12.medium(),
            color = TextSecondary
        )
    }
}

