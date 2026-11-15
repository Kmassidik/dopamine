package app.dopamine.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dopamine.AppContainer
import app.dopamine.data.network.UpdateException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UpdateUi {
    data object Idle : UpdateUi
    data class Running(val done: Int, val total: Int) : UpdateUi
    data class Done(val message: String) : UpdateUi
    data class Failed(val message: String) : UpdateUi
}

class HomeViewModel(private val c: AppContainer) : ViewModel() {
    val manifest = c.content.manifest
    private val _update = MutableStateFlow<UpdateUi>(UpdateUi.Idle)
    val update: StateFlow<UpdateUi> = _update.asStateFlow()
    private val _lastUpdated = MutableStateFlow(c.store.lastUpdatedAt())
    val lastUpdated: StateFlow<Long> = _lastUpdated.asStateFlow()
    private var job: Job? = null

    init {
        viewModelScope.launch { c.content.load() }
    }

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
