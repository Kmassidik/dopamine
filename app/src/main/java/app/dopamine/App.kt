package app.dopamine

import android.app.Application
import app.dopamine.data.content.ContentStore
import app.dopamine.data.db.AppDatabase
import app.dopamine.data.network.UpdateManager
import app.dopamine.data.repo.ContentRepository
import app.dopamine.data.repo.ProfileRepository
import app.dopamine.data.repo.ProgressRepository
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

class App : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Manual dependency injection. Grows with each step; no framework on purpose. */
class AppContainer(val app: Application) {
    val store = ContentStore(app)
    val content = ContentRepository(store)
    val db by lazy { AppDatabase.build(app) }
    val progress by lazy { ProgressRepository(db.progressDao()) }
    val profile by lazy { ProfileRepository(db.profileDao()) }

    private val http = OkHttpClient.Builder()
        .followSslRedirects(false)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val updates = UpdateManager(store, http, { BuildConfig.CONTENT_BASE_URL }, BuildConfig.VERSION_CODE)
}
