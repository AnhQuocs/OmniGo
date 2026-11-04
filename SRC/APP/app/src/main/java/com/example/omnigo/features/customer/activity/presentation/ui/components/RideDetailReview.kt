package com.example.omnigo.features.customer.activity.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.BookingReview
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OutlineLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.RatingGold
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14

/** Hiển thị đánh giá đã gửi (read-only). */
@Composable
internal fun RideDetailReviewDisplay(review: BookingReview) {
    RideDetailSectionCard(title = stringResource(R.string.ride_detail_review_section)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.S)) {
            Row {
                for (s in 1..5) {
                    val filled = s <= review.rating
                    Icon(
                        imageVector = if (filled) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = null,
                        tint = if (filled) RatingGold else OutlineLight,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                }
            }
            if (review.comment.isNotBlank()) {
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
            }
        }
    }
}

/** Form đánh giá chuyến xe đã hoàn thành (chọn sao + nhận xét + gửi). */
@Composable
internal fun RideDetailReviewForm(
    isSubmitting: Boolean,
    onSubmit: (Int, String?) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    RideDetailSectionCard(title = stringResource(R.string.ride_detail_review_section)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            for (star in 1..5) {
                val filled = star <= rating
                Icon(
                    imageVector = if (filled) Icons.Default.Star else Icons.Outlined.StarOutline,
                    contentDescription = null,
                    tint = if (filled) RatingGold else OutlineLight,
                    modifier = Modifier
                        .size(Dimen.SizeXL)
                        .clip(CircleShape)
                        .clickable { rating = star }
                        .padding(Dimen.PaddingXXS)
                )
            }
        }
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            placeholder = {
                Text(
                    text = stringResource(id = R.string.ride_review_comment_hint),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppShape.ShapeM),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = CardBorderColor,
                focusedContainerColor = SurfaceLight,
                unfocusedContainerColor = SurfaceLight
            ),
            maxLines = 3
        )
        Button(
            onClick = { onSubmit(rating, comment.takeIf { it.isNotBlank() }) },
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimen.HeightDefault),
            shape = RoundedCornerShape(AppShape.ShapeM),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimen.SizeM),
                    color = TextWhite,
                    strokeWidth = Dimen.PaddingXXS
                )
            } else {
                Text(
                    text = stringResource(id = R.string.ride_detail_rate_now_btn),
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextWhite
                )
            }
        }
    }
}

