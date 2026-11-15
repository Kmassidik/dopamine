package app.dopamine.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dopamine.AppContainer
import kotlinx.coroutines.launch

class HomeViewModel(private val c: AppContainer) : ViewModel() {
    val manifest = c.content.manifest

    init {
        viewModelScope.launch { c.content.load() }
    }
}
