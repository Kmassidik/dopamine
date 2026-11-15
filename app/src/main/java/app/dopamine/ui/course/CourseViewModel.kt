package app.dopamine.ui.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dopamine.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LessonInfo(val questions: Int, val xp: Int)

class CourseViewModel(private val c: AppContainer, val courseId: String) : ViewModel() {
    val manifest = c.content.manifest
    private val _info = MutableStateFlow<Map<String, LessonInfo>>(emptyMap())
    val info: StateFlow<Map<String, LessonInfo>> = _info.asStateFlow()

    init {
        viewModelScope.launch {
            c.content.load()
            val course = c.content.manifest.value?.courses?.firstOrNull { it.id == courseId } ?: return@launch
            _info.value = course.units.flatMap { it.lessons }.mapNotNull { ref ->
                c.content.lesson(ref)?.let { ref.id to LessonInfo(it.questions.size, it.xp) }
            }.toMap()
        }
    }
}
