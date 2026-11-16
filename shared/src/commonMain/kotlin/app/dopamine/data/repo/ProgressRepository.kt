package app.dopamine.data.repo

import app.dopamine.data.db.ProgressDao
import app.dopamine.domain.progress.DEFAULT_GOAL_XP
import app.dopamine.domain.progress.epochDay
import app.dopamine.domain.progress.QUIZ_XP
import app.dopamine.domain.progress.quizPassed
import app.dopamine.domain.progress.scorePercent
import app.dopamine.domain.progress.streak
import app.dopamine.platform.localUtcOffsetMillis
import app.dopamine.platform.nowMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** What Home shows. Immutable so Compose can skip unchanged readers. */
data class HomeStats(val totalXp: Int = 0, val streak: Int = 0, val todayXp: Int = 0, val goalXp: Int = DEFAULT_GOAL_XP)

/** Reads are Flows (Room runs them off the main thread); writes are suspend (also off main). */
class ProgressRepository(private val dao: ProgressDao, private val now: () -> Long = ::nowMillis) {
    val completedLessons: Flow<Set<String>> =
        dao.observeLessons().map { rows -> rows.filter { it.completed }.mapTo(HashSet()) { it.lessonId } as Set<String> }.distinctUntilChanged()

    val passedQuizzes: Flow<Set<String>> =
        dao.observeQuizzes().map { rows -> rows.filter { it.passed }.mapTo(HashSet()) { it.unitId } as Set<String> }.distinctUntilChanged()

    val homeStats: Flow<HomeStats> = dao.observeDays().map { days ->
        val today = today()
        HomeStats(
            totalXp = days.sumOf { it.xp },
            streak = streak(days.mapTo(HashSet()) { it.day }, today),
            todayXp = days.firstOrNull { it.day == today }?.xp ?: 0,
        )
    }.distinctUntilChanged()

    suspend fun recordLesson(lessonId: String, correct: Int, total: Int, xp: Int) {
        val t = now()
        dao.record(lessonId, scorePercent(correct, total), xp, epochDay(t, localUtcOffsetMillis(t)), t)
    }

    /** Saves a finished unit quiz. Returns true when it was the first pass (QUIZ_XP was added). */
    suspend fun recordQuiz(unitId: String, correct: Int, total: Int): Boolean {
        val t = now()
        return dao.recordQuiz(unitId, scorePercent(correct, total), quizPassed(correct, total), QUIZ_XP, today(), t)
    }

    private fun today(): Long {
        val t = now()
        return epochDay(t, localUtcOffsetMillis(t))
    }
}
