package com.example.omnigo.features.auth.presentation.ui.register.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.semiBold

@Composable
fun RegisterStepperHeader(
    currentStep: Int,
    totalSteps: Int,
    stepTitles: List<String>,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (totalSteps > 1) currentStep.toFloat() / (totalSteps - 1).toFloat() else 0f,
        label = "stepper_progress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Continuous Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimen.PaddingXS)
                .clip(RoundedCornerShape(AppShape.ShapeXS)),
            color = PrimaryColor,
            trackColor = CardBorderColor.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        // Step circles and labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            stepTitles.forEachIndexed { index, title ->
                val isDone = index < currentStep
                val isCurrent = index == currentStep

                val circleBg by animateColorAsState(
                    targetValue = when {
                        isDone -> SuccessColor
                        isCurrent -> PrimaryColor
                        else -> CardBorderColor.copy(alpha = 0.3f)
                    },
                    label = "circle_bg"
                )

                val textColor by animateColorAsState(
                    targetValue = when {
                        isCurrent -> PrimaryColor
                        isDone -> TextPrimary
                        else -> TextSecondary
                    },
                    label = "text_color"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(Dimen.SizeSM)
                            .background(circleBg, CircleShape)
                            .border(
                                width = Dimen.PaddingXXS,
                                color = if (isCurrent) PrimaryColor else Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(Dimen.PaddingM)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.s10.bold(),
                                color = if (isCurrent) Color.White else TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.XXS))

                    Text(
                        text = title,
                        style = if (isCurrent) MaterialTheme.typography.s12.semiBold() else MaterialTheme.typography.s10.normal(),
                        color = textColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
