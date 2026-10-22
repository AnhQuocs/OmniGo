package com.example.omnigo.features.customer.food.presentation.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s16

@Composable
fun OrderDetailRestaurantHeroHeader(
    restaurantName: String,
    restaurantAddress: String?,
    restaurantImageUrl: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimen.HeightPromoBanner),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Cover background image
            if (!restaurantImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = restaurantImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.82f)
                            )
                        )
                    )
            )

            // 3. Avatar & Restaurant metadata at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(Dimen.PaddingM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Round Avatar with White Border
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeXXL)
                        .clip(CircleShape)
                        .border(Dimen.PaddingXXS, TextWhite, CircleShape)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!restaurantImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = restaurantImageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            placeholder = rememberVectorPainter(Icons.Default.Restaurant),
                            error = rememberVectorPainter(Icons.Default.Restaurant),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(Dimen.SizeM)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacing.M))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = restaurantName.ifBlank { stringResource(id = R.string.order_detail_restaurant_title) },
                        style = MaterialTheme.typography.s16.bold(),
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.XXS))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (!restaurantAddress.isNullOrBlank()) {
                                restaurantAddress
                            } else {
                                stringResource(id = R.string.food_title)
                            },
                            style = MaterialTheme.typography.s13.medium(),
                            color = TextWhite.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(AppSpacing.XS))

                        Text(
                            text = "• 4.7",
                            style = MaterialTheme.typography.s13.medium(),
                            color = TextWhite.copy(alpha = 0.88f)
                        )

                        Spacer(modifier = Modifier.width(AppSpacing.XXS))

                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = SecondaryColor,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                    }
                }
            }
        }
    }
}
