package app.dopamine.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dopamine.AppContainer
import app.dopamine.domain.model.LessonRef
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LessonInfo(val questions: Int, val xp: Int)

class CategoryViewModel(private val c: AppContainer, val categoryId: String) : ViewModel() {
    val manifest = c.content.manifest
    private val _info = MutableStateFlow<Map<String, LessonInfo>>(emptyMap())
    val info: StateFlow<Map<String, LessonInfo>> = _info.asStateFlow()

    init {
        viewModelScope.launch {
            c.content.load()
            val m = c.content.manifest.value ?: return@launch
            val refs: List<LessonRef> = m.courses.filter { it.categoryId == categoryId }
                .flatMap { it.units }.flatMap { it.lessons }
            _info.value = refs.mapNotNull { ref ->
                c.content.lesson(ref)?.let { ref.id to LessonInfo(it.questions.size, it.xp) }
            }.toMap()
        }
    }
}
