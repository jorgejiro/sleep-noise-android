package com.jjrapps.sleepnoise

import android.app.Application
import com.jjrapps.sleepnoise.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SleepNoiseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Debug only: a release build with a logger is a release build leaking
        // whatever it logs.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
