package app.dopamine

import android.app.Application

class App : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = createAppContainer(this, BuildConfig.CONTENT_BASE_URL, BuildConfig.VERSION_CODE)
    }
}
