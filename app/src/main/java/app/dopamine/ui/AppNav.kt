package app.dopamine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.dopamine.ui.components.FloatingNav
import app.dopamine.ui.components.Tab
import app.dopamine.ui.course.CourseScreen
import app.dopamine.ui.course.CourseViewModel
import app.dopamine.ui.lesson.LessonScreen
import app.dopamine.ui.lesson.LessonViewModel
import app.dopamine.ui.review.ReviewScreen
import app.dopamine.AppContainer
import app.dopamine.data.repo.ProfileState
import app.dopamine.ui.category.CategoryScreen
import app.dopamine.ui.category.CategoryViewModel
import app.dopamine.ui.home.HomeScreen
import app.dopamine.ui.home.HomeViewModel
import app.dopamine.ui.onboarding.OnboardingScreen
import app.dopamine.ui.onboarding.OnboardingViewModel
import app.dopamine.ui.settings.SettingsScreen
import app.dopamine.ui.settings.SettingsViewModel
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
    // Activity-scoped so an update keeps running while you move between screens.
    val settings: SettingsViewModel = viewModel(factory = viewModelFactory { initializer { SettingsViewModel(container) } })

    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val inLesson = route?.startsWith("lesson/") == true
    val selected = when (route) {
        "review" -> Tab.Review
        "settings" -> Tab.Settings
        else -> if (inLesson) null else Tab.Home
    }
    fun goTab(tab: Tab) = nav.navigate(tab.route) {
        popUpTo("home") { inclusive = false }
        launchSingleTop = true
    }

    Box(Modifier.fillMaxSize()) {
        NavHost(nav, startDestination = "home") {
            composable("home") {
                HomeScreen(name, home, onOpenCategory = { nav.navigate("category/$it") }, onOpenSettings = { goTab(Tab.Settings) })
            }
            composable("review") { ReviewScreen() }
            composable("settings") { SettingsScreen(settings) }
            composable("category/{id}") { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val vm: CategoryViewModel = viewModel(key = "category-$id", factory = viewModelFactory {
                    initializer { CategoryViewModel(container, id) }
                })
                CategoryScreen(vm, onBack = { nav.popBackStack() }, onOpenCourse = { nav.navigate("course/$it") })
            }
            composable("course/{id}") { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val vm: CourseViewModel = viewModel(key = "course-$id", factory = viewModelFactory {
                    initializer { CourseViewModel(container, id) }
                })
                CourseScreen(vm, onBack = { nav.popBackStack() }, onOpenLesson = { nav.navigate("lesson/$it") })
            }
            composable("lesson/{id}") { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val vm: LessonViewModel = viewModel(key = "lesson-$id", factory = viewModelFactory {
                    initializer { LessonViewModel(container, id) }
                })
                LessonScreen(vm, onClose = { nav.popBackStack() })
            }
        }
        if (!inLesson) {
            FloatingNav(
                selected,
                onSelect = ::goTab,
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 12.dp),
            )
        }
    }
}
