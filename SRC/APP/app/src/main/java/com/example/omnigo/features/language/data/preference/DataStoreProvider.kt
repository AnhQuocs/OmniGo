package com.example.omnigo.features.language.data.preference

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.languageDataStore by preferencesDataStore(name = "settings")