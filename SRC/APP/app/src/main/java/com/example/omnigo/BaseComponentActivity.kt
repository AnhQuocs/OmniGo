package com.example.omnigo

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.example.omnigo.core.components.GlobalConnectivityHost
import com.example.omnigo.features.language.data.preference.LanguagePreferenceManager
import com.example.omnigo.features.language.domain.model.AppLanguage
import com.example.omnigo.ui.theme.OmniGoTheme
import com.example.omnigo.utils.LangUtils
import com.example.omnigo.utils.LanguageManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

open class BaseComponentActivity : ComponentActivity() {

    /**
     * Helper method để set content cho Activity kèm theo:
     * 1. Theme chuẩn của App
     * 2. Banner theo dõi kết nối mạng toàn cầu
     */
    fun setOmniGoContent(
        content: @Composable () -> Unit
    ) {
        setContent {
            OmniGoTheme(darkTheme = false) {
                GlobalConnectivityHost {
                    content()
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val updatedContext = runBlocking {
            val manager = LanguagePreferenceManager(newBase)
            val lang = manager.languageFlow.firstOrNull() ?: AppLanguage.ENGLISH
            val contextWithLocale = LanguageManager.setAppLocale(newBase, lang)

            LangUtils.currentLang = lang.code

            contextWithLocale
        }
        super.attachBaseContext(updatedContext)
    }
}
