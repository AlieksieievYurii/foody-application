package com.yurii.foody

import android.app.Application
import timber.log.Timber
import timber.log.Timber.DebugTree


class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    companion object Configuration {
        const val SERVER_URL = "http://10.17.96.189:80/"
    }
}