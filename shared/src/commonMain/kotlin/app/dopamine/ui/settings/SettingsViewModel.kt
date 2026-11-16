package app.dopamine.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dopamine.AppContainer
import app.dopamine.data.network.UpdateException
import app.dopamine.data.repo.ProfileState
import app.dopamine.domain.model.ProfileName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UpdateUi {
    data object Idle : UpdateUi
    data class Running(val done: Int, val total: Int) : UpdateUi
    data class Done(val message: String) : UpdateUi
    data class Failed(val message: String) : UpdateUi
}

class SettingsViewModel(private val c: AppContainer) : ViewModel() {
    val versionName: String = c.appVersionName

    // --- name ---------------------------------------------------------------
    val name: StateFlow<String> = c.profile.state
        .map { (it as? ProfileState.Ready)?.name.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")
    private val _draft = MutableStateFlow<String?>(null)
    val draft: StateFlow<String?> = _draft.asStateFlow()

    fun startEdit() {
        _draft.value = name.value
    }

    fun setDraft(value: String) {
        if (_draft.value != null) _draft.value = value
    }

    fun cancelEdit() {
        _draft.value = null
    }

    fun saveName() {
        val clean = ProfileName.clean(_draft.value.orEmpty()) ?: return
        viewModelScope.launch {
            c.profile.save(clean)
            _draft.value = null
        }
    }

    // --- content update -----------------------------------------------------
    private val _update = MutableStateFlow<UpdateUi>(UpdateUi.Idle)
    val update: StateFlow<UpdateUi> = _update.asStateFlow()
    private val _lastUpdated = MutableStateFlow(c.store.lastUpdatedAt())
    val lastUpdated: StateFlow<Long> = _lastUpdated.asStateFlow()
    private var job: Job? = null

    fun startUpdate() {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            _update.value = UpdateUi.Running(0, 0)
            try {
                val r = c.updates.update { d, t -> _update.value = UpdateUi.Running(d, t) }
                c.content.reload()
                _lastUpdated.value = c.store.lastUpdatedAt()
                _update.value = UpdateUi.Done(
                    if (r.upToDate) "You already have the latest lessons."
                    else "${r.newCount} new, ${r.updatedCount} updated, ${r.removedCount} removed."
                )
            } catch (e: CancellationException) {
                _update.value = UpdateUi.Idle
                throw e
            } catch (e: UpdateException) {
                _update.value = UpdateUi.Failed(e.message ?: "Update failed. Your lessons are unchanged.")
            } catch (e: Exception) {
                _update.value = UpdateUi.Failed("Update failed. Your lessons are unchanged.")
            }
        }
    }

    fun cancelUpdate() {
        job?.cancel()
    }

    fun dismissMessage() {
        if (_update.value is UpdateUi.Done || _update.value is UpdateUi.Failed) _update.value = UpdateUi.Idle
    }
}
