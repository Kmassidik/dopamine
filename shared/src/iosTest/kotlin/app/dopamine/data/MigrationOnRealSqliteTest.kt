package app.dopamine.data

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import app.dopamine.data.db.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Runs the real 1 -> 2 -> 3 migrations on real SQLite (the bundled driver, on the iOS simulator): a database
 * created exactly as an old app version made it (SQL and identity hash from app/schemas) must open with the
 * current AppDatabase and keep its rows. Android opens the same file format with the same driver and the same
 * Migration objects, so this also guards existing Android installs.
 */
class MigrationOnRealSqliteTest {
    private val profile =
        "CREATE TABLE IF NOT EXISTS `profile` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"

    private fun freshPath(): String = NSTemporaryDirectory().trimEnd('/') + "/migration-" + NSUUID().UUIDString + ".db"

    private fun createOldDb(path: String, version: Int, hash: String, extra: List<String>) {
        val conn = BundledSQLiteDriver().open(path)
        try {
            conn.execSQL(profile)
            extra.forEach { conn.execSQL(it) }
            conn.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
            conn.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '$hash')")
            conn.execSQL("INSERT INTO profile (id, name, createdAt) VALUES (1, 'Kurnia', 1700000000000)")
            conn.execSQL("PRAGMA user_version = $version")
        } finally {
            conn.close()
        }
    }

    @Test fun version1KeepsTheProfileAndGainsProgressTables() = runTest {
        val path = freshPath()
        createOldDb(path, 1, "29fe73ad3698f34c77613bf3be36916d", emptyList())
        val db = buildAppDatabase(path)
        try {
            assertEquals("Kurnia", assertNotNull(db.profileDao().observe().first()).name)
            db.progressDao().record("l1", 80, 10, 19000L, 1L)
            assertEquals(true, db.progressDao().lesson("l1")?.completed)
            assertEquals(true, db.progressDao().recordQuiz("u1", 90, true, 20, 19000L, 2L))
        } finally {
            db.close()
            FileSystem.SYSTEM.delete(path.toPath())
        }
    }

    @Test fun version2GainsTheQuizTableAndKeepsProgress() = runTest {
        val path = freshPath()
        createOldDb(
            path, 2, "540b9ced05f85618eeef2de2c3754c20",
            listOf(
                "CREATE TABLE IF NOT EXISTS `lesson_progress` (`lessonId` TEXT NOT NULL, `bestScore` INTEGER NOT NULL, `completed` INTEGER NOT NULL, `completedAt` INTEGER NOT NULL, PRIMARY KEY(`lessonId`))",
                "CREATE TABLE IF NOT EXISTS `daily_stat` (`day` INTEGER NOT NULL, `xp` INTEGER NOT NULL, PRIMARY KEY(`day`))",
                "INSERT INTO lesson_progress VALUES ('old-lesson', 70, 1, 5)",
                "INSERT INTO daily_stat VALUES (19000, 30)",
            ),
        )
        val db = buildAppDatabase(path)
        try {
            assertEquals(70, db.progressDao().lesson("old-lesson")?.bestScore)
            assertEquals(30, db.progressDao().observeDays().first().single().xp)
            assertEquals(true, db.progressDao().recordQuiz("u1", 100, true, 20, 19000L, 2L))
            assertEquals(50, db.progressDao().observeDays().first().single().xp)
        } finally {
            db.close()
            FileSystem.SYSTEM.delete(path.toPath())
        }
    }
}
