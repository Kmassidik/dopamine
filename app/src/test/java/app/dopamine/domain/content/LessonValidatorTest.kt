package app.dopamine.domain.content

import app.dopamine.domain.model.Lesson
import app.dopamine.domain.model.LessonRef
import app.dopamine.domain.model.Question
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LessonValidatorTest {
    private fun mc(id: String = "q1", answer: Int? = 0, choices: List<String>? = listOf("a", "b")) =
        Question(id, "multiple_choice", prompt = "p", choices = choices, answerIndex = answer, explanation = "e")

    private fun lesson(vararg q: Question) = Lesson("l", 1, "t", 10, questions = q.toList())

    @Test fun validLessonPasses() = assertNull(LessonValidator.validate(lesson(mc())))

    @Test fun emptyLessonFails() = assertNotNull(LessonValidator.validate(lesson()))

    @Test fun answerIndexOutOfRangeFails() = assertNotNull(LessonValidator.validate(lesson(mc(answer = 5))))

    @Test fun duplicateIdsFail() = assertNotNull(LessonValidator.validate(lesson(mc("q1"), mc("q1"))))

    @Test fun unknownTypeIsAllowed() {
        assertNull(LessonValidator.validate(lesson(mc(), Question("q9", "hologram", explanation = "e"))))
    }

    @Test fun manifestMismatchFails() {
        val ref = LessonRef("other", "t", "x/y.json", 1, "h", 1, 1)
        assertNotNull(LessonValidator.validate(lesson(mc()), ref))
    }

    @Test fun matchNeedsTwoPairs() {
        val q = Question("q", "match_pairs", pairs = listOf(listOf("a", "b")), explanation = "e")
        assertNotNull(LessonValidator.validate(lesson(q)))
    }
}
