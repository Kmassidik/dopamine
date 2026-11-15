package app.dopamine

import android.app.Application

class App : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Manual dependency injection. Grows with each step; no framework on purpose. */
class AppContainer(val app: Application)
