package app.dopamine

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import app.dopamine.platform.logI
import app.dopamine.ui.AppNav
import app.dopamine.ui.theme.DopamineTheme
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIViewController

/** Same address and version as the Android build (app/build.gradle.kts). Keep them in sync. */
private const val CONTENT_BASE_URL = "https://raw.githubusercontent.com/Kmassidik/dopamine-content/main/"
private const val APP_VERSION_CODE = 3
private const val APP_VERSION_NAME = "0.3.0"

/**
 * The root of the iPhone app, hosted by iosApp (SwiftUI) through UIViewControllerRepresentable.
 *
 * Test hooks, read only from environment variables, which only exist when the app is started by a developer tool
 * (`SIMCTL_CHILD_NAME=value xcrun simctl launch ...` for the simulator); a normal launch has none of them:
 *  - DOPAMINE_SEED_NAME: save this name as the profile (skips onboarding, which has no keyboard automation).
 *  - DOPAMINE_ROUTE: first screen route, e.g. "settings" or "learn/<lessonId>".
 *  - DOPAMINE_LEARN_PAGE: reading page number a learn screen opens on (touch input cannot be automated).
 *  - DOPAMINE_AUTO_UPDATE=1: run "Update content" once at start and log the result.
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    val container = remember { createAppContainer(CONTENT_BASE_URL, APP_VERSION_CODE, APP_VERSION_NAME) }
    val env = remember { NSProcessInfo.processInfo.environment }
    remember { DevHooks.learnPage = (env["DOPAMINE_LEARN_PAGE"] as? String)?.toIntOrNull() ?: 0 }
    LaunchedEffect(Unit) {
        (env["DOPAMINE_SEED_NAME"] as? String)?.let { container.profile.save(it) }
        if (env["DOPAMINE_AUTO_UPDATE"] == "1") {
            val msg = try {
                val r = container.updates.update { d, t -> logI("Update", "progress $d/$t") }
                "update ok: new=${r.newCount} updated=${r.updatedCount} removed=${r.removedCount} upToDate=${r.upToDate}"
            } catch (e: Exception) {
                "update failed: ${e.message}"
            }
            logI("Update", msg)
        }
    }
    DopamineTheme(darkTheme = false) {
        AppNav(container, startRoute = (env["DOPAMINE_ROUTE"] as? String) ?: "home")
    }
}
