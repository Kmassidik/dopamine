package app.dopamine.ui.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dopamine.AppContainer
import app.dopamine.domain.content.ContentContract
import app.dopamine.domain.model.KNOWN_QUESTION_TYPES
import app.dopamine.domain.model.Lesson
import app.dopamine.domain.model.Question
import app.dopamine.domain.progress.QUIZ_XP
import app.dopamine.domain.progress.earnedXp
import app.dopamine.domain.progress.pickBalanced
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LessonState(
    val loading: Boolean = true,
    val lesson: Lesson? = null,
    val questions: List<Question> = emptyList(),
    val index: Int = 0,
    val correct: Int = 0,
    val checked: Boolean = false,
    val lastCorrect: Boolean = false,
    /** Questions left out because this version of the app cannot show them (for example a picture). */
    val skipped: Int = 0,
    /** True when this is a unit quiz (the unit id is the screen's id). */
    val quiz: Boolean = false,
    /** Quiz only: set once the result is saved. True when this pass earned the quiz XP. */
    val quizFirstPass: Boolean? = null,
) {
    val quizPassed get() = quiz && app.dopamine.domain.progress.quizPassed(correct, questions.size)
    val finished get() = !loading && questions.isNotEmpty() && index >= questions.size
    val xpEarned get() = if (quiz) (if (quizFirstPass == true) QUIZ_XP else 0) else earnedXp(lesson?.xp ?: 0, correct, questions.size)
}

class LessonViewModel(private val c: AppContainer, private val lessonId: String, private val quiz: Boolean = false) : ViewModel() {
    private val _state = MutableStateFlow(LessonState())
    val state: StateFlow<LessonState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            c.content.load()
            _state.value = if (quiz) loadQuiz() else loadLesson()
        }
    }

    private suspend fun loadLesson(): LessonState {
        val ref = c.content.manifest.value?.courses?.flatMap { it.units }?.flatMap { it.lessons }?.firstOrNull { it.id == lessonId }
        val lesson = ref?.let { c.content.lesson(it) }
        // Unknown types are skipped by design (and counted on the result screen).
        val qs = lesson?.questions?.filter { it.type in KNOWN_QUESTION_TYPES }.orEmpty()
        return LessonState(loading = false, lesson = lesson, questions = qs, skipped = (lesson?.questions?.size ?: 0) - qs.size)
    }

    /** A unit quiz: questions drawn from the unit's own lessons, balanced between them. */
    private suspend fun loadQuiz(): LessonState {
        val unit = c.content.manifest.value?.courses?.flatMap { it.units }?.firstOrNull { it.id == lessonId }
        val groups = unit?.lessons.orEmpty().filter { !ContentContract.isLocked(it.minApp) }.mapNotNull { ref ->
            c.content.lesson(ref)?.questions?.filter { it.type in KNOWN_QUESTION_TYPES }
        }
        val qs = pickBalanced(groups)
        val lesson = Lesson("quiz-$lessonId", 1, unit?.title.orEmpty(), QUIZ_XP, questions = qs)
        return LessonState(loading = false, lesson = lesson, questions = qs, quiz = true)
    }

    fun check(correct: Boolean) = _state.update {
        if (it.checked) it else it.copy(checked = true, lastCorrect = correct, correct = it.correct + if (correct) 1 else 0)
    }

    private var saved = false

    fun next() {
        _state.update { it.copy(index = it.index + 1, checked = false) }
        val s = _state.value
        if (s.finished && !saved) {
            saved = true
            // Saved once; Room runs it off the main thread and it is not cancelled if the screen closes right away.
            viewModelScope.launch {
                withContext(NonCancellable) {
                    if (quiz) {
                        val first = c.progress.recordQuiz(lessonId, s.correct, s.questions.size)
                        _state.update { it.copy(quizFirstPass = first) }
                    } else {
                        c.progress.recordLesson(lessonId, s.correct, s.questions.size, s.xpEarned)
                    }
                }
            }
        }
    }
}
