package com.example.omnigo.features.auth.presentation.ui.register.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12

enum class PasswordStrengthLevel {
    WEAK,
    MEDIUM,
    STRONG
}

@Composable
fun PasswordStrengthMeter(
    password: String,
    modifier: Modifier = Modifier
) {
    if (password.isBlank()) return

    val score = calculateStrength(password)
    val level = when {
        score <= 1 -> PasswordStrengthLevel.WEAK
        score in 2..3 -> PasswordStrengthLevel.MEDIUM
        else -> PasswordStrengthLevel.STRONG
    }

    val (labelRes, levelColor) = when (level) {
        PasswordStrengthLevel.WEAK -> Pair(R.string.password_strength_weak, ErrorColor)
        PasswordStrengthLevel.MEDIUM -> Pair(R.string.password_strength_medium, Color(0xFFF59E0B))
        PasswordStrengthLevel.STRONG -> Pair(R.string.password_strength_strong, SuccessColor)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.XS)
        ) {
            // Bar 1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(Dimen.PaddingXXS * 2)
                    .clip(RoundedCornerShape(AppShape.ShapeXXS))
                    .background(levelColor)
            )

            // Bar 2
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(Dimen.PaddingXXS * 2)
                    .clip(RoundedCornerShape(AppShape.ShapeXXS))
                    .background(if (level != PasswordStrengthLevel.WEAK) levelColor else CardBorderColor.copy(alpha = 0.3f))
            )

            // Bar 3
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(Dimen.PaddingXXS * 2)
                    .clip(RoundedCornerShape(AppShape.ShapeXXS))
                    .background(if (level == PasswordStrengthLevel.STRONG) levelColor else CardBorderColor.copy(alpha = 0.3f))
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.XXS))

        Text(
            text = stringResource(id = R.string.password_strength_label, stringResource(id = labelRes)),
            style = MaterialTheme.typography.s12.normal(),
            color = levelColor
        )
    }
}

private fun calculateStrength(password: String): Int {
    var score = 0
    if (password.length >= 6) score++
    if (password.length >= 8) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { it.isUpperCase() } || password.any { !it.isLetterOrDigit() }) score++
    return score
}
