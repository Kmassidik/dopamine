package app.dopamine.domain.progress

import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test

class QuizTest {
    @Test fun passMarkIsSeventyPercent() {
        assertTrue(quizPassed(7, 10))
        assertFalse(quizPassed(6, 10))
        assertTrue(quizPassed(3, 4))   // 75%
        assertFalse(quizPassed(2, 3))  // 67%
        assertFalse(quizPassed(0, 0))
    }

    @Test fun pickBalancedTakesFromEveryLesson() {
        val a = (1..10).map { "a$it" }
        val b = (1..10).map { "b$it" }
        val picked = pickBalanced(listOf(a, b), 6, Random(1))
        assertEquals(6, picked.size)
        assertEquals(3, picked.count { it.startsWith("a") })
        assertEquals(3, picked.count { it.startsWith("b") })
        assertEquals(picked.size, picked.toSet().size)
    }

    @Test fun pickBalancedUsesEverythingWhenThePoolIsSmall() {
        val picked = pickBalanced(listOf(listOf(1, 2), listOf(3)), 10, Random(2))
        assertEquals(setOf(1, 2, 3), picked.toSet())
        assertEquals(3, picked.size)
    }

    @Test fun pickBalancedHandlesEmptyGroups() {
        assertEquals(emptyList<Int>(), pickBalanced(listOf(emptyList<Int>(), emptyList()), 5))
        assertEquals(2, pickBalanced(listOf(emptyList(), listOf(1, 2)), 5).size)
    }

    @Test fun pickBalancedNeverExceedsTheLimit() {
        val big = listOf((1..50).toList(), (51..100).toList(), (101..150).toList())
        assertEquals(QUIZ_MAX_QUESTIONS, pickBalanced(big).size)
    }

    @Test fun unevenLessonsStillShareTheSlots() {
        val picked = pickBalanced(listOf((1..20).toList(), listOf(100)), 4, Random(3))
        assertTrue(100 in picked)
    }

    @Test fun firstPassEarnsXpAndRecordsTheTime() {
        val m = mergeQuiz(null, false, 0, 80, true, now = 1000)
        assertEquals(QuizMerge(80, true, 1000, true), m)
    }

    @Test fun passingAgainDoesNotEarnXpOrMoveTheTime() {
        val m = mergeQuiz(80, true, 1000, 100, true, now = 5000)
        assertEquals(QuizMerge(100, true, 1000, false), m)
    }

    @Test fun failingKeepsTheBestScoreAndAnEarlierPass() {
        assertEquals(QuizMerge(40, false, 0, false), mergeQuiz(null, false, 0, 40, false, 9))
        assertEquals(QuizMerge(80, true, 1000, false), mergeQuiz(80, true, 1000, 30, false, 9))
        assertEquals(QuizMerge(60, false, 0, false), mergeQuiz(60, false, 0, 20, false, 9))
    }
}
