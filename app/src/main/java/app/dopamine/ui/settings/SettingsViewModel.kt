package app.dopamine.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dopamine.AppContainer
import app.dopamine.BuildConfig
import app.dopamine.data.network.AppUpdateException
import app.dopamine.data.network.ReleaseVersion
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
    data class Done(val newCount: Int, val updatedCount: Int, val removedCount: Int, val upToDate: Boolean) : UpdateUi
    data class Failed(val message: String?) : UpdateUi
}

sealed interface AppUpdateUi {
    data object Idle : AppUpdateUi
    data object Running : AppUpdateUi
    data object UpToDate : AppUpdateUi
    data class Available(val version: String, val url: String) : AppUpdateUi
    data class Failed(val message: String?) : AppUpdateUi
}

class SettingsViewModel(private val c: AppContainer) : ViewModel() {
    val versionName: String = BuildConfig.VERSION_NAME

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
                _update.value = UpdateUi.Done(r.newCount, r.updatedCount, r.removedCount, r.upToDate)
            } catch (e: CancellationException) {
                _update.value = UpdateUi.Idle
                throw e
            } catch (e: UpdateException) {
                _update.value = UpdateUi.Failed(e.message)
            } catch (e: Exception) {
                _update.value = UpdateUi.Failed(null)
            }
        }
    }

    fun cancelUpdate() {
        job?.cancel()
    }

    fun dismissMessage() {
        if (_update.value is UpdateUi.Done || _update.value is UpdateUi.Failed) _update.value = UpdateUi.Idle
    }

    // --- app update ---------------------------------------------------------
    private val _appUpdate = MutableStateFlow<AppUpdateUi>(AppUpdateUi.Idle)
    val appUpdate: StateFlow<AppUpdateUi> = _appUpdate.asStateFlow()
    private var appUpdateJob: Job? = null

    fun checkAppUpdate() {
        if (appUpdateJob?.isActive == true) return
        appUpdateJob = viewModelScope.launch {
            _appUpdate.value = AppUpdateUi.Running
            try {
                val release = c.appUpdates.check()
                _appUpdate.value = if (ReleaseVersion.isNewer(release.version, versionName)) {
                    AppUpdateUi.Available(release.version, release.url)
                } else {
                    AppUpdateUi.UpToDate
                }
            } catch (e: CancellationException) {
                _appUpdate.value = AppUpdateUi.Idle
                throw e
            } catch (e: AppUpdateException) {
                _appUpdate.value = AppUpdateUi.Failed(e.message)
            } catch (e: Exception) {
                _appUpdate.value = AppUpdateUi.Failed(null)
            }
        }
    }

    fun dismissAppUpdateMessage() {
        if (_appUpdate.value !is AppUpdateUi.Running) _appUpdate.value = AppUpdateUi.Idle
    }
}
