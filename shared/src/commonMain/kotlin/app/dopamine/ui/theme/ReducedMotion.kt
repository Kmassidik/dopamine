package app.dopamine.ui.theme

import androidx.compose.runtime.Composable

/** True when the system asks for no animations (Android: animator scale 0, iOS: Reduce Motion). */
@Composable
expect fun systemReducedMotion(): Boolean

/** Makes Compose resources load in Android Studio previews and screenshot tests; does nothing elsewhere. */
@Composable
expect fun PlatformPreviewSetup()
