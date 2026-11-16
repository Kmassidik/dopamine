package app.dopamine.domain.progress

import kotlin.test.assertEquals
import kotlin.test.Test

class ProgressTest {
    @Test fun xpScalesWithAccuracy() {
        assertEquals(10, earnedXp(10, 6, 6))
        assertEquals(5, earnedXp(10, 3, 6))
        assertEquals(0, earnedXp(10, 0, 6))
        assertEquals(10, earnedXp(10, 9, 6)) // correct is clamped to total
        assertEquals(0, earnedXp(10, 3, 0))
        assertEquals(0, earnedXp(-5, 3, 3))
    }

    @Test fun scorePercentRounds() {
        assertEquals(67, scorePercent(2, 3))
        assertEquals(100, scorePercent(3, 3))
        assertEquals(0, scorePercent(1, 0))
    }

    @Test fun epochDayUsesLocalOffset() {
        val h = 3_600_000L
        val midnightUtc = 20_000L * 86_400_000L
        assertEquals(20_000L, epochDay(midnightUtc + h, 0))
        assertEquals(19_999L, epochDay(midnightUtc + h, -2 * h)) // still the previous day in UTC-2
        assertEquals(20_000L, epochDay(midnightUtc - h, 2 * h)) // already the next day in UTC+2
        assertEquals(-1L, epochDay(-1L, 0))
    }

    @Test fun streakCountsConsecutiveDays() {
        assertEquals(0, streak(emptySet(), 100))
        assertEquals(1, streak(setOf(100L), 100))
        assertEquals(3, streak(setOf(98L, 99L, 100L), 100))
        assertEquals(2, streak(setOf(90L, 91L, 99L, 100L), 100)) // a gap ends the run
    }

    @Test fun streakSurvivesUntilTodayEnds() {
        assertEquals(2, streak(setOf(98L, 99L), 100)) // nothing yet today, still alive
        assertEquals(0, streak(setOf(97L, 98L), 100)) // a full empty day breaks it
    }

    @Test fun futureDaysDoNotBreakTheCount() {
        assertEquals(2, streak(setOf(99L, 100L, 105L), 100)) // clock moved back: ignore later days
    }

    @Test fun goalFractionIsClamped() {
        assertEquals(0f, goalFraction(0), 0f)
        assertEquals(0.5f, goalFraction(10), 0f)
        assertEquals(1f, goalFraction(45), 0f)
        assertEquals(1f, goalFraction(5, 0), 0f)
    }

    @Test fun bestScoreOnlyGoesUp() {
        assertEquals(80, betterScore(null, 80))
        assertEquals(80, betterScore(80, 50))
        assertEquals(100, betterScore(80, 100))
    }
}
