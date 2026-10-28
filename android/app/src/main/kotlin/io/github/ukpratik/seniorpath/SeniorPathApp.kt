package io.github.ukpratik.seniorpath

import android.app.Application

class SeniorPathApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
