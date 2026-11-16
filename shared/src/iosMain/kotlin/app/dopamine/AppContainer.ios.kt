package app.dopamine

import app.dopamine.data.buildAppDatabase
import app.dopamine.data.documentsDirectory
import app.dopamine.data.network.createHttpClient
import app.dopamine.platform.BundleSeedSource
import app.dopamine.platform.UserDefaultsKeyValueStore
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSBundle

/** [appVersion] is the build number (like Android's versionCode); the manifest's minAppVersion is compared with it. */
fun createAppContainer(contentBaseUrl: String, appVersion: Int, appVersionName: String): AppContainer {
    val bundled = requireNotNull(NSBundle.mainBundle.resourcePath) { "no bundle path" }.toPath() / "content"
    return AppContainer(
        fs = FileSystem.SYSTEM,
        filesDir = (documentsDirectory().toPath() / "dopamine"),
        seed = BundleSeedSource(bundled),
        meta = UserDefaultsKeyValueStore("content_meta"),
        database = { buildAppDatabase() },
        http = createHttpClient(),
        contentBaseUrl = contentBaseUrl,
        appVersion = appVersion,
        appVersionName = appVersionName,
    )
}
