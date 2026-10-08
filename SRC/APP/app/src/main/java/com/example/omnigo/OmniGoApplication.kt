package com.example.omnigo

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient

@HiltAndroidApp
class OmniGoApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header(
                                "User-Agent",
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                            )
                            .build()
                        chain.proceed(request)
                    }
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build()
            }
            .crossfade(true)
            .logger(DebugLogger())
            .build()
    }
}