package app.dopamine.ui.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dopamine.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LessonInfo(val questions: Int, val xp: Int)

class CourseViewModel(private val c: AppContainer, val courseId: String) : ViewModel() {
    val manifest = c.content.manifest
    val done: StateFlow<Set<String>> = c.progress.completedLessons.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())
    val passedQuizzes: StateFlow<Set<String>> = c.progress.passedQuizzes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())
    private val _info = MutableStateFlow<Map<String, LessonInfo>>(emptyMap())
    val info: StateFlow<Map<String, LessonInfo>> = _info.asStateFlow()

    init {
        viewModelScope.launch {
            c.content.load()
            val course = c.content.manifest.value?.courses?.firstOrNull { it.id == courseId } ?: return@launch
            _info.value = course.units.flatMap { it.lessons }.filter { !app.dopamine.domain.content.ContentContract.isLocked(it.minApp) }.mapNotNull { ref ->
                // Counts are cached per lesson version+hash, so reopening a course reads no lesson files.
                val key = "${ref.id}:${ref.version}:${ref.sha256}"
                val info = infoCache[key] ?: c.content.lesson(ref)?.let { LessonInfo(it.questions.count { q -> q.type in app.dopamine.domain.model.KNOWN_QUESTION_TYPES }, it.xp) }?.also { infoCache[key] = it }
                info?.let { ref.id to it }
            }.toMap()
        }
    }

    private companion object {
        val infoCache = java.util.concurrent.ConcurrentHashMap<String, LessonInfo>()
    }
}
