package app.dopamine.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.dopamine.data.db.AppDatabase
import app.dopamine.data.db.AppDatabaseConstructor
import kotlinx.cinterop.ExperimentalForeignApi
import app.dopamine.platform.IoDispatcher
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun documentsDirectory(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(NSDocumentDirectory, NSUserDomainMask, null, true, null)
    return requireNotNull(url?.path) { "no documents directory" }
}

fun buildAppDatabase(): AppDatabase =
    Room.databaseBuilder<AppDatabase>(name = documentsDirectory() + "/" + AppDatabase.FILE_NAME, factory = { AppDatabaseConstructor.initialize() })
        .setDriver(BundledSQLiteDriver())
        .addMigrations(*AppDatabase.MIGRATIONS)
        .setQueryCoroutineContext(IoDispatcher)
        .build()
