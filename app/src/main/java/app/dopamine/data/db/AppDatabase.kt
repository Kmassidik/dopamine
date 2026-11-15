package app.dopamine.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Transaction
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.dopamine.domain.progress.mergeQuiz
import kotlinx.coroutines.flow.Flow

/** One row, id = 1: the person using this phone. */
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val createdAt: Long,
)

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = 1")
    fun observe(): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: ProfileEntity)
}

/** Best result per lesson. [bestScore] is a percent (0..100). [completedAt] is epoch millis. */
@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val bestScore: Int,
    val completed: Boolean,
    val completedAt: Long,
)

/** XP earned on one local day. [day] is an epoch day in local time; a row exists for every day a lesson was finished. */
@Entity(tableName = "daily_stat")
data class DailyStatEntity(
    @PrimaryKey val day: Long,
    val xp: Int,
)

/** Best result of a unit quiz. [passed] stays true once earned. [passedAt] is epoch millis, 0 until passed. */
@Entity(tableName = "unit_quiz")
data class UnitQuizEntity(
    @PrimaryKey val unitId: String,
    val bestScore: Int,
    val passed: Boolean,
    val passedAt: Long,
)

@Dao
abstract class ProgressDao {
    @Query("SELECT * FROM lesson_progress")
    abstract fun observeLessons(): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :id")
    abstract suspend fun lesson(id: String): LessonProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertLesson(row: LessonProgressEntity)

    @Query("SELECT * FROM daily_stat ORDER BY day DESC")
    abstract fun observeDays(): Flow<List<DailyStatEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertDay(row: DailyStatEntity): Long

    @Query("UPDATE daily_stat SET xp = xp + :xp WHERE day = :day")
    abstract suspend fun addXp(day: Long, xp: Int)

    @Query("SELECT * FROM unit_quiz")
    abstract fun observeQuizzes(): Flow<List<UnitQuizEntity>>

    @Query("SELECT * FROM unit_quiz WHERE unitId = :id")
    abstract suspend fun quiz(id: String): UnitQuizEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertQuiz(row: UnitQuizEntity)

    /**
     * One transaction: the quiz's best score, whether it is passed, and the XP for the FIRST pass only.
     * Returns true when this call was the first pass (so XP was added).
     */
    @Transaction
    open suspend fun recordQuiz(unitId: String, scorePercent: Int, passed: Boolean, xp: Int, day: Long, now: Long): Boolean {
        val before = quiz(unitId)
        val m = mergeQuiz(before?.bestScore, before?.passed == true, before?.passedAt ?: 0L, scorePercent, passed, now)
        upsertQuiz(UnitQuizEntity(unitId, m.best, m.passed, m.passedAt))
        if (m.firstPass && xp > 0) {
            insertDay(DailyStatEntity(day, 0))
            addXp(day, xp)
        }
        return m.firstPass
    }

    /** One transaction: lesson best score + the day's XP. Room runs it off the main thread. */
    @Transaction
    open suspend fun record(lessonId: String, scorePercent: Int, xp: Int, day: Long, now: Long) {
        val best = maxOf(lesson(lessonId)?.bestScore ?: 0, scorePercent)
        upsertLesson(LessonProgressEntity(lessonId, best, true, now))
        insertDay(DailyStatEntity(day, 0))
        addXp(day, xp)
    }
}

/** Version 1 to 2: progress tables. SQL must match app/schemas/.../2.json (checked by MigrationSqlTest). */
object Migrations {
    val STATEMENTS_1_2 = listOf(
        "CREATE TABLE IF NOT EXISTS `lesson_progress` (`lessonId` TEXT NOT NULL, `bestScore` INTEGER NOT NULL, `completed` INTEGER NOT NULL, `completedAt` INTEGER NOT NULL, PRIMARY KEY(`lessonId`))",
        "CREATE TABLE IF NOT EXISTS `daily_stat` (`day` INTEGER NOT NULL, `xp` INTEGER NOT NULL, PRIMARY KEY(`day`))",
    )

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            STATEMENTS_1_2.forEach { db.execSQL(it) }
        }
    }
}

/** Version 2 to 3: unit quiz results. SQL must match app/schemas/.../3.json (checked by MigrationSqlTest). */
object Migrations3 {
    val STATEMENTS_2_3 = listOf(
        "CREATE TABLE IF NOT EXISTS `unit_quiz` (`unitId` TEXT NOT NULL, `bestScore` INTEGER NOT NULL, `passed` INTEGER NOT NULL, `passedAt` INTEGER NOT NULL, PRIMARY KEY(`unitId`))",
    )

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            STATEMENTS_2_3.forEach { db.execSQL(it) }
        }
    }
}

/** Never use destructive migration. New tables arrive as explicit migrations. */
@Database(
    entities = [ProfileEntity::class, LessonProgressEntity::class, DailyStatEntity::class, UnitQuizEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun progressDao(): ProgressDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "dopamine.db")
                .addMigrations(Migrations.MIGRATION_1_2, Migrations3.MIGRATION_2_3)
                .build()
    }
}
