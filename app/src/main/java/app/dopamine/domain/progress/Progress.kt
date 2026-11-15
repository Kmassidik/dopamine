package app.dopamine.domain.progress

import kotlin.math.roundToInt

/** Pure progress rules: no Android, no clock. Days are "epoch days" in the user's local time. */
const val DEFAULT_GOAL_XP = 20
private const val DAY_MS = 86_400_000L

/** XP for a finished lesson: the lesson's XP scaled by the share of correct answers. */
fun earnedXp(lessonXp: Int, correct: Int, total: Int): Int =
    if (total <= 0 || lessonXp <= 0) 0 else (lessonXp * correct.coerceIn(0, total).toFloat() / total).roundToInt()

/** Accuracy as a whole percent, 0 when there were no questions. */
fun scorePercent(correct: Int, total: Int): Int =
    if (total <= 0) 0 else (correct.coerceIn(0, total) * 100f / total).roundToInt()

/** Local calendar day number for a timestamp. [tzOffsetMs] is the zone offset valid at that instant. */
fun epochDay(millis: Long, tzOffsetMs: Long): Long = Math.floorDiv(millis + tzOffsetMs, DAY_MS)

/**
 * Consecutive days with at least one finished lesson, ending today. If nothing was finished today yet,
 * the streak is still alive when yesterday has one (it ends only after a full empty day).
 */
fun streak(activeDays: Set<Long>, today: Long): Int {
    var day = when {
        today in activeDays -> today
        (today - 1) in activeDays -> today - 1
        else -> return 0
    }
    var n = 0
    while (day in activeDays) {
        n++
        day--
    }
    return n
}

/** Share of the daily goal reached, 0..1. */
fun goalFraction(todayXp: Int, goal: Int = DEFAULT_GOAL_XP): Float =
    if (goal <= 0) 1f else (todayXp.toFloat() / goal).coerceIn(0f, 1f)

/** Best score only goes up. */
fun betterScore(previous: Int?, new: Int): Int = maxOf(previous ?: 0, new)

// ---- Unit quiz ------------------------------------------------------------------------------

const val QUIZ_PASS_PERCENT = 70
const val QUIZ_XP = 20
const val QUIZ_MAX_QUESTIONS = 10
const val QUIZ_MIN_QUESTIONS = 3

fun quizPassed(correct: Int, total: Int): Boolean = total > 0 && scorePercent(correct, total) >= QUIZ_PASS_PERCENT

/**
 * Picks up to [n] questions for a unit quiz, taking turns between the lessons so each lesson is represented.
 * Each lesson's questions are shuffled first, and the final order is shuffled too.
 */
fun <T> pickBalanced(groups: List<List<T>>, n: Int = QUIZ_MAX_QUESTIONS, random: kotlin.random.Random = kotlin.random.Random.Default): List<T> {
    val queues = groups.map { ArrayDeque(it.shuffled(random)) }.filter { it.isNotEmpty() }
    val out = ArrayList<T>()
    while (out.size < n && queues.any { it.isNotEmpty() }) {
        for (q in queues) {
            if (out.size >= n) break
            q.removeFirstOrNull()?.let { out.add(it) }
        }
    }
    return out.shuffled(random)
}

/** What to store after a quiz attempt. The best score only goes up; a pass is never lost; XP only on the first pass. */
data class QuizMerge(val best: Int, val passed: Boolean, val passedAt: Long, val firstPass: Boolean)

fun mergeQuiz(prevBest: Int?, prevPassed: Boolean, prevPassedAt: Long, score: Int, passed: Boolean, now: Long): QuizMerge {
    val first = passed && !prevPassed
    return QuizMerge(
        best = maxOf(prevBest ?: 0, score),
        passed = passed || prevPassed,
        passedAt = if (first) now else prevPassedAt,
        firstPass = first,
    )
}
