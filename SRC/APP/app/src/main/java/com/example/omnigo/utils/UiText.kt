package com.example.omnigo.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.auth.domain.error.RegisterError

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }
}

fun RegisterError.asUiText(): UiText {
    return when (this) {
        RegisterError.EMPTY_FULL_NAME -> UiText.StringResource(R.string.error_empty_full_name)
        RegisterError.EMPTY_EMAIL -> UiText.StringResource(R.string.error_empty_email)
        RegisterError.INVALID_EMAIL -> UiText.StringResource(R.string.error_invalid_email)
        RegisterError.EMPTY_PHONE -> UiText.StringResource(R.string.error_empty_phone)
        RegisterError.INVALID_PHONE -> UiText.StringResource(R.string.error_invalid_phone)
        RegisterError.EMPTY_PASSWORD -> UiText.StringResource(R.string.error_empty_password)
        RegisterError.PASSWORD_TOO_SHORT -> UiText.StringResource(R.string.error_password_too_short)
        RegisterError.PHONE_VERIFICATION_REQUIRED -> UiText.StringResource(R.string.error_phone_verification_required)
        RegisterError.EMPTY_LICENSE_PLATE -> UiText.StringResource(R.string.error_empty_license_plate)
        RegisterError.EMPTY_VEHICLE_MODEL -> UiText.StringResource(R.string.error_empty_vehicle_model)
        RegisterError.INVALID_OTP -> UiText.StringResource(R.string.error_invalid_otp)
        RegisterError.OTP_EXPIRED -> UiText.StringResource(R.string.error_otp_expired)
        RegisterError.TOO_MANY_REQUESTS -> UiText.StringResource(R.string.error_too_many_requests)
        RegisterError.PHONE_NUMBER_MISMATCH -> UiText.StringResource(R.string.error_phone_number_mismatch)
        RegisterError.EMAIL_OR_PHONE_ALREADY_EXISTS -> UiText.StringResource(R.string.error_email_or_phone_already_exists)
        RegisterError.BAD_REQUEST -> UiText.StringResource(R.string.error_bad_request)
        RegisterError.SERVER_ERROR -> UiText.StringResource(R.string.error_server_error)
        RegisterError.NETWORK_ERROR -> UiText.StringResource(R.string.error_network_error)
        RegisterError.UNKNOWN -> UiText.StringResource(R.string.error_unknown)
    }
}

fun com.example.omnigo.features.auth.domain.error.LoginError.asUiText(): UiText {
    return when (this) {
        com.example.omnigo.features.auth.domain.error.LoginError.EMPTY_PHONE -> UiText.StringResource(R.string.error_empty_phone)
        com.example.omnigo.features.auth.domain.error.LoginError.INVALID_PHONE -> UiText.StringResource(R.string.error_invalid_phone)
        com.example.omnigo.features.auth.domain.error.LoginError.EMPTY_PASSWORD -> UiText.StringResource(R.string.error_empty_password)
        com.example.omnigo.features.auth.domain.error.LoginError.INVALID_CREDENTIALS -> UiText.StringResource(R.string.error_invalid_credentials)
        com.example.omnigo.features.auth.domain.error.LoginError.ACCOUNT_NOT_FOUND -> UiText.StringResource(R.string.error_account_not_found)
        com.example.omnigo.features.auth.domain.error.LoginError.ACCOUNT_BLOCKED -> UiText.StringResource(R.string.error_account_blocked)
        com.example.omnigo.features.auth.domain.error.LoginError.SERVER_ERROR -> UiText.StringResource(R.string.error_server_error)
        com.example.omnigo.features.auth.domain.error.LoginError.NETWORK_ERROR -> UiText.StringResource(R.string.error_network_error)
        com.example.omnigo.features.auth.domain.error.LoginError.UNKNOWN -> UiText.StringResource(R.string.error_unknown)
    }
}
