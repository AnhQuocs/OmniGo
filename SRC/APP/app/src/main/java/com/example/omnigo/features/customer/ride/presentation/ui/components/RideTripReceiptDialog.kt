package com.example.omnigo.features.customer.ride.presentation.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.OutlineLight
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s20
import com.example.omnigo.utils.semiBold
import java.text.DecimalFormat

@Composable
fun RideTripReceiptDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRating by remember { mutableIntStateOf(5) }
    val selectedTags = remember { mutableStateListOf<Int>() }

    val feedbackTags = listOf(
        R.string.ride_trip_rating_tag_safe,
        R.string.ride_trip_rating_tag_clean,
        R.string.ride_trip_rating_tag_polite,
        R.string.ride_trip_rating_tag_ontime
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(AppShape.ShapeXL),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = Dimen.PaddingL,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingL),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Badge Icon
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeMega)
                        .clip(CircleShape)
                        .background(SuccessColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessColor,
                        modifier = Modifier.size(Dimen.SizeXXL)
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Title
                Text(
                    text = stringResource(id = R.string.ride_trip_completed_title),
                    style = MaterialTheme.typography.s20.bold(),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppSpacing.S))

                // Total Fare
                val formattedFare = DecimalFormat("#,###").format(booking.price) + " đ"
                Text(
                    text = formattedFare,
                    style = MaterialTheme.typography.s20.bold(),
                    color = PrimaryColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(id = R.string.active_trip_sheet_payment_cash),
                    style = MaterialTheme.typography.s12.medium(),
                    color = TextTertiary
                )

                Spacer(modifier = Modifier.height(AppSpacing.M))
                HorizontalDivider(color = OutlineVariantLight, thickness = Dimen.PaddingXXS)
                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Rating Prompt
                Text(
                    text = stringResource(id = R.string.ride_trip_rating_prompt),
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(AppSpacing.S))

                // 5 Star Rating Row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (star in 1..5) {
                        val isFilled = star <= selectedRating
                        Icon(
                            imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (isFilled) SecondaryColor else OutlineLight,
                            modifier = Modifier
                                .size(Dimen.SizeXL)
                                .clip(CircleShape)
                                .clickable { selectedRating = star }
                                .padding(Dimen.PaddingXXS)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Quick Feedback Tags Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    feedbackTags.take(2).forEach { tagRes ->
                        val isTagSelected = selectedTags.contains(tagRes)
                        TagItem(
                            tagRes = tagRes,
                            isSelected = isTagSelected,
                            onClick = {
                                if (isTagSelected) selectedTags.remove(tagRes) else selectedTags.add(tagRes)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppSpacing.XS))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    feedbackTags.drop(2).forEach { tagRes ->
                        val isTagSelected = selectedTags.contains(tagRes)
                        TagItem(
                            tagRes = tagRes,
                            isSelected = isTagSelected,
                            onClick = {
                                if (isTagSelected) selectedTags.remove(tagRes) else selectedTags.add(tagRes)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Complete Button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault)
                ) {
                    Text(
                        text = stringResource(id = R.string.ride_trip_btn_finish),
                        style = MaterialTheme.typography.s16.bold(),
                        color = OnPrimaryColor
                    )
                }
            }
        }
    }
}

@Composable
private fun TagItem(
    tagRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryColor else OutlineLight
    val bgColor = if (isSelected) PrimaryColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    val textColor = if (isSelected) PrimaryColor else TextSecondary

    Surface(
        shape = RoundedCornerShape(AppShape.ShapeM),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(Dimen.PaddingXXS, borderColor),
        modifier = Modifier
            .clip(RoundedCornerShape(AppShape.ShapeM))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = stringResource(id = tagRes),
            style = MaterialTheme.typography.s12.medium(),
            color = textColor,
            modifier = Modifier.padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXS)
        )
    }
}
