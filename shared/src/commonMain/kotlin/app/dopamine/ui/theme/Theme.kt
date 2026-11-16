package app.dopamine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalDopamineColors = staticCompositionLocalOf { LightDopamineColors }

object Dopamine {
    val colors: DopamineColors
        @Composable @ReadOnlyComposable get() = LocalDopamineColors.current
    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current
}

@Composable
fun DopamineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    PlatformPreviewSetup()
    val systemReduced = systemReducedMotion()

    CompositionLocalProvider(
        LocalDopamineColors provides if (darkTheme) DarkDopamineColors else LightDopamineColors,
        LocalSpacing provides Spacing(),
        LocalReducedMotion provides systemReduced,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = dopamineTypography(),
            shapes = DopamineShapes,
            content = content,
        )
    }
}
