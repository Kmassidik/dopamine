package app.dopamine

import android.content.Context
import app.dopamine.data.buildAppDatabase
import app.dopamine.data.network.createHttpClient
import app.dopamine.platform.AssetsSeedSource
import app.dopamine.platform.SharedPrefsKeyValueStore
import okio.FileSystem
import okio.Path.Companion.toPath

fun createAppContainer(context: Context, contentBaseUrl: String, appVersion: Int, appVersionName: String): AppContainer {
    val app = context.applicationContext
    return AppContainer(
        fs = FileSystem.SYSTEM,
        filesDir = app.filesDir.absolutePath.toPath(),
        seed = AssetsSeedSource(app),
        meta = SharedPrefsKeyValueStore(app, "content_meta"),
        database = { buildAppDatabase(app) },
        http = createHttpClient(),
        contentBaseUrl = contentBaseUrl,
        appVersion = appVersion,
        appVersionName = appVersionName,
    )
}
