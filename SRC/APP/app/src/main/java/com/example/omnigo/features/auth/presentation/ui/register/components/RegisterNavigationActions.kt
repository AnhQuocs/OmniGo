package com.example.omnigo.features.auth.presentation.ui.register.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.core.components.AppButton
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.TextPrimary

@Composable
fun RegisterNavigationActions(
    currentStep: Int,
    isLastStep: Boolean,
    isCurrentStepValid: Boolean,
    isRegistering: Boolean,
    onPreviousStep: () -> Unit,
    onNextOrSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentStep > 0) {
            AppButton(
                text = stringResource(id = R.string.btn_prev_step),
                onClick = onPreviousStep,
                isEnabled = !isRegistering,
                containerColor = CardBorderColor.copy(alpha = 0.25f),
                contentColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(AppSpacing.M))
        }

        AppButton(
            text = if (isLastStep) stringResource(id = R.string.btn_complete_registration) else stringResource(id = R.string.btn_next_step),
            isLoading = isRegistering && isLastStep,
            isEnabled = isCurrentStepValid && !isRegistering,
            onClick = onNextOrSubmit,
            modifier = Modifier.weight(if (currentStep > 0) 1.5f else 1f)
        )
    }
}
