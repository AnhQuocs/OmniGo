package com.example.omnigo.features.customer.food.presentation.ui.review.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.SecondaryColor

@Composable
fun RatingBarSection(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isReadOnly: Boolean = false,
    activeColor: Color = SecondaryColor,
    inactiveColor: Color = Color(0xFFD0D5DD)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            val isSelected = i <= rating
            val interactionSource = remember { MutableInteractionSource() }
            Icon(
                imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.StarOutline,
                contentDescription = "$i stars",
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier
                    .size(Dimen.SizeL)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = !isReadOnly
                    ) {
                        onRatingChanged(i)
                    }
            )
        }
    }
}
