package com.example.omnigo.features.auth.presentation.ui.login

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import com.example.omnigo.R
import com.example.omnigo.core.components.AppButton
import com.example.omnigo.core.components.AppTextField
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
fun LoginHeader(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.omni_logo),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier.size(Dimen.SizeUltra),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        Text(
            text = stringResource(id = R.string.login_title),
            style = MaterialTheme.typography.s22.bold(),
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AppSpacing.XS))

        Text(
            text = stringResource(id = R.string.login_subtitle),
            style = MaterialTheme.typography.s14.normal(),
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LoginErrorBanner(
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
fun LoginPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(id = R.string.phone_number),
        placeholder = stringResource(id = R.string.phone_number_hint),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { onNext() }),
        modifier = modifier
    )
}

@Composable
fun LoginPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(id = R.string.password),
        placeholder = stringResource(id = R.string.password_hint),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onVisibilityToggle) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = modifier
    )
}

@Composable
fun LoginSubmitButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppButton(
        text = stringResource(id = R.string.login),
        isLoading = isLoading,
        isEnabled = isEnabled,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun LoginFooter(
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.dont_have_account),
            style = MaterialTheme.typography.s14.normal(),
            color = TextSecondary
        )
        TextButton(onClick = onNavigateToRegister) {
            Text(
                text = stringResource(id = R.string.register_now),
                style = MaterialTheme.typography.s14.semiBold(),
                color = PrimaryColor
            )
        }
    }
}
