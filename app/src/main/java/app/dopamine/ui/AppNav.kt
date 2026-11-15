package app.dopamine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.dopamine.AppContainer
import app.dopamine.data.repo.ProfileState
import app.dopamine.ui.category.CategoryScreen
import app.dopamine.ui.category.CategoryViewModel
import app.dopamine.ui.home.HomeScreen
import app.dopamine.ui.home.HomeViewModel
import app.dopamine.ui.onboarding.OnboardingScreen
import app.dopamine.ui.onboarding.OnboardingViewModel
import app.dopamine.ui.theme.Dopamine

@Composable
fun AppNav(container: AppContainer) {
    val profile by container.profile.state.collectAsStateWithLifecycle(ProfileState.Loading)
    when (val p = profile) {
        ProfileState.Loading -> Box(Modifier.fillMaxSize().background(Dopamine.colors.bg))
        ProfileState.None -> {
            val vm: OnboardingViewModel = viewModel(factory = viewModelFactory { initializer { OnboardingViewModel(container) } })
            OnboardingScreen(vm)
        }
        is ProfileState.Ready -> Main(container, p.name)
    }
}

@Composable
private fun Main(container: AppContainer, name: String) {
    val nav = rememberNavController()
    val home: HomeViewModel = viewModel(factory = viewModelFactory { initializer { HomeViewModel(container) } })
    NavHost(nav, startDestination = "home") {
        composable("home") {
            HomeScreen(name, home, onOpenCategory = { nav.navigate("category/$it") })
        }
        composable("category/{id}") { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            val vm: CategoryViewModel = viewModel(key = "category-$id", factory = viewModelFactory {
                initializer { CategoryViewModel(container, id) }
            })
            CategoryScreen(vm, onBack = { nav.popBackStack() })
        }
    }
}
