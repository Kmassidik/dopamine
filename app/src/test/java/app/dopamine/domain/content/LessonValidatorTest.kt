package app.dopamine.domain.content

import app.dopamine.domain.model.LearnPage
import app.dopamine.domain.model.Lesson
import app.dopamine.domain.model.LessonRef
import app.dopamine.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LessonValidatorTest {
    private fun mc(id: String = "q1", answer: Int? = 0, choices: List<String>? = listOf("a", "b")) =
        Question(id, "multiple_choice", prompt = "p", choices = choices, answerIndex = answer, explanation = "e")

    private fun lesson(vararg q: Question, minApp: Int = 1, learn: List<LearnPage> = emptyList()) =
        Lesson("l", 1, "t", 10, questions = q.toList(), learn = learn, minApp = minApp)

    private fun bad(l: Lesson) = assertNotNull(LessonValidator.validate(l))

    @Test fun validLessonPasses() = assertNull(LessonValidator.validate(lesson(mc())))
    @Test fun emptyLessonFails() = bad(lesson())
    @Test fun answerIndexOutOfRangeFails() = bad(lesson(mc(answer = 5)))
    @Test fun duplicateIdsFail() = bad(lesson(mc("q1"), mc("q1")))
    @Test fun duplicateChoicesFail() = bad(lesson(mc(choices = listOf("a", "a"))))
    @Test fun tooManyChoicesFail() = bad(lesson(mc(choices = listOf("a", "b", "c", "d", "e", "f"))))
    @Test fun badIdFails() = bad(lesson(mc("Bad Id")))
    @Test fun missingExplanationFails() = bad(lesson(mc().copy(explanation = "")))
    @Test fun overlongTextFails() = bad(lesson(mc().copy(prompt = "x".repeat(601))))
    @Test fun badImagePathFails() = bad(lesson(mc().copy(image = "../secret.svg")))
    @Test fun xpOutOfRangeFails() = bad(lesson(mc()).copy(xp = 0))

    @Test fun unknownQuestionTypeIsRejected() = bad(lesson(mc(), Question("q9", "hologram", explanation = "e")))
    @Test fun unknownPageKindIsRejected() = bad(lesson(mc(), learn = listOf(LearnPage("p1", "hologram", body = "b"))))

    @Test fun lockedLessonIsNotReadButPasses() {
        // Needs a newer app: even unknown kinds are fine because the app will not open it.
        val l = lesson(Question("q9", "hologram", explanation = "e"), minApp = ContentContract.APP_VERSION + 1)
        assertNull(LessonValidator.validate(l))
        assertEquals(true, ContentContract.isLocked(l.minApp))
    }

    @Test fun codePageNeedsTitleAndCode() {
        bad(lesson(mc(), learn = listOf(LearnPage("c1", "code", title = null, code = "x = 1"))))
        bad(lesson(mc(), learn = listOf(LearnPage("c1", "code", title = "T", code = null))))
        assertNull(LessonValidator.validate(lesson(mc(), learn = listOf(LearnPage("c1", "code", title = "T", code = "x = 1")))))
    }

    @Test fun examplePageNeedsSteps() {
        bad(lesson(mc(), learn = listOf(LearnPage("e1", "example", title = "T", steps = emptyList()))))
        assertNull(LessonValidator.validate(lesson(mc(), learn = listOf(LearnPage("e1", "example", title = "T", steps = listOf("s"))))))
    }

    @Test fun duplicateLearnPageIdsFail() {
        val p = LearnPage("p1", "text", body = "b")
        bad(lesson(mc(), learn = listOf(p, p)))
    }

    @Test fun manifestMismatchFails() {
        val ref = LessonRef("other", "t", "x/y.json", 1, "h", 1, 1)
        assertNotNull(LessonValidator.validate(lesson(mc()), ref))
    }
}
