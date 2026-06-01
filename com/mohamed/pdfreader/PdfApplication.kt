package com.mohamed.pdfreader

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PdfApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
