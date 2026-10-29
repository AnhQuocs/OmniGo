package com.example.omnigo.features.customer.profile.presentation.ui.legal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import com.example.omnigo.BaseComponentActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TermsAndPrivacyActivity : BaseComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)

        setOmniGoContent {
            TermsAndPrivacyScreen(
                onBackClick = { finish() }
            )
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, TermsAndPrivacyActivity::class.java)
        }
    }
}

