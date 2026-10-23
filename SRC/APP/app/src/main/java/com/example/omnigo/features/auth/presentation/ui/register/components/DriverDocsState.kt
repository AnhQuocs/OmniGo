package com.example.omnigo.features.auth.presentation.ui.register.components

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.omnigo.features.auth.presentation.viewmodel.AuthViewModel
import com.example.omnigo.utils.ImageUploadUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DriverDocsState(
    private val scope: CoroutineScope,
    private val context: Context,
    private val authViewModel: AuthViewModel
) {
    var cccdNumber by mutableStateOf("")
    var cccdFrontUri by mutableStateOf<Uri?>(null)
    var cccdFrontUrl by mutableStateOf<String?>(null)
    var isCccdFrontUploading by mutableStateOf(false)

    var cccdBackUri by mutableStateOf<Uri?>(null)
    var cccdBackUrl by mutableStateOf<String?>(null)
    var isCccdBackUploading by mutableStateOf(false)

    var gplxNumber by mutableStateOf("")
    var gplxFrontUri by mutableStateOf<Uri?>(null)
    var gplxFrontUrl by mutableStateOf<String?>(null)
    var isGplxFrontUploading by mutableStateOf(false)

    var gplxBackUri by mutableStateOf<Uri?>(null)
    var gplxBackUrl by mutableStateOf<String?>(null)
    var isGplxBackUploading by mutableStateOf(false)

    val isDocumentsUploaded: Boolean
        get() = !cccdFrontUrl.isNullOrBlank() && !cccdBackUrl.isNullOrBlank() &&
                !gplxFrontUrl.isNullOrBlank() && !gplxBackUrl.isNullOrBlank()

    fun uploadDoc(
        uri: Uri,
        name: String,
        setLoading: (Boolean) -> Unit,
        setUrl: (String) -> Unit
    ) {
        scope.launch {
            setLoading(true)
            val part = ImageUploadUtils.uriToMultipart(context, uri, partName = "file", fileName = name)
            if (part != null) {
                val res = authViewModel.uploadDriverDocument(part)
                res.onSuccess { setUrl(it) }
                    .onFailure { setUrl(uri.toString()) }
            } else {
                setUrl(uri.toString())
            }
            setLoading(false)
        }
    }

    fun onCccdFrontSelected(uri: Uri) {
        cccdFrontUri = uri
        uploadDoc(uri, "cccd_front.jpg", { isCccdFrontUploading = it }, { cccdFrontUrl = it })
    }

    fun clearCccdFront() {
        cccdFrontUri = null
        cccdFrontUrl = null
    }

    fun onCccdBackSelected(uri: Uri) {
        cccdBackUri = uri
        uploadDoc(uri, "cccd_back.jpg", { isCccdBackUploading = it }, { cccdBackUrl = it })
    }

    fun clearCccdBack() {
        cccdBackUri = null
        cccdBackUrl = null
    }

    fun onGplxFrontSelected(uri: Uri) {
        gplxFrontUri = uri
        uploadDoc(uri, "gplx_front.jpg", { isGplxFrontUploading = it }, { gplxFrontUrl = it })
    }

    fun clearGplxFront() {
        gplxFrontUri = null
        gplxFrontUrl = null
    }

    fun onGplxBackSelected(uri: Uri) {
        gplxBackUri = uri
        uploadDoc(uri, "gplx_back.jpg", { isGplxBackUploading = it }, { gplxBackUrl = it })
    }

    fun clearGplxBack() {
        gplxBackUri = null
        gplxBackUrl = null
    }
}

@Composable
fun rememberDriverDocsState(
    context: Context,
    authViewModel: AuthViewModel,
    scope: CoroutineScope = rememberCoroutineScope()
): DriverDocsState {
    return remember {
        DriverDocsState(scope, context, authViewModel)
    }
}
