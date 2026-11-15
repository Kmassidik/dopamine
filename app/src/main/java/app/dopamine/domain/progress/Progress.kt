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
