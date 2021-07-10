package dev.arpan.location.engine.demo

import android.app.Application
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(object : Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String? {
                return "GMSHMS_" + super.createStackElementTag(element)
            }
        })
    }
}
