package com.example.omnigo

import android.content.Context
import androidx.activity.ComponentActivity
import com.example.omnigo.features.language.data.preference.LanguagePreferenceManager
import com.example.omnigo.features.language.domain.model.AppLanguage
import com.example.omnigo.utils.LangUtils
import com.example.omnigo.utils.LanguageManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

open class BaseComponentActivity : ComponentActivity() {

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