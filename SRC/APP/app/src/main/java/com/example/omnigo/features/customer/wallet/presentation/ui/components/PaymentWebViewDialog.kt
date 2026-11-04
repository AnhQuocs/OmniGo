package com.example.omnigo.features.customer.wallet.presentation.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.util.Log
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.s16

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PaymentWebViewDialog(
    url: String,
    onPaymentSuccess: () -> Unit,
    onPaymentFailed: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.checkout_payment_gateway_title)
) {
    val context = LocalContext.current
    var progress by remember { mutableIntStateOf(0) }
    var isHandled by remember { mutableStateOf(false) }

    fun checkPaymentCallback(targetUrl: String?): Boolean {
        if (targetUrl.isNullOrBlank() || isHandled) return false
        val uri = runCatching { Uri.parse(targetUrl) }.getOrNull()

        // 1. Check VNPay Callback via query param
        val vnpResponseCode = uri?.getQueryParameter("vnp_ResponseCode")
        if (vnpResponseCode != null) {
            isHandled = true
            if (vnpResponseCode == "00") {
                onPaymentSuccess()
            } else {
                onPaymentFailed("VNPay: $vnpResponseCode")
            }
            return true
        }

        // 2. Check MoMo Callback via query param
        val momoResultCode = uri?.getQueryParameter("resultCode")
        if (momoResultCode != null) {
            isHandled = true
            if (momoResultCode == "0") {
                onPaymentSuccess()
            } else {
                onPaymentFailed("MoMo: $momoResultCode")
            }
            return true
        }

        // 3. Fallback for Backend Return URL or query string patterns
        if (targetUrl.contains("vnp_ResponseCode=00") ||
            targetUrl.contains("resultCode=0") ||
            (targetUrl.contains("/vnpay/return") && targetUrl.contains("00")) ||
            targetUrl.contains("payment-success")
        ) {
            isHandled = true
            onPaymentSuccess()
            return true
        }

        if (targetUrl.contains("vnp_ResponseCode=") || targetUrl.contains("resultCode=")) {
            isHandled = true
            onPaymentFailed("Payment cancelled or failed")
            return true
        }

        return false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        val dialogView = LocalView.current
        DisposableEffect(dialogView) {
            val window = (dialogView.parent as? DialogWindowProvider)?.window
                ?: (context as? Activity)?.window
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            onDispose {}
        }

        Surface(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = BackgroundLight
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.s16.bold(),
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = TextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceLight
                    )
                )

                if (progress in 1..99) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppSpacing.XXS),
                        color = PrimaryColor,
                        trackColor = PrimaryColor.copy(alpha = 0.2f)
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                val cookieManager = CookieManager.getInstance()
                                cookieManager.setAcceptCookie(true)
                                cookieManager.setAcceptThirdPartyCookies(this, true)

                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.databaseEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                settings.javaScriptCanOpenWindowsAutomatically = true
                                settings.setSupportMultipleWindows(false)
                                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                                // Standardize user agent to mobile Chrome
                                val currentUa = settings.userAgentString
                                settings.userAgentString = currentUa
                                    .replace("; wv", "")
                                    .replace("Version/4.0 ", "")

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        progress = newProgress
                                    }

                                    override fun onCloseWindow(window: WebView?) {
                                        super.onCloseWindow(window)
                                        if (!isHandled) {
                                            isHandled = true
                                            onPaymentSuccess()
                                        }
                                    }

                                    override fun onJsAlert(
                                        view: WebView?,
                                        url: String?,
                                        message: String?,
                                        result: JsResult?
                                    ): Boolean {
                                        Log.d("PaymentWebView", "JS Alert: $message")
                                        result?.confirm()
                                        return true
                                    }

                                    override fun onJsConfirm(
                                        view: WebView?,
                                        url: String?,
                                        message: String?,
                                        result: JsResult?
                                    ): Boolean {
                                        Log.d("PaymentWebView", "JS Confirm: $message")
                                        result?.confirm()
                                        return true
                                    }

                                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                        Log.d("PaymentWebView", "JS Console: ${consoleMessage?.message()}")
                                        return true
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val nextUrl = request?.url?.toString() ?: return false
                                        val scheme = request.url.scheme
                                        if (scheme != null && !scheme.startsWith("http")) {
                                            runCatching {
                                                val intent = Intent(Intent.ACTION_VIEW, request.url).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                ctx.startActivity(intent)
                                            }
                                            return true
                                        }
                                        // Allow return URLs to execute GET request on backend
                                        return false
                                    }

                                    override fun onPageStarted(
                                        view: WebView?,
                                        targetUrl: String?,
                                        favicon: Bitmap?
                                    ) {
                                        super.onPageStarted(view, targetUrl, favicon)
                                    }

                                    override fun onReceivedSslError(
                                        view: WebView?,
                                        handler: SslErrorHandler?,
                                        error: SslError?
                                    ) {
                                        Log.w("PaymentWebView", "SSL Error bypassed: $error")
                                        handler?.proceed()
                                    }

                                    override fun onPageFinished(view: WebView?, targetUrl: String?) {
                                        super.onPageFinished(view, targetUrl)
                                        checkPaymentCallback(targetUrl)

                                        // Auto-accept VNPay sandbox terms and handle continue button
                                        view?.evaluateJavascript(
                                            """
                                            (function() {
                                                try {
                                                    var terms = document.getElementById('accepted_terms');
                                                    if (terms && !terms.value) {
                                                        terms.value = '5562958';
                                                    }
                                                    var btnAgree = document.getElementById('btnAgree');
                                                    var modal = document.getElementById('modalDKSD');
                                                    if (modal && modal.classList && modal.classList.contains('show') && btnAgree) {
                                                        btnAgree.click();
                                                    }
                                                } catch(e) {}
                                            })();
                                            """.trimIndent(),
                                            null
                                        )

                                        view?.title?.let { pageTitle ->
                                            if (pageTitle.contains("Thành Công", ignoreCase = true) ||
                                                pageTitle.contains("Success", ignoreCase = true)
                                            ) {
                                                if (!isHandled) {
                                                    isHandled = true
                                                    onPaymentSuccess()
                                                }
                                            }
                                        }
                                    }
                                }

                                loadUrl(url)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
