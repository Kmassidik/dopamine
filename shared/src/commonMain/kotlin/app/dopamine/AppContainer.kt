package app.dopamine

import app.dopamine.data.content.ContentStore
import app.dopamine.data.db.AppDatabase
import app.dopamine.data.network.UpdateManager
import app.dopamine.data.repo.ContentRepository
import app.dopamine.data.repo.ProfileRepository
import app.dopamine.data.repo.ProgressRepository
import app.dopamine.platform.KeyValueStore
import app.dopamine.platform.SeedSource
import io.ktor.client.HttpClient
import okio.FileSystem
import okio.Path

/**
 * Manual dependency injection, shared by Android and iOS. The platform supplies the pieces that differ
 * (file system root, bundled seed content, key-value store, database, HTTP engine); see the factories in
 * androidMain (createAppContainer(context)) and iosMain (createAppContainer()).
 */
class AppContainer(
    fs: FileSystem,
    filesDir: Path,
    seed: SeedSource,
    meta: KeyValueStore,
    database: () -> AppDatabase,
    http: HttpClient,
    contentBaseUrl: String,
    appVersion: Int,
    val appVersionName: String,
) {
    val store = ContentStore(fs, filesDir, seed, meta)
    val content = ContentRepository(store)
    val db by lazy(database)
    val progress by lazy { ProgressRepository(db.progressDao()) }
    val profile by lazy { ProfileRepository(db.profileDao()) }
    val updates = UpdateManager(store, http, { contentBaseUrl }, appVersion)
}
