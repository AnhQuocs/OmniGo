package com.example.omnigo.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * Centralized Toast utility to prevent Toast queuing/spam in Android.
 * Automatically cancels any previously showing toast and throttles identical messages.
 */
object AppToast {
    private var currentToast: Toast? = null
    private var lastMessage: String? = null
    private var lastShownTime: Long = 0L
    private const val THROTTLE_WINDOW_MS = 1500L

    @Synchronized
    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        if (message.isBlank()) return
        val now = System.currentTimeMillis()
        if (message == lastMessage && now - lastShownTime < THROTTLE_WINDOW_MS) {
            return
        }
        currentToast?.cancel()
        lastMessage = message
        lastShownTime = now
        currentToast = Toast.makeText(context.applicationContext, message, duration).apply {
            show()
        }
    }

    @Synchronized
    fun show(context: Context, @StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
        show(context, context.getString(resId), duration)
    }

    @Synchronized
    fun show(context: Context, uiText: UiText, duration: Int = Toast.LENGTH_SHORT) {
        show(context, uiText.asString(context), duration)
    }

    @Synchronized
    fun cancel() {
        currentToast?.cancel()
        currentToast = null
        lastMessage = null
        lastShownTime = 0L
    }
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    AppToast.show(this, message, duration)
}

fun Context.showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    AppToast.show(this, resId, duration)
}

fun Context.showToast(uiText: UiText, duration: Int = Toast.LENGTH_SHORT) {
    AppToast.show(this, uiText, duration)
}
