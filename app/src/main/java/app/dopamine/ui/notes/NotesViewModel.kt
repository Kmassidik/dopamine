package app.dopamine.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dopamine.AppContainer
import app.dopamine.domain.model.KNOWN_NOTE_KINDS
import app.dopamine.domain.model.NoteBlock
import app.dopamine.domain.model.Notes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotesState(val loading: Boolean = true, val notes: Notes? = null)

class NotesViewModel(private val c: AppContainer, private val unitId: String) : ViewModel() {
    private val _state = MutableStateFlow(NotesState())
    val state: StateFlow<NotesState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            c.content.load()
            val ref = c.content.manifest.value?.courses?.flatMap { it.units }?.firstOrNull { it.id == unitId }?.notes
            val notes = ref?.let { c.content.notes(it) }
            // Unknown block kinds are skipped, like unknown question types.
            _state.value = NotesState(false, notes?.copy(blocks = notes.blocks.map { if (it.kind in KNOWN_NOTE_KINDS) it else NoteBlock(it.id, "unsupported") }))
        }
    }
}
