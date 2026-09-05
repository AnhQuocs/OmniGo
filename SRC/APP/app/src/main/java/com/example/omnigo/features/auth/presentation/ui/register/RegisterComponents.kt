package com.example.omnigo.features.auth.presentation.ui.register

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.core.components.AppButton
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.ErrorContainer
import com.example.omnigo.ui.theme.OnErrorContainer
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s22
import com.example.omnigo.utils.semiBold

@Composable
fun RegisterHeader(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.omni_logo),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier.size(Dimen.SizeXXL),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(AppSpacing.S))

        Text(
            text = stringResource(id = R.string.register_title),
            style = MaterialTheme.typography.s22.bold(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.XS))

        Text(
            text = stringResource(id = R.string.register_subtitle),
            style = MaterialTheme.typography.s14.normal(),
            color = TextSecondary
        )
    }
}

@Composable
fun RegisterErrorBanner(
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = ErrorContainer,
                shape = RoundedCornerShape(AppShape.ShapeM)
            )
            .border(
                width = Dimen.PaddingXXS,
                color = ErrorColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(AppShape.ShapeM)
            )
            .padding(Dimen.PaddingSM)
    ) {
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.s13.normal(),
            color = OnErrorContainer
        )
    }
}

@Composable
fun RegisterSubmitButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppButton(
        text = stringResource(id = R.string.complete_registration),
        isLoading = isLoading,
        isEnabled = isEnabled,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun RegisterFooter(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.already_have_account),
            style = MaterialTheme.typography.s14.normal(),
            color = TextSecondary
        )
        TextButton(onClick = onNavigateToLogin) {
            Text(
                text = stringResource(id = R.string.back_to_login),
                style = MaterialTheme.typography.s14.semiBold(),
                color = PrimaryColor
            )
        }
    }
}
