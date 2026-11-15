package app.dopamine.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dopamine.AppContainer
import kotlinx.coroutines.launch

class CategoryViewModel(c: AppContainer, val categoryId: String) : ViewModel() {
    val manifest = c.content.manifest

    init {
        viewModelScope.launch { c.content.load() }
    }
}
