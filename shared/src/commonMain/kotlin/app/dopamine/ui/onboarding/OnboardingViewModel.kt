package app.dopamine.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dopamine.AppContainer
import app.dopamine.domain.model.ProfileName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(private val c: AppContainer) : ViewModel() {
    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    fun set(value: String) {
        _text.value = value
    }

    fun save() {
        val name = ProfileName.clean(_text.value) ?: return
        viewModelScope.launch { c.profile.save(name) }
    }
}
