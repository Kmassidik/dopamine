package app.dopamine.ui.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dopamine.AppContainer
import app.dopamine.domain.model.KNOWN_PAGE_KINDS
import app.dopamine.domain.model.LearnPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LearnState(
    val loading: Boolean = true,
    val pages: List<LearnPage> = emptyList(),
    val index: Int = 0,
    /** For an example page: how many steps are visible so far. */
    val shown: Int = 0,
) {
    val page get() = pages.getOrNull(index)
    val atLastStep get() = page?.kind != "example" || shown >= (page?.steps?.size ?: 0)
    val isLast get() = index >= pages.lastIndex
}

class LearnViewModel(private val c: AppContainer, private val lessonId: String) : ViewModel() {
    private val _state = MutableStateFlow(LearnState())
    val state: StateFlow<LearnState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            c.content.load()
            val ref = c.content.manifest.value?.courses?.flatMap { it.units }?.flatMap { it.lessons }?.firstOrNull { it.id == lessonId }
            val pages = ref?.let { c.content.lesson(it) }?.learn.orEmpty()
                .map { if (it.kind in KNOWN_PAGE_KINDS) it else LearnPage(it.id, "unsupported") }
            _state.value = LearnState(loading = false, pages = pages, index = app.dopamine.DevHooks.learnPage.coerceIn(0, maxOf(0, pages.lastIndex)))
        }
    }

    /** The main button: reveal the next step, else the next page. Returns true when reading is finished. */
    fun advance(): Boolean {
        val s = _state.value
        if (!s.atLastStep) {
            _state.update { it.copy(shown = it.shown + 1) }
            return false
        }
        if (s.isLast) return true
        _state.update { it.copy(index = it.index + 1, shown = 0) }
        return false
    }
}
