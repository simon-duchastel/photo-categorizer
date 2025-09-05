package com.duchastel.simon.photocategorizer

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.duchastel.simon.photocategorizer.utils.applyIf
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .applyIf(BuildConfig.DEBUG) { logger(DebugLogger()) }
            .build()
    }
}