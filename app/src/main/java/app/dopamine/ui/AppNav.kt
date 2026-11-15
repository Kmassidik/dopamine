package app.dopamine.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.dopamine.AppContainer
import app.dopamine.ui.category.CategoryScreen
import app.dopamine.ui.category.CategoryViewModel
import app.dopamine.ui.home.HomeScreen
import app.dopamine.ui.home.HomeViewModel

@Composable
fun AppNav(container: AppContainer) {
    val nav = rememberNavController()
    val home: HomeViewModel = viewModel(factory = viewModelFactory { initializer { HomeViewModel(container) } })
    NavHost(nav, startDestination = "home") {
        composable("home") {
            HomeScreen(home, onOpenCategory = { nav.navigate("category/$it") })
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
