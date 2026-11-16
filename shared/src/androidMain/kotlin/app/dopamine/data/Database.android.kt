package app.dopamine.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.dopamine.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers

/** The same file as before the move (databases/dopamine.db), now opened with the bundled SQLite driver. */
fun buildAppDatabase(context: Context): AppDatabase =
    Room.databaseBuilder<AppDatabase>(context, context.getDatabasePath(AppDatabase.FILE_NAME).absolutePath)
        .setDriver(BundledSQLiteDriver())
        .addMigrations(*AppDatabase.MIGRATIONS)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
