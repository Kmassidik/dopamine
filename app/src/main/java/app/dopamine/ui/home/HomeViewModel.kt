package app.dopamine.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dopamine.AppContainer
import app.dopamine.data.repo.HomeStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val c: AppContainer) : ViewModel() {
    val manifest = c.content.manifest
    val loaded = c.content.loaded
    val stats: StateFlow<HomeStats> = c.progress.homeStats.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeStats())

    init {
        viewModelScope.launch { c.content.load() }
    }
}
