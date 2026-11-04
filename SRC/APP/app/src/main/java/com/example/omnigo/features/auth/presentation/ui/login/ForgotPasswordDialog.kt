package com.example.omnigo.features.auth.presentation.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.window.Dialog
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16

@Composable
fun ForgotPasswordDialog(
    initialPhoneNumber: String = "",
    isLoading: Boolean,
    errorMessage: String? = null,
    isOtpSent: Boolean = false,
    onSendOtp: (phoneNumber: String) -> Unit = {},
    onDismiss: () -> Unit,
    onSubmit: (phoneNumber: String, newPass: String, confirmPass: String, otpCode: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var phoneNumber by remember { mutableStateOf(initialPhoneNumber) }
    var otpCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val passMismatchMsg = stringResource(id = R.string.password_not_match)
    val passTooShortMsg = stringResource(id = R.string.error_password_too_short)
    val phoneEmptyMsg = stringResource(id = R.string.error_empty_phone)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(AppShape.ShapeL),
            color = SurfaceLight,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingL)
            ) {
                Text(
                    text = stringResource(id = R.string.auth_forgot_password_title),
                    style = MaterialTheme.typography.s16.bold(),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(AppSpacing.XS))

                Text(
                    text = stringResource(id = R.string.auth_forgot_password_desc),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(AppSpacing.M))

                val displayError = localError ?: errorMessage
                if (!displayError.isNullOrBlank()) {
                    Text(
                        text = displayError,
                        style = MaterialTheme.typography.s12.normal(),
                        color = ErrorColor,
                        modifier = Modifier.padding(bottom = Dimen.PaddingS)
                    )
                }

                // Phone Input + Send OTP Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            localError = null
                        },
                        label = { Text(stringResource(id = R.string.phone_number)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = CardBorderColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XS))
                    TextButton(
                        onClick = {
                            if (phoneNumber.isBlank()) {
                                localError = phoneEmptyMsg
                            } else {
                                onSendOtp(phoneNumber.trim())
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text(
                            text = stringResource(
                                id = if (isOtpSent) R.string.auth_resend_otp_btn else R.string.auth_send_otp_btn
                            ),
                            style = MaterialTheme.typography.s12.bold(),
                            color = PrimaryColor
                        )
                    }
                }

                if (isOtpSent) {
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Text(
                        text = stringResource(id = R.string.auth_otp_sent_msg, phoneNumber),
                        style = MaterialTheme.typography.s12.medium(),
                        color = PrimaryColor
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                // 6-Digit OTP Input
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = {
                        otpCode = it.filter { ch -> ch.isDigit() }.take(6)
                        localError = null
                    },
                    label = { Text(stringResource(id = R.string.auth_otp_input_label)) },
                    placeholder = { Text(stringResource(id = R.string.auth_otp_input_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = CardBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(AppSpacing.S))

                // New Password Input
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        localError = null
                    },
                    label = { Text(stringResource(id = R.string.account_new_password)) },
                    singleLine = true,
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                imageVector = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = CardBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(AppSpacing.S))

                // Confirm Password Input
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        localError = null
                    },
                    label = { Text(stringResource(id = R.string.account_confirm_new_password)) },
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = CardBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(AppShape.ShapeM)
                    ) {
                        Text(
                            text = stringResource(id = R.string.account_logout_cancel),
                            style = MaterialTheme.typography.s14.normal(),
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(AppSpacing.SPlus))

                    Button(
                        onClick = {
                            if (phoneNumber.isBlank()) {
                                localError = phoneEmptyMsg
                                return@Button
                            }
                            if (newPassword.length < 6) {
                                localError = passTooShortMsg
                                return@Button
                            }
                            if (newPassword != confirmPassword) {
                                localError = passMismatchMsg
                                return@Button
                            }
                            onSubmit(phoneNumber, newPassword, confirmPassword, otpCode)
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(AppShape.ShapeM),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = OnPrimaryColor,
                                strokeWidth = Dimen.PaddingXXS,
                                modifier = Modifier.height(Dimen.SizeM).width(Dimen.SizeM)
                            )
                        } else {
                            Text(
                                text = stringResource(id = R.string.auth_forgot_password_title),
                                style = MaterialTheme.typography.s14.bold(),
                                color = OnPrimaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}
