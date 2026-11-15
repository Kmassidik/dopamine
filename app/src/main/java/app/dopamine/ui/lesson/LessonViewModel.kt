package app.dopamine.ui.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dopamine.AppContainer
import app.dopamine.domain.model.KNOWN_QUESTION_TYPES
import app.dopamine.domain.model.Lesson
import app.dopamine.domain.model.Question
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
) {
    val finished get() = !loading && questions.isNotEmpty() && index >= questions.size
    val xpEarned get() = if (questions.isEmpty()) 0 else Math.round((lesson?.xp ?: 0) * correct.toFloat() / questions.size)
}

class LessonViewModel(private val c: AppContainer, private val lessonId: String) : ViewModel() {
    private val _state = MutableStateFlow(LessonState())
    val state: StateFlow<LessonState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            c.content.load()
            val ref = c.content.manifest.value?.courses?.flatMap { it.units }?.flatMap { it.lessons }?.firstOrNull { it.id == lessonId }
            val lesson = ref?.let { c.content.lesson(it) }
            // Unknown types are skipped by design. Questions with pictures wait for the image renderer.
            val qs = lesson?.questions?.filter { it.type in KNOWN_QUESTION_TYPES && it.image == null }.orEmpty()
            _state.value = LessonState(loading = false, lesson = lesson, questions = qs)
        }
    }

    fun check(correct: Boolean) = _state.update {
        if (it.checked) it else it.copy(checked = true, lastCorrect = correct, correct = it.correct + if (correct) 1 else 0)
    }

    fun next() = _state.update { it.copy(index = it.index + 1, checked = false) }
}
